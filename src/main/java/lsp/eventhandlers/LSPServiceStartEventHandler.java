package lsp.eventhandlers;

import lsp.events.LSPServiceStartEvent;
import org.matsim.core.events.handler.EventHandler;


public interface LSPServiceStartEventHandler extends EventHandler {

	public void handleEvent( LSPServiceStartEvent event );

}
