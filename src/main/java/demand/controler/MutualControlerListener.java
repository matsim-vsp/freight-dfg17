package demand.controler;

import demand.demandObject.DemandObjects;
import demand.mutualReplanning.MutualReplanningModule;
import demand.scoring.MutualScoringModule;
import lsp.LSP;
import lsp.LSPPlan;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.controler.LSPSimulationTracker;
import lsp.functions.LSPInfo;
import lsp.resources.LSPCarrierResource;
import lsp.shipment.LSPShipment;
import org.matsim.api.core.v01.events.Event;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.controler.CarrierAgentTracker;
import org.matsim.contrib.freight.events.eventsCreator.LSPEventCreator;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.*;
import org.matsim.core.controler.listener.*;
import org.matsim.core.events.handler.EventHandler;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/*package-private*/ class MutualControlerListener implements BeforeMobsimListener, AfterMobsimListener,
		ScoringListener, ReplanningListener, IterationEndsListener, StartupListener, IterationStartsListener {

	private CarrierAgentTracker carrierResourceTracker;
	private final Carriers carriers;
	private final LSPDecorators lsps;
	private final DemandObjects demandObjects;
	private final MutualScoringModule mutualScoringModule;
	private final MutualReplanningModule replanningModule;
	private final Collection<LSPEventCreator> creators;
	private ArrayList<EventHandler> registeredHandlers;

	@Inject	EventsManager eventsManager;

	@Inject
	protected MutualControlerListener(LSPDecorators lsps, DemandObjects demandObjects,
			MutualScoringModule demandScoringModule, MutualReplanningModule replanningModule, Collection<LSPEventCreator> creators) {
		this.lsps = lsps;
		this.demandObjects = demandObjects;
		this.mutualScoringModule = demandScoringModule;
		this.replanningModule = replanningModule;
		this.creators = creators;
		this.carriers = getCarriers();
	}

	
	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {

		SupplyRescheduler rescheduler = new SupplyRescheduler(lsps);
		rescheduler.notifyBeforeMobsim(event);
		
		carrierResourceTracker = new CarrierAgentTracker(carriers, creators, eventsManager );
		eventsManager.addHandler(carrierResourceTracker);
		registeredHandlers = new ArrayList<>();

		for (LSP lsp : lsps.getLSPs().values()) {
			for (LSPShipment shipment : lsp.getShipments()) {
				for (EventHandler handler : shipment.getEventHandlers()) {
					eventsManager.addHandler(handler);
				}
			}
			LSPPlan selectedPlan = lsp.getSelectedPlan();
			for (LogisticsSolution solution : selectedPlan.getSolutions()) {
				for (EventHandler handler : solution.getEventHandlers()) {
					eventsManager.addHandler(handler);
				}
				for (LogisticsSolutionElement element : solution.getSolutionElements()) {
					for (EventHandler handler : element.getEventHandlers()) {
						eventsManager.addHandler(handler);
					}
					ArrayList<EventHandler> resourceHandlers = (ArrayList<EventHandler>) element.getResource().getEventHandlers();
					for (EventHandler handler : resourceHandlers) {
						if (!registeredHandlers.contains(handler)) {
							eventsManager.addHandler(handler);
							registeredHandlers.add(handler);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		SupplyClearer supplyClearer = new SupplyClearer(lsps);
		supplyClearer.notifyIterationEnds(event);
	}

	@Override
	public void notifyReplanning(ReplanningEvent event) {
		replanningModule.replan(event);
	}

	@Override
	public void notifyScoring(ScoringEvent event) {
		mutualScoringModule.scoreLSPs(event);
		mutualScoringModule.scoreDemandObjects(event);
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		eventsManager.removeHandler(carrierResourceTracker);

		ArrayList<LSPSimulationTracker> alreadyUpdatedTrackers = new ArrayList<>();
		for (LSP lsp : lsps.getLSPs().values()) {
			for (LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
				for (LogisticsSolutionElement element : solution.getSolutionElements()) {
					for ( LSPSimulationTracker tracker : element.getResource().getSimulationTrackers()) {
						if (!alreadyUpdatedTrackers.contains(tracker)) {
							tracker.notifyAfterMobsim(event);
							alreadyUpdatedTrackers.add(tracker);
						}
					}
					for ( LSPSimulationTracker tracker : element.getSimulationTrackers()) {
						tracker.notifyAfterMobsim(event);
					}
				}
				for ( LSPSimulationTracker tracker : solution.getSimulationTrackers()) {
					tracker.notifyAfterMobsim(event);
				}
			}
		}

		for (LSP lsp : lsps.getLSPs().values()) {
			for (LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
				for (LogisticsSolutionElement element : solution.getSolutionElements()) {
					for (LSPInfo info : element.getInfos()) {
						info.update();
					}
				}
				for (LSPInfo info : solution.getInfos()) {
					info.update();
				}
			}
		}

	}

	
	public void processEvent(Event event) {
		eventsManager.processEvent(event);
	}

	private Carriers getCarriers() {
		Carriers carriers = new Carriers();
		for (LSP lsp : lsps.getLSPs().values()) {
			LSPPlan selectedPlan = lsp.getSelectedPlan();
			for (LogisticsSolution solution : selectedPlan.getSolutions()) {
				for (LogisticsSolutionElement element : solution.getSolutionElements()) {
					if (element.getResource() instanceof LSPCarrierResource) {

						LSPCarrierResource carrierResource = (LSPCarrierResource) element.getResource();
						Carrier carrier = carrierResource.getCarrier();
						if (!carriers.getCarriers().containsKey(carrier.getId())) {
							carriers.addCarrier(carrier);
						}
					}
				}
			}
		}
		return carriers;
	}

	@Override
	public void notifyStartup(StartupEvent event) {
		InitialDemandAssigner initialAssigner = new InitialDemandAssigner(demandObjects, lsps);
		initialAssigner.notifyStartup(event);
	}

	public CarrierAgentTracker getCarrierResourceTracker() {
		return carrierResourceTracker;
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		if(event.getIteration() > 0) {
			for(EventHandler handler : registeredHandlers) {
				eventsManager.removeHandler(handler);
			}
		
			for(LSP lsp : lsps.getLSPs().values()) {
				for(LSPShipment shipment : lsp.getShipments()) {
					shipment.getEventHandlers().clear();
				}
			
				for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
					for(EventHandler handler : solution.getEventHandlers()) {
						handler.reset(event.getIteration());
					}
					for( LSPSimulationTracker tracker : solution.getSimulationTrackers()) {
						tracker.reset();
					}			
					for(LogisticsSolutionElement element : solution.getSolutionElements()) {
						for(EventHandler handler : element.getEventHandlers()) {
							handler.reset(event.getIteration());
						}
						for( LSPSimulationTracker tracker : element.getSimulationTrackers()) {
							tracker.reset();
						}
						for(EventHandler handler : element.getResource().getEventHandlers()) {
							handler.reset(event.getIteration());
						}		
						for( LSPSimulationTracker tracker : element.getResource().getSimulationTrackers()) {
							tracker.reset();
						}
					}			
				}		
			}			
		}	
		
	}
	
}
