package testLSPWithCostTracker;

import org.matsim.contrib.freight.events.LSPTourStartEvent;
import org.matsim.contrib.freight.eventhandler.LSPTourStartEventHandler;

public class TourStartHandler implements LSPTourStartEventHandler {

	private double vehicleFixedCosts;
		
	@Override
	public void reset(int iteration) {
		vehicleFixedCosts = 0;
	}

	@Override
	public void handleEvent(LSPTourStartEvent event) {
		vehicleFixedCosts = vehicleFixedCosts + event.getVehicle().getVehicleType().getCostInformation().getFix();
	}

	public double getVehicleFixedCosts() {
		return vehicleFixedCosts;
	}

}
