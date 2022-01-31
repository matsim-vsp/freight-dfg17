package demand;

import demand.demandObject.ShipperShipment;
import demand.offer.Offer;

public interface UtilityFunction {

	String getName();
	double getUtilityValue(Offer offer, ShipperShipment shipment);
	
}
