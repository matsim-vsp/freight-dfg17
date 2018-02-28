package testDemandObjectsWithLotsizes;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.TimeWindow;

import demand.demandObject.DemandObject;
import demand.demandObject.ShipperShipment;
import lsp.shipment.LSPShipment;

public class LotSizeShipment implements ShipperShipment{

	private Id<ShipperShipment> id;
	private double shipmentSize;
	public TimeWindow startTimeWindow;
	public TimeWindow endTimeWindow;
	private double serviceTime;
	private LSPShipment lspShipment;
	private DemandObject demandObject;
	
	public static class Builder{
		private Id<ShipperShipment> id;
		private double shipmentSize;
		public TimeWindow startTimeWindow;
		public TimeWindow endTimeWindow;
		private double serviceTime;
		private DemandObject demandObject;
	
		public Builder getInstance() {
			return new Builder();
		}
	
		public Builder setId(Id<ShipperShipment> id) { 
			this.id = id;
			return this;
		}
		
		public Builder setShipmentSize(double shipmentSize) {
			this.shipmentSize = shipmentSize;
			return this;
		}
		
		public Builder setStartTimeWindow(TimeWindow startTimeWindow) {
			this.startTimeWindow = startTimeWindow;
			return this;
		}
	
		public Builder setEndTimeWindow(TimeWindow endTimeWindow) {
			this.endTimeWindow = endTimeWindow;
			return this;
		}
		
		public Builder setServiceTime(double serviceTime) {
			this.serviceTime = serviceTime;
			return this;
		}
		public LotSizeShipment build() {
			return new LotSizeShipment(this);
		}
		
		private Builder() {
			
		}
	
		
	}	
	
	private LotSizeShipment(Builder builder) {
		this.id = builder.id;
		this.shipmentSize = builder.shipmentSize;
		this.startTimeWindow  = builder.startTimeWindow;
		this.endTimeWindow = builder.endTimeWindow;
		this.serviceTime = builder.serviceTime;
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
	public TimeWindow getEndTimeWindow() {
		return endTimeWindow;
	}

	@Override
	public void setDemandObject(DemandObject demandObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DemandObject getDemandObject() {
		// TODO Auto-generated method stub
		return null;
	}
}
