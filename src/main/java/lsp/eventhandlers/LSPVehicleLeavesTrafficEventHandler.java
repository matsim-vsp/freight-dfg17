package lsp.eventhandlers;

import lsp.events.FreightVehicleLeavesTrafficEvent;

public interface LSPVehicleLeavesTrafficEventHandler{
	
	public void handleEvent( FreightVehicleLeavesTrafficEvent event );
}
