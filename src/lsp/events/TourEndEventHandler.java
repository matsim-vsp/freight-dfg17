package lsp.events;

import org.matsim.core.events.handler.EventHandler;


public interface TourEndEventHandler extends EventHandler {

	public void handleEvent(TourEndEvent event);

}