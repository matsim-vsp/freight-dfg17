package usecase;

import org.matsim.core.events.handler.EventHandler;

import events.FreightLinkEnterEvent;

public interface FreightLinkEnterEventHandler extends EventHandler{

	public void handleEvent(FreightLinkEnterEvent event);

}
