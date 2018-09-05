package demand.controler;

import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.contrib.freight.carrier.Tour.Leg;
import org.matsim.contrib.freight.carrier.Tour.ServiceActivity;
import org.matsim.contrib.freight.carrier.Tour.TourElement;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LSPDecorators;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.decoratedLSP.LogisticsSolutionWithOffers;
import demand.demandObject.DemandObject;
import demand.demandObject.DemandObjects;
import demand.demandObject.DemandPlan;
import demand.offer.Offer;
import lsp.functions.Info;
import lsp.functions.InfoFunctionValue;
import lsp.resources.CarrierResource;
import lsp.resources.Resource;
import lsp.shipment.LSPShipment;
import lsp.shipment.LSPShipmentImpl;

public class InitialDemandAssigner implements StartupListener{

	private DemandObjects demandObjects;
	private LSPDecorators lsps;
		int day;
		int evening;
	public InitialDemandAssigner(DemandObjects demandObjects, LSPDecorators lsps) {
		this.demandObjects = demandObjects;
		this.lsps = lsps;
	}

	@Override
	public void notifyStartup(StartupEvent event) {
		day = 0;
		evening = 0;
		for(DemandObject demandObject : demandObjects.getDemandObjects().values()) {
			if(demandObject.getSelectedPlan() == null) {
				createInitialPlan(demandObject);
			}
				assignShipmentToLSP(demandObject);			
			}
		
		
		/*for(LSPDecorator lsp : lsps.getLSPs().values()) {
			for(LogisticsSolutionDecorator solution : lsp.getSelectedPlan().getSolutionDecorators()) {
				System.out.println( solution.getId() + " : " + solution.getShipments().size());
			}
		}*/
		
		for(LSPDecorator lsp : lsps.getLSPs().values()) {
			lsp.scheduleSoultions();
		}
		/*double distance = 0;
		for(LSPDecorator lsp : lsps.getLSPs().values()) {
			for(Resource resource : lsp.getResources()) {
				if(resource instanceof CarrierResource) {
					CarrierResource carrierResource = (CarrierResource) resource;
					for(ScheduledTour scheduledTour : carrierResource.getCarrier().getSelectedPlan().getScheduledTours()) {
						for(TourElement element : scheduledTour.getTour().getTourElements()) {
							if(element instanceof Leg) {
								Leg leg = (Leg) element;
								distance = distance + leg.getRoute().getDistance();
								System.out.println(distance);
							}
						}
					}
				}
			}
		}
		System.out.println(distance);
		//System.exit(1);*/
	}

	private void createInitialPlan(DemandObject demandObject) {
		Collection<Offer> offers = demandObject.getOfferRequester().requestOffers(lsps.getLSPs().values());
		DemandPlan initialPlan = demandObject.getDemandPlanGenerator().createDemandPlan(offers);
		demandObject.setSelectedPlan(initialPlan);	
	}
	
	private void assignShipmentToLSP(DemandObject demandObject) {
		Id<LSPShipment> id = Id.create(demandObject.getSelectedPlan().getShipment().getId(), LSPShipment.class);
		LSPShipmentImpl.Builder builder = LSPShipmentImpl.Builder.newInstance(id);
		builder.setFromLinkId(demandObject.getFromLinkId());
		builder.setToLinkId(demandObject.getToLinkId());
		builder.setCapacityDemand((int)demandObject.getSelectedPlan().getShipment().getShipmentSize());
		builder.setServiceTime(demandObject.getSelectedPlan().getShipment().getServiceTime());
		builder.setStartTimeWindow(demandObject.getSelectedPlan().getShipment().getStartTimeWindow());
		builder.setEndTimeWindow(demandObject.getSelectedPlan().getShipment().getEndTimeWindow());
		for(Info info : demandObject.getInfos()) {
			builder.addInfo(info);
		}
		LSPShipment lspShipment = builder.build();
		if(demandObject.getSelectedPlan().getLsp() != null) {
			demandObject.getSelectedPlan().getLsp().assignShipmentToSolution(lspShipment, demandObject.getSelectedPlan().getSolutionId());
			if(demandObject.getSelectedPlan().getSolutionId().toString() == "Evening Solution") {
				evening++;
			}
			else {
				day++;
			}
			demandObject.getSelectedPlan().getShipment().setLSPShipment(lspShipment);
		}
	}
	
	
}
