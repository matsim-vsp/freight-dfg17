package lsp.eventhandlers;

import org.matsim.contrib.freight.controler.LSPTourEndEvent;
import org.matsim.core.events.handler.EventHandler;


public interface LSPTourEndEventHandler extends EventHandler {

	public void handleEvent( LSPTourEndEvent event );

}
