package org.cytoscape.vsdl3c.internal;

import java.io.File;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.vsdl3c.RDF2CyNetworkTransformer;
import org.cytoscape.vsdl3c.internal.action.ShowSPARQLEndpointConfigPanelAction;
import org.cytoscape.vsdl3c.internal.action.ShowSPARQLQueryDialogAction;
import org.cytoscape.vsdl3c.internal.task.ImportVizmapTask;
import org.cytoscape.vsdl3c.internal.task.SchemaExtractionTask;
import org.cytoscape.vsdl3c.internal.task.TransformTaskFactory;
import org.cytoscape.vsdl3c.internal.ui.ContextSynchronizerPanel;
import org.cytoscape.vsdl3c.internal.ui.SPARQLEndpointConfigPanel;
import org.cytoscape.vsdl3c.internal.ui.SPARQLQueryDialog;
import org.cytoscape.vsdl3c.internal.ui.SPARQLQueryTemplateForNodeContextMenuFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import com.hp.hpl.jena.shared.PrefixMapping;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		try {
			// System.getProperties().put("http.proxyHost", "127.0.0.1");
			// System.getProperties().put("http.proxyPort", "8087");

			// make sure there's the semscape, context and download directory
			// for config in %user_home%
			final File configDir = new File(System.getProperty("user.home"),
					CyProperty.DEFAULT_PROPS_CONFIG_DIR);
			final File semscape = new File(configDir, "semscape");
			if (!semscape.exists()) {
				semscape.mkdir();
			}
			final File context = new File(semscape, "context");
			if (!context.exists()) {
				context.mkdir();
			}
			final File download = new File(semscape, "download");
			if (!download.exists()) {
				download.mkdir();
			}
			final DialogTaskManager dialogTaskManager = getService(bc,
					DialogTaskManager.class);

			final SPARQLEndpointConfig endpointConfig = new SPARQLEndpointConfig();
			SPARQLEndpointConfigPanel endpointConfigPanel = new SPARQLEndpointConfigPanel(
					endpointConfig, dialogTaskManager);
			final ContextManager cm = new ContextManager();

			ContextPropsReader contextProps = new ContextPropsReader(
					"semscape/context", "semscape/context.props",
					CyProperty.SavePolicy.CONFIG_DIR, cm);
			Properties contextPropsProps = new Properties();
			contextPropsProps.setProperty("cyPropertyName",
					"semscape/context.props");
			registerService(bc, contextProps, CyProperty.class,
					contextPropsProps);

			registerService(bc, cm, PropertyUpdatedListener.class,
					contextPropsProps);

			final AbstractConfigDirPropsReader configProps = new AbstractConfigDirPropsReader(
					"semscape/config", "semscape/config.props",
					CyProperty.SavePolicy.CONFIG_DIR) {
			};
			Properties configPropsProps = new Properties();
			configPropsProps.setProperty("cyPropertyName",
					"semscape/config.props");
			registerService(bc, configProps, CyProperty.class, configPropsProps);

			final CyApplicationManager appManger = getService(bc,
					CyApplicationManager.class);
			final CyNetworkManager netMgr = getService(bc,
					CyNetworkManager.class);
			final CyNetworkNaming namingUtil = getService(bc,
					CyNetworkNaming.class);
			final CyNetworkFactory cnf = getService(bc, CyNetworkFactory.class);

			final CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(
					bc, CyNetworkViewFactory.class);
			final CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(
					bc, CyNetworkViewManager.class);
			final VisualMappingManager vmm = getService(bc,
					VisualMappingManager.class);
			final VisualMappingFunctionFactory vmfFactoryP = getService(bc,
					VisualMappingFunctionFactory.class,
					"(mapping.type=passthrough)");
			final CyNetworkTableManager tableMgr = getService(bc,
					CyNetworkTableManager.class);

			final CySwingApplication cytoscapeDesktopService = getService(bc,
					CySwingApplication.class);
			final PrefixMapping pm = Util.getPrefixMapping("prefix.properties");
			RDF2CyNetworkTransformer transformer = new RDF2CyNetworkTransformerImpl(
					cnf, namingUtil, pm);
			final CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc,
					CyLayoutAlgorithmManager.class);

			registerService(bc, endpointConfig, CyShutdownListener.class,
					new Properties());

			registerService(bc, endpointConfigPanel, CytoPanelComponent.class,
					new Properties());
			ContextSynchronizerPanel contextSynPanel = new ContextSynchronizerPanel(
					cm, dialogTaskManager, endpointConfig, endpointConfigPanel);
			registerService(bc, contextSynPanel, CytoPanelComponent.class,
					new Properties());

			ShowSPARQLEndpointConfigPanelAction showEndpointConfigPanel = new ShowSPARQLEndpointConfigPanelAction(
					cytoscapeDesktopService, endpointConfigPanel);
			registerService(bc, showEndpointConfigPanel, CyAction.class,
					new Properties());

			TransformTaskFactory ttf = new TransformTaskFactory(transformer,
					netMgr, cyNetworkViewFactoryServiceRef, vmm,
					cyNetworkViewManagerServiceRef, vmfFactoryP, tableMgr,
					cyLayoutsServiceRef, dialogTaskManager);
			SPARQLQueryDialog dialog = new SPARQLQueryDialog(dialogTaskManager,
					ttf, cm, pm, endpointConfig, endpointConfigPanel, appManger);
			ShowSPARQLQueryDialogAction showSPARQLQueryDialogAction = new ShowSPARQLQueryDialogAction(
					cytoscapeDesktopService, dialog);

			SPARQLQueryTemplateForNodeContextMenuFactory nodeContextFactory = new SPARQLQueryTemplateForNodeContextMenuFactory(
					pm, ttf, dialogTaskManager, cm,
					configProps.getProperties(), endpointConfig,
					endpointConfigPanel);

			registerService(bc, showSPARQLQueryDialogAction, CyAction.class,
					new Properties());
			registerService(bc, nodeContextFactory,
					CyNodeViewContextMenuFactory.class, new Properties());

			Properties schemaExtractionTaskProps = new Properties();
			schemaExtractionTaskProps.setProperty("preferredMenu", "Apps");
			schemaExtractionTaskProps.setProperty("menuGravity", "21.0");
			schemaExtractionTaskProps.setProperty("title",
					"SPARQL Endpoint Schema Extraction");

			AbstractTaskFactory schemaExtractionTaskFactory = new AbstractTaskFactory() {

				public TaskIterator createTaskIterator() {
					return new TaskIterator(new SchemaExtractionTask(cnf,
							namingUtil, pm, netMgr,
							cyNetworkViewFactoryServiceRef,
							cyNetworkViewManagerServiceRef, vmm, cm,
							configProps.getProperties(), dialogTaskManager,
							cyLayoutsServiceRef, endpointConfig));
				}
			};

			registerService(bc, schemaExtractionTaskFactory, TaskFactory.class,
					schemaExtractionTaskProps);

			VizmapReaderManager vizmapReaderMgr = getService(bc,
					VizmapReaderManager.class);
			CyApplicationConfiguration config = getService(bc,
					CyApplicationConfiguration.class);
			ImportVizmapTask vimapTask1 = new ImportVizmapTask(vizmapReaderMgr,
					vmm, config, Util.SPARQL_ENDPOINT_SCHEMA_VIZMAP_TITLE,
					Util.SPARQL_ENDPOINT_SCHEMA_VIZMAP_FILE);

			ImportVizmapTask vimapTask2 = new ImportVizmapTask(vizmapReaderMgr,
					vmm, config, Util.SPARQL_CONSTRUCT_QUERY_VIZMAP_TITLE,
					Util.SPARQL_CONSTRUCT_QUERY_VIZMAP_FILE);
			dialogTaskManager.execute(new TaskIterator(vimapTask1, vimapTask2));

			VizmapWriterFactory vwf = getService(bc, VizmapWriterFactory.class);

			VizmapWriter vizWriter = new VizmapWriter(vwf, vmm, config,
					dialogTaskManager,
					Util.SPARQL_ENDPOINT_SCHEMA_VIZMAP_TITLE,
					Util.SPARQL_ENDPOINT_SCHEMA_VIZMAP_FILE);
			registerService(bc, vizWriter, CyShutdownListener.class,
					new Properties());
			vizWriter = new VizmapWriter(vwf, vmm, config, dialogTaskManager,
					Util.SPARQL_CONSTRUCT_QUERY_VIZMAP_TITLE,
					Util.SPARQL_CONSTRUCT_QUERY_VIZMAP_FILE);
			registerService(bc, vizWriter, CyShutdownListener.class,
					new Properties());
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}
}
