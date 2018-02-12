package shipment;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.events.handler.EventHandler;

public class LSPShipmentImpl implements LSPShipment {

	private Id<LSPShipment> id;
	private Id<Link> fromLinkId;
	private Id<Link> toLinkId;
	private TimeWindow startTimeWindow;
	private TimeWindow endTimeWindow;
	private int capacityDemand;
	private double serviceTime;
	private AbstractShipmentPlan schedule;
	private AbstractShipmentPlan log;
	private ArrayList<EventHandler> eventHandlers;

	
	public static class Builder {	

		private Id<LSPShipment> id;
		private Id<Link> fromLinkId;
		private Id<Link> toLinkId;
		private TimeWindow startTimeWindow;
		private TimeWindow endTimeWindow;
		private int capacityDemand;
		private double serviceTime;
		
	public static Builder newInstance(Id<LSPShipment> id){
		return new Builder(id);
	}
	
	private Builder(Id<LSPShipment> id){
		this.id = id;
	}
	
	public Builder setFromLinkId(Id<Link> fromLinkId){
		this.fromLinkId = fromLinkId;
		return this;
	}
	
	public Builder setToLinkId(Id<Link> toLinkId){
		this.toLinkId = toLinkId;
		return this;
	}
	
	public Builder setStartTimeWindow(TimeWindow startTimeWindow){
		this.startTimeWindow = startTimeWindow;
		return this;
	}
	
	public Builder setEndTimeWindow(TimeWindow endTimeWindow){
		this.endTimeWindow = endTimeWindow;
		return this;
	}
	
	public Builder setCapacityDemand(int capacityDemand){
		this.capacityDemand = capacityDemand;
		return this;
	}
	
	public Builder setServiceTime(double serviceTime){
		this.serviceTime = serviceTime;
		return this;
	}
	
	public LSPShipmentImpl build(){
		return new LSPShipmentImpl(this);
	}
	
	
	}	
	
	private LSPShipmentImpl(LSPShipmentImpl.Builder builder){
		this.id = builder.id;
		this.fromLinkId = builder.fromLinkId;
		this.toLinkId = builder.toLinkId;
		this.startTimeWindow = builder.startTimeWindow;
		this.endTimeWindow = builder.endTimeWindow;
		this.capacityDemand = builder.capacityDemand;
		this.serviceTime = builder.serviceTime;
		this.schedule = new Schedule(this);
		this.log = new Log(this);
		this.eventHandlers = new ArrayList<EventHandler>();
	}
	
	
	@Override
	public Id<LSPShipment> getId() {
		return id;
	}

	@Override
	public Id<Link> getFromLinkId() {
		return fromLinkId;
	}

	@Override
	public Id<Link> getToLinkId() {
		return toLinkId;
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
	public AbstractShipmentPlan getSchedule() {
		return schedule;
	}

	@Override
	public AbstractShipmentPlan getLog() {
		return log;
	}

	@Override
	public int getCapacityDemand() {
		return capacityDemand;
	}

	@Override
	public double getServiceTime() {
		return serviceTime;
	}

	@Override
	public Collection<EventHandler> getEventHandlers() {
		return eventHandlers;
	}

}
