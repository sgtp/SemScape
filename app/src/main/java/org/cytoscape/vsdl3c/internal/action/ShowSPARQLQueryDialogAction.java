package org.cytoscape.vsdl3c.internal.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.vsdl3c.internal.ui.SPARQLQueryDialog;

/**
 * Display the SPARQL query dialog 
 */
public class ShowSPARQLQueryDialogAction extends AbstractCyAction{
	
	private static final long serialVersionUID = 8154892420540965802L;

	private SPARQLQueryDialog dialog;
	
	private final Window parent;

	public ShowSPARQLQueryDialogAction(CySwingApplication desktopApp, SPARQLQueryDialog dialog) {
		super("SPARQL Construct Query");
		setPreferredMenu("Apps");
		this.dialog = dialog;
		this.parent= desktopApp.getJFrame();
		
	}

	public void actionPerformed(ActionEvent e) {
		dialog.initSPARQLEndpoint();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

}
