package lsp.eventhandlers;

import lsp.events.FreightVehicleLeavesTrafficEvent;

public interface FreightVehicleLeavesTrafficEventHandler {
	
	public void handleEvent( FreightVehicleLeavesTrafficEvent event );
}
