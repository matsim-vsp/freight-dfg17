package lsp.events;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.vehicles.Vehicle;

import lsp.resources.CarrierResource;

public class FreightLinkLeaveEvent extends Event {

	public static final String EVENT_TYPE = "freight vehicle left link";
	public static final String ATTRIBUTE_VEHICLE = "vehicle";
	public static final String ATTRIBUTE_LINK = "link";
	public static final String ATTRIBUTE_CARRIER = "carrier";
	public static final String ATTRIBUTE_DRIVER = "driver";
	public static final String ATTRIBUTE_RESOURCE = "resource";
	
	private CarrierVehicle carrierVehicle;
	private Id<Carrier> carrierId;
	private Id<Person> driverId;
	private Id<Vehicle> vehicleId; 
	private Id<Link>linkId; 
	private CarrierResource resource;
	
	public FreightLinkLeaveEvent(CarrierResource resource, Id<Vehicle> vehicleId, Id<Person>driverId, Id<Link>linkId, double time, CarrierVehicle vehicle) {
		super(time);
		this.carrierVehicle = vehicle ;
		this.resource = resource;
		this.driverId = driverId;
		this.vehicleId = vehicleId; 
		this.linkId = linkId; 
	}

	public CarrierVehicle getCarrierVehicle() {
		return carrierVehicle;
	}

	public Id<Carrier> getCarrierId() {
		return resource.getCarrier().getId();
	}

	public Id<Person> getDriverId() {
		return driverId;	
	}

	public Id<Vehicle> getVehicleId() {
		return vehicleId;
	}

	public Id<Link> getLinkId() {
		return linkId;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
	
	public CarrierResource getResource() {
		return resource;
	}
	
	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_VEHICLE, this.vehicleId.toString());
		attr.put(ATTRIBUTE_LINK, this.linkId.toString());
		attr.put(ATTRIBUTE_CARRIER, this.resource.getCarrier().getId().toString());
		attr.put(ATTRIBUTE_DRIVER, this.driverId.toString());
		return attr;
	}
}
