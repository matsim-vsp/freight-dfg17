package lsp.eventhandlers;

import org.matsim.contrib.freight.controler.LSPServiceStartEvent;
import org.matsim.core.events.handler.EventHandler;


public interface LSPServiceStartEventHandler extends EventHandler {

	public void handleEvent( LSPServiceStartEvent event );

}
