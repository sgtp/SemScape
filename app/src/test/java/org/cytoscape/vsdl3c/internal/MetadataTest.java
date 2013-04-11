package org.cytoscape.vsdl3c.internal;

import static org.cytoscape.vsdl3c.internal.ContextManager.CONTEXT;
import static org.cytoscape.vsdl3c.internal.ContextManager.HAS_CONTEXT_NAME;
import static org.cytoscape.vsdl3c.internal.ContextManager.HAS_SOURCE;
import static org.cytoscape.vsdl3c.internal.ContextManager.HAS_TIME;
import static org.cytoscape.vsdl3c.internal.Util.resource;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQuery.HAS_SPARQL_QUERY_STRING;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate.APPLYS_TO;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate.HAS_ATTRIBUTE_VALUE;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate.HAS_COLUMN_NAME;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate.HAS_CONSTRAINT;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate.HAS_PARAMETER;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate.HAS_PARAMETER_NAME;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate.NODE;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQueryTemplate.SPARQL_QUERY_TEMPLATE;
import static org.cytoscape.vsdl3c.internal.model.SPARQLQuery.SPARQL_QUERY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class MetadataTest {

	@Test
	public void testYigangContextMetadata() throws Exception {
		PrefixMapping pm = Util.getPrefixMapping("prefix.properties");

		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefixes(pm);
		Resource yigang = m.createResource(resource("yigang").getURI());
		yigang.addProperty(HAS_CONTEXT_NAME, "yigang");
		yigang.addProperty(RDF.type, CONTEXT);
		yigang.addProperty(
				HAS_SOURCE,
				m.createResource("https://vsdlc3.googlecode.com/svn/trunk/vsdl3c/app/binary/semscape/yigang.tar.gz"));
		Calendar calendar = Calendar.getInstance();
		calendar.set(2012, 8, 11, 10, 1, 0);
		yigang.addLiteral(HAS_TIME, m.createTypedLiteral(calendar));

		String queryString;

		Resource query1 = m.createResource(resource("query1").getURI());
		query1.addProperty(RDF.type, SPARQL_QUERY_TEMPLATE);

		queryString = Util.getConfigMap("fraunhofer.queries").get(
				"?node_pao:Hit_01");

		query1.addProperty(HAS_SPARQL_QUERY_STRING, queryString);

		query1.addProperty(RDFS.label, "query 1 for pao:Hit");
		query1.addProperty(RDFS.comment,
				"This is a query template sample for pao:Hit");
		query1.addProperty(DC.publisher, resource("yigang_the_publisher"));
		query1.addProperty(APPLYS_TO, NODE);

		Resource para1 = m.createResource(resource("para1").getURI());
		query1.addProperty(HAS_PARAMETER, para1);
		para1.addProperty(HAS_PARAMETER_NAME, "!!!node_uri!!!");
		para1.addProperty(HAS_COLUMN_NAME, "name");

		Resource constraint1 = m.createResource(resource("constraint1")
				.getURI());
		query1.addProperty(HAS_CONSTRAINT, constraint1);
		constraint1.addProperty(HAS_COLUMN_NAME, "namespace");
		constraint1.addProperty(HAS_ATTRIBUTE_VALUE,
				"http://www.scai.fraunhofer.de/");

		Resource constraint2 = m.createResource(resource("constraint2")
				.getURI());
		query1.addProperty(HAS_CONSTRAINT, constraint2);
		constraint2.addProperty(HAS_COLUMN_NAME, pm.expandPrefix("rdf:type"));
		constraint2
				.addProperty(HAS_ATTRIBUTE_VALUE, pm.expandPrefix("pao:Hit"));

		Resource constraint3 = m.createResource(resource("constraint3")
				.getURI());
		query1.addProperty(HAS_CONSTRAINT, constraint3);
		constraint3.addProperty(HAS_COLUMN_NAME, pm.expandPrefix("endpoint"));
		constraint3.addProperty(HAS_ATTRIBUTE_VALUE,
				"http://ops-virtuoso.scai.fraunhofer.de:8891/sparql");

		Resource query2 = m.createResource(resource("query2").getURI());
		query2.addProperty(RDF.type, SPARQL_QUERY);

		queryString = Util.getConfigMap("fraunhofer.queries").get(
				"query_sample_01");

		query2.addProperty(HAS_SPARQL_QUERY_STRING, queryString);
		query2.addProperty(RDFS.label, "query sample 2 new");
		query2.addProperty(RDFS.comment, "This is a query sample.");
		query2.addProperty(DC.publisher, resource("yigang_the_publisher"));

		//TODO to be fixed
		//m.write(new OutputStreamWriter(new FileOutputStream(new File("/context/yigang/query.rdf")), "utf-8"), "");

	}

	@Test
	public void testSPARQLEndpointMetadata() throws Exception {
		// Model m = ModelFactory.createDefaultModel();
		// Resource endpoint =
		// m.createResource("http://mesh.bio2rdf.org/sparql");
		// endpoint.addProperty(RDF.type, SPARQLEndpoint.SPARQL_ENDPOINT);
		// endpoint.addProperty(RDFS.label, "MeSH : Medical Subject Headings");
		// endpoint.addProperty(SPARQLEndpoint.HAS_NAMED_GRAPH, "default");
		// endpoint.addProperty(SPARQLEndpoint.HAS_SPARQL_ENDPOINT_USER,
		// "theUser");
		// endpoint.addProperty(SPARQLEndpoint.HAS_SPARQL_ENDPOINT_PASSWORD,
		// "thePassword");
		// m.write(new OutputStreamWriter(new FileOutputStream(new File(
		// "/test/endpoint.rdf")), "utf-8"), "");
	}

	@Test
	public void testCommonContextMetadata() throws Exception {
		PrefixMapping pm = Util.getPrefixMapping("prefix.properties");

		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefixes(pm);
		Resource common = m.createResource(resource("common").getURI());
		common.addProperty(HAS_CONTEXT_NAME, "common");
		common.addProperty(RDF.type, CONTEXT);
		common.addProperty(
				HAS_SOURCE,
				m.createResource("https://vsdlc3.googlecode.com/svn/trunk/vsdl3c/app/binary/semscape/common.tar.gz"));
		Calendar calendar = Calendar.getInstance();
		calendar.set(2012, 8, 11, 10, 0, 1);
		common.addLiteral(HAS_TIME, m.createTypedLiteral(calendar));

		String queryString;

		Resource query1 = m.createResource(resource("query1").getURI());
		query1.addProperty(RDF.type, SPARQL_QUERY_TEMPLATE);

		queryString = Util.getConfigMap("fraunhofer.queries").get(
				"?node_as_subject");

		query1.addProperty(HAS_SPARQL_QUERY_STRING, queryString);
		query1.addProperty(RDFS.label, "Node as subject");
		query1.addProperty(RDFS.comment, "Query for {?node ?p ?o}");
		query1.addProperty(DC.publisher, resource("common_the_publisher"));
		query1.addProperty(APPLYS_TO, NODE);

		Resource para1 = m.createResource(resource("para1").getURI());
		query1.addProperty(HAS_PARAMETER, para1);
		para1.addProperty(HAS_PARAMETER_NAME, "!!!node_uri!!!");
		para1.addProperty(HAS_COLUMN_NAME, "name");

		Resource query2 = m.createResource(resource("query2").getURI());
		query2.addProperty(RDF.type, SPARQL_QUERY_TEMPLATE);
		queryString = Util.getConfigMap("fraunhofer.queries").get(
				"?node_as_object");

		query2.addProperty(HAS_SPARQL_QUERY_STRING, queryString);
		query2.addProperty(RDFS.label, "Node as object");
		query2.addProperty(RDFS.comment, "Query for {?s ?p ?node}");
		query2.addProperty(DC.publisher, resource("common_the_publisher"));
		query2.addProperty(APPLYS_TO, NODE);

		Resource para2 = m.createResource(resource("para2").getURI());
		query2.addProperty(HAS_PARAMETER, para2);
		para2.addProperty(HAS_PARAMETER_NAME, "!!!node_uri!!!");
		para2.addProperty(HAS_COLUMN_NAME, "name");
		
		
		Resource query3 = m.createResource(resource("query3").getURI());
		query3.addProperty(RDF.type, SPARQL_QUERY_TEMPLATE);
		queryString = Util.getConfigMap("fraunhofer.queries").get(
				"?node_rdfs_label");

		query3.addProperty(HAS_SPARQL_QUERY_STRING, queryString);
		query3.addProperty(RDFS.label, "Node rdfs:label");
		query3.addProperty(RDFS.comment, "Query for {?node rdfs:label ?o}");
		query3.addProperty(DC.publisher, resource("common_the_publisher"));
		query3.addProperty(APPLYS_TO, NODE);

		Resource para3 = m.createResource(resource("para3").getURI());
		query3.addProperty(HAS_PARAMETER, para3);
		para3.addProperty(HAS_PARAMETER_NAME, "!!!node_uri!!!");
		para3.addProperty(HAS_COLUMN_NAME, "name");
		
		//TODO to be fixed
		//m.write(new OutputStreamWriter(new FileOutputStream(new File("/context/common/query.rdf")), "utf-8"), "");
	}
}
