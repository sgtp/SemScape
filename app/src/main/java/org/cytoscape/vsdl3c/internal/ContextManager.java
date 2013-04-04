package org.cytoscape.vsdl3c.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;
import org.cytoscape.vsdl3c.internal.model.SPARQLQuery;
import org.cytoscape.vsdl3c.internal.ui.SPARQLEndpointConfigPanel;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A context is a package containing the SPARQL queries and their metadata for a
 * single user or shared by a group of users published by an organization. A
 * context is technically represented with a RDF file of "query.rdf", stored in
 * %user_home%/semscape/context/%context_name% directory. ContextManager is
 * responsible for loading, activating, deactivating and updating the contexts
 * by manipulating the RDF model.
 * 
 */
public class ContextManager implements PropertyUpdatedListener {

	/**
	 * the rdf:type of context RDF resource
	 */
	public static final Resource CONTEXT = Util.resource("Context");

	/**
	 * the timestamp of the context, used for "check update"
	 */
	public static final Property HAS_TIME = Util.property("hasTime");

	/**
	 * the name of the context, used in
	 * %user_home%/semscape/context/%context_name% to store the RDF file of
	 * "query.rdf"
	 */
	public static final Property HAS_CONTEXT_NAME = Util
			.property("hasContextName");

	/**
	 * the publication URI of the context
	 */
	public static final Property HAS_SOURCE = Util.property("hasSource");

	/**
	 * in-memory cache of <context_name, RDF Jena Model of "queyr.rdf">
	 */
	private HashMap<String, Model> context2Model;

	/**
	 * the activated context set
	 */
	private Set<String> activeContexts;

	public ContextManager() {
		context2Model = new HashMap<String, Model>();
		activeContexts = new HashSet<String>();
	}

	/**
	 * load the context from %user_home%/semscape/context/%context_name% and
	 * cache it
	 * 
	 * @param context
	 *            , the name of the context
	 */
	private void loadContext(String context) {
		final File configDir = new File(System.getProperty("user.home"),
				CyProperty.DEFAULT_PROPS_CONFIG_DIR);
		File semscape = new File(configDir, "semscape");
		File contextDir = new File(semscape, "context");

		File rdf = new File(contextDir, context + File.separator + "query.rdf");
		System.out.println("start parsing: " + rdf.getAbsolutePath());
		Model m = ModelFactory.createDefaultModel();

		try {
			m.read(new InputStreamReader(new FileInputStream(rdf), "utf-8"), "");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		context2Model.put(context, m);
	}

	/**
	 * activate the context, whose queries are ready to use by the users.
	 * 
	 * @param context
	 *            , the name of the context
	 */
	public void activateContext(String context) {
		activeContexts.add(context);
		if (getModel(context) == null) {
			loadContext(context);
		}
	}

	/**
	 * reload the context and update the in-memory cache
	 * 
	 * @param context
	 *            , the name of the context
	 */
	public void updateContext(String context) {
		activeContexts.add(context);
		loadContext(context);
	}

	/**
	 * deactivate the context, whose queries are hidden from the users.
	 * 
	 * @param context
	 *            , the name of the context
	 */
	public void deactivateContext(String context) {
		activeContexts.remove(context);
	}

	public Set<String> getActivateContexts() {
		return activeContexts;
	}

	/**
	 * get the the Jena RDF Model of the context.
	 * 
	 * @param context
	 *            , the name of the context
	 * @return the Jena RDF Model
	 */
	public Model getModel(String context) {
		if (isActive(context)) {
			return context2Model.get(context);
		} else {
			return null;
		}
	}

	public boolean isActive(String context) {
		return activeContexts.contains(context);
	}

	/**
	 * get the publication URI of the context
	 * 
	 * @param context
	 *            , the name of the context
	 * @return the publication URI
	 */
	public String getContextSource(String context) {
		String source = null;
		if (!isActive(context)) {
			return source;
		}
		ResIterator iter = this.getModel(context).listSubjectsWithProperty(
				HAS_CONTEXT_NAME, context);

		while (iter.hasNext()) {
			Resource subject = iter.nextResource();
			Resource src = subject.getPropertyResourceValue(HAS_SOURCE);
			if (src != null) {
				source = src.getURI();
				break;
			}
		}
		return source;
	}

	/**
	 * the timestamp of the context, used for "check update"
	 * 
	 * @param context
	 *            , the name of the context
	 * @return the timestamp
	 */
	public Calendar getTime(String context) {
		Calendar time = null;
		if (!isActive(context)) {
			return time;
		}
		ResIterator iter = this.getModel(context).listSubjectsWithProperty(
				HAS_CONTEXT_NAME, context);

		while (iter.hasNext()) {
			Resource subject = iter.nextResource();
			Statement stmt = subject.getProperty(ContextManager.HAS_TIME);

			if (stmt != null) {
				Object value = stmt.getObject().asLiteral().getValue();
				if (value instanceof XSDDateTime) {
					time = ((XSDDateTime) value).asCalendar();
					break;
				}
			}
		}
		return time;
	}

	/**
	 * get all of the SPARQL query strings of all the active contexts
	 * 
	 * @return all of the SPARQL query strings of all the active contexts
	 */
	public Set<SPARQLQuery> getAllSPARQLQueries() {
		Set<SPARQLQuery> queries = new HashSet<SPARQLQuery>();
		for (String context : getActivateContexts()) {
			Model m = getModel(context);
			ResIterator it = m.listSubjectsWithProperty(RDF.type,
					SPARQLQuery.SPARQL_QUERY);
			while (it.hasNext()) {
				SPARQLQuery query = new SPARQLQuery(it.nextResource());
				queries.add(query);
			}
		}
		return queries;
	}

	/**
	 * There’re listeners monitoring the changes of the configuration
	 * properties. The context menu of parametric SPARQL query will only load
	 * the queries in the contexts that activated by the user
	 */
	public void handleEvent(final PropertyUpdatedEvent e) {

		if (e.getSource() == null || e.getSource().getName() == null)
			return;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (e.getSource().getName().equals("semscape/context")) {
					Set<String> toDeactivate = new HashSet<String>();
					final Properties props = (Properties) e.getSource()
							.getProperties();
					for (Entry entry : props.entrySet()) {
						String context = entry.getKey().toString();
						String active = entry.getValue().toString();
						if (!activeContexts.contains(context)
								&& "true".equals(active)) {
							activateContext(context);
						}
					}
					for (String context : activeContexts) {
						if ((props.containsKey(context) && !"true".equals(props
								.get(context))) || !props.containsKey(context)) {
							toDeactivate.add(context);
						}
					}
					for (String context : toDeactivate) {
						deactivateContext(context);
					}

				}
			}
		});
	}
}
