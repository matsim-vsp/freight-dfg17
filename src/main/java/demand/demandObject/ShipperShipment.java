package demand.demandObject;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.TimeWindow;

import lsp.shipment.LSPShipment;



public interface ShipperShipment {

	Id<ShipperShipment> getId();
	double getShipmentSize();
	TimeWindow getStartTimeWindow();
	void setStartTimeWindow(TimeWindow timeWindow);
	TimeWindow getEndTimeWindow();
	void setEndTimeWindow(TimeWindow timeWindow);
	double getServiceTime();
	void setLSPShipment(LSPShipment lspShipment);
	LSPShipment getLSPShipment();
	void setDemandObject(DemandObject demandObject);
	DemandObject getDemandObject();
	
}
