package testDemandObjectsWithLotsizes;

import demand.demandObject.ShipperShipment;
import demand.offer.Offer;
import demand.utilityFunctions.UtilityFunction;

public class LotSizeUtilityFunction implements UtilityFunction{

	private String name;
	private double value;
	
	public LotSizeUtilityFunction(double value) {
		this.value = value;
		this.name = "lotSize";
	}
	
	
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getUtilityValue(Offer offer, ShipperShipment shipment) {
		return value;
	}

	
	
}
