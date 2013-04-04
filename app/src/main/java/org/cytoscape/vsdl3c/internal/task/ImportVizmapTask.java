package org.cytoscape.vsdl3c.internal.task;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.vsdl3c.internal.Util;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Import the default vizmap xml definitions from %user_home%/semscape
 *
 */
public class ImportVizmapTask extends AbstractTask {

	private final File vizmapFile;

	private final VisualMappingManager vmm;
	private final VizmapReaderManager vizmapReaderMgr;
	private final String title;
	private final String file;

	public ImportVizmapTask(final VizmapReaderManager vizmapReaderMgr,
			final VisualMappingManager vmm,
			final CyApplicationConfiguration config, String title, String file) {

		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmm = vmm;
		this.title = title;
		this.file = file;
		final File semscape = new File(
				config.getConfigurationDirectoryLocation(), "semscape");
		this.vizmapFile = new File(semscape, file);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		final VizmapReader reader;
		if (Util.getVisualStyleByTitle(title, vmm) != null) {
			return;
		}
		if (vizmapFile.exists() == false) {
			// get the file from resource
			final URL url = ImportVizmapTask.class.getClassLoader()
					.getResource(this.file);
			reader = vizmapReaderMgr.getReader(url.toURI(), url.getPath());
		} else {
			reader = vizmapReaderMgr.getReader(vizmapFile.toURI(),
					vizmapFile.getName());
		}

		if (reader == null)
			throw new NullPointerException(
					"Failed to find Default Vizmap loader.");

		insertTasksAfterCurrentTask(reader,
				new AddVisualStylesTask(reader, vmm));
	}

	private static final class AddVisualStylesTask extends AbstractTask {

		private final VizmapReader reader;
		private final VisualMappingManager vmMgr;

		public AddVisualStylesTask(VizmapReader reader,
				VisualMappingManager vmMgr) {
			this.reader = reader;
			this.vmMgr = vmMgr;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setTitle("Loading preset Visual Styles...");
			final Set<VisualStyle> styles = reader.getVisualStyles();

			if (styles != null) {
				int count = 1;
				int total = styles.size();

				for (VisualStyle vs : styles) {
					if (cancelled)
						break;
					taskMonitor.setStatusMessage(count + " of " + total + ": "
							+ vs.getTitle());
					vmMgr.addVisualStyle(vs);
					taskMonitor.setProgress(count / total);
					count++;
				}

				if (cancelled) {
					for (VisualStyle vs : styles)
						vmMgr.removeVisualStyle(vs);
					taskMonitor.setProgress(1.0);
				}
			}
		}
	}
}
