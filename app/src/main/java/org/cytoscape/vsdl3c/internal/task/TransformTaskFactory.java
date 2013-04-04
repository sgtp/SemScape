package org.cytoscape.vsdl3c.internal.task;

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
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Factory for TransformTask and TunableTransformTask
 *
 */
public class TransformTaskFactory {

	private RDF2CyNetworkTransformer transformer;
	private CyNetworkManager netMgr;
	private CyNetworkViewFactory cyNetworkViewFactoryServiceRef;
	private VisualMappingManager vmm;
	private CyNetworkViewManager cyNetworkViewManagerServiceRef;
	private VisualMappingFunctionFactory vmfFactoryP;
	private CyNetworkTableManager tableMgr;
	private CyLayoutAlgorithmManager cyLayoutsServiceRef;
	private DialogTaskManager tm;

	public TransformTaskFactory(RDF2CyNetworkTransformer transformer,
			CyNetworkManager netMgr,
			CyNetworkViewFactory cyNetworkViewFactoryServiceRef,
			VisualMappingManager vmm,
			CyNetworkViewManager cyNetworkViewManagerServiceRef,
			VisualMappingFunctionFactory vmfFactoryP,
			CyNetworkTableManager tableMgr,
			CyLayoutAlgorithmManager cyLayoutsServiceRef, DialogTaskManager tm) {
		super();
		this.transformer = transformer;
		this.netMgr = netMgr;
		this.cyNetworkViewFactoryServiceRef = cyNetworkViewFactoryServiceRef;
		this.vmm = vmm;
		this.cyNetworkViewManagerServiceRef = cyNetworkViewManagerServiceRef;
		this.vmfFactoryP = vmfFactoryP;
		this.tableMgr = tableMgr;
		this.cyLayoutsServiceRef = cyLayoutsServiceRef;
		this.tm = tm;

	}

	public TaskIterator createTaskIterator(SPARQLEndpoint endpoint, String query, CyNetworkView view) {
		return new TaskIterator(new TransformTask(transformer, netMgr,
				cyNetworkViewFactoryServiceRef, vmm,
				cyNetworkViewManagerServiceRef, vmfFactoryP, tableMgr,
				cyLayoutsServiceRef, tm, endpoint, query, view));
	}
	
	public TaskIterator createTunableTaskIterator(SPARQLEndpointConfig endpointConfig, String query, CyNetworkView view) {
		return new TaskIterator(new TunableTransformTask(transformer, netMgr,
				cyNetworkViewFactoryServiceRef, vmm,
				cyNetworkViewManagerServiceRef, vmfFactoryP, tableMgr,
				cyLayoutsServiceRef, tm, endpointConfig, query, view));
	}
}
