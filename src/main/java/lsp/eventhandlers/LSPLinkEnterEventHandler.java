package lsp.eventhandlers;

import org.matsim.core.events.handler.EventHandler;

import lsp.events.LSPFreightLinkEnterEvent;

public interface LSPLinkEnterEventHandler extends EventHandler{

	public void handleEvent(LSPFreightLinkEnterEvent event);

}
