package org.cytoscape.vsdl3c.internal.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;

import org.cytoscape.property.CyProperty;
import org.cytoscape.vsdl3c.internal.ContextManager;
import org.cytoscape.vsdl3c.internal.SPARQLEndpointConfig;
import org.cytoscape.vsdl3c.internal.Util;
import org.cytoscape.vsdl3c.internal.ui.ContextSynchronizerPanel;
import org.cytoscape.vsdl3c.internal.ui.SPARQLEndpointConfigPanel;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Add a new context from remote publication URI
 *
 */
public class AddContextTask extends AbstractTask {

	private String source;
	private ContextManager cm;
	private String context = "temp";
	private ContextSynchronizerPanel panel;

	public AddContextTask(String source, ContextManager cm,
			ContextSynchronizerPanel panel,
			SPARQLEndpointConfig endpointConfig,
			SPARQLEndpointConfigPanel configPanel) {
		this.source = source;
		this.cm = cm;
		this.panel = panel;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {

		final File configDir = new File(System.getProperty("user.home"),
				CyProperty.DEFAULT_PROPS_CONFIG_DIR);
		File semscape = new File(configDir, "semscape");
		File download = new File(semscape, "download");
		File contextTarGzip = new File(download, context + ".tar.gz");

		URL url = new URL(source);
		url.openConnection();
		InputStream reader = url.openStream();

		FileOutputStream writer = new FileOutputStream(contextTarGzip);
		byte[] buffer = new byte[1024];

		int totalBytesRead = 0;
		int bytesRead = 0;

		while ((bytesRead = reader.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
			buffer = new byte[1024];
			totalBytesRead += bytesRead;
		}
		writer.close();
		reader.close();

		List<File> files = Util.unTarGzip(download, context + ".tar.gz",
				download);

		File rdf = null;
		for (File file : files) {
			if (file.getName().endsWith("rdf")) {
				rdf = file;
				break;
			}
		}
		if (rdf == null) {
			JOptionPane.showMessageDialog(null,
					"No context metadata (rdf) found from: " + source);
			return;
		}

		System.out.println("start parsing: " + rdf.getAbsolutePath());
		Model m = ModelFactory.createDefaultModel();

		try {
			m.read(new InputStreamReader(new FileInputStream(rdf), "utf-8"), "");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ResIterator iter = m.listSubjectsWithProperty(RDF.type,
				ContextManager.CONTEXT);

		if (!iter.hasNext()) {
			JOptionPane.showMessageDialog(null,
					"No remote context found from: " + source);
			return;
		}
		String newContext = null;
		while (iter.hasNext()) {
			Resource subject = iter.nextResource();
			Statement stmt = subject
					.getProperty(ContextManager.HAS_CONTEXT_NAME);
			newContext = stmt.getObject().toString();
			break;
		}
		if (newContext == null) {
			JOptionPane.showMessageDialog(null,
					"No remote context name found from: " + source);
			return;
		}

		File contextParentDir = new File(semscape, "context");
		File contextDirSource = new File(download, newContext);
		File contextDirTarget = new File(contextParentDir, newContext);

		Util.copyDirectory(contextDirSource, contextDirTarget);
		cm.updateContext(newContext);
		panel.initGUI();
	}
}
