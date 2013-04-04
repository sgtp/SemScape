package org.cytoscape.vsdl3c.internal.task;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.cytoscape.property.CyProperty;
import org.cytoscape.vsdl3c.internal.ContextManager;
import org.cytoscape.vsdl3c.internal.Util;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Check for update of the context, by comparing the timestamp of the publication time.
 *
 */
public class CheckForUpdateTask extends AbstractTask {

	private String context;
	private ContextManager cm;
	private JLabel checkResult;
	private JButton u;

	public CheckForUpdateTask(String context, ContextManager cm,
			JLabel checkResult, JButton u) {
		this.context = context;
		this.cm = cm;
		this.checkResult = checkResult;
		this.u = u;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		final File configDir = new File(System.getProperty("user.home"),
				CyProperty.DEFAULT_PROPS_CONFIG_DIR);
		File semscape = new File(configDir, "semscape");
		File download = new File(semscape, "download");
		File contextTarGzip = new File(download, context + ".tar.gz");

		String source = this.cm.getContextSource(this.context);
		if (source == null) {
			JOptionPane.showMessageDialog(null,
					"The source URI is not found for: " + this.context);
			return;
		}
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

		Util.unTarGzip(download, context + ".tar.gz", download);

		File rdf = new File(download, context + File.separator + "query.rdf");
		System.out.println("start parsing: " + rdf.getAbsolutePath());
		Model m = ModelFactory.createDefaultModel();

		try {
			m.read(new InputStreamReader(new FileInputStream(rdf), "utf-8"), "");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ResIterator iter = m.listSubjectsWithProperty(
				ContextManager.HAS_CONTEXT_NAME, context);

		if (!iter.hasNext()) {
			JOptionPane.showMessageDialog(null, "No remote context found for: "
					+ this.context);
			return;
		}

		Calendar time = null;
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
		if (time == null) {
			JOptionPane.showMessageDialog(null, "Unkown publication time for: "
					+ this.context);
			return;
		}

		Calendar localTime = this.cm.getTime(this.context);
		if (localTime == null || localTime.before(time)) {
			checkResult.setText(" <- Require Update");
			checkResult.setForeground(Color.YELLOW);
			u.setEnabled(true);
		} else if (localTime.after(time)) {
			JOptionPane.showMessageDialog(null,
					"Publication time in remote server is ealier than that of the local one: "
							+ this.context);
		}
	}
}
