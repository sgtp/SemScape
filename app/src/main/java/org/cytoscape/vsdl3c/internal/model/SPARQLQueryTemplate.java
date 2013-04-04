package org.cytoscape.vsdl3c.internal.model;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.vsdl3c.internal.Util;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * The SPARQL query template (parametric query) with its metadata in RDF
 * 
 */
public abstract class SPARQLQueryTemplate extends SPARQLQuery {

	public static final Resource SPARQL_QUERY_TEMPLATE = Util
			.resource("SPARQLQueryTemplate");

	public static final Property APPLYS_TO = Util.property("applysTo");

	public static final Resource NODE = Util.resource("Node");
	public static final Resource EDGE = Util.resource("Edge");
	public static final Property HAS_PARAMETER = Util.property("hasParameter");
	public static final Property HAS_PARAMETER_NAME = Util
			.property("hasParameterName");
	public static final Property HAS_COLUMN_NAME = Util
			.property("hasColumnName");

	public static final Property HAS_CONSTRAINT = Util
			.property("hasConstraint");
	public static final Property HAS_ATTRIBUTE_VALUE = Util
			.property("hasAttributeValue");

	protected CyIdentifiable id;
	protected CyNetwork network;
	protected PrefixMapping pm;

	public SPARQLQueryTemplate(Resource query, CyNetwork network,
			CyIdentifiable id, PrefixMapping pm) {
		super(query);
		this.id = id;
		this.network = network;
		this.pm = pm;
	}

	/**
	 * check whether this query applys to the CyNode/CyEdge
	 * 
	 * @param id
	 *            , the CyNode/CyEdge
	 * @return true, if applicable; otherwise false
	 */
	public abstract boolean applysTo(CyIdentifiable id);
}
