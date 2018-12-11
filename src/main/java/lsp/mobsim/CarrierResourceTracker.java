package lsp.mobsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.algorithms.Vehicle2DriverEventHandler;
import lsp.controler.FreightControlerListener;
import lsp.events.EventCreator;
import lsp.events.FreightLinkEnterEvent;
import lsp.mobsim.CarrierResourceAgent.CarrierDriverAgent;
import lsp.resources.CarrierResource;
import lsp.resources.Resource;



public class CarrierResourceTracker implements ActivityStartEventHandler, ActivityEndEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler,  LinkEnterEventHandler, 
 LinkLeaveEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler, PersonEntersVehicleEventHandler,  PersonLeavesVehicleEventHandler{

	
	private final Collection<CarrierResource> resources;

	private Vehicle2DriverEventHandler delegate = new Vehicle2DriverEventHandler();

	private final Collection<CarrierResourceAgent> carrierResourceAgents = new ArrayList<CarrierResourceAgent>();
	
	private Map<Id<Person>, CarrierResourceAgent> driverAgentMap = new HashMap<Id<Person>, CarrierResourceAgent>();

	private Collection<EventCreator> eventCreators;
	
	private EventsManager eventsManager;
	
	public CarrierResourceTracker(Collection<CarrierResource> resources, Network network, Collection<EventCreator> creators, EventsManager eventsManager) {
		this.resources = resources;
		this.eventCreators = creators;
		createCarrierResourceAgents();
		this.eventsManager = eventsManager;
	}

	public EventsManager getEventsManager() {
		return this.eventsManager;
	}
	
	private void createCarrierResourceAgents() {
		for (CarrierResource resource : resources) {
				CarrierResourceAgent carrierResourceAgent = new CarrierResourceAgent(this, resource, delegate);
				carrierResourceAgents.add(carrierResourceAgent);
		}
	}

	/**
	 * Returns the entire set of selected carrier plans.
	 * 
	 * @return collection of plans
	 * @see Plan, CarrierPlan
	 */
	public Collection<MobSimVehicleRoute> createPlans() {
		List<MobSimVehicleRoute> vehicleRoutes = new ArrayList<MobSimVehicleRoute>();
		for (CarrierResourceAgent carrierResourceAgent : carrierResourceAgents) {
			List<MobSimVehicleRoute> plansForCarrier = carrierResourceAgent.createFreightDriverPlans();
			vehicleRoutes.addAll(plansForCarrier);
		}
		return vehicleRoutes;
	}
	
	@Override
	public void reset(int iteration) {
		delegate.reset(iteration);
	}

	private void processEvent(Event event) {
		eventsManager.processEvent(event);
	}
	
	public void notifyEventHappened(Event event, Resource resource, Activity activity, ScheduledTour scheduledTour, Id<Person> driverId, int activityCounter) {
		for(EventCreator eventCreator : eventCreators) {
			Event customEvent = eventCreator.createEvent(event, resource, activity, scheduledTour, driverId, activityCounter);
			if(customEvent != null) {
				processEvent(customEvent);
			}
		}
	}
		
	@Override
	public void handleEvent(ActivityEndEvent event) {
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(event.getPersonId());
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(delegate.getDriverOfVehicle(event.getVehicleId()));
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(event.getPersonId());
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);
	}


	@Override
	public void handleEvent(PersonArrivalEvent event) {
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(event.getPersonId());
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(event.getPersonId());
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);
	}

	private CarrierResourceAgent getCarrierResourceAgent(Id<Person> driverId) {
		if(driverAgentMap.containsKey(driverId)){
			return driverAgentMap.get(driverId);
		}
		for(CarrierResourceAgent ca : carrierResourceAgents){
			if(ca.getDriverIds().contains(driverId)){
				driverAgentMap.put(driverId, ca);
				return ca;
			}
		}
		return null;	
	}
	
	public CarrierDriverAgent getDriver(Id<Person> driverId){
		CarrierResourceAgent carrierAgent = getCarrierResourceAgent(driverId);
		if(carrierAgent == null) throw new IllegalStateException("missing carrier agent. cannot find carrierAgent to driver " + driverId);
		return carrierAgent.getDriver(driverId);
	}

	@Override
	public void handleEvent(VehicleLeavesTrafficEvent event) {
		delegate.handleEvent(event);
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(event.getPersonId());
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);
	}

	@Override
	public void handleEvent(VehicleEntersTrafficEvent event) {
		delegate.handleEvent(event);
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(event.getPersonId());
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(delegate.getDriverOfVehicle(event.getVehicleId()));
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);	
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(event.getPersonId());
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		CarrierResourceAgent carrierResourceAgent = getCarrierResourceAgent(event.getPersonId());
		if(carrierResourceAgent == null) return;
		carrierResourceAgent.handleEvent(event);
	}
	
}