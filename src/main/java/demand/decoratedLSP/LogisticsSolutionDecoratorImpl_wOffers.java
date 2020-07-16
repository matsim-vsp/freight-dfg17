package demand.decoratedLSP;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.handler.EventHandler;

import demand.demandObject.DemandObject;
import demand.offer.Offer;
import demand.offer.OfferFactory;
import lsp.functions.Info;
import lsp.LSP;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.shipment.LSPShipment;
import lsp.controler.LSPSimulationTracker;

/*package-private*/ class LogisticsSolutionDecoratorImpl_wOffers implements LogisticsSolutionDecorator {

	private Id<LogisticsSolution> id;
	private LSPWithOffers lsp;
	private Collection<LogisticsSolutionElement> solutionElements; 
	private Collection<LSPShipment> shipments;
	private Collection<Info> solutionInfos;
	private Collection<EventHandler> eventHandlers;
	private Collection<LSPSimulationTracker>trackers;
	private EventsManager eventsManager;
	private OfferFactory offerFactory;


	LogisticsSolutionDecoratorImpl_wOffers(DecoratedLSPUtils.LogisticsSolutionDecoratorImpl_wOffersBuilder builder){
		this.id = builder.getId();
		this.solutionElements = builder.getElements();
		for(LogisticsSolutionElement element : this.solutionElements) {
			element.setLogisticsSolution(this);
		}
		this.shipments = new ArrayList <LSPShipment>();
		this.solutionInfos = builder.getSolutionInfos();
		this.eventHandlers = builder.getEventHandlers();
		this.trackers = builder.getTrackers();
		this.offerFactory = builder.getOfferFactory();
		if(this.offerFactory != null) {
			this.offerFactory.setLogisticsSolution(this);
			this.offerFactory.setLSP(lsp);
		}
	}
	
	
	
	@Override
	public Id<LogisticsSolution> getId() {
		return id;
	}

	@Override
	public void setLSP(LSP lsp) {
		try {
			this.lsp = (LSPWithOffers) lsp;
		}
		catch(ClassCastException e) {
			System.out.println("The class " + this.toString() + " expects an LSPWithOffers and not any other implementation of LSP");
			System.exit(1);
		}
	}

	@Override
	public LSPDecorator getLSP() {
		return lsp;
	}
		
	@Override
	public Collection<LogisticsSolutionElement> getSolutionElements() {
		return  solutionElements;
	}

	@Override
	public Collection<LSPShipment> getShipments() {
		return shipments;
	}

	@Override
	public void assignShipment(LSPShipment shipment) {
		shipments.add(shipment);			
	}
	
	private LogisticsSolutionElement getFirstElement(){
		for(LogisticsSolutionElement element : solutionElements){
			if(element.getPreviousElement() == null){
				return element;
			}
			
		}
		return null;
	}

	@Override
	public Collection<Info> getInfos() {
		return solutionInfos;
	}

	@Override
	public Collection<EventHandler> getEventHandlers() {
		return eventHandlers;
	}

	@Override
	public void addSimulationTracker( LSPSimulationTracker tracker ) {
		this.trackers.add(tracker);
		this.eventHandlers.addAll(tracker.getEventHandlers());
		this.solutionInfos.addAll(tracker.getInfos());
	}

	@Override
	public Collection<LSPSimulationTracker> getSimulationTrackers() {
		return trackers;
	}
	
	@Override
	public void setEventsManager(EventsManager eventsManager) {
		this.eventsManager = eventsManager;
	}

	@Override
	public Offer getOffer(DemandObject object, String type) {
		return offerFactory.makeOffer(object, type);
	}

	@Override
	public void setOfferFactory(OfferFactory factory) {
		this.offerFactory = factory;
		this.offerFactory.setLogisticsSolution(this);
		this.offerFactory.setLSP(lsp);	
	}

	@Override
	public OfferFactory getOfferFactory() {
		return offerFactory;
	}
	
}
