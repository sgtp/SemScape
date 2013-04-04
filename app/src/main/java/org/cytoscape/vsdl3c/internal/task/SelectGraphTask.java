package org.cytoscape.vsdl3c.internal.task;

import java.util.List;

import javax.swing.JTextField;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

/**
 * Ask the user to select the graphs for the SPARQL endponits
 * 
 */
public class SelectGraphTask extends AbstractTask {

	@Tunable(description = "Please select the following graphs:")
	public ListMultipleSelection<String> graphs;

	private JTextField jf;

	public SelectGraphTask(JTextField jf, List<String> list) {

		this.jf = jf;
		graphs = new ListMultipleSelection<String>(list);

	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		List<String> selected = graphs.getSelectedValues();
		if (selected.size() == 1
				&& selected.get(0).equals("--- No SPARQL Endpoint ---")) {
			jf.setText("");
			return;
		}
		String str = "";
		for (String s : selected) {
			str += s + " ";
		}
		str = str.trim();
		jf.setText(str);

	}
}
