package org.cytoscape.vsdl3c.internal;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.vsdl3c.RDF2CyNetworkTransformer;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Implementation of RDF2CyNetworkTransformer
 * 
 */
public class RDF2CyNetworkTransformerImpl implements RDF2CyNetworkTransformer {

	private CyNetworkFactory cnf;
	private CyNetworkNaming namingUtil;
	private PrefixMapping pm;

	public RDF2CyNetworkTransformerImpl(CyNetworkFactory cnf,
			CyNetworkNaming namingUtil, PrefixMapping pm) {
		this.cnf = cnf;
		this.namingUtil = namingUtil;
		this.pm = pm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cytoscape.vsdl3c.RDF2CyNetworkTransformer#transform(org.cytoscape
	 * .vsdl3c.internal.model.SPARQLEndpoint, java.lang.String)
	 */
	public CyNetwork transform(SPARQLEndpoint service, String query) {
		Util.setAuth(service);
		QueryEngineHTTP qexec = (QueryEngineHTTP) QueryExecutionFactory
				.sparqlService(service.getUri(), query);
		qexec = Util.setNamedGraphs(service, query);
		qexec = Util.setAPIKey(qexec, service);

		Model resultModel = qexec.execConstruct();
		CyNetwork cyNetwork = transform(resultModel, service.getUri());
		qexec.close();
		return cyNetwork;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cytoscape.vsdl3c.RDF2CyNetworkTransformer#transform(com.hp.hpl.jena
	 * .rdf.model.Model)
	 */
	public CyNetwork transform(Model model) {
		return transform(model, null);
	}

	private CyNetwork transform(Model model, String endpoint) {

		final Map<Resource, CyNode> r2n = new HashMap<Resource, CyNode>();
		final CyNetwork network = cnf.createNetwork();
		network.getDefaultNodeTable().createColumn(Util.NAMESPACE_URI,
				String.class, true);
		network.getDefaultEdgeTable().createColumn(Util.NAMESPACE_URI,
				String.class, true);
		if (endpoint != null) {
			network.getDefaultNodeTable().createColumn(
					Util.ENDPOINT_COLOMN_NAME, String.class, true);
			network.getDefaultEdgeTable().createColumn(
					Util.ENDPOINT_COLOMN_NAME, String.class, true);
		}

		network.getRow(network).set(CyNetwork.NAME,
				namingUtil.getSuggestedNetworkTitle("RDF Network"));

		StmtIterator iter = model.listStatements();
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
			if (endpoint != null) {
				Util.addEndpointValue(network, cySource, endpoint);
			}

			Resource p = st.getPredicate();
			RDFNode o = st.getObject();
			if (o.isLiteral()) {
				Literal lo = o.asLiteral();
				SafeLiteral sl = new SafeLiteral(lo);

				if (network.getDefaultNodeTable().getColumn(
						pm.shortForm(p.getURI())) == null) {
					network.getDefaultNodeTable().createListColumn(
							pm.shortForm(p.getURI()), sl.getType(), true);
				}
				List values = network.getDefaultNodeTable()
						.getRow(cySource.getSUID())
						.getList(pm.shortForm(p.getURI()), sl.getType());
				if (values == null) {
					values = new ArrayList();
				}
				values.add(sl.getValue());
				network.getDefaultNodeTable().getRow(cySource.getSUID())
						.set(pm.shortForm(p.getURI()), values);
			} else if (RDF.type.getURI().equals(p.getURI())) {
				if (network.getDefaultNodeTable().getColumn(
						pm.shortForm(p.getURI())) == null) {
					network.getDefaultNodeTable().createListColumn(
							pm.shortForm(p.getURI()), String.class, true);
				}

				List values = network.getDefaultNodeTable()
						.getRow(cySource.getSUID())
						.getList(pm.shortForm(p.getURI()), String.class);
				if (values == null) {
					values = new ArrayList();
				}
				values.add(pm.shortForm(o.as(Resource.class).getURI()));
				network.getDefaultNodeTable().getRow(cySource.getSUID())
						.set(pm.shortForm(p.getURI()), values);

			} else {
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
					if (endpoint != null) {
						network.getDefaultNodeTable()
								.getRow(cyTarget.getSUID())
								.set("endpoint", endpoint);
					}
					r2n.put(ro, cyTarget);
				}
				if (endpoint != null) {
					Util.addEndpointValue(network, cyTarget, endpoint);
				}

				final CyEdge cyEdge = network.addEdge(cySource, cyTarget, true);
				network.getDefaultEdgeTable().getRow(cyEdge.getSUID())
						.set("interaction", pm.shortForm(p.getURI()));
				network.getDefaultEdgeTable()
						.getRow(cyEdge.getSUID())
						.set(Util.NAMESPACE_URI,
								Util.getNamespace(p.toString()));

				if (endpoint != null) {
					Util.addEndpointValue(network, cyEdge, endpoint);
				}
			}
		}
		return network;
	}
}

/**
 * Literal class mapping from to Jena to Cytoscape, defined in
 * "datatypes.properties"
 * 
 */
class SafeLiteral {

	private static HashMap<Class, Class> typeMap;

	static {
		Properties props = new Properties();
		URL dp = SafeLiteral.class.getResource("datatypes.properties");
		try {
			props.load(dp.openStream());

			typeMap = new HashMap<Class, Class>();
			Iterator<Entry<Object, Object>> iter = props.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Object, Object> entry = iter.next();
				typeMap.put(Class.forName(entry.getKey().toString()),
						Class.forName(entry.getValue().toString()));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Class type;
	private Object value;

	SafeLiteral(Literal l) {
		super();
		Class type = l.getValue().getClass();
		Object value = l.getValue();
		if (typeMap.values().contains(type)) {
			this.type = type;
			this.value = value;
		} else if (typeMap.containsKey(type)) {
			this.type = typeMap.get(type);
			try {
				Method m = this.type.getMethod("valueOf",
						new Class[] { String.class });
				this.value = m.invoke(this.type,
						new Object[] { value.toString() });
			} catch (Exception e) {
				this.type = String.class;
				this.value = value;
				e.printStackTrace();
			}
		} else {
			this.type = String.class;
			this.value = value.toString();
		}
	}

	public Class getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

}
