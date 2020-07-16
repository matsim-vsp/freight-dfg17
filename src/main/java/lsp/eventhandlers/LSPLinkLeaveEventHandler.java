package lsp.eventhandlers;

import lsp.events.LSPFreightLinkLeaveEvent;
import org.matsim.core.events.handler.EventHandler;

public interface LSPLinkLeaveEventHandler extends EventHandler{

	public void handleEvent( LSPFreightLinkLeaveEvent event );

}
