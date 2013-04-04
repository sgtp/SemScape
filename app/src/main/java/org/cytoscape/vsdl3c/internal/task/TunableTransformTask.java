package org.cytoscape.vsdl3c.internal.task;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.vsdl3c.RDF2CyNetworkTransformer;
import org.cytoscape.vsdl3c.internal.SPARQLEndpointConfig;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;

/**
 * Transform the SPARQL query result into a CyNetwork. Asking the user to select
 * the SPARQL endpoints and the strategy at runtime. The strategy is a choice
 * between: (1) Append the query results to the network"; (2) Create a new
 * network to display the query result";
 * 
 */
public class TunableTransformTask extends TransformTask {

	@Tunable(description = "Endpoint:")
	public ListSingleSelection<String> endpoints;

	@Tunable(description = "Strategy:")
	public ListSingleSelection<String> strategies;

	private SPARQLEndpointConfig endpointConfig;

	private static String DEFAULT_FLAG = "(default)";

	public TunableTransformTask(RDF2CyNetworkTransformer transformer,
			CyNetworkManager netMgr,
			CyNetworkViewFactory cyNetworkViewFactoryServiceRef,
			VisualMappingManager vmm,
			CyNetworkViewManager cyNetworkViewManagerServiceRef,
			VisualMappingFunctionFactory vmfFactoryP,
			CyNetworkTableManager tableMgr,
			CyLayoutAlgorithmManager cyLayoutsServiceRef, DialogTaskManager tm,
			SPARQLEndpointConfig endpointConfig, String query,
			CyNetworkView view) {
		super(transformer, netMgr, cyNetworkViewFactoryServiceRef, vmm,
				cyNetworkViewManagerServiceRef, vmfFactoryP, tableMgr,
				cyLayoutsServiceRef, tm, null, query, view);
		this.endpointConfig = endpointConfig;
		List<String> list = new ArrayList<String>();

		SPARQLEndpoint defaultEndpoint = endpointConfig
				.getDefaultSPARQLEndpoint();
		if (defaultEndpoint != null) {
			list.add(DEFAULT_FLAG + defaultEndpoint.getUri());
		}
		for (SPARQLEndpoint ep : endpointConfig.getAllSPARQLEndpoints()) {
			if (!ep.equals(defaultEndpoint)) {
				list.add(ep.getUri());
			}
		}
		if (list.isEmpty()) {
			list.add("--- No SPARQL Endpoint ---");
		}
		endpoints = new ListSingleSelection<String>(list);

		List<String> list1 = new ArrayList<String>();
		list1.add(DEFAULT_FLAG + "Append the query results to the network");
		list1.add("Create a new network to display the query result");
		strategies = new ListSingleSelection<String>(list1);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		String selected = endpoints.getSelectedValue();

		SPARQLEndpoint ep = null;
		if (selected == null
				&& endpointConfig.getDefaultSPARQLEndpoint() != null) {
			ep = endpointConfig.getDefaultSPARQLEndpoint();

		} else if (selected != null
				&& !selected.equals("--- No SPARQL Endpoint ---")) {
			if (selected.startsWith(DEFAULT_FLAG)) {
				selected = selected.substring(DEFAULT_FLAG.length());
			}
			ep = endpointConfig.getSPARQLEndpoint(selected);
		}
		if (ep == null) {
			return;
		}
		super.setEndpoint(ep);

		selected = strategies.getSelectedValue();

		if (selected == null
				|| selected.equals(DEFAULT_FLAG
						+ "Append the query results to the network")) {
		} else {
			super.setNetworkView(null);
		}

		super.run(taskMonitor);
	}
}
