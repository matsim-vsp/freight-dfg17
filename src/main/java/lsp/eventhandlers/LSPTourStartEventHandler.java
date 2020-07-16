package lsp.eventhandlers;

import org.matsim.contrib.freight.controler.LSPTourStartEvent;
import org.matsim.core.events.handler.EventHandler;


public interface LSPTourStartEventHandler extends EventHandler {

	public void handleEvent( LSPTourStartEvent event );

}
