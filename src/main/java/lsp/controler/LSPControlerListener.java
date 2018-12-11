package lsp.controler;


import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.controler.events.ScoringEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ReplanningListener;
import org.matsim.core.controler.listener.ScoringListener;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.handler.EventHandler;

import lsp.functions.Info;
import lsp.LSP;
import lsp.LSPPlan;
import lsp.LSPs;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.events.EventCreator;
import lsp.mobsim.CarrierResourceTracker;
import lsp.replanning.LSPReplanningModule;
import lsp.resources.CarrierResource;
import lsp.resources.Resource;
import lsp.scoring.LSPScoringModule;
import lsp.shipment.LSPShipment;
import lsp.tracking.SimulationTracker;



public class LSPControlerListener implements FreightControlerListener, BeforeMobsimListener, AfterMobsimListener, ScoringListener,
ReplanningListener, IterationEndsListener, IterationStartsListener{

	
	private CarrierResourceTracker carrierResourceTracker;
	private Collection<CarrierResource> carrierResources;    
	private LSPs lsps;
	private LSPReplanningModule replanningModule;
	private LSPScoringModule scoringModule;
	private Collection<EventCreator> creators;
	private EventsManager freightEventsManager;
	private Collection <EventHandler> registeredHandlers;
	
	
	@Inject EventsManager eventsManager;
	@Inject Network network;

	
	@Inject
	protected LSPControlerListener(LSPs lsps, LSPReplanningModule replanningModule, LSPScoringModule scoringModule, Collection<EventCreator> creators) {
	        this.lsps = lsps;
	        this.replanningModule = replanningModule;
	        this.scoringModule = scoringModule;
	        this.creators = creators;
	        this.carrierResources = getResources();
	        this.freightEventsManager = EventsUtils.createEventsManager();
	}
	
	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		
		LSPRescheduler rescheduler = new LSPRescheduler(lsps);
		rescheduler.notifyBeforeMobsim(event);
		
		carrierResourceTracker = new CarrierResourceTracker(carrierResources, network, creators, freightEventsManager);
		freightEventsManager = carrierResourceTracker.getEventsManager();
		eventsManager.addHandler(carrierResourceTracker);
		registeredHandlers = new ArrayList<EventHandler>();
		 
		
		
		for(LSP lsp : lsps.getLSPs().values()) {
			for(LSPShipment shipment : lsp.getShipments()) {
				for(EventHandler handler : shipment.getEventHandlers()) {
					freightEventsManager.addHandler(handler);
					registeredHandlers.add(handler);
				}
			}
			LSPPlan selectedPlan = lsp.getSelectedPlan();
				for(Resource resource : lsp.getResources()) {
					resource.setEventsManager(freightEventsManager);
				}		
				for(LogisticsSolution solution : selectedPlan.getSolutions()) {
					for(EventHandler handler : solution.getEventHandlers()) {
						freightEventsManager.addHandler(handler);
						registeredHandlers.add(handler);
					}
					for(LogisticsSolutionElement element : solution.getSolutionElements()) {
						for(EventHandler handler : element.getEventHandlers()) {
							freightEventsManager.addHandler(handler);
							registeredHandlers.add(handler);
						}	
						ArrayList <EventHandler> resourceHandlers = (ArrayList<EventHandler>)element.getResource().getEventHandlers();
							for(EventHandler handler : resourceHandlers) {
								if(!registeredHandlers.contains(handler)) {
									freightEventsManager.addHandler(handler);
									registeredHandlers.add(handler);
								}
							}
						}
					}		
			}
	}
	
	
	@Override
	public void notifyReplanning(ReplanningEvent event) {
		replanningModule.replanLSPs(event);	
	}

	@Override
	public void notifyScoring(ScoringEvent event) {
		scoringModule.scoreLSPs(event);	
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		eventsManager.removeHandler(carrierResourceTracker);
		
		ArrayList<SimulationTracker> alreadyUpdatedTrackers = new ArrayList<SimulationTracker>();
		for(LSP lsp : lsps.getLSPs().values()) {
			for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
				for(LogisticsSolutionElement element : solution.getSolutionElements()) {
					for(SimulationTracker tracker : element.getResource().getSimulationTrackers()) {
						if(!alreadyUpdatedTrackers.contains(tracker)) {
							tracker.notifyAfterMobsim(event);
							alreadyUpdatedTrackers.add(tracker);
						}
					}
					for(SimulationTracker tracker : element.getSimulationTrackers()) {
						tracker.notifyAfterMobsim(event);
					}
				}
				for(SimulationTracker tracker : solution.getSimulationTrackers()) {
					tracker.notifyAfterMobsim(event);
				}
			}
		}
	
		for(LSP lsp : lsps.getLSPs().values()) {
			for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
				for(LogisticsSolutionElement element : solution.getSolutionElements()) {
					for(Info info : element.getInfos()) {
						info.update();
					}
				}
				for(Info info : solution.getInfos()) {
					info.update();
				}			
			}
		}
	}

	
	private Collection<CarrierResource> getResources() {
		ArrayList<CarrierResource> resources = new ArrayList<>();
		for(LSP lsp : lsps.getLSPs().values()) {
			for(Resource resource : lsp.getResources()) {
				if(resource instanceof CarrierResource) {
					resources.add((CarrierResource) resource);
				}
			}
		}
		return resources;
	}

	public void processEvent(Event event){
		   eventsManager.processEvent(event);
	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		for(EventHandler handler : registeredHandlers) {
			freightEventsManager.removeHandler(handler);
		}

		for(LSP lsp : lsps.getLSPs().values()) {
			for(LSPShipment shipment : lsp.getShipments()) {
				shipment.getEventHandlers().clear();
			}
		
			for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
				for(EventHandler handler : solution.getEventHandlers()) {
					handler.reset(event.getIteration());
				}
				for(SimulationTracker tracker : solution.getSimulationTrackers()) {
					tracker.reset();
				}			
				for(LogisticsSolutionElement element : solution.getSolutionElements()) {
					for(EventHandler handler : element.getEventHandlers()) {
						handler.reset(event.getIteration());
					}
					for(SimulationTracker tracker : element.getSimulationTrackers()) {
						tracker.reset();
					}
					for(EventHandler handler : element.getResource().getEventHandlers()) {
						handler.reset(event.getIteration());
					}		
					for(SimulationTracker tracker : element.getResource().getSimulationTrackers()) {
						tracker.reset();
					}
				}			
			}		
		}		
		
	}

	public CarrierResourceTracker getCarrierResourceTracker() {
		return carrierResourceTracker;
	}

	public EventsManager getFreightEventsManager() {
		return freightEventsManager;
	}
	
	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		/*if(event.getIteration() > 0) {
			for(EventHandler handler : registeredHandlers) {
				freightEventsManager.removeHandler(handler);
			}

			for(LSP lsp : lsps.getLSPs().values()) {
				for(LSPShipment shipment : lsp.getShipments()) {
					shipment.getEventHandlers().clear();
				}
			
				for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
					for(EventHandler handler : solution.getEventHandlers()) {
						handler.reset(event.getIteration());
					}
					for(SimulationTracker tracker : solution.getSimulationTrackers()) {
						tracker.reset();
					}			
					for(LogisticsSolutionElement element : solution.getSolutionElements()) {
						for(EventHandler handler : element.getEventHandlers()) {
							handler.reset(event.getIteration());
						}
						for(SimulationTracker tracker : element.getSimulationTrackers()) {
							tracker.reset();
						}
						for(EventHandler handler : element.getResource().getEventHandlers()) {
							handler.reset(event.getIteration());
						}		
						for(SimulationTracker tracker : element.getResource().getSimulationTrackers()) {
							tracker.reset();
						}
					}			
				}		
			}			
		}*/	
	}	
}
