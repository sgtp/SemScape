package org.cytoscape.vsdl3c.internal.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.vsdl3c.RDF2CyNetworkTransformer;
import org.cytoscape.vsdl3c.internal.Util;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Transform the SPARQL query result into a CyNetwork.
 * 
 */
public class TransformTask extends AbstractTask {

	final private RDF2CyNetworkTransformer transformer;
	final private CyNetworkManager netMgr;
	private SPARQLEndpoint endpoint;
	final private String query;
	final private CyNetworkViewFactory cyNetworkViewFactoryServiceRef;
	final private VisualMappingManager vmm;
	final private CyNetworkViewManager cyNetworkViewManagerServiceRef;
	final private CyLayoutAlgorithmManager cyLayoutsServiceRef;
	final private DialogTaskManager tm;
	private CyNetworkView view;

	public TransformTask(RDF2CyNetworkTransformer transformer,
			CyNetworkManager netMgr,
			CyNetworkViewFactory cyNetworkViewFactoryServiceRef,
			VisualMappingManager vmm,
			CyNetworkViewManager cyNetworkViewManagerServiceRef,
			VisualMappingFunctionFactory vmfFactoryP,
			CyNetworkTableManager tableMgr,
			CyLayoutAlgorithmManager cyLayoutsServiceRef, DialogTaskManager tm,
			SPARQLEndpoint endpoint, String query, CyNetworkView view) {
		this.transformer = transformer;
		this.netMgr = netMgr;
		this.endpoint = endpoint;
		this.query = query;
		this.cyNetworkViewFactoryServiceRef = cyNetworkViewFactoryServiceRef;
		this.vmm = vmm;
		this.cyNetworkViewManagerServiceRef = cyNetworkViewManagerServiceRef;
		this.cyLayoutsServiceRef = cyLayoutsServiceRef;
		this.tm = tm;
		this.view = view;

	}

