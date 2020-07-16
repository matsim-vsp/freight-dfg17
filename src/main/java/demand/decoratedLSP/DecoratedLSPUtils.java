package demand.decoratedLSP;

import demand.offer.OfferFactory;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.controler.SimulationTracker;
import lsp.functions.Info;
import org.matsim.api.core.v01.Id;
import org.matsim.core.events.handler.EventHandler;

import java.util.ArrayList;
import java.util.Collection;

public class DecoratedLSPUtils {

	public static class LogisticsSolutionDecoratorImpl_wOffersBuilder {
		private Id<LogisticsSolution> id;
		private Collection<LogisticsSolutionElement> elements;
		private Collection<Info> solutionInfos;
		private Collection<EventHandler> eventHandlers;
		private Collection<SimulationTracker>trackers;
		private OfferFactory offerFactory;

		public static LogisticsSolutionDecoratorImpl_wOffersBuilder newInstance(Id<LogisticsSolution>id){
			return new LogisticsSolutionDecoratorImpl_wOffersBuilder(id);
		}

		private LogisticsSolutionDecoratorImpl_wOffersBuilder(Id<LogisticsSolution> id){
			this.elements = new ArrayList<LogisticsSolutionElement>();
			this.solutionInfos = new ArrayList<Info>();
			this.eventHandlers = new ArrayList<EventHandler>();
			this.trackers = new ArrayList<SimulationTracker>();
			this.id = id;
		}

		public LogisticsSolutionDecoratorImpl_wOffersBuilder addSolutionElement(LogisticsSolutionElement element){
			elements.add(element);
			return this;
		}

		public LogisticsSolutionDecoratorImpl_wOffersBuilder addInfo(Info info) {
			solutionInfos.add(info);
			return this;
		}

		public LogisticsSolutionDecoratorImpl_wOffersBuilder addEventHandler(EventHandler handler) {
			eventHandlers.add(handler);
			return this;
		}

		public LogisticsSolutionDecoratorImpl_wOffersBuilder addTracker(SimulationTracker tracker) {
			trackers.add(tracker);
			return this;
		}

		public LogisticsSolutionDecoratorImpl_wOffersBuilder addOfferFactory(OfferFactory offerFactory) {
			this.offerFactory = offerFactory;
			return this;
		}

		public LogisticsSolutionDecoratorImpl_wOffers build(){
			return new LogisticsSolutionDecoratorImpl_wOffers(this);
		}


		// --- Getters ---
		Id<LogisticsSolution> getId() {
			return id;
		}

		Collection<LogisticsSolutionElement> getElements() {
			return elements;
		}

		Collection<Info> getSolutionInfos() {
			return solutionInfos;
		}

		Collection<EventHandler> getEventHandlers() {
			return eventHandlers;
		}

		Collection<SimulationTracker> getTrackers() {
			return trackers;
		}

		OfferFactory getOfferFactory() {
			return offerFactory;
		}
	}
}
