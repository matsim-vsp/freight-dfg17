package events;

import org.matsim.core.events.handler.EventHandler;

public interface FreightLinkLeaveEventHandler extends EventHandler{

	public void handleEvent(FreightLinkLeaveEvent event);

}
