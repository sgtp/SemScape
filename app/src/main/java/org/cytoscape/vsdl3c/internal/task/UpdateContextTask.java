package org.cytoscape.vsdl3c.internal.task;

import java.awt.Color;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.cytoscape.property.CyProperty;
import org.cytoscape.vsdl3c.internal.ContextManager;
import org.cytoscape.vsdl3c.internal.SPARQLEndpointConfig;
import org.cytoscape.vsdl3c.internal.Util;
import org.cytoscape.vsdl3c.internal.ui.SPARQLEndpointConfigPanel;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Update the context from remote publication URI
 *
 */
public class UpdateContextTask extends AbstractTask {

	private String context;
	private ContextManager cm;
	private JLabel checkResult;
	private JButton u;

	public UpdateContextTask(String context, ContextManager cm,
			JLabel checkResult, JButton u, SPARQLEndpointConfig endpointConfig,
			SPARQLEndpointConfigPanel configPanel) {
		this.context = context;
		this.cm = cm;
		this.checkResult = checkResult;
		this.u = u;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		System.out.println("update: " + context);
		final File configDir = new File(System.getProperty("user.home"),
				CyProperty.DEFAULT_PROPS_CONFIG_DIR);
		File semscape = new File(configDir, "semscape");
		File download = new File(semscape, "download");
		File contextParentDir = new File(semscape, "context");
		File contextDirSource = new File(download, context);
		File contextDirTarget = new File(contextParentDir, context);

		Util.copyDirectory(contextDirSource, contextDirTarget);
		cm.updateContext(context);


		checkResult.setText("Updated!");
		checkResult.setForeground(Color.GREEN.darker());
		u.setEnabled(false);
	}

}
