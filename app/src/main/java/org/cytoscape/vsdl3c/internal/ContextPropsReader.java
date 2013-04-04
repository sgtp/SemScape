package org.cytoscape.vsdl3c.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * During initialization of the plugin bundle, it loads the settings into
 * CyProperty and registers the property as a service. Then the configuration
 * will appear in the Edit-->Preference->Properties. This class is the reader of
 * the context configs: "context.props" and "config.props" in
 * %user_home%/semscape.
 * 
 * In Context Configuration Panel (semscape/context), "context.props" tells
 * whether it's active or inactive. Other UI configuration, e.g. whether to show
 * tooltip of SPARQL query string in context menu, can be customized in UI
 * Configuration Panel (semscape/config), i.e. "config.props".
 * 
 * Each property value is “true” for active or “false” for inactive (other
 * values not “true”, means “false”). If you delete the property, it’s the same
 * as making the value “false”.
 * 
 */
public class ContextPropsReader extends AbstractConfigDirPropsReader {

	private ContextManager cm;

	ContextPropsReader(String name, String fileName, SavePolicy sp,
			ContextManager cm) {
		super(name, fileName, sp);
		this.cm = cm;

		scanLocalContexts();
	}

	/**
	 * load "context.props" and "config.props" in %user_home%/semscape.
	 */
	private void scanLocalContexts() {

		final File configDir = new File(System.getProperty("user.home"),
				CyProperty.DEFAULT_PROPS_CONFIG_DIR);
		final File semscape = new File(configDir, "semscape");
		final File contextDir = new File(semscape, "context");

		HashSet<String> subDirs = new HashSet<String>();
		for (File file : contextDir.listFiles()) {
			if (file.isDirectory()) {
				String context = file.getName();
				subDirs.add(context);
				if (this.props.getProperty(context) == null) {
					props.put(context, "true");
				}
			}
		}

		HashSet<String> toRemove = new HashSet<String>();
		for (Object key : props.keySet()) {
			if (!subDirs.contains(key)) {
				toRemove.add(key.toString());
			}
		}
		for (String r : toRemove) {
			props.remove(r);
		}

		for (Object key : props.keySet()) {
			if ("true".equals(props.get(key.toString()))) {
				cm.activateContext(key.toString());
			}
		}

	}

}