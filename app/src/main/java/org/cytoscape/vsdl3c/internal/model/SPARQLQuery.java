package org.cytoscape.vsdl3c.internal.model;

import org.cytoscape.vsdl3c.internal.Util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * The SPARQL query with its metadata in RDF
 *
 */
public class SPARQLQuery {

	
	protected Resource query;
	protected Model model;
	public static final Property HAS_SPARQL_QUERY_STRING = Util.property("hasSPARQLQueryString");
	public static final Resource SPARQL_QUERY = Util.resource("SPARQLQuery");
	public static final Property HAS_SPARQL_ENDPOINT = Util.property("hasSPARQLEndpoint");

	public SPARQLQuery(Resource query) {
		super();
		this.query = query;
		this.model = query.getModel();
	}

	public String getUri() {
		return query.getURI();
	}

	public String getSPARQLQueryString() {
		StmtIterator it = query.listProperties(HAS_SPARQL_QUERY_STRING);
		if (it.hasNext()) {
			return it.nextStatement().getObject().toString();
		} else {
			return null;
		}
	}

	public String getLabel() {
		StmtIterator it = query.listProperties(RDFS.label);
		if (it.hasNext()) {
			return it.nextStatement().getObject().toString();
		} else {
			return null;
		}
	}

	public String getComment() {
		StmtIterator it = query.listProperties(RDFS.comment);
		if (it.hasNext()) {
			return it.nextStatement().getObject().toString();
		} else {
			return null;
		}
	}

	public String getPublisher() {
		StmtIterator it = query.listProperties(DC.publisher);
		if (it.hasNext()) {
			return it.nextStatement().getObject().toString();
		} else {
			return null;
		}
	}

	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		if (otherObject == null)
			return false;
		if (getClass() != otherObject.getClass())
			return false;
		SPARQLQuery other = (SPARQLQuery) otherObject;
		return this.getUri().equals(other.getUri());
	}
	public int hashCode(){
		return this.getUri().hashCode();
	}
}
