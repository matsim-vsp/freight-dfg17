package lsp.usecase;

import lsp.shipment.*;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.CarrierService;

import org.matsim.contrib.freight.events.LSPServiceStartEvent;
import org.matsim.contrib.freight.events.eventhandler.LSPServiceStartEventHandler;
import lsp.LogisticsSolutionElement;
import lsp.resources.LSPCarrierResource;

/*package-private*/  class DistributionServiceStartEventHandler implements LSPServiceStartEventHandler {

	private final CarrierService carrierService;
	private final LSPShipment lspShipment;
	private final LogisticsSolutionElement solutionElement;
	private final LSPCarrierResource resource;

	DistributionServiceStartEventHandler(CarrierService carrierService, LSPShipment lspShipment, LogisticsSolutionElement element, LSPCarrierResource resource) {
		this.carrierService = carrierService;
		this.lspShipment = lspShipment;
		this.solutionElement = element;
		this.resource = resource;
	}

	@Override
	public void reset(int iteration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(LSPServiceStartEvent event) {
		if (event.getService().getId() == carrierService.getId() && event.getCarrierId() == resource.getCarrier().getId()) {
			logTransport(event);
			logUnload(event);
		}
	}

	private void logTransport(LSPServiceStartEvent event) {
		String idString = resource.getId() + "" + solutionElement.getId() + "" + "TRANSPORT";
		Id<ShipmentPlanElement> id = Id.create(idString, ShipmentPlanElement.class);
		ShipmentPlanElement abstractPlanElement = lspShipment.getLog().getPlanElements().get(id);
		if(abstractPlanElement instanceof LoggedShipmentTransport) {
			LoggedShipmentTransport transport = (LoggedShipmentTransport) abstractPlanElement;
			transport.setEndTime(event.getTime());
		}		
	}

	private void logUnload(LSPServiceStartEvent event) {
		ShipmentUtils.LoggedShipmentUnloadBuilder builder = ShipmentUtils.LoggedShipmentUnloadBuilder.newInstance();
		builder.setCarrierId(event.getCarrierId());
		builder.setLinkId(event.getService().getLocationLinkId());
		builder.setLogisticsSolutionElement(solutionElement);
		builder.setResourceId(resource.getId());
		builder.setStartTime(event.getTime());
		builder.setEndTime(event.getTime() + event.getService().getServiceDuration());
		ShipmentPlanElement unload = builder.build();
		String idString = unload.getResourceId() + "" + unload.getSolutionElement().getId() + "" + unload.getElementType();
		Id<ShipmentPlanElement> unloadId = Id.create(idString, ShipmentPlanElement.class);
		lspShipment.getLog().addPlanElement(unloadId, unload);
	}

	public CarrierService getCarrierService() {
		return carrierService;
	}

	public LSPShipment getLspShipment() {
		return lspShipment;
	}

	public LogisticsSolutionElement getSolutionElement() {
		return solutionElement;
	}

	public LSPCarrierResource getResource() {
		return resource;
	}

	

}
