package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.FreightConstants;
import org.matsim.contrib.freight.carrier.ScheduledTour;

import lsp.resources.CarrierResource;
import lsp.resources.Resource;

public class TourEndEventCreator implements EventCreator {

	@Override
	public Event createEvent(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour, Id<Person> driverId, int activityCounter) {
		if(event instanceof ActivityStartEvent && resource instanceof CarrierResource) {
			ActivityStartEvent startEvent = (ActivityStartEvent) event;
			CarrierResource carrierResource  = (CarrierResource) resource;
			if(startEvent.getActType().equals(FreightConstants.END)) {
				return new TourEndEvent(carrierResource,  driverId, scheduledTour.getTour(), startEvent.getTime(), scheduledTour.getVehicle());
			}
		}	
		return null;
	}

	

}
