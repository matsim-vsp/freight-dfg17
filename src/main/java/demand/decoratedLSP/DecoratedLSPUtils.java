package demand.decoratedLSP;

import demand.offer.OfferFactory;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.controler.LSPSimulationTracker;
import lsp.functions.LSPInfo;
import org.matsim.api.core.v01.Id;
import org.matsim.core.events.handler.EventHandler;

import java.util.ArrayList;
import java.util.Collection;

public class DecoratedLSPUtils {

	public static class LogisticsSolutionDecoratorImpl_wOffersBuilder {
		private final Id<LogisticsSolution> id;
		private final Collection<LogisticsSolutionElement> elements;
		private final Collection<LSPInfo> solutionInfos;
		private final Collection<EventHandler> eventHandlers;
		private final Collection<LSPSimulationTracker>trackers;
		private OfferFactory offerFactory;

		public static LogisticsSolutionDecoratorImpl_wOffersBuilder newInstance(Id<LogisticsSolution>id){
			return new LogisticsSolutionDecoratorImpl_wOffersBuilder(id);
		}

		private LogisticsSolutionDecoratorImpl_wOffersBuilder(Id<LogisticsSolution> id){
			this.elements = new ArrayList<LogisticsSolutionElement>();
			this.solutionInfos = new ArrayList<LSPInfo>();
			this.eventHandlers = new ArrayList<EventHandler>();
			this.trackers = new ArrayList<LSPSimulationTracker>();
			this.id = id;
		}

		public LogisticsSolutionDecoratorImpl_wOffersBuilder addSolutionElement(LogisticsSolutionElement element){
			elements.add(element);
			return this;
		}

		public LogisticsSolutionDecoratorImpl_wOffersBuilder addInfo(LSPInfo info) {
			solutionInfos.add(info);
			return this;
		}

		public LogisticsSolutionDecoratorImpl_wOffersBuilder addEventHandler(EventHandler handler) {
			eventHandlers.add(handler);
			return this;
		}

		public LogisticsSolutionDecoratorImpl_wOffersBuilder addTracker( LSPSimulationTracker tracker ) {
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

		Collection<LSPInfo> getSolutionInfos() {
			return solutionInfos;
		}

		Collection<EventHandler> getEventHandlers() {
			return eventHandlers;
		}

		Collection<LSPSimulationTracker> getTrackers() {
			return trackers;
		}

		OfferFactory getOfferFactory() {
			return offerFactory;
		}
	}
}
