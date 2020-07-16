package lsp.eventhandlers;


import lsp.events.LSPServiceEndEvent;
import org.matsim.core.events.handler.EventHandler;

public interface LSPServiceEndEventHandler extends EventHandler{
	

		public void handleEvent( LSPServiceEndEvent event );

	
}
