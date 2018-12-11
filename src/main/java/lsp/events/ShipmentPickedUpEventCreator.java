package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.FreightConstants;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.contrib.freight.carrier.Tour.Pickup;
import org.matsim.contrib.freight.events.ShipmentPickedUpEvent;

import lsp.resources.CarrierResource;
import lsp.resources.Resource;

public class ShipmentPickedUpEventCreator implements EventCreator{

	@Override
	public Event createEvent(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour, Id<Person> driverId, int activityCounter) {
		if(event instanceof ActivityEndEvent && resource instanceof CarrierResource) {
			CarrierResource carrierResource = (CarrierResource) resource;
			if(event.getEventType().equals(FreightConstants.PICKUP)) {
				Pickup pickup = (Pickup) activity;
				return new ShipmentPickedUpEvent(carrierResource.getCarrier().getId(), driverId, pickup.getShipment(), event.getTime());
			}
		}
		return null;
	}
}
