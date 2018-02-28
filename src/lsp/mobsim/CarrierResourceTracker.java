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
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierShipment;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.carrier.Tour;
import org.matsim.contrib.freight.events.ShipmentDeliveredEvent;
import org.matsim.contrib.freight.events.ShipmentPickedUpEvent;
import org.matsim.core.events.algorithms.Vehicle2DriverEventHandler;
import org.matsim.vehicles.Vehicle;

import lsp.controler.FreightControlerListener;
import lsp.events.FreightLinkEnterEvent;
import lsp.events.FreightLinkLeaveEvent;
import lsp.events.FreightVehicleLeavesTrafficEvent;
import lsp.events.ServiceBeginsEvent;
import lsp.events.ServiceCompletedEvent;
import lsp.events.TourEndEvent;
import lsp.events.TourStartEvent;
import lsp.mobsim.CarrierResourceAgent.CarrierDriverAgent;



public class CarrierResourceTracker implements ActivityStartEventHandler, ActivityEndEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler,  LinkEnterEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler {

	
	private final FreightControlerListener listener;
	
	private final Carriers carriers;

	private Vehicle2DriverEventHandler delegate = new Vehicle2DriverEventHandler();

	private final Collection<CarrierResourceAgent> carrierResourceAgents = new ArrayList<CarrierResourceAgent>();
	
	private Map<Id<Person>, CarrierResourceAgent> driverAgentMap = new HashMap<Id<Person>, CarrierResourceAgent>();

	public CarrierResourceTracker(Carriers carriers, Network network,  FreightControlerListener listener) {
		this.carriers = carriers;
		createCarrierResourceAgents();
		this.listener = listener;
	}

	private void createCarrierResourceAgents() {
		for (Carrier carrier : carriers.getCarriers().values()) {
			CarrierResourceAgent carrierAgent = new CarrierResourceAgent(this, carrier, delegate);
			carrierResourceAgents.add(carrierAgent);
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
		listener.processEvent(event);
	}
	
	public void notifyPickedUp(Id<Carrier> carrierId, Id<Person> driverId, CarrierShipment shipment, double time) {
		processEvent(new ShipmentPickedUpEvent(carrierId, driverId, shipment, time));
	}

	public void notifyDelivered(Id<Carrier> carrierId, Id<Person> driverId, CarrierShipment shipment, double time) {
		processEvent(new ShipmentDeliveredEvent(carrierId, driverId, shipment,time));
	}

	public void notifyStart(Id<Carrier>  carrierId, Id<Person> driverId, Tour tour, double time, CarrierVehicle vehicle) {
		processEvent(new TourStartEvent(carrierId, driverId, tour ,time, vehicle));
	}
	
	public void notifyEnd(Id<Carrier>  carrierId, Id<Person> driverId, Tour tour, double time, CarrierVehicle vehicle) {
		processEvent(new TourEndEvent(carrierId, driverId, tour ,time, vehicle));
	}
	
	public void notifyServiceEnd(Id<Carrier>  carrierId, Id<Person> driverId, CarrierService service, double time, CarrierVehicle vehicle) {
		processEvent(new ServiceCompletedEvent(carrierId, driverId, service ,time, vehicle));
	}
	
	public void notifyServiceStart(Id<Carrier>  carrierId, Id<Person> driverId, CarrierService service, double time, CarrierVehicle vehicle) {
		processEvent(new ServiceBeginsEvent(carrierId, driverId, service ,time, vehicle));
	}

	public void  notifyLinkEntered(Id<Carrier> carrierId, Id<Vehicle> vehicleId, Id<Person> driverId ,Id<Link> linkId, double time, CarrierVehicle vehicle) {
		processEvent(new FreightLinkEnterEvent(carrierId, vehicleId,  driverId, linkId ,time, vehicle));
	}
	
	public void  notifyLinkLeft(Id<Carrier> carrierId, Id<Vehicle> vehicleId, Id<Person> driverId ,Id<Link> linkId, double time, CarrierVehicle vehicle) {
		processEvent(new FreightLinkLeaveEvent(carrierId, vehicleId,  driverId, linkId ,time, vehicle));
	}
	
	public void notifyLeavesTraffic(Id<Carrier> carrierId, double time, Id<Person> driverId, Id<Link> linkId, Id<Vehicle> vehicleId, CarrierVehicle vehicle) {
		processEvent(new FreightVehicleLeavesTrafficEvent(carrierId, time, driverId, linkId, vehicleId, vehicle));
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
	}

	@Override
	public void handleEvent(VehicleEntersTrafficEvent event) {
		delegate.handleEvent(event);
	}
	
}