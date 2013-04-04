package org.cytoscape.vsdl3c;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Transform RDF into CyNetwork
 *
 */
public interface RDF2CyNetworkTransformer {

	/**
	 * Transform the CONSTRUCT SPARQL query result into a CyNetwork
	 * @param service, the SPARQL endpoint
	 * @param query, the CONSTRUCT SPARQL Query String
	 * @return the CyNetwork
	 */
	CyNetwork transform(SPARQLEndpoint service, String query);
	
	/**
	 * Transform the RDF Model into a CyNetwork
	 * @param model, the RDF Model
	 * @return the CyNetwork
	 */
	CyNetwork transform(Model model);
}
