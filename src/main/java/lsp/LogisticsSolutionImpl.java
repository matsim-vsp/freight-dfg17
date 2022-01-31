package lsp;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.handler.EventHandler;

import lsp.functions.LSPInfo;
import lsp.shipment.LSPShipment;
import lsp.controler.LSPSimulationTracker;

/* package-private */ class LogisticsSolutionImpl implements LogisticsSolution {

	
	private final Id<LogisticsSolution> id;
	private LSP lsp;
	private final Collection<LogisticsSolutionElement> solutionElements;
	private final Collection<LSPShipment> shipments;
	private final Collection<LSPInfo> solutionInfos;
	private final Collection<EventHandler> eventHandlers;
	private final Collection<LSPSimulationTracker>trackers;


	LogisticsSolutionImpl( LSPUtils.LogisticsSolutionBuilder builder ){
		this.id = builder.id;
		this.solutionElements = builder.elements;
		for(LogisticsSolutionElement element : this.solutionElements) {
			element.setLogisticsSolution(this);
		}
		this.shipments = new ArrayList<>();
		this.solutionInfos = builder.solutionInfos;
		this.eventHandlers = builder.eventHandlers; 
		this.trackers = builder.trackers;
	}
	
	
	@Override
	public Id<LogisticsSolution> getId() {
		return id;
	}

	@Override
	public void setLSP(LSP lsp) {
		this.lsp = lsp;
	}

	@Override
	public LSP getLSP() {
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
	
	@Override
	public Collection<LSPInfo> getInfos() {
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
	}

}	
