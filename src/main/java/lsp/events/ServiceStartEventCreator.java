package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.contrib.freight.carrier.Tour.ServiceActivity;
import org.matsim.contrib.freight.carrier.Tour.TourActivity;
import org.matsim.contrib.freight.carrier.Tour.TourElement;

import lsp.resources.CarrierResource;
import lsp.resources.Resource;

public class ServiceStartEventCreator implements EventCreator{

	@Override
	public Event createEvent(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour, Id<Person> driverId, int activityCounter) {
		if(event instanceof ActivityStartEvent && resource instanceof CarrierResource){
			ActivityStartEvent startEvent = (ActivityStartEvent) event;
			CarrierResource carrierResorce  = (CarrierResource) resource;
			if(startEvent.getActType() == "service") {
				TourElement element = scheduledTour.getTour().getTourElements().get(activityCounter);
				if(element instanceof ServiceActivity) {
					ServiceActivity serviceActivity = (ServiceActivity) element;
					return new ServiceStartEvent(startEvent, carrierResorce, driverId, serviceActivity.getService(), event.getTime(), scheduledTour.getVehicle());
				}
			}	
		}
		return null;
	}
}	
