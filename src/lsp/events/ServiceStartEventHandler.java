package lsp.events;

import org.matsim.core.events.handler.EventHandler;


public interface ServiceBeginsEventHandler extends EventHandler {

	public void handleEvent(ServiceBeginsEvent event);

}
