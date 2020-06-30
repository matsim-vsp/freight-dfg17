package lsp.eventhandlers;

import lsp.events.FreightLinkLeaveEvent;
import org.matsim.core.events.handler.EventHandler;

public interface FreightLinkLeaveEventHandler extends EventHandler{

	public void handleEvent( FreightLinkLeaveEvent event );

}
