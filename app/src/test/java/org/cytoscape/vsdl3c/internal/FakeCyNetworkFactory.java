package org.cytoscape.vsdl3c.internal;

import static org.mockito.Mockito.mock;

import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

public class FakeCyNetworkFactory implements CyNetworkFactory {

	public CyNetwork createNetwork() {
		return getInstance();
	}

	public CyNetwork createNetworkWithPrivateTables() {
		return getInstanceWithPrivateTables();
	}
	
	public static CyNetwork getInstance() {
		return getPublicRootInstance().getBaseNetwork();
	}

	public static CyNetwork getInstanceWithPrivateTables() {
		return getPrivateRootInstance().getBaseNetwork();
	}

	public static CyRootNetwork getPublicRootInstance() {	
		return getPublicRootInstance(new DummyCyEventHelper());
	}

	public static CyRootNetwork getPublicRootInstance(DummyCyEventHelper deh) {	
		final CyNetworkTableManagerImpl ntm = new CyNetworkTableManagerImpl();
		final CyTableManagerImpl tm = new CyTableManagerImpl(deh, ntm, new CyNetworkManagerImpl(deh));
		
		final Interpreter interp = new InterpreterImpl();
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		
		final CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(deh, interp, serviceRegistrar);
		return new CyRootNetworkImpl(deh, tm, ntm, tableFactory, serviceRegistrar, true, SavePolicy.DO_NOT_SAVE);
	}

	public static CyRootNetwork getPrivateRootInstance() {	
		DummyCyEventHelper deh = new DummyCyEventHelper();
		CyNetworkTableManagerImpl ntm = new CyNetworkTableManagerImpl();
		CyTableManagerImpl tm = new CyTableManagerImpl(deh, ntm, new CyNetworkManagerImpl(deh));
		Interpreter interp = new InterpreterImpl();
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		CyRootNetworkImpl ar =
			new CyRootNetworkImpl(deh, tm, ntm, new CyTableFactoryImpl(deh, interp, serviceRegistrar),
			               serviceRegistrar, false, SavePolicy.DO_NOT_SAVE);
		return ar; 
	}

	public CyNetwork createNetwork(SavePolicy policy) {
		return getPublicRootInstance().getBaseNetwork();
	}

	public CyNetwork createNetworkWithPrivateTables(SavePolicy policy) {
		return getInstanceWithPrivateTables();
	}

}
