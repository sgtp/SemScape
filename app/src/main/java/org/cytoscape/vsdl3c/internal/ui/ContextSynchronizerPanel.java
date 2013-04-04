package org.cytoscape.vsdl3c.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.java.dev.designgridlayout.DesignGridLayout;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.vsdl3c.internal.ContextManager;
import org.cytoscape.vsdl3c.internal.SPARQLEndpointConfig;
import org.cytoscape.vsdl3c.internal.task.AddContextTask;
import org.cytoscape.vsdl3c.internal.task.CheckForUpdateTask;
import org.cytoscape.vsdl3c.internal.task.DeleteContextTask;
import org.cytoscape.vsdl3c.internal.task.UpdateContextTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

/**
 * The panel of synchronize the contexts with remote publications.
 * 
 */
public class ContextSynchronizerPanel extends JPanel implements
		CytoPanelComponent {

	private static final long serialVersionUID = -6830385298767621319L;
	final private ContextManager cm;
	final private TaskManager tm;
	private JPanel content;
	final private SPARQLEndpointConfig endpointConfig;
	final private SPARQLEndpointConfigPanel configPanel;

	public ContextSynchronizerPanel(ContextManager cm, TaskManager tm,
			SPARQLEndpointConfig endpointConfig,
			SPARQLEndpointConfigPanel configPanel) {
		this.cm = cm;
		this.tm = tm;
		this.endpointConfig = endpointConfig;
		this.configPanel = configPanel;
		this.setLayout(new BorderLayout());
		initGUI();

	}

	public void initGUI() {
		if (content != null) {
			this.remove(content);
		}
		content = new JPanel();
		DesignGridLayout layout = new DesignGridLayout(content);

		this.add(content, BorderLayout.CENTER);

		Set<String> contexts = cm.getActivateContexts();
		for (final String context : contexts) {
			JButton delete = new JButton("Delete");
			JButton cfu = new JButton("Check");
			final JLabel checkResult = new JLabel("Updated!");
			checkResult.setForeground(Color.GREEN.darker());
			final JButton u = new JButton("Update");
			u.setEnabled(false);
			layout.row().grid().add(delete).add(new JLabel(context)).add(cfu)
					.add(u).add(checkResult, 3);

			cfu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tm.execute(new TaskIterator(new CheckForUpdateTask(context,
							cm, checkResult, u)));
				}
			});

			u.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tm.execute(new TaskIterator(new UpdateContextTask(context,
							cm, checkResult, u, endpointConfig, configPanel)));
				}
			});

			delete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tm.execute(new TaskIterator(new DeleteContextTask(context,
							cm, ContextSynchronizerPanel.this)));
				}
			});
		}
		JButton add = new JButton("Add");
		layout.row().grid().add(add).empty(5);
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String source = JOptionPane.showInputDialog("Context URL");
				if (source == null) {
					return;
				}
				tm.execute(new TaskIterator(new AddContextTask(source, cm,
						ContextSynchronizerPanel.this, endpointConfig,
						configPanel)));

			}
		});

	}

	public Component getComponent() {
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	public String getTitle() {
		return "Context Synchronizer";
	}

	public Icon getIcon() {
		return null;
	}

}
