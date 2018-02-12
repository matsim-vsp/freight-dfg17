package events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.vehicles.Vehicle;

public class FreightLinkLeaveEvent extends Event {

	private CarrierVehicle carrierVehicle;
	private Id<Carrier> carrierId;
	private Id<Person> driverId;
	private Id<Vehicle> vehicleId; 
	private Id<Link>linkId; 
	
	public FreightLinkLeaveEvent(Id<Carrier>carrierId, Id<Vehicle> vehicleId, Id<Person>driverId, Id<Link>linkId, double time, CarrierVehicle vehicle) {
		super(time);
		this.carrierVehicle = vehicle ;
		this.carrierId = carrierId;
		this.driverId = driverId;
		this.vehicleId = vehicleId; 
		this.linkId = linkId; 
	}

	public CarrierVehicle getCarrierVehicle() {
		return carrierVehicle;
	}

	public Id<Carrier> getCarrierId() {
		return carrierId;
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
		// TODO Auto-generated method stub
		return null;
	}
	
}
