package lsp.resources;

import java.util.ArrayList;
import java.util.Collections;

import lsp.LogisticsSolutionElement;
import lsp.ShipmentTuple;
import lsp.shipment.ShipmentComparator;

public abstract class LSPResourceScheduler {

	protected LSPResource resource;
	protected ArrayList<ShipmentTuple>shipments;
	
	
	public final void scheduleShipments(LSPResource resource, int bufferTime) {
		this.resource = resource;
		this.shipments = new ArrayList<ShipmentTuple>();
		initializeValues(resource);
		presortIncomingShipments();	
		scheduleResource();
		updateShipments();
		switchHandeledShipments(bufferTime);
		shipments.clear();	
	}		
	
	protected abstract void initializeValues(LSPResource resource);
	
	protected abstract void scheduleResource();
	
	protected abstract void updateShipments();
		
	public final void presortIncomingShipments(){
		this.shipments = new ArrayList<ShipmentTuple>();
		for(LogisticsSolutionElement element : resource.getClientElements()){
			shipments.addAll(element.getIncomingShipments().getShipments());		
		}
		Collections.sort(shipments, new ShipmentComparator());
	}
	
	
	public final void switchHandeledShipments(int bufferTime){
		for(ShipmentTuple tuple : shipments) {
			double endOfTransportTime = tuple.getShipment().getSchedule().getMostRecentEntry().getEndTime() + bufferTime;
			ShipmentTuple outgoingTuple = new ShipmentTuple(endOfTransportTime,tuple.getShipment());
			for(LogisticsSolutionElement element : resource.getClientElements()){
				if(element.getIncomingShipments().getShipments().contains(tuple)){
					element.getOutgoingShipments().getShipments().add(outgoingTuple);
					element.getIncomingShipments().getShipments().remove(tuple);
					if(element.getNextElement() != null) {
						element.getNextElement().getIncomingShipments().getShipments().add(outgoingTuple);
						element.getOutgoingShipments().getShipments().remove(outgoingTuple);
					}
				}
			}
		}	
	}	
	
	
}
