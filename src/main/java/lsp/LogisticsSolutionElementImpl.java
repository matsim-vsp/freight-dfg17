package lsp;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.handler.EventHandler;

import lsp.functions.LSPInfo;
import lsp.resources.LSPResource;
import lsp.controler.LSPSimulationTracker;



/* package-private */ class LogisticsSolutionElementImpl implements LogisticsSolutionElement {

	private Id<LogisticsSolutionElement>id;
	//die beiden nicht im Builder. Die koennen erst in der Solution als ganzes gesetzt werden
	private LogisticsSolutionElement previousElement;
	private LogisticsSolutionElement nextElement;
	private LSPResource resource;
	private WaitingShipments incomingShipments;
	private WaitingShipments outgoingShipments;
	private LogisticsSolution solution;
	private Collection<LSPInfo> infos;
	private Collection<LSPSimulationTracker> trackers;
	private Collection<EventHandler> handlers;
//	private EventsManager eventsManager;

	LogisticsSolutionElementImpl( LSPUtils.LogisticsSolutionElementBuilder builder ){
		this.id = builder.id;
		this.resource = builder.resource;
		this.incomingShipments = builder.incomingShipments;
		this.outgoingShipments = builder.outgoingShipments;
		resource.getClientElements().add(this);
		this.handlers = new ArrayList<>();
		this.infos = new ArrayList<>();
		this.trackers = new ArrayList<>();
	}
	
	@Override
	public Id<LogisticsSolutionElement> getId() {
		return id;
	}

	@Override
	public void setPreviousElement(LogisticsSolutionElement element) {
		this.previousElement = element;
	}

	@Override
	public void setNextElement(LogisticsSolutionElement element) {
		this.nextElement = element;
		//TODO KMT Dez21: add some functionality like
		// * element.setPreviousElement(this); ->
		// * rename Method to connectWithNextElement and co the bi-directional connection
		// * and remove "setPreviousElement
	}

	@Override
	public LSPResource getResource() {
		return resource;
	}

	@Override
	public WaitingShipments getIncomingShipments() {
		return incomingShipments;
	}

	@Override
	public WaitingShipments getOutgoingShipments() {
		return outgoingShipments;
	}

//	@Override
//	//KMT, KN: Never Used? -- Wäre wenn eher eh was für eine Utilsklasse. (ggf. mit anderem Namen). Frage: gedoppelt mit Scheduler?
//	public void schedulingOfResourceCompleted() {
//		for( ShipmentWithTime tuple : outgoingShipments.getSortedShipments()){
//			nextElement.getIncomingShipments().addShipment(tuple.getTime(), tuple.getShipment());
//		}
//	}

	@Override
	public void setLogisticsSolution(LogisticsSolution solution) {
		this.solution = solution;
	}

	@Override
	public LogisticsSolution getLogisticsSolution() {
		return solution;
	}

	@Override
	public LogisticsSolutionElement getPreviousElement() {
		return previousElement;
	}

	@Override
	public LogisticsSolutionElement getNextElement() {
		return nextElement;
	}

	@Override
	public void addSimulationTracker( LSPSimulationTracker tracker ) {
		trackers.add(tracker);
		infos.addAll(tracker.getInfos());
		handlers.addAll(tracker.getEventHandlers());
	}

	@Override
	public Collection<LSPInfo> getInfos() {
		return infos;
	}

	public Collection<EventHandler> getEventHandlers(){
		return handlers;
	}

	@Override
	public Collection<LSPSimulationTracker> getSimulationTrackers() {
		return trackers;
	}

//	@Override
//	public void setEventsManager(EventsManager eventsManager) {
//		this.eventsManager = eventsManager;
//	}
	

}
