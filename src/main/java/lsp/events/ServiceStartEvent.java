package lsp.events;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierVehicle;

import lsp.resources.CarrierResource;

public class ServiceStartEvent extends Event{

	public static final String ATTRIBUTE_PERSON = "driver";
	public static final String EVENT_TYPE = "service ends";
	public static final String ATTRIBUTE_LINK = "link";
	public static final String ATTRIBUTE_ACTTYPE = "actType";
	public static final String ATTRIBUTE_SERVICE = "service";
	public static final String ATTRIBUTE_VEHICLE = "vehicle";
	public static final String ATTRIBUTE_CARRIER = "carrier";
	public static final String ATTRIBUTE_RESOURCE = "resource";
	
	private CarrierService service;
	private Id<Person> driverId; 
	private CarrierVehicle vehicle;	
	private CarrierResource resource;
	private ActivityStartEvent event;
	
	public ServiceStartEvent(ActivityStartEvent event, CarrierResource resource, Id<Person> driverId, CarrierService service, double time, CarrierVehicle vehicle) {
		super(time);
		this.service = service;
		this.driverId = driverId;
		this.vehicle = vehicle;
		this.resource = resource;
		this.event = event;
	}

	@Override
	public String getEventType() {
		return "service";
	}

	public CarrierService getService() {
		return service;
	}

	public Id<Carrier> getCarrierId() {
		return resource.getCarrier().getId();
	}

	public Id<Person> getDriverId() {
		return driverId;
	}

	public CarrierVehicle getVehicle() {
		return vehicle;
	}

	public CarrierResource getResource() {
		return resource;
	}
	
	//@Override
	/*public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_PERSON, this.driverId.toString());
		attr.put(ATTRIBUTE_LINK, (event.getLinkId() == null ? null : event.getLinkId().toString()));
		if (this.vehicleId != null) {
			attr.put(ATTRIBUTE_VEHICLE, this.vehicleId.toString());
		}
		if (event.getNetworkMode() != null) {
			attr.put(ATTRIBUTE_NETWORKMODE, event.getNetworkMode());
		}
		attr.put(ATTRIBUTE_POSITION, Double.toString(event.getRelativePositionOnLink()));
		attr.put(ATTRIBUTE_CARRIER, this.carrierResource.getCarrier().getId().toString());
		attr.put(ATTRIBUTE_RESOURCE, this.carrierResource.getId().toString());
		return attr;
	}*/
}
