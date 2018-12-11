package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.ScheduledTour;

import lsp.resources.Resource;

public class FreightVehicleEntersTrafficEventCreator implements EventCreator {

	@Override
	public Event createEvent(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour,
			Id<Person> driverId, int activityCounter) {
		// TODO Auto-generated method stub
		return null;
	}

}
