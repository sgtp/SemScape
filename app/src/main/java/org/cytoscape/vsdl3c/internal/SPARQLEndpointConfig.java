package org.cytoscape.vsdl3c.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * The configuration of SPARQL endpoints defined in
 * %user_home%/semscape/sparql_endpoint.rdf.
 * 
 */
public class SPARQLEndpointConfig implements CyShutdownListener {

	// temperate URI and Resource for creating new endpoint definition
	public static String newURI = "http://new.sparql.endpoint.uri/sparql";
	private Resource newEndpoint;

	/**
	 * the in-memory cache of %user_home%/semscape/sparql_endpoint.rdf
	 */
	private Model model;

	/**
	 * the RDF file of %user_home%/semscape/sparql_endpoint.rdf
	 */
	private File rdf;

	public SPARQLEndpointConfig() {
		model = ModelFactory.createDefaultModel();
		newEndpoint = model.createResource(newURI);

		final File configDir = new File(System.getProperty("user.home"),
				CyProperty.DEFAULT_PROPS_CONFIG_DIR);
		final File semscape = new File(configDir, "semscape");

		rdf = new File(semscape, "sparql_endpoint.rdf");

		if (rdf.exists()) {
			System.out.println("start parsing: " + rdf.getAbsolutePath());

			try {
				model.read(new InputStreamReader(new FileInputStream(rdf),
						"utf-8"), "");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * get all SPARQL endpoints
	 * 
	 * @return all SPARQL endpoints
	 */
	public Set<SPARQLEndpoint> getAllSPARQLEndpoints() {
		Set<SPARQLEndpoint> endpoints = new HashSet<SPARQLEndpoint>();
		ResIterator it = model.listResourcesWithProperty(RDF.type,
				SPARQLEndpoint.SPARQL_ENDPOINT);
		while (it.hasNext()) {
			endpoints.add(new SPARQLEndpoint(it.nextResource()));
		}
		return endpoints;
	}

	/**
	 * check whether the config contains the endpoint
	 * 
	 * @param uri
	 *            , the URI of the endpoint to be checked
	 * @return true, if contains; otherwise, false.
	 */
	public boolean containsEndpoint(String uri) {
		if (uri == null) {
			return false;
		}
		return model.contains(model.createResource(uri), RDF.type,
				SPARQLEndpoint.SPARQL_ENDPOINT);
	}

	/**
	 * set the endpoint as the default one
	 * 
	 * @param endpoint
	 */
	public void setDefaultSPARQLEndpoint(SPARQLEndpoint endpoint) {
		for (SPARQLEndpoint dp : getAllSPARQLEndpoints()) {
			if (dp.isDefaultSPARQLEndpoint()) {
				dp.setDefaultSPARQLEndpoint(false);
			}
		}
		endpoint.setDefaultSPARQLEndpoint(true);
	}

	/**
	 * set the endpoint as NOT the default one
	 * 
	 * @param endpoint
	 */
	public void removeDefaultSPARQLEndpoint(SPARQLEndpoint endpoint) {
		endpoint.setDefaultSPARQLEndpoint(false);
	}

	/**
	 * get the default endpoint
	 * 
	 * @return the default endpoint
	 */
	public SPARQLEndpoint getDefaultSPARQLEndpoint() {
		for (SPARQLEndpoint dp : getAllSPARQLEndpoints()) {
			if (dp.isDefaultSPARQLEndpoint()) {
				return dp;
			}
		}
		return null;
	}

	/**
	 * get endpoint list by URIs
	 * 
	 * @param uris
	 *            , the URI list of the endpoints
	 * @return the endpoint list
	 */
	public List<SPARQLEndpoint> getSPARQLEndpointList(List<String> uris) {
		List<SPARQLEndpoint> endpoints = new ArrayList<SPARQLEndpoint>();
		for (String uri : uris) {
			if (containsEndpoint(uri)) {
				endpoints.add(new SPARQLEndpoint(model.getResource(uri)));
			}
		}
		return endpoints;
	}

	/**
	 * get endpoint by URI
	 * 
	 * @param uri
	 *            , the URI of the endpoint
	 * @return the endpoint with the URI
	 */
	public SPARQLEndpoint getSPARQLEndpoint(String uri) {
		if (containsEndpoint(uri)) {
			return new SPARQLEndpoint(model.getResource(uri));
		} else {
			return null;
		}
	}

	/**
	 * create a new endpoint with the URI
	 * 
	 * @param uri
	 *            , the URI of the endpoint
	 * @return the new endpoint created
	 */
	public SPARQLEndpoint createSPARQLEndpoint(String uri) {
		return new SPARQLEndpoint(model, uri);
	}

	/**
	 * delete the endpoint from the configuration
	 * 
	 * @param endpoint
	 *            , the endpoint to be deleted
	 */
	public void deleteSPARQLEndpoint(SPARQLEndpoint endpoint) {
		PrintUtil.printOut(model.listStatements());
		if (endpoint == null) {
			return;
		}
		model.removeAll(model.createResource(endpoint.getUri()), null, null);
	}

	/**
	 * create a new endpoint with the temperate URI of
	 * SPARQLEndpointConfig.newURI
	 * 
	 * @return the endpoint to be deleted
	 */
	public SPARQLEndpoint createSPARQLEndpoint() {
		model.removeAll(newEndpoint, null, null);
		newEndpoint.addProperty(RDF.type, SPARQLEndpoint.SPARQL_ENDPOINT);
		newEndpoint.addLiteral(RDFS.label, "New SPARQL Endpoint");
		return new SPARQLEndpoint(model.createResource(newURI));
	}

	/** 
	 * save the config before the Cytoscape is closing
	 */
	public void handleEvent(CyShutdownEvent e) {
		System.out.println("start saving: " + rdf.getAbsolutePath());
		try {
			model.write(new OutputStreamWriter(new FileOutputStream(rdf),
					"utf-8"), "");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
}
