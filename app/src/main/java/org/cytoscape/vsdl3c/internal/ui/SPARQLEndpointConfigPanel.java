package org.cytoscape.vsdl3c.internal.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import org.apache.commons.validator.routines.UrlValidator;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.vsdl3c.internal.SPARQLEndpointConfig;
import org.cytoscape.vsdl3c.internal.model.GraphPolicy;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;
import org.cytoscape.vsdl3c.internal.task.QueryGraphTask;
import org.cytoscape.vsdl3c.internal.task.SelectGraphTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Panel for SPARQL endpoint metatdata configuration
 * 
 */
public class SPARQLEndpointConfigPanel extends JPanel implements
		CytoPanelComponent {

	private static final long serialVersionUID = -3012063214093408535L;

	private static final String NO_SPARQL_ENDPOINT = "--- No SPARQL Endpoint ---";

	private SPARQLEndpointConfig config;

	private JComboBox box;
	private JTextField endpointLabel;
	private JTextField endpointUri;
	private JTextField namedGraphString;
	private JTextField user;
	private JPasswordField password;
	private JTextField apiKey;
	private JCheckBox graphBox;
	private JCheckBox authBox;
	private JCheckBox apiKeyBox;
	private JCheckBox defaultEnpoint;

	private JRadioButton defaultGraph;
	private JRadioButton namedGraph;
	private JRadioButton allGraph;

	private JButton selectGraphs;

	private JButton delete;
	private JButton save;
	private JButton create;

	private SPARQLEndpoint currentEndpoint;

	private UrlValidator validator = new UrlValidator();
	private DialogTaskManager dialogTaskManager;

	public SPARQLEndpointConfigPanel(SPARQLEndpointConfig config,
			DialogTaskManager dialogTaskManager) {
		this.config = config;
		this.dialogTaskManager = dialogTaskManager;

		initGUI();
		initAction();

	}

	public void checkStatus() {
		if (box.getItemCount() == 0) {
			box.addItem(NO_SPARQL_ENDPOINT);
			clear();
			delete.setEnabled(false);
			save.setEnabled(false);
			endpointLabel.setEnabled(false);
			endpointUri.setEnabled(false);
			namedGraphString.setEnabled(false);
			user.setEnabled(false);
			password.setEnabled(false);
			authBox.setEnabled(false);
			apiKeyBox.setEnabled(false);
			apiKey.setEditable(false);
			graphBox.setEnabled(false);
			defaultGraph.setEnabled(false);
			namedGraph.setEnabled(false);
			allGraph.setEnabled(false);
			defaultEnpoint.setEnabled(false);

		} else {
			box.removeItem(NO_SPARQL_ENDPOINT);
			delete.setEnabled(true);
			save.setEnabled(true);
			endpointLabel.setEnabled(true);
			endpointUri.setEnabled(true);
			namedGraphString.setEnabled(true);
			user.setEnabled(true);
			password.setEnabled(true);
			authBox.setEnabled(true);
			apiKeyBox.setEnabled(true);
			apiKey.setEditable(true);
			graphBox.setEnabled(true);
			defaultGraph.setEnabled(true);
			namedGraph.setEnabled(true);
			allGraph.setEnabled(true);
			defaultEnpoint.setEnabled(true);
		}
	}

	public void updateSPARQLEndpointConfig() {
		box.removeAllItems();
		for (SPARQLEndpoint endpoint : config.getAllSPARQLEndpoints()) {
			box.addItem(endpoint);
		}
	}

	private void initGUI() {
		DesignGridLayout layout = new DesignGridLayout(this);
		box = new JComboBox();
		box.setRenderer(new MyComboBoxCellRenderer());

		updateSPARQLEndpointConfig();
		layout.row().grid().add(box);

		endpointLabel = new JTextField();
		endpointUri = new JTextField();
		namedGraphString = new JTextField();
		user = new JTextField();
		password = new JPasswordField();
		apiKey = new JTextField();
		defaultEnpoint = new JCheckBox("Default");

		layout.row().grid().add(new JLabel("Endpoint Name:"))
				.add(endpointLabel, 3);
		layout.row().grid().add(new JLabel("Endpoint URI:"))
				.add(endpointUri, 3);
		layout.row().left().add(defaultEnpoint).fill();

		RowGroup group = new RowGroup();
		authBox = addGroup(layout, "Authentication", group);
		layout.row().group(group).grid().add(new JLabel("Auth Username:"))
				.add(user, 3);
		layout.row().group(group).grid().add(new JLabel("Auth Password:"))
				.add(password, 3);
		group = new RowGroup();
		apiKeyBox = addGroup(layout, "API Key", group);
		layout.row().group(group).grid().add(new JLabel("API Key:"))
				.add(apiKey, 3);

		group = new RowGroup();
		graphBox = addGroup(layout, "Graph", group);
		defaultGraph = new JRadioButton("Set Default Graphs (for Virtuoso)");
		defaultGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectGraphs.setEnabled(true);
			}

		});
		namedGraph = new JRadioButton("Restrict to Named Graphs (for 4Store)");
		namedGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectGraphs.setEnabled(true);
			}

		});
		allGraph = new JRadioButton("Query All Graphs");
		allGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectGraphs.setEnabled(false);
			}

		});

		allGraph.setSelected(true);
		selectGraphs = new JButton("Select");
		selectGraphs.setEnabled(false);

		selectGraphs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> list = new ArrayList<String>();
				dialogTaskManager.execute(new TaskIterator(new QueryGraphTask(
						currentEndpoint, list), new SelectGraphTask(
						namedGraphString, list)));
			}
		});

		ButtonGroup bg = new ButtonGroup();
		bg.add(defaultGraph);
		bg.add(namedGraph);
		bg.add(allGraph);
		layout.row().group(group).grid().add(defaultGraph);
		layout.row().group(group).grid().add(namedGraph);
		layout.row().group(group).grid().add(allGraph);
		layout.row().group(group).grid().add(new JLabel("Graphs:"))
				.add(namedGraphString, 2).add(selectGraphs);
		namedGraphString.setEditable(false);

		layout.emptyRow();

		create = new JButton("New");
		delete = new JButton("Delete");
		save = new JButton("Save");

		layout.emptyRow();
		layout.emptyRow();
		layout.row().right().add(create).add(delete).add(save);

		SPARQLEndpointComboBoxActionPerformed(null);
		checkStatus();
		this.setVisible(true);
	}

	private void initAction() {
		box.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				SPARQLEndpointComboBoxActionPerformed(evt);
			}
		});

		create.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				createSPARQLEndpointActionPerformed(evt);
			}
		});

		delete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteSPARQLEndpointActionPerformed(evt);
			}
		});

		save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveSPARQLEndpointActionPerformed(evt);
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
		return "SPARQL Endpoint Metadata Configuration";
	}

	public Icon getIcon() {
		return null;
	}

	private void SPARQLEndpointComboBoxActionPerformed(
			java.awt.event.ActionEvent evt) {

		Object selected = box.getSelectedItem();
		if (selected == null) {
			selected = box.getItemAt(0);
			if (selected == null)
				return;
		}
		if (selected instanceof SPARQLEndpoint == false) {
			currentEndpoint = null;
			clear();

		} else {
			currentEndpoint = (SPARQLEndpoint) selected;
			showConfig(currentEndpoint);
		}

	}

	private void createSPARQLEndpointActionPerformed(
			java.awt.event.ActionEvent evt) {
		SPARQLEndpoint newEndpoint = config.createSPARQLEndpoint();
		box.removeItem(newEndpoint);
		box.addItem(newEndpoint);
		box.setSelectedItem(newEndpoint);
		checkStatus();
	}

	private void deleteSPARQLEndpointActionPerformed(
			java.awt.event.ActionEvent evt) {
		if (this.currentEndpoint != null) {
			int ret = JOptionPane
					.showConfirmDialog(null,
							"Do you really want to delete the selected SPARQL endpoint metadata?");
			if (ret == JOptionPane.OK_OPTION) {
				config.deleteSPARQLEndpoint(this.currentEndpoint);
				box.removeItem(this.currentEndpoint);

			}
		} else {
			JOptionPane
					.showMessageDialog(null,
							"Please firstly select a SPARQL Endpoint, and then delete it!");
		}
		checkStatus();
	}

	private void saveSPARQLEndpointActionPerformed(
			java.awt.event.ActionEvent evt) {
		if (this.currentEndpoint != null) {
			if (checkForm()) {
				if (config.containsEndpoint(this.endpointUri.getText().trim())
						&& !this.currentEndpoint.getUri().equals(
								this.endpointUri.getText().trim())) {
					JOptionPane
							.showMessageDialog(
									null,
									"The SPARQL Endpoint to be saved already exists! Please change the 'Endpoint URI', or select the existing one from the list.");
					return;
				}

				this.currentEndpoint.setUri(this.endpointUri.getText().trim());
				this.currentEndpoint.setLabel(this.endpointLabel.getText()
						.trim());
				if (graphBox.isSelected()) {
					if (defaultGraph.isSelected()) {
						this.currentEndpoint
								.setGraphPolicy(GraphPolicy.DEFAUT_GRAPH);
					} else if (namedGraph.isSelected()) {
						this.currentEndpoint
								.setGraphPolicy(GraphPolicy.NAMED_GRAPH);
					} else if (allGraph.isSelected()) {
						this.currentEndpoint
								.setGraphPolicy(GraphPolicy.ALL_GRAPH);
					} else {
						this.currentEndpoint.setGraphPolicy(GraphPolicy.NONE);
					}
					this.currentEndpoint
							.setNamedGraphString(this.namedGraphString
									.getText().trim());
				} else {
					this.currentEndpoint.setGraphPolicy(GraphPolicy.NONE);
				}

				if (authBox.isSelected()) {
					this.currentEndpoint.setSPARQLEndpointUser(this.user
							.getText().trim());
					this.currentEndpoint.setSPARQLEndpointPassword(new String(
							this.password.getPassword()).trim());
				} else {
					this.currentEndpoint.setSPARQLEndpointUser(null);
					this.currentEndpoint.setSPARQLEndpointPassword(null);
				}

				if (apiKeyBox.isSelected()) {
					this.currentEndpoint
							.setAPIKey(this.apiKey.getText().trim());
				} else {
					this.currentEndpoint.setAPIKey(null);
				}
				if (defaultEnpoint.isSelected()) {
					config.setDefaultSPARQLEndpoint(currentEndpoint);
				} else {
					config.removeDefaultSPARQLEndpoint(currentEndpoint);
				}

				JOptionPane
						.showMessageDialog(null,
								"The SPARQL Endpoint metadata has been saved successfully!");
				box.setSelectedItem(this.currentEndpoint);

				config.handleEvent(null);
			}

		} else {
			JOptionPane
					.showMessageDialog(null,
							"Please firstly create a new SPARQL Endpoint, and then save it!");
		}
	}

	private boolean checkForm() {
		String uri = this.endpointUri.getText();

		if (uri.trim().isEmpty()) {
			JOptionPane.showMessageDialog(null,
					"The URI of SPARQL endpoint can not be empty!");
			return false;
		}
		if (SPARQLEndpointConfig.newURI.equals(uri.trim())) {
			JOptionPane.showMessageDialog(null,
					"Please change the SPARQL endpoint URI before saving!");
			return false;
		}
		if (!validator.isValid(uri.trim())) {
			JOptionPane.showMessageDialog(null, "Invalid SPARQL endpoint URI!");
			return false;
		}

		return true;
	}

	private JCheckBox addGroup(DesignGridLayout layout, String name,
			RowGroup group) {
		JCheckBox groupBox = new JCheckBox(name);
		groupBox.setName(name);
		groupBox.setForeground(Color.BLUE);
		groupBox.setSelected(true);
		groupBox.addItemListener(new ShowHideAction(group));
		layout.emptyRow();
		layout.row().left().add(groupBox, new JSeparator()).fill();
		return groupBox;
	}

	private void clear() {
		endpointLabel.setText("");
		endpointUri.setText("");
		namedGraphString.setText("");
		user.setText("");
		password.setText("");
		apiKey.setText("");
		allGraph.setSelected(true);
		authBox.setSelected(false);
		apiKeyBox.setSelected(false);
		graphBox.setSelected(false);
		defaultEnpoint.setSelected(false);
	}

	private void showConfig(SPARQLEndpoint endpoint) {
		endpointLabel.setText(endpoint.getLabel() == null ? "" : endpoint
				.getLabel());
		endpointUri.setText(endpoint.getUri() == null ? "" : endpoint.getUri());
		if (endpoint.getAPIKey() != null) {
			apiKeyBox.setSelected(true);
			apiKey.setText(endpoint.getAPIKey());

		} else {
			apiKeyBox.setSelected(false);
			apiKey.setText("");
		}
		if (endpoint.getSPARQLEndpointUser() == null
				&& endpoint.getSPARQLEndpointPassword() == null) {
			authBox.setSelected(false);
		} else {
			authBox.setSelected(true);
		}
		user.setText(endpoint.getSPARQLEndpointUser() == null ? "" : endpoint
				.getSPARQLEndpointUser());
		password.setText(endpoint.getSPARQLEndpointPassword() == null ? ""
				: endpoint.getSPARQLEndpointPassword());

		if (endpoint.getGraphPolicy().equals(GraphPolicy.NONE)) {
			graphBox.setSelected(false);
			defaultGraph.setSelected(false);
			namedGraph.setSelected(false);
			allGraph.setSelected(false);
		} else if (endpoint.getGraphPolicy().equals(GraphPolicy.DEFAUT_GRAPH)) {
			graphBox.setSelected(true);
			defaultGraph.setSelected(true);
		} else if (endpoint.getGraphPolicy().equals(GraphPolicy.NAMED_GRAPH)) {
			graphBox.setSelected(true);
			namedGraph.setSelected(true);
		} else if (endpoint.getGraphPolicy().equals(GraphPolicy.DEFAUT_GRAPH)) {
			graphBox.setSelected(true);
			allGraph.setSelected(true);
		}
		namedGraphString.setText(endpoint.getNamedGraphString() == null ? ""
				: endpoint.getNamedGraphString());

		if (endpoint.isDefaultSPARQLEndpoint()) {
			defaultEnpoint.setSelected(true);
		} else {
			defaultEnpoint.setSelected(false);
		}

	}

	private final class MyComboBoxCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -7343040491022678781L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if (value instanceof SPARQLEndpoint) {
				String label = ((SPARQLEndpoint) value).getUri();
				this.setText(label);
			}
			return this;

		}
	}

	private class ShowHideAction implements ItemListener {
		public ShowHideAction(RowGroup group) {
			_group = group;
		}

		public void itemStateChanged(ItemEvent event) {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				_group.show();
			} else {
				_group.hide();
			}
			// frame().pack();
		}

		final private RowGroup _group;
	}
}
