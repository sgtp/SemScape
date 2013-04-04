package org.cytoscape.vsdl3c.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.vsdl3c.internal.model.GraphPolicy;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class Util {

	/**
	 * the default namespace of the RDF metadata of endpoint, query..
	 */
	public static final String SEMSCAPE = "http://www.cytoscape.org/semscape#";

	// some constant
	public static String SPARQL_ENDPOINT_SCHEMA_VIZMAP_TITLE = "SPARQL Endpoint Schema";
	public static final String SPARQL_ENDPOINT_SCHEMA_VIZMAP_FILE = "sparql_endpoint_schema_vizmap.xml";
	public static String SPARQL_CONSTRUCT_QUERY_VIZMAP_TITLE = "SPARQL Construct Query";
	public static final String SPARQL_CONSTRUCT_QUERY_VIZMAP_FILE = "sparql_construct_query_vizmap.xml";

	public static String ENDPOINT_COLOMN_NAME = "endpoint";
	public static String NAMESPACE_URI = "namespace";

	/**
	 * load the configuration from classpath
	 * 
	 * @param config
	 *            , the config file name
	 * @return map of the configuration <configuration_key, configuration_value>
	 */
	public static Map<String, String> getConfigMap(String config) {
		HashMap<String, String> map = new HashMap<String, String>();
		Properties props = new Properties();
		URL dp = CyActivator.class.getResource(config);
		try {
			props.load(dp.openStream());
			Iterator<Entry<Object, Object>> iter = props.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Object, Object> entry = iter.next();
				map.put(entry.getKey().toString(), entry.getValue().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return map;
	}

	/**
	 * load PrefixMapping from classpath
	 * 
	 * @param prefixConfig
	 *            , the config file name
	 * @return PrefixMapping object
	 */
	public static PrefixMapping getPrefixMapping(String prefixConfig) {
		Map<String, String> map = Util.getConfigMap(prefixConfig);
		PrefixMapping pm = new PrefixMappingImpl() {
			public String expandPrefix(String arg) {
				if (arg == null) {
					return "";
				}
				return super.expandPrefix(arg);
			}

			public String shortForm(String arg) {
				if (arg == null) {
					return "";
				}
				return super.shortForm(arg);
			}
		};
		Iterator<Entry<String, String>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			pm.setNsPrefix(entry.getKey(), entry.getValue());
		}
		return pm;
	}

	/**
	 * get VisualStyle by its title
	 * 
	 * @param title
	 *            , the title of the VisualStyle
	 * @param vmm
	 *            , VisualMappingManager object
	 * @return the VisualStyle object with the title, or null if no such title
	 */
	public static VisualStyle getVisualStyleByTitle(String title,
			VisualMappingManager vmm) {
		for (VisualStyle vs : vmm.getAllVisualStyles()) {
			if (vs.getTitle().equals(title)) {
				return vs;
			}
		}
		return null;
	}

	public static Set<VisualStyle> getVisualStyleByTitleAsSet(String title,
			VisualMappingManager vmm) {
		Set<VisualStyle> set = new HashSet<VisualStyle>();
		VisualStyle vs = getVisualStyleByTitle(title, vmm);
		if (vs != null) {
			set.add(vs);
		}
		return set;
	}

	/**
	 * get the namespace of a URI
	 * 
	 * @param uri
	 *            , the URI
	 * @return the namesapce of the URI
	 */
	public static String getNamespace(String uri) {
		int split = com.hp.hpl.jena.rdf.model.impl.Util.splitNamespace(uri);
		String ns = uri.substring(0, split);
		return ns;
	}

	/**
	 * reconstruct the query by the graph settings of the endpoint
	 * 
	 * @param endpoint
	 *            , the endpoint
	 * @param query
	 *            , the query string
	 * @return the modified QueryEngineHTTP object
	 */
	public static QueryEngineHTTP setNamedGraphs(SPARQLEndpoint endpoint,
			String query) {
		if (endpoint.getGraphPolicy().equals(GraphPolicy.NAMED_GRAPH)) {
			if (endpoint.getNamedGraphString() != null) {
				String[] namedGraphArray = endpoint.getNamedGraphString()
						.split(" ");

				String[] parts = Pattern.compile("where",
						Pattern.CASE_INSENSITIVE).split(query, 0);
				query = parts[0];
				for (String graph : namedGraphArray) {
					query += " FROM <" + graph + "> ";
				}
				query += " WHERE ";
				for (int i = 1; i < parts.length; i++) {
					query += parts[i];
				}
			}
			System.out.println("Modified Query String: " + query);
		} else if (endpoint.getGraphPolicy().equals(GraphPolicy.ALL_GRAPH)) {
			Matcher match = Pattern.compile("where", Pattern.CASE_INSENSITIVE)
					.matcher(query);
			match.find();
			int whereIndex = match.start();
			int left = query.indexOf("{", whereIndex);
			int right = query.lastIndexOf("}");
			String beforeLeft = query.substring(0, left);
			String graphG = " GRAPH ?g { ";
			String betweenLeftRight = query.substring(left + 1, right + 1);
			query = beforeLeft + " { " + graphG + betweenLeftRight + " } ";
			System.out.println("Modified Query String: " + query);
		}
		QueryEngineHTTP qexec = (QueryEngineHTTP) QueryExecutionFactory
				.sparqlService(endpoint.getUri(), query);

		if (endpoint.getGraphPolicy().equals(GraphPolicy.DEFAUT_GRAPH)) {
			if (endpoint.getNamedGraphString() != null) {
				String[] namedGraphArray = endpoint.getNamedGraphString()
						.split(" ");
				qexec.setDefaultGraphURIs(Arrays.asList(namedGraphArray));
			}
		}
		return qexec;

	}

	/**
	 * set APIKey parameter of the query
	 * 
	 * @param qexec
	 *            , the QueryEngineHTTP object
	 * @param endpoint
	 *            , the endpoint
	 * @return the modified QueryEngineHTTP object
	 */
	public static QueryEngineHTTP setAPIKey(QueryEngineHTTP qexec,
			SPARQLEndpoint endpoint) {

		if (endpoint.getAPIKey() != null) {
			qexec.addParam("apikey", endpoint.getAPIKey());
		}
		return qexec;

	}

	/**
	 * set authentication parameter of the query
	 * 
	 * @param endpoint
	 *            , the endpoint
	 */
	public static void setAuth(SPARQLEndpoint endpoint) {
		final String authUser = endpoint.getSPARQLEndpointUser();
		final String authPassword = endpoint.getSPARQLEndpointPassword();
		if (authUser != null && authPassword != null) {
			Authenticator.setDefault(new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(authUser, authPassword
							.toCharArray());
				}
			});
		}
	}

	/**
	 * convenient method of create RDF resource with the default namespace
	 * 
	 * @param local
	 *            , the local name of the RDF resource
	 * @return the created RDF resource
	 */
	public static final Resource resource(String local) {
		return ResourceFactory.createResource(SEMSCAPE + local);
	}

	/**
	 * convenient method of create RDF property with the default namespace
	 * 
	 * @param local
	 *            , the local name of the RDF property
	 * @return the created RDF property
	 */
	public static final Property property(String local) {
		return ResourceFactory.createProperty(SEMSCAPE, local);
	}

	/**
	 * Untar an input file into an output file.
	 * 
	 * The output file is created in the output folder, having the same name as
	 * the input file, minus the '.tar' extension.
	 * 
	 * @param inputFile
	 *            the input .tar file
	 * @param outputDir
	 *            the output directory file.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 * @return The {@link List} of {@link File}s with the untared content.
	 * @throws ArchiveException
	 */
	private static List<File> unTar(final File inputFile, final File outputDir)
			throws FileNotFoundException, IOException, ArchiveException {

		final List<File> untaredFiles = new LinkedList<File>();
		final InputStream is = new FileInputStream(inputFile);
		final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory()
				.createArchiveInputStream("tar", is);
		TarArchiveEntry entry = null;
		while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
			final File outputFile = new File(outputDir, entry.getName());
			if (entry.isDirectory()) {
				if (!outputFile.exists()) {
					if (!outputFile.mkdirs()) {
						throw new IllegalStateException(String.format(
								"Couldn't create directory %s.",
								outputFile.getAbsolutePath()));
					}
				}
			} else {
				final OutputStream outputFileStream = new FileOutputStream(
						outputFile);
				IOUtils.copy(debInputStream, outputFileStream);
				outputFileStream.close();
			}
			untaredFiles.add(outputFile);
		}
		debInputStream.close();

		return untaredFiles;
	}

	/**
	 * Ungzip an input file into an output file.
	 * <p>
	 * The output file is created in the output folder, having the same name as
	 * the input file, minus the '.gz' extension.
	 * 
	 * @param inputFile
	 *            the input .gz file
	 * @param outputDir
	 *            the output directory file.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 * @return The {@File} with the ungzipped content.
	 */
	private static File unGzip(final File inputFile, final File outputDir)
			throws FileNotFoundException, IOException {

		final File outputFile = new File(outputDir, inputFile.getName()
				.substring(0, inputFile.getName().length() - 3));

		final GZIPInputStream in = new GZIPInputStream(new FileInputStream(
				inputFile));
		final FileOutputStream out = new FileOutputStream(outputFile);

		for (int c = in.read(); c != -1; c = in.read()) {
			out.write(c);
		}

		in.close();
		out.close();

		return outputFile;
	}

	/**
	 * unTar and then unGzip the package
	 * 
	 * @param download
	 *            , the download directory where the package stored
	 * @param gizpFileName
	 *            , the name of the gizp file
	 * @param destination
	 *            , the destination of the directory for the extracted files
	 * @return the list of the extracted files
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ArchiveException
	 */
	public static List<File> unTarGzip(final File download,
			final String gizpFileName, final File destination)
			throws FileNotFoundException, IOException, ArchiveException {
		File tar = unGzip(new File(download, gizpFileName), download);
		return unTar(tar, destination);
	}

	/**
	 * copy the directory with its sub directories/files to the target location
	 * 
	 * @param sourceLocation
	 *            , the directory of the source
	 * @param targetLocation
	 *            , the directory of the target
	 * @throws IOException
	 */
	public static void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	/**
	 * Append the endpoint URI to the CyNode attribute
	 * 
	 * @param network
	 *            , the CyNetwork
	 * @param node
	 *            , the CyNode
	 * @param endpoint
	 *            , the endpoint URI
	 */
	public static void addEndpointValue(CyNetwork network, CyNode node,
			String endpoint) {
		String eps = network.getDefaultNodeTable().getRow(node.getSUID())
				.get(ENDPOINT_COLOMN_NAME, String.class);
		List<String> newEps = new ArrayList<String>();
		if (eps == null) {
			newEps.add(endpoint);
		} else {
			String[] array = eps.split(" ");
			List<String> l = Arrays.asList(array);
			if (l.contains(endpoint)) {
				return;
			} else {
				newEps.addAll(l);
				newEps.add(endpoint);
			}
		}
		String combine = "";
		boolean firstTime = true;
		for (String newEp : newEps) {
			if (!firstTime) {
				combine += " ";
			} else {
				firstTime = false;
			}
			combine += newEp;
		}

		network.getDefaultNodeTable().getRow(node.getSUID())
				.set(ENDPOINT_COLOMN_NAME, combine);

	}

	/**
	 * Append the endpoint URI to the CyEdge attribute
	 * 
	 * @param network
	 *            , the CyNetwork
	 * @param edge
	 *            , the CyEdge
	 * @param endpoint
	 *            , the endpoint URI
	 */
	public static void addEndpointValue(CyNetwork network, CyEdge edge,
			String endpoint) {
		String eps = network.getDefaultEdgeTable().getRow(edge.getSUID())
				.get(ENDPOINT_COLOMN_NAME, String.class);
		List<String> newEps = new ArrayList<String>();
		if (eps == null) {
			newEps.add(endpoint);
		} else {
			String[] array = eps.split(" ");
			List<String> l = Arrays.asList(array);
			if (l.contains(endpoint)) {
				return;
			} else {
				newEps.addAll(l);
				newEps.add(endpoint);
			}
		}
		String combine = "";
		boolean firstTime = true;
		for (String newEp : newEps) {
			if (!firstTime) {
				combine += " ";
			} else {
				firstTime = false;
			}
			combine += newEp;
		}

		network.getDefaultEdgeTable().getRow(edge.getSUID())
				.set(ENDPOINT_COLOMN_NAME, combine);

	}

	/**
	 * delete the directory and its sub directories/files
	 * @param path
	 * @return
	 */
	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

}
