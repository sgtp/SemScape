package org.cytoscape.vsdl3c.internal.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.vsdl3c.internal.ContextManager;
import org.cytoscape.vsdl3c.internal.SPARQLEndpointConfig;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;
import org.cytoscape.vsdl3c.internal.model.SPARQLQuery;
import org.cytoscape.vsdl3c.internal.task.TransformTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Dialog for SPARQL query
 *
 */
public class SPARQLQueryDialog extends JDialog {
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5243231064687929393L;

	private DialogTaskManager dialogTaskManager;

	private TransformTaskFactory ttf;
	private ContextManager cm;
	private PrefixMapping pm;

	final private SPARQLEndpointConfig endpointConfig;
	final private SPARQLEndpointConfigPanel configPanel;

	private JPanel buttonPanel;
	private JButton clearButton;
	private JButton cancelButton;
	private JButton searchButton;

	private JPanel queryPanel;

	private JPanel SPARQLEndpointPanel;
	private JComboBox SPARQLEndpointComboBox;
	private JLabel SPARQLEndpointLabel;

	private JPanel querySamplePanel;
	private JComboBox queryComboBox;
	private JLabel querySampleLabel;

	private JPanel addNetworkPanel;
	private JRadioButton newNetwork;
	private JRadioButton appendNetwork;

	private JScrollPane queryStringScrollPane;
	private JTextArea queryStringArea = new JTextArea();

	private CyApplicationManager appManger;

	private static final String NO_SPARQL_ENDPOINT = "No SPARQL Endpoint";
	private static final String NO_SPARQL_QUERY_SAMPLE = "No SPARQL Query Sample";

	public SPARQLQueryDialog(DialogTaskManager dialogTaskManager,
			TransformTaskFactory ttf, ContextManager cm, PrefixMapping pm,
			SPARQLEndpointConfig endpointConfig,
			SPARQLEndpointConfigPanel configPanel,
			CyApplicationManager appManger) {
		super();
		this.dialogTaskManager = dialogTaskManager;

		this.ttf = ttf;
		this.cm = cm;
		this.pm = pm;
		this.appManger = appManger;

		this.endpointConfig = endpointConfig;
		this.configPanel = configPanel;

		this.setTitle("SPARQL Query");
		this.setSize(600, 200);
		initGUI();

		initSPARQLEndpoint();
	}

	private void initGUI() {
		System.out.println("Start GUI for SPARQL Query");
		initComponents();

		getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(addNetworkPanel, BorderLayout.NORTH);
		this.getContentPane().add(queryPanel, BorderLayout.CENTER);
		this.pack();
	}

