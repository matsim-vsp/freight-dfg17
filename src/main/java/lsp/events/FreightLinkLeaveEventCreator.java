package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.ScheduledTour;

import lsp.resources.CarrierResource;
import lsp.resources.Resource;

public class FreightLinkLeaveEventCreator implements EventCreator{

	@Override
	
	
	public Event createEvent(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour, Id<Person> driverId, int activityCounter) {
		if((event instanceof LinkLeaveEvent) && resource instanceof CarrierResource) {
			LinkLeaveEvent  leaveEvent = (LinkLeaveEvent) event;
			CarrierResource carrierResource  = (CarrierResource) resource;
			return new FreightLinkLeaveEvent(carrierResource, leaveEvent.getVehicleId(), driverId, leaveEvent.getLinkId(), leaveEvent.getTime(), scheduledTour.getVehicle());
		}	
		return null;
	}
}
