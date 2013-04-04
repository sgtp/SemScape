package org.cytoscape.vsdl3c.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

/**
 * Save the vismap before closing the Cytoscape
 *
 */
public class VizmapWriter implements CyShutdownListener {
	
	
	private VizmapWriterFactory vwf;
	private VisualMappingManager vmm;
	private CyApplicationConfiguration config ;
	private TaskManager dialogTaskManager;
	private File vizmapFile;
	private String title;
	

	public VizmapWriter(VizmapWriterFactory vwf,
			VisualMappingManager vmm, CyApplicationConfiguration config,
			TaskManager dialogTaskManager, String title, String file) {
		super();
		this.vwf = vwf;
		this.vmm = vmm;
		this.config = config;
		this.dialogTaskManager = dialogTaskManager;
		this.title = title;
		final File semscape = new File(
				config.getConfigurationDirectoryLocation(), "semscape");
		this.vizmapFile = new File(semscape, file);
	}



	public void handleEvent(CyShutdownEvent e) {
		try {
			Set<VisualStyle> set = Util
					.getVisualStyleByTitleAsSet(title, vmm);
			if (set.size() > 0) {
				CyWriter writer = vwf.createWriter(new FileOutputStream(
						vizmapFile), set);
				dialogTaskManager.execute(new TaskIterator(writer));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