	private void initComponents() {

		addNetworkPanel = new JPanel();
		newNetwork = new JRadioButton("Create new network view");
		appendNetwork = new JRadioButton("Append to the current network view");

		ButtonGroup group = new ButtonGroup();
		group.add(newNetwork);
		group.add(appendNetwork);
		addNetworkPanel.add(newNetwork);
		addNetworkPanel.add(appendNetwork);
		if (appManger.getCurrentNetworkView() == null) {
			newNetwork.setSelected(true);
		} else {
			appendNetwork.setSelected(true);
		}

		SPARQLEndpointPanel = new JPanel();
		SPARQLEndpointLabel = new JLabel();
		SPARQLEndpointComboBox = new JComboBox();
		SPARQLEndpointComboBox.setRenderer(new MyComboBoxCellRenderer());

		SPARQLEndpointLabel.setFont(new java.awt.Font("SansSerif", 0, 12));
		SPARQLEndpointLabel.setText("SPARQL Endpoint: ");

		querySamplePanel = new JPanel();
		querySampleLabel = new JLabel();
		queryComboBox = new JComboBox();
		queryComboBox.setRenderer(new MyComboBoxCellRenderer());
		querySampleLabel.setFont(new java.awt.Font("SansSerif", 0, 12));
		querySampleLabel.setText("Query Sample: ");

		queryStringScrollPane = new JScrollPane();

		queryStringArea = new JTextArea();
		queryStringArea.setText("");

		final TitledBorder border = new TitledBorder("SPARQL Query String");
		border.setBorder(BorderFactory.createEmptyBorder());
		queryStringScrollPane.setBorder(border);
		queryStringScrollPane.setPreferredSize(new Dimension(300, 200));
		queryStringScrollPane.setViewportView(queryStringArea);

		buttonPanel = new JPanel();
		searchButton = new JButton();
		cancelButton = new JButton();
		clearButton = new JButton();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		SPARQLEndpointComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						SPARQLEndpointComboBoxActionPerformed(evt);
					}
				});
		SPARQLEndpointComboBox.setEditable(true);
		queryComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				querySampleComboBoxActionPerformed(evt);
			}
		});

		GroupLayout SPARQLEndpointPanelLayout = new GroupLayout(
				SPARQLEndpointPanel);
		SPARQLEndpointPanel.setLayout(SPARQLEndpointPanelLayout);
		SPARQLEndpointPanelLayout.setHorizontalGroup(SPARQLEndpointPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						SPARQLEndpointPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(SPARQLEndpointLabel)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(SPARQLEndpointComboBox, 0, 301,
										Short.MAX_VALUE).addContainerGap()));
		SPARQLEndpointPanelLayout
				.setVerticalGroup(SPARQLEndpointPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								SPARQLEndpointPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												SPARQLEndpointPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																SPARQLEndpointLabel)
														.addComponent(
																SPARQLEndpointComboBox,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		GroupLayout querySamplePanelLayout = new GroupLayout(querySamplePanel);
		querySamplePanel.setLayout(querySamplePanelLayout);
		querySamplePanelLayout.setHorizontalGroup(querySamplePanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						querySamplePanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(querySampleLabel)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(queryComboBox, 0, 301,
										Short.MAX_VALUE).addContainerGap()));
		querySamplePanelLayout
				.setVerticalGroup(querySamplePanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								querySamplePanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												querySamplePanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																querySampleLabel)
														.addComponent(
																queryComboBox,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		buttonPanel.setBorder(BorderFactory.createEmptyBorder());

		searchButton.setText("Execute");
		searchButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				Object selected = SPARQLEndpointComboBox.getSelectedItem();
				if (selected == null) {
					selected = SPARQLEndpointComboBox.getItemAt(0);
					if (selected == null) {
						JOptionPane.showMessageDialog(null,
								"No SPARQL Endpoint Selected!");
						return;
					}
				}
				if (selected instanceof SPARQLEndpoint == false) {
					JOptionPane.showMessageDialog(null,
							"No SPARQL Endpoint Selected!");
					return;
				}
				final String uri = SPARQLEndpointComboBox.getSelectedItem()
						.toString();
				SPARQLEndpoint endpoint = endpointConfig.getSPARQLEndpoint(uri);
				if (endpoint == null) {
					endpoint = endpointConfig.createSPARQLEndpoint(uri);
					configPanel.updateSPARQLEndpointConfig();
					configPanel.checkStatus();
				}

				if (queryStringArea.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"No SPARQL Query String!");
					return;
				}
				if (appManger.getCurrentNetworkView() != null
						&& appendNetwork.isSelected() == true) {
					dialogTaskManager.execute(ttf.createTaskIterator(endpoint,
							queryStringArea.getText(),
							appManger.getCurrentNetworkView()));
				} else {
					dialogTaskManager.execute(ttf.createTaskIterator(endpoint,
							queryStringArea.getText(), null));
				}

				SPARQLQueryDialog.this.setVisible(false);
				SPARQLQueryDialog.this.pack();
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				SPARQLQueryDialog.this.setVisible(false);
				SPARQLQueryDialog.this.pack();
			}
		});

		clearButton.setText("Clear");
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				SPARQLEndpointComboBox.setSelectedIndex(-1);
				queryComboBox.setSelectedIndex(-1);
				queryStringArea.setText("");

			}
		});

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);
		buttonPanelLayout.setHorizontalGroup(buttonPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						GroupLayout.Alignment.TRAILING,
						buttonPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(clearButton)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED,
										225, Short.MAX_VALUE)
								.addComponent(cancelButton)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(searchButton).addContainerGap()));
		buttonPanelLayout
				.setVerticalGroup(buttonPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								GroupLayout.Alignment.TRAILING,
								buttonPanelLayout
										.createSequentialGroup()
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addGroup(
												buttonPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																searchButton)
														.addComponent(
																cancelButton)
														.addComponent(
																clearButton))
										.addContainerGap()));

		queryPanel = new JPanel();
		GroupLayout layout = new GroupLayout(queryPanel);
		queryPanel.setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(SPARQLEndpointPanel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(querySamplePanel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(queryStringScrollPane, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(SPARQLEndpointPanel,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(querySamplePanel,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(queryStringScrollPane,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(buttonPanel,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)));

	}

	public void initSPARQLEndpoint() {
		SPARQLEndpointComboBox.removeAllItems();
		for (SPARQLEndpoint add : this.endpointConfig.getAllSPARQLEndpoints()) {
			SPARQLEndpointComboBox.addItem(add);
		}

		if (SPARQLEndpointComboBox.getItemCount() == 0) {
			SPARQLEndpointComboBox.addItem(NO_SPARQL_ENDPOINT);
		}

		queryComboBox.removeAllItems();
		for (SPARQLQuery add : this.cm.getAllSPARQLQueries()) {
			queryComboBox.addItem(add);
		}

		if (queryComboBox.getItemCount() == 0) {
			queryComboBox.addItem(NO_SPARQL_QUERY_SAMPLE);
		}

	}

	private void SPARQLEndpointComboBoxActionPerformed(
			java.awt.event.ActionEvent evt) {
		Object selected = SPARQLEndpointComboBox.getSelectedItem();
		if (selected == null) {
			selected = SPARQLEndpointComboBox.getItemAt(0);
			if (selected == null)
				return;
		}
	}

	private void querySampleComboBoxActionPerformed(
			java.awt.event.ActionEvent evt) {

		Object selected = queryComboBox.getSelectedItem();
		if (selected == null) {
			selected = queryComboBox.getItemAt(0);
			if (selected == null)
				return;
		}

		if (selected instanceof SPARQLQuery == false) {
			this.queryStringArea.setText("");
			return;
		}

		final SPARQLQuery sample = (SPARQLQuery) selected;
		this.queryStringArea.setText(sample.getSPARQLQueryString());
	}

	private final class MyComboBoxCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1494017058040636621L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if (value instanceof SPARQLEndpoint) {
				String url = ((SPARQLEndpoint) value).getUri();
				this.setText(url);
			}
			if (value instanceof SPARQLQuery) {
				SPARQLQuery query = (SPARQLQuery) value;

				String label = "";
				if (query.getLabel() != null) {
					label = query.getLabel();
				} else {
					label = pm.shortForm(query.getUri());
				}
				this.setText(label);
			}

			return this;

		}
	}

}
