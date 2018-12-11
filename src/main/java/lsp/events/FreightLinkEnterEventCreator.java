package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.vehicles.Vehicle;

import lsp.resources.CarrierResource;
import lsp.resources.Resource;

public class FreightLinkEnterEventCreator implements EventCreator {

	@Override
	public Event createEvent(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour,
			Id<Person> driverId, int activityCounter) {
		if(event instanceof LinkEnterEvent && resource instanceof CarrierResource) {
			LinkEnterEvent enterEvent = (LinkEnterEvent) event;
			CarrierResource carrierResource  = (CarrierResource) resource;
			return new FreightLinkEnterEvent(carrierResource, enterEvent.getVehicleId(), driverId, enterEvent.getLinkId(), enterEvent.getTime(), scheduledTour.getVehicle());
		}
		return null;
	}	
}
