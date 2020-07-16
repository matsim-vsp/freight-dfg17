package lsp.eventhandlers;

import lsp.events.LSPFreightVehicleLeavesTrafficEvent;

public interface LSPVehicleLeavesTrafficEventHandler{
	
	public void handleEvent( LSPFreightVehicleLeavesTrafficEvent event );
}
