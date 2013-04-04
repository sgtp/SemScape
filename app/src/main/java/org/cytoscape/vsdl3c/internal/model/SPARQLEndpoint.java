package org.cytoscape.vsdl3c.internal.model;

import org.cytoscape.vsdl3c.internal.Util;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * The SPARQL endpoint with its metadata in RDF
 * 
 */
public class SPARQLEndpoint {

	public static final Resource SPARQL_ENDPOINT = Util
			.resource("SPARQLEndpoint");
	public static final Resource GRAPH_POLICY_DEFAULT_GRAPH = Util
			.resource("GraphPolicyDefaultGraph");
	public static final Resource GRAPH_POLICY_NAMED_GRAPH = Util
			.resource("GraphPolicyNamedGraph");
	public static final Resource GRAPH_POLICY_ALL_GRAPH = Util
			.resource("GraphPolicyAllGraph");
	public static final Resource GRAPH_POLICY_NONE = Util
			.resource("GraphPolicyNone");

	public static final Property HAS_NAMED_GRAPH = Util
			.property("hasNamedGraph");
	public static final Property HAS_SPARQL_ENDPOINT_USER = Util
			.property("hasSPARQLEndpointUser");
	public static final Property HAS_SPARQL_ENDPOINT_PASSWORD = Util
			.property("hasSPARQLEndpointPassword");
	public static final Property HAS_GRAPH_POLICY = Util
			.property("hasGraphPolicy");
	public static final Property HAS_API_KEY = Util.property("hasAPIKey");
	public static final Property IS_DEFAULT_SPARQL_Endpoint = Util
			.property("isDefaultSPARQLEndpoint");

	protected Resource endpoint;
	protected Model model;

	public SPARQLEndpoint(Resource endpoint) {
		super();
		this.endpoint = endpoint;
		this.model = endpoint.getModel();
	}

	public SPARQLEndpoint(Model model, String uri) {
		super();
		this.model = model;
		this.endpoint = model.createResource(uri);
		this.endpoint.addProperty(RDF.type, SPARQLEndpoint.SPARQL_ENDPOINT);
	}

	public boolean isDefaultSPARQLEndpoint() {
		StmtIterator it = endpoint.listProperties(IS_DEFAULT_SPARQL_Endpoint);
		if (it.hasNext()) {
			return ((Literal) it.nextStatement().getObject()).getBoolean();
		} else {
			return false;
		}
	}

	public void setDefaultSPARQLEndpoint(boolean b) {
		endpoint.removeAll(IS_DEFAULT_SPARQL_Endpoint);
		endpoint.addLiteral(IS_DEFAULT_SPARQL_Endpoint, b);
	}

	public String getNamedGraphString() {
		String str = "";
		StmtIterator it = endpoint.listProperties(HAS_NAMED_GRAPH);
		while (it.hasNext()) {
			str += ((Literal) it.nextStatement().getObject()).getString() + " ";
		}
		if (!"".equals(str)) {
			return str.substring(0, str.length() - 1);
		} else {
			return null;
		}
	}

	public void setNamedGraphString(String namedGraphString) {
		endpoint.removeAll(HAS_NAMED_GRAPH);
		if (!"".equals(namedGraphString) && namedGraphString != null) {
			String[] namedGraphArray = namedGraphString.trim().split(" ");
			for (String namedGraph : namedGraphArray) {
				endpoint.addLiteral(HAS_NAMED_GRAPH, namedGraph);
			}
		}
	}

	public String getSPARQLEndpointPassword() {
		StmtIterator it = endpoint.listProperties(HAS_SPARQL_ENDPOINT_PASSWORD);
		if (it.hasNext()) {
			return ((Literal) it.nextStatement().getObject()).getString();
		} else {
			return null;
		}
	}

