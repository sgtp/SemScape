package org.cytoscape.vsdl3c.internal;

import junit.framework.TestCase;

import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class SPARQLAuthTest {

	@Test
	public void testAuth() {
		// final String authUser = "test";
		// final String authPassword = "test";
		// Authenticator.setDefault(new Authenticator() {
		// public PasswordAuthentication getPasswordAuthentication() {
		// return new PasswordAuthentication(authUser, authPassword
		// .toCharArray());
		// }
		// });
		// String sparqlQueryString =
		// " SELECT ?document (COUNT(?document) as ?c) WHERE {   ?annotation <http://purl.org/ao/onSourceDocument> ?document .   ?annotation <http://purl.org/ao/hasTopic> <https://trac.nbic.nl/data-mining/14948> .  }";
		// Query query = QueryFactory.create(sparqlQueryString);
		// QueryEngineHTTP qexec = (QueryEngineHTTP) QueryExecutionFactory
		// .sparqlService(
		// "http://ops-virtuoso.scai.fraunhofer.de:8891/sparql",
		// sparqlQueryString);
		//
		// qexec.execSelect();

	}

}
