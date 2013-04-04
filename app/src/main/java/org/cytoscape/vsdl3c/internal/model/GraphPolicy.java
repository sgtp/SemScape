package org.cytoscape.vsdl3c.internal.model;

public enum GraphPolicy {
	/**
	 * Set default graph(s), and internally we map to the http header (for
	 * Virtuoso)
	 */
	DEFAUT_GRAPH,

	/**
	 * Restrict to named graph(s), and we use the FROM GRAPH <X> construct (for 4Store) 
	 */
	NAMED_GRAPH,

	/**
	 * Query all graphs, and we use GRAPH ?G {?s ?p ?o}
	 */
	ALL_GRAPH,

	/**
	 * No graph is needed to be selected
	 */
	NONE
}
