package org.cytoscape.vsdl3c.internal.action;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.vsdl3c.internal.ui.SPARQLEndpointConfigPanel;

/**
 * Display the SPARQL endpoint configuration panel
 *
 */
public class ShowSPARQLEndpointConfigPanelAction extends AbstractCyAction {

	private static final long serialVersionUID = 6487667539345226899L;
	private CySwingApplication desktopApp;
	private final CytoPanel cytoPanelWest;
	private SPARQLEndpointConfigPanel myCytoPanel;
	
	public ShowSPARQLEndpointConfigPanelAction(CySwingApplication desktopApp,
			SPARQLEndpointConfigPanel myCytoPanel){
		// Add a menu item -- Apps->sample02
		super("SPARQL Endpoint Configuration");
		setPreferredMenu("Apps");

		this.desktopApp = desktopApp;
		
		//Note: myCytoPanel is bean we defined and registered as a service
		this.cytoPanelWest = this.desktopApp.getCytoPanel(CytoPanelName.WEST);
		this.myCytoPanel = myCytoPanel;
	}
	
	public void actionPerformed(ActionEvent e) {
		// If the state of the cytoPanelWest is HIDE, show it
		if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
			cytoPanelWest.setState(CytoPanelState.DOCK);
		}	

		// Select my panel
		int index = cytoPanelWest.indexOfComponent(myCytoPanel);
		if (index == -1) {
			return;
		}
		cytoPanelWest.setSelectedIndex(index);
	}

}
