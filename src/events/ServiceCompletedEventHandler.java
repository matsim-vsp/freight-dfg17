package events;


import org.matsim.core.events.handler.EventHandler;

public interface ServiceCompletedEventHandler extends EventHandler{
	

		public void handleEvent(ServiceCompletedEvent event);

	
}
