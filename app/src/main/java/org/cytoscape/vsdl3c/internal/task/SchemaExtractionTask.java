package org.cytoscape.vsdl3c.internal.task;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.vsdl3c.internal.ContextManager;
import org.cytoscape.vsdl3c.internal.SPARQLEndpointConfig;
import org.cytoscape.vsdl3c.internal.Util;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListMultipleSelection;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

/**
 * Extract schema of the SPARQL endpoint(s), and transform it into a CyNetwork
 * 
 */
public class SchemaExtractionTask extends AbstractTask {

	/**
	 * the SPARQL endpoint URI(s) that requires the user to select at runtime
	 */
	@Tunable(description = "The SPARQL endpoint:")
	public ListMultipleSelection<String> endpoints;

	private ArrayList<String> prefixes;

	private CyNetworkFactory cnf;
	private CyNetworkNaming namingUtil;
	private PrefixMapping pm;
	private CyNetworkManager netMgr;
	private CyNetworkViewFactory cyNetworkViewFactoryServiceRef;
	private CyNetworkViewManager cyNetworkViewManagerServiceRef;
	final private VisualMappingManager vmm;
	private Map<Resource, CyNode> r2n;
	private final DialogTaskManager tm;
	private final CyLayoutAlgorithmManager cyLayoutsServiceRef;
	private Set<String> successfulEndpoints;
	private SPARQLEndpointConfig endpointConfig;
	private NumberFormat nFormat;

