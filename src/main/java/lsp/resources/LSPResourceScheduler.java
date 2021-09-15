package lsp.resources;

import java.util.ArrayList;

import lsp.LogisticsSolutionElement;
import lsp.ShipmentWithTime;
import lsp.shipment.ShipmentComparator;

public abstract class LSPResourceScheduler {

	protected LSPResource resource;
	protected ArrayList<ShipmentWithTime>shipments;
	
	
	public final void scheduleShipments(LSPResource resource, int bufferTime) {
		this.resource = resource;
		this.shipments = new ArrayList<>();
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
		this.shipments = new ArrayList<>();
		for(LogisticsSolutionElement element : resource.getClientElements()){
			shipments.addAll(element.getIncomingShipments().getShipments());		
		}
		shipments.sort( new ShipmentComparator() );
	}
	
	
	public final void switchHandeledShipments(int bufferTime){
		for( ShipmentWithTime shipment : shipments) {
			double endOfTransportTime = shipment.getShipment().getShipmentPlan().getMostRecentEntry().getEndTime() + bufferTime;
			ShipmentWithTime outgoingTuple = new ShipmentWithTime(endOfTransportTime,shipment.getShipment());
			for(LogisticsSolutionElement element : resource.getClientElements()){
				if(element.getIncomingShipments().getShipments().contains(shipment)){
					element.getOutgoingShipments().getShipments().add(outgoingTuple);
					element.getIncomingShipments().getShipments().remove(shipment);
					if(element.getNextElement() != null) {
						element.getNextElement().getIncomingShipments().getShipments().add(outgoingTuple);
						element.getOutgoingShipments().getShipments().remove(outgoingTuple);
					}
				}
			}
		}	
	}	
	
	
}