	protected void setEndpoint(SPARQLEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * If the view is null, we will create a new network view in a new panel to
	 * visualize the network; otherwise the query result will be appended to the
	 * network view.
	 * 
	 * @param view
	 *            , the network view to be appended.
	 */
	protected void setNetworkView(CyNetworkView view) {
		this.view = view;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final CyNetwork network = transformer.transform(endpoint, query);
		if (this.view != null) {
			merge(this.view.getModel(), network);
		} else {
			netMgr.addNetwork(network);
			this.view = cyNetworkViewFactoryServiceRef
					.createNetworkView(network);
			cyNetworkViewManagerServiceRef.addNetworkView(this.view);
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TransformTask.this.view.updateView();
				CyLayoutAlgorithm layout = cyLayoutsServiceRef
						.getLayout("force-directed");
				tm.execute(layout.createTaskIterator(TransformTask.this.view,
						layout.getDefaultLayoutContext(),
						CyLayoutAlgorithm.ALL_NODE_VIEWS, ""));

				VisualStyle style = Util.getVisualStyleByTitle(
						Util.SPARQL_CONSTRUCT_QUERY_VIZMAP_TITLE, vmm);
				style.apply(TransformTask.this.view);

				vmm.setCurrentVisualStyle(style);

				TransformTask.this.view.updateView();

			}
		});

	}

	/**
	 * Append the CyNodes and CyEdges with their attribute values of net2 to
	 * net1
	 * 
	 * @param net1
	 *            , the original CyNetwork
	 * @param net2
	 *            , the CyNetwork to be appended
	 */
	private void merge(CyNetwork net1, CyNetwork net2) {
		
		// merge nodes
		for (CyNode node2 : net2.getNodeList()) {
			CyRow node2row = net2.getDefaultNodeTable().getRow(node2.getSUID());
			String name = node2row.get("name", String.class);
			Collection<CyRow> matches = net1.getDefaultNodeTable()
					.getMatchingRows("name", name);
			CyRow node1row = null;
			if (matches.isEmpty()) {
				CyNode node1 = net1.addNode();
				node1row = net1.getDefaultNodeTable().getRow(node1.getSUID());

			} else {
				node1row = matches.iterator().next();
			}
			for (final CyColumn column : net2.getDefaultNodeTable()
					.getColumns()) {
				final String columnName = column.getName();
				final Class<?> type = column.getType();

				if (type == List.class) {
					final Class<?> elementType = column.getListElementType();

					if (net1.getDefaultNodeTable().getColumn(columnName) == null) {
						net1.getDefaultNodeTable().createListColumn(columnName,
								elementType, true);
					}
					List list1 = node1row
							.getList(columnName, elementType, null);
					List list2 = node2row
							.getList(columnName, elementType, null);
					if (list2 != null && !list2.isEmpty()) {
						if (list1 == null) {
							list1 = new ArrayList();
						}
						for (Object v2 : list2) {
							if (!list1.contains(v2)) {
								list1.add(v2);
							}
						}
						node1row.set(columnName, list1);
					}
				} else {
					if (net1.getDefaultNodeTable().getColumn(columnName) == null) {
						net1.getDefaultNodeTable().createColumn(columnName,
								type, true);
					}
					Object v1 = node1row.get(columnName, type, null);
					Object v2 = node2row.get(columnName, type, null);

					if (v2 != null) {
						if (columnName.equals(Util.ENDPOINT_COLOMN_NAME)) {
							Util.addEndpointValue(net1, net1.getNode(node1row
									.get("SUID", Long.class)), v2.toString());
						} else {
							node1row.set(columnName, v2);
						}
					}
				}
			}
		}
		
		// merge edges
		for (CyEdge edge2 : net2.getEdgeList()) {
			CyRow edge2row = net2.getDefaultEdgeTable().getRow(edge2.getSUID());
			String edge2name = edge2row.get("interaction", String.class);

			CyNode source2 = edge2.getSource();
			CyRow source2row = net2.getDefaultNodeTable().getRow(
					source2.getSUID());
			String source2name = source2row.get("name", String.class);

			CyNode target2 = edge2.getTarget();
			CyRow target2row = net2.getDefaultNodeTable().getRow(
					target2.getSUID());
			String target2name = target2row.get("name", String.class);

			Collection<CyRow> matches = net1.getDefaultEdgeTable()
					.getMatchingRows("interaction", edge2name);

			Iterator<CyRow> it = matches.iterator();
			boolean contains = false;
			while (it.hasNext()) {
				CyRow edge1row = it.next();
				CyEdge edge1 = net1.getEdge(edge1row.get("SUID", Long.class));

				CyNode source1 = edge1.getSource();
				CyRow source1row = net1.getDefaultNodeTable().getRow(
						source1.getSUID());
				String source1name = source1row.get("name", String.class);

				CyNode target1 = edge1.getTarget();
				CyRow target1row = net1.getDefaultNodeTable().getRow(
						target1.getSUID());
				String target1name = target1row.get("name", String.class);
				if (source1name != null && target1name != null) {
					if (source1name.equals(source2name)
							&& target1name.equals(target2name)) {
						contains = true;
						break;
					}
				}
			}
			if (!contains) {
				CyRow source1row = net1.getDefaultNodeTable()
						.getMatchingRows("name", source2name).iterator().next();
				CyNode source1 = net1.getNode(source1row
						.get("SUID", Long.class));

				CyRow target1row = net1.getDefaultNodeTable()
						.getMatchingRows("name", target2name).iterator().next();
				CyNode target1 = net1.getNode(target1row
						.get("SUID", Long.class));

				final CyEdge cyEdge = net1.addEdge(source1, target1, true);
				CyRow edge1row = net1.getDefaultEdgeTable().getRow(
						cyEdge.getSUID());
				for (final CyColumn column : net2.getDefaultEdgeTable()
						.getColumns()) {
					final String columnName = column.getName();
					final Class<?> type = column.getType();

					if (type == List.class) {
						final Class<?> elementType = column
								.getListElementType();

						if (net1.getDefaultEdgeTable().getColumn(columnName) == null) {
							net1.getDefaultEdgeTable().createListColumn(
									columnName, elementType, true);
						}
						List list1 = edge1row.getList(columnName, elementType,
								null);
						List list2 = edge2row.getList(columnName, elementType,
								null);
						if (list2 != null && !list2.isEmpty()) {
							if (list1 == null) {
								list1 = new ArrayList();
							}
							for (Object v2 : list2) {
								if (!list1.contains(v2)) {
									list1.add(v2);
								}
							}
							edge1row.set(columnName, list1);
						}

					} else {
						if (net1.getDefaultEdgeTable().getColumn(columnName) == null) {
							net1.getDefaultEdgeTable().createColumn(columnName,
									type, true);
						}
						Object v1 = edge1row.get(columnName, type, null);
						Object v2 = edge2row.get(columnName, type, null);
						if (v2 != null) {
							if (columnName.equals(Util.ENDPOINT_COLOMN_NAME)) {
								Util.addEndpointValue(net1, net1
										.getEdge(edge1row.get("SUID",
												Long.class)), v2.toString());
							} else {
								edge1row.set(columnName, v2);
							}
						}
					}
				}
			}
		}
	}
}
