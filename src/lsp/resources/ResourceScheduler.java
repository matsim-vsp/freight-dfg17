package lsp.resources;

import java.util.ArrayList;
import java.util.Collections;

import lsp.LogisticsSolutionElement;
import lsp.ShipmentTuple;
import lsp.shipment.ShipmentComparator;

public abstract class ResourceScheduler {

	protected Resource resource;
	protected ArrayList<ShipmentTuple>shipments;
	private int bufferTime;
	
	public void setBufferTime(int bufferTime) {
		this.bufferTime = bufferTime;
	}
	
	public final void scheduleShipments(Resource resource) {
		this.resource = resource;
		this.shipments = new ArrayList<ShipmentTuple>();
		initializeValues(resource);
		presortIncomingShipments();	
		scheduleResource();
		updateShipments();
		switchHandeledShipments();
		shipments.clear();	
	}		
	
	protected abstract void initializeValues(Resource resource);
	
	protected abstract void scheduleResource();
	
	protected abstract void updateShipments();
		
	public final void presortIncomingShipments(){
		this.shipments = new ArrayList<ShipmentTuple>();
		for(LogisticsSolutionElement element : resource.getClientElements()){
			shipments.addAll(element.getIncomingShipments().getShipments());		
		}
		Collections.sort(shipments, new ShipmentComparator());
	}
	
	
	public final void switchHandeledShipments(){
		for(ShipmentTuple tuple : shipments) {
			double endOfTransportTime = tuple.getShipment().getSchedule().getMostRecentEntry().getEndTime() + bufferTime;
			ShipmentTuple outgoingTuple = new ShipmentTuple(endOfTransportTime,tuple.getShipment());
			for(LogisticsSolutionElement element : resource.getClientElements()){
				if(element.getIncomingShipments().getShipments().contains(tuple)){
					element.getOutgoingShipments().getShipments().add(outgoingTuple);
					if(element.getNextElement() != null) {
						element.getNextElement().getIncomingShipments().getShipments().add(outgoingTuple);
					}
				}
			}
		}	
	}	
	
	
}
