package lsp.eventhandlers;

import org.matsim.contrib.freight.controler.LSPFreightVehicleLeavesTrafficEvent;

public interface LSPVehicleLeavesTrafficEventHandler{
	
	public void handleEvent( LSPFreightVehicleLeavesTrafficEvent event );
}
