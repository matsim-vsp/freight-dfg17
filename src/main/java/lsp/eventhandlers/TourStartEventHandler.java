package lsp.eventhandlers;

import lsp.events.TourStartEvent;
import org.matsim.core.events.handler.EventHandler;


public interface TourStartEventHandler extends EventHandler {

	public void handleEvent( TourStartEvent event );

}
