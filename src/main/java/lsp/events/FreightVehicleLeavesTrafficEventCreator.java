package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.ScheduledTour;

import lsp.resources.CarrierResource;
import lsp.resources.Resource;

public class FreightVehicleLeavesTrafficEventCreator implements EventCreator{

	@Override
	public Event createEvent(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour, Id<Person> driverId, int activityCounter) {
		if(event instanceof VehicleLeavesTrafficEvent && resource instanceof CarrierResource) {
			CarrierResource carrierResource = (CarrierResource) resource;
			VehicleLeavesTrafficEvent leavesEvent = (VehicleLeavesTrafficEvent) event;
			return new FreightVehicleLeavesTrafficEvent(leavesEvent, carrierResource, leavesEvent.getTime(), driverId, leavesEvent.getLinkId(), scheduledTour.getVehicle().getVehicleId(), scheduledTour.getVehicle());
		}
		return null;
	}
}	