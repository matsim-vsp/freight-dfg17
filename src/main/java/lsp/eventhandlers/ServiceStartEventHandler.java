package lsp.eventhandlers;

import lsp.events.ServiceStartEvent;
import org.matsim.core.events.handler.EventHandler;


public interface ServiceStartEventHandler extends EventHandler {

	public void handleEvent( ServiceStartEvent event );

}
