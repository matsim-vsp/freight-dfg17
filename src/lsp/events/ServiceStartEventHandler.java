package lsp.events;

import org.matsim.core.events.handler.EventHandler;


public interface ServiceStartEventHandler extends EventHandler {

	public void handleEvent(ServiceStartEvent event);

}