	public void setSPARQLEndpointPassword(String password) {
		endpoint.removeAll(HAS_SPARQL_ENDPOINT_PASSWORD);
		if (!"".equals(password) && password != null) {
			endpoint.addLiteral(HAS_SPARQL_ENDPOINT_PASSWORD, password);
		}
	}

	public String getSPARQLEndpointUser() {
		StmtIterator it = endpoint.listProperties(HAS_SPARQL_ENDPOINT_USER);
		if (it.hasNext()) {
			return ((Literal) it.nextStatement().getObject()).getString();
		} else {
			return null;
		}
	}

	public void setSPARQLEndpointUser(String user) {
		endpoint.removeAll(HAS_SPARQL_ENDPOINT_USER);
		if (!"".equals(user) && user != null) {
			endpoint.addLiteral(HAS_SPARQL_ENDPOINT_USER, user);
		}
	}

	public GraphPolicy getGraphPolicy() {
		StmtIterator it = endpoint.listProperties(HAS_GRAPH_POLICY);
		if (it.hasNext()) {
			RDFNode obj = it.nextStatement().getObject();
			if (obj.equals(GRAPH_POLICY_DEFAULT_GRAPH)) {
				return GraphPolicy.DEFAUT_GRAPH;
			} else if (obj.equals(GRAPH_POLICY_NAMED_GRAPH)) {
				return GraphPolicy.NAMED_GRAPH;
			} else if (obj.equals(GRAPH_POLICY_ALL_GRAPH)) {
				return GraphPolicy.ALL_GRAPH;
			} else {
				return GraphPolicy.NONE;
			}
		} else {
			return GraphPolicy.NONE;
		}
	}

	public void setGraphPolicy(GraphPolicy policy) {
		endpoint.removeAll(HAS_GRAPH_POLICY);
		if (policy.equals(GraphPolicy.DEFAUT_GRAPH)) {
			endpoint.addProperty(HAS_GRAPH_POLICY, GRAPH_POLICY_DEFAULT_GRAPH);
		} else if (policy.equals(GraphPolicy.NAMED_GRAPH)) {
			endpoint.addProperty(HAS_GRAPH_POLICY, GRAPH_POLICY_NAMED_GRAPH);
		} else if (policy.equals(GraphPolicy.ALL_GRAPH)) {
			endpoint.addProperty(HAS_GRAPH_POLICY, GRAPH_POLICY_ALL_GRAPH);
		} else {
			endpoint.addProperty(HAS_GRAPH_POLICY, GRAPH_POLICY_NONE);
		}
	}

	public String getAPIKey() {
		StmtIterator it = endpoint.listProperties(HAS_API_KEY);
		if (it.hasNext()) {
			return ((Literal) it.nextStatement().getObject()).getString();
		} else {
			return null;
		}
	}

	public void setAPIKey(String APIKey) {
		endpoint.removeAll(HAS_API_KEY);
		if (!"".equals(APIKey) && APIKey != null) {
			endpoint.addLiteral(HAS_API_KEY, APIKey);
		}
	}

	public String getLabel() {
		StmtIterator it = endpoint.listProperties(RDFS.label);
		if (it.hasNext()) {
			return ((Literal) it.nextStatement().getObject()).getString();
		} else {
			return null;
		}
	}

	public void setLabel(String label) {
		endpoint.removeAll(RDFS.label);
		if (!"".equals(label) && label != null) {
			endpoint.addLiteral(RDFS.label, label);
		}
	}

	public String getUri() {
		return endpoint.getURI();
	}

	public void setUri(String uri) {
		if (!"".equals(uri) && uri != null) {
			endpoint = ResourceUtils.renameResource(endpoint, uri);
		}
	}

	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		if (otherObject == null)
			return false;
		if (getClass() != otherObject.getClass())
			return false;
		SPARQLEndpoint other = (SPARQLEndpoint) otherObject;
		return this.getUri().equals(other.getUri());
	}

	public int hashCode() {
		return this.getUri().hashCode();
	}

	public String toString() {
		return this.getUri();
	}
}
