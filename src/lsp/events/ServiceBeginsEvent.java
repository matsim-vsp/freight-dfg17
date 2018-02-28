package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierVehicle;

public class ServiceBeginsEvent extends Event{

	private CarrierService service;
	private Id<Carrier> carrierId;
	private Id<Person> driverId; 
	private CarrierVehicle vehicle;	
	
	public ServiceBeginsEvent(Id<Carrier> carrierId, Id<Person> driverId, CarrierService service, double time, CarrierVehicle vehicle) {
		super(time);
		this.carrierId = carrierId;
		this.service = service;
		this.driverId = driverId;
		this.vehicle = vehicle;
	}

	@Override
	public String getEventType() {
		return "service";
	}

	public CarrierService getService() {
		return service;
	}

	public Id<Carrier> getCarrierId() {
		return carrierId;
	}

	public Id<Person> getDriverId() {
		return driverId;
	}

	public CarrierVehicle getVehicle() {
		return vehicle;
	}
	
}
