package lsp.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.Tour;
import org.matsim.contrib.freight.carrier.Tour.Start;

public class TourStartEvent extends Event{

	private Id<Carrier> carrierId;
	private Id<Person> driverId;
	private Tour tour;
	private CarrierVehicle vehicle;
	
	public TourStartEvent(Id<Carrier>  carrierId, Id<Person> driverId, Tour tour, double time, CarrierVehicle vehicle) {
		super(time);
		this.carrierId = carrierId;
		this.driverId = driverId;
		this.tour = tour;
		this.vehicle = vehicle;
	}

	@Override
	public String getEventType() {
		// TODO Auto-generated method stub
		return null;
	}

	public Id<Carrier> getCarrierId() {
		return carrierId;
	}

	public Id<Person> getDriverId() {
		return driverId;
	}

	public Tour getTour() {
		return tour;
	}

	public CarrierVehicle getVehicle() {
		return vehicle;
	}
	
}
