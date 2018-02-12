package usecase;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.CarrierService;

import events.ServiceCompletedEvent;
import events.ServiceCompletedEventHandler;
import lsp.LogisticsSolutionElement;
import lsp.resources.CarrierResource;
import shipment.AbstractShipmentPlanElement;
import shipment.LSPShipment;
import shipment.LoggedShipmentTransport;
import shipment.LoggedShipmentUnload;

public class DistributionServiceEventHandler implements ServiceCompletedEventHandler {

	private CarrierService carrierService;
	private LSPShipment lspShipment;
	private LogisticsSolutionElement solutionElement;
	private CarrierResource resource;

	public DistributionServiceEventHandler(CarrierService carrierService, LSPShipment lspShipment, LogisticsSolutionElement element, CarrierResource resource) {
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
	public void handleEvent(ServiceCompletedEvent event) {
		if (event.getService().getId() == carrierService.getId() && event.getCarrierId() == resource.getCarrier().getId()) {
			logTransport(event);
			logUnload(event);
		}
	}

	private void logTransport(ServiceCompletedEvent event) {
		String idString = resource.getId() + "" + solutionElement.getId() + "" + "TRANSPORT";
		Id<AbstractShipmentPlanElement> id = Id.create(idString, AbstractShipmentPlanElement.class);
		AbstractShipmentPlanElement abstractPlanElement = lspShipment.getLog().getPlanElements().get(id);
		if(abstractPlanElement instanceof LoggedShipmentTransport) {
			LoggedShipmentTransport transport = (LoggedShipmentTransport) abstractPlanElement;
			transport.setEndTime(event.getTime());
		}		
	}

	private void logUnload(ServiceCompletedEvent event) {
		LoggedShipmentUnload.Builder builder = LoggedShipmentUnload.Builder.newInstance();
		builder.setCarrierId(event.getCarrierId());
		builder.setLinkId(event.getService().getLocationLinkId());
		builder.setLogisticsSolutionElement(solutionElement);
		builder.setResourceId(resource.getId());
		builder.setStartTime(event.getTime());
		builder.setEndTime(event.getTime() + event.getService().getServiceDuration());
		LoggedShipmentUnload unload = builder.build();
		String idString = unload.getResourceId() + "" + unload.getSolutionElement().getId() + "" + unload.getElementType();
		Id<AbstractShipmentPlanElement> unloadId = Id.create(idString, AbstractShipmentPlanElement.class);
		lspShipment.getLog().getPlanElements().put(unloadId, unload);
	}
}
