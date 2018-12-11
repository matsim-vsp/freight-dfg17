package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.FreightConstants;
import org.matsim.contrib.freight.carrier.ScheduledTour;

import lsp.resources.CarrierResource;
import lsp.resources.Resource;

public class TourStartEventCreator implements EventCreator{

	@Override
	public Event createEvent(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour, Id<Person> driverId, int activityCounter) {
		if((event instanceof ActivityEndEvent) && resource instanceof CarrierResource) {
			ActivityEndEvent endEvent = (ActivityEndEvent) event;
			CarrierResource carrierResource = (CarrierResource) resource;
			if(endEvent.getActType().equals(FreightConstants.START) ) {
				return new TourStartEvent(carrierResource, driverId, scheduledTour.getTour(), event.getTime(), scheduledTour.getVehicle());
			}	
		}
		return null;	
	}

}
