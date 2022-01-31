package demand.demandObject;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.TimeWindow;

import lsp.shipment.LSPShipment;

public class ShipperShipmentImpl implements ShipperShipment{

	private final Id<ShipperShipment> id;
	private final double shipmentSize;
	private TimeWindow startTimeWindow;
	private TimeWindow endTimeWindow;
	private final double serviceTime;
	private LSPShipment lspShipment;
	private DemandObject demandObject;
	
	public static class Builder{
		private Id<ShipperShipment> id;
		private double shipmentSize;
		private TimeWindow startTimeWindow;
		private TimeWindow endTimeWindow;
		private double serviceTime;
		private DemandObject demandObject;
	
		public static Builder newInstance() {
			return new Builder();
		}
	
		public void setId(Id<ShipperShipment> id) {
			this.id = id;
		}
	
		public void setShipmentSize(double shipmentSize) {
			this.shipmentSize = shipmentSize;
		}
	
		public void setStartTimeWindow(TimeWindow startTimeWindow) {
			this.startTimeWindow = startTimeWindow;
		}
	
		public void setEndTimeWindow(TimeWindow endTimeWindow) {
			this.endTimeWindow = endTimeWindow;
		}
	
		public void setServiceTime(double serviceTime) {
			this.serviceTime = serviceTime;
		}
		
		public void setDemandObject(DemandObject demandObject) {
			this.demandObject = demandObject;
		}
	
		public ShipperShipment build() {
			return new ShipperShipmentImpl(this);
		}
	}
	
	private ShipperShipmentImpl(Builder builder) {
		this.id = builder.id;
		this.shipmentSize = builder.shipmentSize;
		if(builder.startTimeWindow == null) {
			this.startTimeWindow = TimeWindow.newInstance(0, Double.MAX_VALUE);
		}
		else {
			this.startTimeWindow = builder.startTimeWindow;
		}
		if(builder.endTimeWindow == null) {
			this.endTimeWindow = TimeWindow.newInstance(0, Double.MAX_VALUE);
		}
		else {
			this.endTimeWindow = builder.endTimeWindow;
		}
		this.serviceTime = builder.serviceTime;
		this.demandObject = builder.demandObject;
	}
	
	@Override
	public Id<ShipperShipment> getId() {
		return id;
	}

	@Override
	public double getShipmentSize() {
		return shipmentSize;
	}

	@Override
	public TimeWindow getStartTimeWindow() {
		return startTimeWindow;
	}

	@Override
	public TimeWindow getEndTimeWindow() {
		return endTimeWindow;
	}

	@Override
	public double getServiceTime() {
		return serviceTime;
	}

	@Override
	public void setLSPShipment(LSPShipment lspShipment) {
		this.lspShipment = lspShipment;
	}

	@Override
	public LSPShipment getLSPShipment() {
		return lspShipment;
	}

	@Override
	public void setDemandObject(DemandObject demandObject) {
		this.demandObject = demandObject;
	}

	@Override
	public DemandObject getDemandObject() {
		return demandObject;
	}

	@Override
	public void setStartTimeWindow(TimeWindow timeWindow) {
		this.startTimeWindow = timeWindow;
	}

	@Override
	public void setEndTimeWindow(TimeWindow timeWindow) {
		this.endTimeWindow = timeWindow;
	}
	

}
