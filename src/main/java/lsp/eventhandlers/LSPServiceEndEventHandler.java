package lsp.eventhandlers;


import org.matsim.contrib.freight.controler.LSPServiceEndEvent;
import org.matsim.core.events.handler.EventHandler;

public interface LSPServiceEndEventHandler extends EventHandler{
	

		public void handleEvent( LSPServiceEndEvent event );

	
}
