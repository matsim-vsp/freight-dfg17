package lsp.eventhandlers;

import lsp.events.LSPTourStartEvent;
import org.matsim.core.events.handler.EventHandler;


public interface LSPTourStartEventHandler extends EventHandler {

	public void handleEvent( LSPTourStartEvent event );

}
