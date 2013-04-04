package org.cytoscape.vsdl3c.internal.task;

import java.io.File;

import org.cytoscape.property.CyProperty;
import org.cytoscape.vsdl3c.internal.ContextManager;
import org.cytoscape.vsdl3c.internal.Util;
import org.cytoscape.vsdl3c.internal.ui.ContextSynchronizerPanel;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Delete the context permanently and clear the caches. 
 *
 */
public class DeleteContextTask extends AbstractTask {

	private String context;
	private ContextManager cm;
	private ContextSynchronizerPanel panel;

	public DeleteContextTask(String context, ContextManager cm,
			ContextSynchronizerPanel panel) {
		this.context = context;
		this.cm = cm;
		this.panel = panel;

	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		System.out.println("delete: " + context);
		final File configDir = new File(System.getProperty("user.home"),
				CyProperty.DEFAULT_PROPS_CONFIG_DIR);
		File semscape = new File(configDir, "semscape");
		File contextParentDir = new File(semscape, "context");
		File contextDirTarget = new File(contextParentDir, context);
		
		Util.deleteDirectory(contextDirTarget);

		cm.deactivateContext(context);
		panel.initGUI();
	}

}
