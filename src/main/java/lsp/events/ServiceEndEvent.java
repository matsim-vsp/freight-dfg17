package lsp.events;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierVehicle;

import lsp.resources.CarrierResource;

public class ServiceEndEvent extends Event {

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
	private ActivityEndEvent event;
	private CarrierResource carrierResource;
	
	public ServiceEndEvent(ActivityEndEvent event, CarrierResource carrierResource, Id<Person> driverId, CarrierService service, double time, CarrierVehicle vehicle) {
		super(time);
		this.service = service;
		this.driverId = driverId;
		this.carrierResource = carrierResource;
		this.vehicle = vehicle;
		this.event = event;
	}

	public Id<Carrier> getCarrierId() {
		return carrierResource.getCarrier().getId();
	}

	public Id<Person> getDriverId() {
		return driverId;
	}

	public CarrierService getService() {
		return service;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}

	public CarrierVehicle getVehicle() {
		return vehicle;
	}
	
	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_PERSON, this.driverId.toString());
		if (service.getLocationLinkId() != null) {
			attr.put(ATTRIBUTE_LINK, service.getLocationLinkId().toString());
		}
		attr.put(ATTRIBUTE_ACTTYPE, event.getActType());
		attr.put(ATTRIBUTE_SERVICE, service.getId().toString());
		attr.put(ATTRIBUTE_VEHICLE, vehicle.getVehicleId().toString());
		attr.put(ATTRIBUTE_CARRIER, carrierResource.getCarrier().getId().toString());
		attr.put(ATTRIBUTE_RESOURCE, carrierResource.getId().toString());
		return attr;
	}
}
