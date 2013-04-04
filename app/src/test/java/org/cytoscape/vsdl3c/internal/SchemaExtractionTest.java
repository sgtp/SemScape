package org.cytoscape.vsdl3c.internal;

import java.util.ArrayList;

import org.junit.Test;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.PrintUtil;

public class SchemaExtractionTest {
	private static String SERVICE = "http://ops-virtuoso.scai.fraunhofer.de:8891/sparql";

	private boolean hasIgnoredPrefix(String uri) {
		ArrayList<String> prefixes = new ArrayList<String>();
		prefixes.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.add("http://www.w3.org/2000/01/rdf-schema#");
		prefixes.add("http://www.w3.org/2002/07/owl#");
		for (String prefix : prefixes) {
			if (uri.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testQueryMetadata() {
//		ArrayList<String> predicates = new ArrayList<String>();
//
//		String query = "SELECT distinct ?p WHERE { ?s ?p ?o . }";
//		QueryExecution qexec = QueryExecutionFactory.sparqlService(SERVICE,
//				query);
//		try {
//			ResultSet r = qexec.execSelect();
//			int i = 0;
//			while (r.hasNext()) {
//				QuerySolution s = r.nextSolution();
//				RDFNode node = s.get("p");
//				if (node.isURIResource() && !hasIgnoredPrefix(node.toString())) {
//					i++;
//					System.out.println(i + " predicates found: "
//							+ node.toString());
//					predicates.add(node.toString());
//				}
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//		Model m = ModelFactory.createDefaultModel();
//		for (String predicate : predicates) {
//			query = "CONSTRUCT  { ?class1 <" + predicate + "> ?class2 } "
//					+ "WHERE { " + "?s <" + predicate + "> ?o . "
//					+ "?s a ?class1 . ?o a ?class2 . }";
//
//			try {
//				qexec = QueryExecutionFactory.sparqlService(SERVICE, query);
//				Model subModel = qexec.execConstruct();
//				System.out.println(subModel.size()
//						+ " schema triple found for: " + predicate);
//				m.add(subModel);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				query += "LIMIT 1000000";
//				try {
//					qexec = QueryExecutionFactory.sparqlService(SERVICE, query);
//					Model subModel = qexec.execConstruct();
//					System.out.println(subModel.size()
//							+ " schema triple found for: " + predicate);
//					m.add(subModel);
//				}catch (Exception e) {
//					ex.printStackTrace();
//				}
//			}
//
//		}
//		PrintUtil.printOut(m.listStatements());
//		System.out.println(m.size());

	}
	// String query =
	// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
	// query += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";
	// query += "PREFIX owl: <http://www.w3.org/2002/07/owl#> ";
	// query += "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> ";
	// query += "PREFIX eg: <http://www.eg.org/eg#> ";
	// query += "CONSTRUCT  {?class2 ?property ?class1} ";
	// query += "WHERE { " +
	// "?s ?property ?o . " +
	// "?o  a ?class1 . " +
	// // "?class1 a rdfs:Class ." +
	// "?s  a ?class2 " +
	// // "?class2 a rdfs:Class ." +
	// "FILTER ( ! regex ( str(?property), \"^http://www.w3.org/2002/07/owl#\") && "
	// +
	// "! regex ( str(?property), \"^http://www.w3.org/2000/01/rdf-schema#\") && "
	// +
	// "! regex ( str(?property), \"^http://www.w3.org/1999/02/22-rdf-syntax-ns#\" ) )"
	// +
	//
	// "}  " +
	// "OFFSET  100000  " +
	// "LIMIT 300000" ;

	// String query =
	// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
	// query += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";
	// query += "PREFIX owl: <http://www.w3.org/2002/07/owl#> ";
	// query += "PREFIX eg: <http://www.eg.org/eg#> ";
	// query += "CONSTRUCT " + "{  ?cls a ?class ; " + " ?p ?o .  " + "} "
	// + "WHERE " + " {  ?cls a ?class ;" + " ?p ?o . "
	// + "FILTER (?class = owl:Class || ?class = rdfs:Class) "
	// + "LET (?clsNs := afn:namespace(?cls)) "
	// + "FILTER (?clsNs != \"http://www.w3.org/2002/07/owl#\" && "
	// + "?clsNs != \"http://www.w3.org/2000/01/rdf-schema#\" && "
	// + "?clsNs != \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\") "
	// + "} ";

	// QueryExecution qexec = QueryExecutionFactory.sparqlService(SERVICE,
	// query);
	// Model resultModel = qexec.execConstruct();
	// PrintUtil.printOut(resultModel.listStatements());
	// qexec.close();

	// query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
	// query += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";
	// query += "PREFIX owl: <http://www.w3.org/2002/07/owl#> ";
	// query += "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> ";
	// query += "PREFIX eg: <http://www.eg.org/eg#> ";
	// query += "SELECT distinct ?class1 ?class2 ";
	// query += "WHERE { " + "?s <http://xmlns.com/foaf/spec/member> ?o . "
	// + "?o  a ?class2 . " +
	// // " ?class2 a rdfs:Class. " +
	// "?s  a ?class1 . " +
	// // "?class1 a rdfs:Class ." +
	// //
	// "FILTER ( ! regex ( str(?property), \"^http://www.w3.org/2002/07/owl#\") && "
	// // +
	// //
	// "! regex ( str(?property), \"^http://www.w3.org/2000/01/rdf-schema#\") && "
	// // +
	// //
	// "! regex ( str(?property), \"^http://www.w3.org/1999/02/22-rdf-syntax-ns#\") ) "
	// // +
	// //
	// "FILTER ( (?c1 = owl:Class || ?c1 = rdfs:Class)   && (?c2 = owl:Class || ?c2 = rdfs:Class) ) "
	// // +
	//
	// "} ";
	//
	// QueryExecution qexec = QueryExecutionFactory.sparqlService(SERVICE,
	// query);
	// ResultSet r = qexec.execSelect();
	// while (r.hasNext()) {
	// QuerySolution s = r.nextSolution();
	// System.out.println(s.get("class1") + " "
	// // + s.getResource("property") + " "
	// + s.get("class2"));
	// // String p = s.get("property").toString();
	// // if (!p.startsWith("http://www.w3.org/2002/07/owl#")
	// // && !p.startsWith("http://www.w3.org/2000/01/rdf-schema#")
	// // && !p.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
	// // {
	// // System.out.println(p);
	// // }
	// }

	// }
}
