package lsp.eventhandlers;

import org.matsim.contrib.freight.controler.LSPFreightLinkLeaveEvent;
import org.matsim.core.events.handler.EventHandler;

public interface LSPLinkLeaveEventHandler extends EventHandler{

	public void handleEvent( LSPFreightLinkLeaveEvent event );

}
