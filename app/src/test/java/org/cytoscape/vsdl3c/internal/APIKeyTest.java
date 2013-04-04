package org.cytoscape.vsdl3c.internal;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class APIKeyTest {

	private String service = null;
	private String apikey = null;

	public APIKeyTest(String service, String apikey) {
		this.service = service;
		this.apikey = apikey;
	}

	public ResultSet executeQuery(String queryString) throws Exception {
		Query query = QueryFactory.create(queryString);

		QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest(
				this.service, query);
		qexec.addParam("apikey", this.apikey);
		ResultSet results = qexec.execSelect();
		return results;

	}

	public static void main(String[] args) throws Exception {
//		System.getProperties().put("http.proxyHost", "127.0.0.1");
//		System.getProperties().put("http.proxyPort", "8087");

		String sparqlService = "http://sparql.bioontology.org/sparql";
		String apikey = "1aca3cdc-7d29-4ed1-850b-e51c9a71ffd0";

		/*
		 * More query examples here: http://sparql.bioontology.org/examples
		 */
//		String query = "PREFIX omv: <http://omv.ontoware.org/2005/05/ontology#> "
//				+ " SELECT distinct ?p"
//				+ "	From  <http://bioportal.bioontology.org/ontologies/HP>"
//				+ " WHERE {  ?s ?p ?o  }";
		
		String query = "SELECT DISTINCT ?g WHERE {  GRAPH ?g { ?s ?p ?o } }";

		APIKeyTest test = new APIKeyTest(sparqlService, apikey);
		ResultSet results = test.executeQuery(query);
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			RDFNode p = soln.get("g");
			System.out.println(p);
		}
	}
}