	public SchemaExtractionTask(CyNetworkFactory cnf,
			CyNetworkNaming namingUtil, PrefixMapping pm,
			CyNetworkManager netMgr,
			CyNetworkViewFactory cyNetworkViewFactoryServiceRef,
			CyNetworkViewManager cyNetworkViewManagerServiceRef,
			VisualMappingManager vmm, ContextManager cm,
			Properties configProps, DialogTaskManager tm,
			CyLayoutAlgorithmManager cyLayoutsServiceRef,
			SPARQLEndpointConfig endpointConfig) {
		prefixes = new ArrayList<String>();
		prefixes.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.add("http://www.w3.org/2000/01/rdf-schema#");
		prefixes.add("http://www.w3.org/2002/07/owl#");
		this.cnf = cnf;
		this.namingUtil = namingUtil;
		this.pm = pm;
		this.netMgr = netMgr;
		this.cyNetworkViewFactoryServiceRef = cyNetworkViewFactoryServiceRef;
		this.cyNetworkViewManagerServiceRef = cyNetworkViewManagerServiceRef;
		this.vmm = vmm;
		this.r2n = new HashMap<Resource, CyNode>();
		this.tm = tm;
		this.cyLayoutsServiceRef = cyLayoutsServiceRef;
		this.successfulEndpoints = new HashSet<String>();
		this.endpointConfig = endpointConfig;
		this.nFormat = NumberFormat.getNumberInstance();
		this.nFormat.setMaximumFractionDigits(2);
		initSPARQLEndpoint();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		successfulEndpoints.addAll(endpoints.getSelectedValues());
		List<SPARQLEndpoint> eps = this.endpointConfig
				.getSPARQLEndpointList(endpoints.getSelectedValues());
		final CyNetwork network = cnf.createNetwork();

		network.getDefaultNodeTable().createColumn(Util.ENDPOINT_COLOMN_NAME,
				String.class, true);
		network.getDefaultEdgeTable().createColumn(Util.ENDPOINT_COLOMN_NAME,
				String.class, true);
		network.getDefaultNodeTable().createColumn(Util.NAMESPACE_URI,
				String.class, true);
		network.getDefaultEdgeTable().createColumn(Util.NAMESPACE_URI,
				String.class, true);

		for (SPARQLEndpoint endpoint : eps) {
			extract(taskMonitor, endpoint, network);
		}

		network.getRow(network).set(
				CyNetwork.NAME,
				namingUtil.getSuggestedNetworkTitle("SemScape: "
						+ getSuccessfulEndpointUris()));

		netMgr.addNetwork(network);

		final CyNetworkView netView = cyNetworkViewFactoryServiceRef
				.createNetworkView(network);

		cyNetworkViewManagerServiceRef.addNetworkView(netView);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				VisualStyle style = Util.getVisualStyleByTitle(
						Util.SPARQL_ENDPOINT_SCHEMA_VIZMAP_TITLE, vmm);
				style.apply(netView);
				netView.updateView();
				vmm.setCurrentVisualStyle(style);

				CyLayoutAlgorithm layout = cyLayoutsServiceRef
						.getLayout("force-directed");
				tm.execute(layout.createTaskIterator(netView,
						layout.getDefaultLayoutContext(),
						CyLayoutAlgorithm.ALL_NODE_VIEWS, ""));

				netView.updateView();

			}
		});
	}

	private String getSuccessfulEndpointUris() {
		String ret = "";
		for (String uri : this.successfulEndpoints) {
			ret = ret + " <" + uri + ">";
		}
		return ret;
	}

	private void extract(TaskMonitor taskMonitor, SPARQLEndpoint endpoint,
			CyNetwork network) {
		Util.setAuth(endpoint);

		long start = System.currentTimeMillis();

		taskMonitor.setTitle("Schema extraction: " + endpoint.getUri());

		ArrayList<String> predicates = new ArrayList<String>();

		String query = "SELECT distinct ?p WHERE { ?s ?p ?o . }";
		// String query =
		// "SELECT distinct ?p WHERE { ?p a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> UNION ?p a <http://www.w3.org/2002/07/owl#AnnotationProperty>. }";
		taskMonitor.setProgress(0.1);
		taskMonitor.setStatusMessage("Query: " + query);

		QueryEngineHTTP qexec = Util.setNamedGraphs(endpoint, query);
		qexec = Util.setAPIKey(qexec, endpoint);

		int i = 0;
		try {
			ResultSet r = qexec.execSelect();

			while (r.hasNext()) {
				QuerySolution s = r.nextSolution();
				RDFNode node = s.get("p");
				if (node.isURIResource() && !hasIgnoredPrefix(node.toString())) {
					i++;
					predicates.add(node.toString());
				}
			}

		} catch (Exception ex) {
			if (ex instanceof QueryExceptionHTTP) {
				JOptionPane.showMessageDialog(null, ex.getMessage() + ": "
						+ endpoint.getUri());
				System.out.println("Error at: " + endpoint.getUri());
				successfulEndpoints.remove(endpoint.getUri());
				ex.printStackTrace();
				return;
			}
			ex.printStackTrace();
		}

		taskMonitor.setStatusMessage(i + " predicates found!");
		taskMonitor.setProgress(0.2);

		long end = System.currentTimeMillis();
		double time = (end - start) / (1000.0 * 60);

		System.out.println("Predicate SPARQL query time spent: "
				+ nFormat.format(time) + " minutes.");

		Model m = ModelFactory.createDefaultModel();
		int j = 0;
		for (String predicate : predicates) {
			j++;
			query = "CONSTRUCT  { ?class1 <" + predicate + "> ?class2 } "
					+ "WHERE { " + "?s <" + predicate + "> ?o . "
					+ "?s a ?class1 . ?o a ?class2 . }";

			try {
				qexec = Util.setNamedGraphs(endpoint, query);
				qexec = Util.setAPIKey(qexec, endpoint);

				Model subModel = qexec.execConstruct();
				double process = 0.2 + 0.6 * j / i;
				taskMonitor.setStatusMessage("("
						+ nFormat.format(process * 100) + "%) We find "
						+ subModel.size() + " schema triple(s) for: "
						+ predicate);

				taskMonitor.setProgress(process);
				m.add(subModel);
			} catch (Exception ex) {
				ex.printStackTrace();
				query += "LIMIT 100000";
				try {
					qexec = Util.setNamedGraphs(endpoint, query);
					qexec = Util.setAPIKey(qexec, endpoint);
					Model subModel = qexec.execConstruct();
					System.out.println(subModel.size()
							+ " schema triple found for: " + predicate);
					m.add(subModel);
				} catch (Exception e) {
					ex.printStackTrace();
				}
			}

		}

		taskMonitor.setProgress(1);

		end = System.currentTimeMillis();
		time = (end - start) / (1000.0 * 60);
		System.out.println("Total SPARQL query time spent: "
				+ nFormat.format(time) + " minutes.");

		StmtIterator iter = m.listStatements();
		while (iter.hasNext()) {
			Statement st = iter.nextStatement();
			Resource s = st.getSubject();
			CyNode cySource = r2n.get(s);

			if (cySource == null) {
				cySource = network.addNode();
				network.getDefaultNodeTable().getRow(cySource.getSUID())
						.set("name", pm.shortForm(s.toString()));
				network.getDefaultNodeTable()
						.getRow(cySource.getSUID())
						.set(Util.NAMESPACE_URI,
								Util.getNamespace(s.toString()));
				r2n.put(s, cySource);
			}
			Util.addEndpointValue(network, cySource, endpoint.getUri());

			Resource p = st.getPredicate();
			RDFNode o = st.getObject();
			Resource ro = o.asResource();

			CyNode cyTarget = r2n.get(ro);
			if (cyTarget == null) {
				cyTarget = network.addNode();
				network.getDefaultNodeTable().getRow(cyTarget.getSUID())
						.set("name", pm.shortForm(ro.toString()));
				network.getDefaultNodeTable()
						.getRow(cyTarget.getSUID())
						.set(Util.NAMESPACE_URI,
								Util.getNamespace(ro.toString()));
				r2n.put(ro, cyTarget);
			}
			Util.addEndpointValue(network, cyTarget, endpoint.getUri());

			final CyEdge cyEdge = network.addEdge(cySource, cyTarget, true);
			network.getDefaultEdgeTable().getRow(cyEdge.getSUID())
					.set("interaction", pm.shortForm(p.getURI()));
			network.getDefaultEdgeTable().getRow(cyEdge.getSUID())
					.set(Util.NAMESPACE_URI, Util.getNamespace(p.toString()));
			Util.addEndpointValue(network, cyEdge, endpoint.getUri());
		}

	}

	private boolean hasIgnoredPrefix(String uri) {

		for (String prefix : prefixes) {
			if (uri.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	private void initSPARQLEndpoint() {
		List<String> list = new ArrayList<String>();

		Set<SPARQLEndpoint> endpoints = endpointConfig.getAllSPARQLEndpoints();

		Iterator<SPARQLEndpoint> iter = endpoints.iterator();
		while (iter.hasNext()) {
			list.add(iter.next().getUri());
		}
		if (list.size() == 0) {
			list.add("--- No SPARQL Endpoint ---");
		}
		Collections.sort(list);
		this.endpoints = new ListMultipleSelection<String>(list);

	}

}
