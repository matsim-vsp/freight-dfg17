package lsp.usecase;

import java.util.HashMap;

import lsp.shipment.*;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.Tour;
import org.matsim.contrib.freight.carrier.Tour.ServiceActivity;
import org.matsim.contrib.freight.carrier.Tour.TourElement;

import org.matsim.contrib.freight.events.LSPTourEndEvent;
import org.matsim.contrib.freight.events.eventhandler.LSPTourEndEventHandler;
import lsp.LogisticsSolutionElement;
import lsp.resources.LSPResource;

/*package-private*/  class ReloadingPointTourEndEventHandler implements LSPTourEndEventHandler {

	static class ReloadingPointEventHandlerPair{
		public final LSPShipment shipment;
		public final LogisticsSolutionElement element;
				
		public ReloadingPointEventHandlerPair(LSPShipment shipment, LogisticsSolutionElement element){
			this.shipment = shipment;
			this.element = element;
		}	
	}
	
	
	private final HashMap<CarrierService, ReloadingPointEventHandlerPair> servicesWaitedFor;
	private final ReloadingPoint reloadingPoint;
	private final Id<LSPResource> resourceId;
	private final Id<Link> linkId;
	
	ReloadingPointTourEndEventHandler(ReloadingPoint reloadingPoint){
		this.reloadingPoint = reloadingPoint;
		this.linkId = reloadingPoint.getEndLinkId();
		this.resourceId = reloadingPoint.getId();
		this.servicesWaitedFor = new HashMap<CarrierService, ReloadingPointEventHandlerPair>();
	}
	
	@Override
	public void reset(int iteration) {
		servicesWaitedFor.clear();
	}

	public void addShipment(LSPShipment shipment, LogisticsSolutionElement solutionElement){
		ReloadingPointEventHandlerPair pair = new ReloadingPointEventHandlerPair(shipment, solutionElement);
		
		for(ShipmentPlanElement planElement: shipment.getShipmentPlan().getPlanElements().values()){
			if(planElement instanceof ScheduledShipmentTransport){
				ScheduledShipmentTransport transport = (ScheduledShipmentTransport) planElement;
				if(transport.getSolutionElement().getNextElement() == solutionElement){
					servicesWaitedFor.put(transport.getCarrierService(), pair);
				}
			}
		}
	}	
	
	@Override
	public void handleEvent(LSPTourEndEvent event) {
		if((event.getTour().getEndLinkId() == this.linkId) && (shipmentsOfTourEndInPoint(event.getTour()))){
			
			for(TourElement tourElement : event.getTour().getTourElements()){
				if(tourElement instanceof ServiceActivity){
					ServiceActivity serviceActivity = (ServiceActivity) tourElement;
					if(serviceActivity.getLocation() == reloadingPoint.getStartLinkId()
							&& allServicesAreInOnePoint(event.getTour())
							&& (event.getTour().getStartLinkId() != reloadingPoint.getStartLinkId())) {
						logReloadAfterMainRun(serviceActivity.getService(), event);
					}
					else {
						logReloadAfterCollection(serviceActivity.getService(), event);
					}
				}
				
			}
		}
		
		

	}

	private boolean shipmentsOfTourEndInPoint(Tour tour){
		boolean shipmentsEndInPoint = true;
		for(TourElement tourElement : tour.getTourElements()){
			if(tourElement instanceof ServiceActivity){
				ServiceActivity serviceActivity = (ServiceActivity) tourElement;
				if(!servicesWaitedFor.containsKey(serviceActivity.getService())){
					return false;				
				}
			}
		}
		return shipmentsEndInPoint;
	}

	private void logReloadAfterCollection(CarrierService carrierService, LSPTourEndEvent event){
		LSPShipment lspShipment = servicesWaitedFor.get(carrierService).shipment;
		ShipmentUtils.LoggedShipmentHandleBuilder builder = ShipmentUtils.LoggedShipmentHandleBuilder.newInstance();
		builder.setLinkId(linkId);
		builder.setResourceId(resourceId);
		double startTime = event.getTime() + getUnloadEndTime(event.getTour());
		builder.setStartTime(startTime);
		double handlingTime = reloadingPoint.getCapacityNeedFixed() + reloadingPoint.getCapacityNeedLinear() * lspShipment.getSize();
		builder.setEndTime(startTime + handlingTime);
		builder.setLogisticsSolutionElement(servicesWaitedFor.get(carrierService).element);
		ShipmentPlanElement loggedShipmentHandle = builder.build();
		String idString = loggedShipmentHandle.getResourceId() + "" + loggedShipmentHandle.getSolutionElement().getId() + "" + loggedShipmentHandle.getElementType();
		Id<ShipmentPlanElement> loadId = Id.create(idString, ShipmentPlanElement.class);
		if(!lspShipment.getLog().getPlanElements().containsKey(loadId)) {
			lspShipment.getLog().addPlanElement(loadId, loggedShipmentHandle);
		}	
	}
	
	private double getUnloadEndTime(Tour tour){
		double unloadEndTime = 0;
		for(TourElement element: tour.getTourElements()){
			if(element instanceof Tour.ServiceActivity){
				Tour.ServiceActivity serviceActivity = (Tour.ServiceActivity) element;
				unloadEndTime = unloadEndTime + serviceActivity.getDuration();
			}
		}
	
		
		return unloadEndTime;
	}

	private void logReloadAfterMainRun(CarrierService carrierService, LSPTourEndEvent event){
		LSPShipment lspShipment = servicesWaitedFor.get(carrierService).shipment;
		ShipmentUtils.LoggedShipmentHandleBuilder builder = ShipmentUtils.LoggedShipmentHandleBuilder.newInstance();
		builder.setLinkId(linkId);
		builder.setResourceId(resourceId);
		double startTime = event.getTime();
		builder.setStartTime(startTime);
		double handlingTime = reloadingPoint.getCapacityNeedFixed() + reloadingPoint.getCapacityNeedLinear() * lspShipment.getSize();
		builder.setEndTime(startTime + handlingTime);
		builder.setLogisticsSolutionElement(servicesWaitedFor.get(carrierService).element);
		ShipmentPlanElement handle = builder.build();
		String idString = handle.getResourceId() + "" + handle.getSolutionElement().getId() + "" + handle.getElementType();
		Id<ShipmentPlanElement> loadId = Id.create(idString, ShipmentPlanElement.class);
		if(!lspShipment.getLog().getPlanElements().containsKey(loadId)) {
			lspShipment.getLog().addPlanElement(loadId, handle);
		}	
	}

	private boolean allServicesAreInOnePoint(Tour tour) {
		for(TourElement element : tour.getTourElements()) {
			if(element instanceof ServiceActivity) {
				ServiceActivity activity = (ServiceActivity) element;
				if(activity.getLocation() != tour.getEndLinkId()) {
					return false;
				}
			}
		}
		return true;
	}
	
	
	public HashMap<CarrierService, ReloadingPointEventHandlerPair> getServicesWaitedFor() {
		return servicesWaitedFor;
	}

	public ReloadingPoint getReloadingPoint() {
		return reloadingPoint;
	}

	public Id<LSPResource> getResourceId() {
		return resourceId;
	}

	public Id<Link> getLinkId() {
		return linkId;
	}
	
	
	
}
