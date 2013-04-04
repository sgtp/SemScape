package org.cytoscape.vsdl3c.internal.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ToolTipManager;

import org.apache.commons.lang3.StringEscapeUtils;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.vsdl3c.internal.ContextManager;
import org.cytoscape.vsdl3c.internal.SPARQLEndpointConfig;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;
import org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate;
import org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplateForNode;
import org.cytoscape.vsdl3c.internal.task.TransformTaskFactory;
import org.cytoscape.work.TaskManager;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Context Menu Factory for parametric queries
 * 
 */
public class SPARQLQueryTemplateForNodeContextMenuFactory implements
		CyNodeViewContextMenuFactory {

	// final private SPARQLEndpointConfig config;
	final private PrefixMapping pm;
	final private TransformTaskFactory ttf;
	final private TaskManager taskManagerServiceRef;
	final private ContextManager cm;
	final private Properties configProps;
	final private SPARQLEndpointConfig endpointConfig;

	public SPARQLQueryTemplateForNodeContextMenuFactory(PrefixMapping pm,
			TransformTaskFactory ttf, TaskManager taskManagerServiceRef,
			ContextManager cm, Properties configProps,
			SPARQLEndpointConfig endpointConfig,
			SPARQLEndpointConfigPanel configPanel) {
		super();
		this.pm = pm;
		this.ttf = ttf;
		this.taskManagerServiceRef = taskManagerServiceRef;
		this.cm = cm;
		this.configProps = configProps;
		this.endpointConfig = endpointConfig;
	}

	public CyMenuItem createMenuItem(final CyNetworkView netView,
			View<CyNode> nodeView) {

		String show = configProps.get("show_sparql_tooltip").toString();

		CyNetwork network = netView.getModel();
		CyNode node = nodeView.getModel();
		JMenu root = new JMenu("SPARQL Query");

		for (String context : cm.getActivateContexts()) {

			Model m = cm.getModel(context);
			ResIterator it = m.listSubjectsWithProperty(RDF.type,
					SPARQLQueryTemplate.SPARQL_QUERY_TEMPLATE);
			while (it.hasNext()) {

				SPARQLQueryTemplateForNode query = new SPARQLQueryTemplateForNode(
						it.nextResource(), network, node, pm);

				if (!query.applysTo(node)) {
					continue;
				}

				String label = "";
				if (query.getLabel() != null) {
					label = query.getLabel();
				} else {
					label = pm.shortForm(query.getUri());
				}
				JMenuItem queryMenuItem = new JMenuItem(label + "(from '"
						+ context + "')");
				root.add(queryMenuItem);
				final String queryString = query.getSPARQLQueryString();

				if ("true".equals(show)) {
					ToolTipManager.sharedInstance().setInitialDelay(2000);
					ToolTipManager.sharedInstance().setDismissDelay(
							Integer.MAX_VALUE);
					String toolTip = "<html>"
							+ StringEscapeUtils.escapeXml(queryString)
									.replaceAll("\n", "<br>") + "</html>";
					queryMenuItem.setToolTipText(toolTip);
				}
				queryMenuItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						taskManagerServiceRef.execute(ttf
								.createTunableTaskIterator(endpointConfig,
										queryString, netView));
					}

				});
			}

		}

		CyMenuItem addAsSource = new CyMenuItem(root, 1.0f);
		return addAsSource;
	}
}
