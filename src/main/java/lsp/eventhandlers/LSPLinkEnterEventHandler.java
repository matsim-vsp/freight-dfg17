package lsp.eventhandlers;

import org.matsim.core.events.handler.EventHandler;

import lsp.events.FreightLinkEnterEvent;

public interface LSPLinkEnterEventHandler extends EventHandler{

	public void handleEvent(FreightLinkEnterEvent event);

}
