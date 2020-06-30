package lsp.eventhandlers;


import lsp.events.ServiceEndEvent;
import org.matsim.core.events.handler.EventHandler;

public interface ServiceEndEventHandler extends EventHandler{
	

		public void handleEvent( ServiceEndEvent event );

	
}
