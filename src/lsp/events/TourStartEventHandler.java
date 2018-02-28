package lsp.events;

import org.matsim.core.events.handler.EventHandler;


public interface TourStartEventHandler extends EventHandler {

	public void handleEvent(TourStartEvent event);

}