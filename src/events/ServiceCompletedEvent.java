package events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierShipment;
import org.matsim.contrib.freight.carrier.CarrierVehicle;

public class ServiceCompletedEvent extends Event {

	private CarrierService service;
	private Id<Carrier> carrierId;
	private Id<Person> driverId;
	private CarrierVehicle vehicle;
	
	public ServiceCompletedEvent(Id<Carrier> carrierId, Id<Person> driverId, CarrierService service, double time, CarrierVehicle vehicle) {
		super(time);
		this.service = service;
		this.driverId = driverId;
		this.carrierId = carrierId;
		this.vehicle = vehicle;
	}

	public Id<Carrier> getCarrierId() {
		return carrierId;
	}

	public Id<Person> getDriverId() {
		return driverId;
	}

	public CarrierService getService() {
		return service;
	}

	@Override
	public String getEventType() {
		// TODO Auto-generated method stub
		return null;
	}

	public CarrierVehicle getVehicle() {
		return vehicle;
	}
	
}
