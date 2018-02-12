package events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.vehicles.Vehicle;

public class FreightVehicleLeavesTrafficEvent extends Event{

	private Id<Person> driverId; 
	private Id<Link> linkId; 
	private Id<Vehicle> vehicleId;
	private CarrierVehicle carrierVehicle;
	private Id<Carrier> carrierId;
	
	
	public FreightVehicleLeavesTrafficEvent(Id<Carrier> carrierId, double time, Id<Person> driverId, Id<Link> linkId, Id<Vehicle> vehicleId, CarrierVehicle carrierVehicle) {
		super(time);
		this.driverId = driverId;
		this.linkId = linkId;
		this.vehicleId = vehicleId;
		this.carrierVehicle = carrierVehicle;
		this.carrierId = carrierId;
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

	public Id<Link> getLinkId() {
		return linkId;
	}

	public Id<Vehicle> getVehicleId() {
		return vehicleId;
	}

	@Override
	public String getEventType() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
