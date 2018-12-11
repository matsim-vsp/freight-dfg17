package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.contrib.freight.carrier.Tour.ServiceActivity;
import org.matsim.contrib.freight.carrier.Tour.TourElement;

import lsp.resources.CarrierResource;
import lsp.resources.Resource;

public class ServiceEndEventCreator implements EventCreator{

	@Override
	public Event createEvent(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour, Id<Person> driverId, int activityCounter) {
		if(event instanceof ActivityEndEvent && resource instanceof CarrierResource){
			CarrierResource carrierResource = (CarrierResource) resource;
			ActivityEndEvent endEvent = (ActivityEndEvent) event;
			if(endEvent.getActType() == "service") {
				TourElement element = scheduledTour.getTour().getTourElements().get(activityCounter);
				if(element instanceof ServiceActivity) {
					ServiceActivity serviceActivity = (ServiceActivity) element;
					return new ServiceEndEvent(endEvent, carrierResource, driverId, serviceActivity.getService(), event.getTime(), scheduledTour.getVehicle());
				}
			}	
		}
		return null;
	}
}
