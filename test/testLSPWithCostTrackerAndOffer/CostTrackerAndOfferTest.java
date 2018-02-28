package testLSPWithCostTrackerAndOffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierCapabilities;
import org.matsim.contrib.freight.carrier.CarrierImpl;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.CarrierVehicleType;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.contrib.freight.carrier.CarrierCapabilities.FleetSize;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LSPPlanDecorator;
import demand.decoratedLSP.LSPPlanWithOfferTransferrer;
import demand.decoratedLSP.LSPWithOffers;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.decoratedLSP.LogisticsSolutionWithOffers;
import demand.mutualReplanning.LSPWithOffersReplanner;
import demand.offer.Offer;
import demand.offer.OfferTransferrerImpl;
import demand.offer.OfferUpdater;
import demand.offer.OfferUpdaterImpl;
import lsp.controler.LSPModule;
import lsp.functions.Info;
import lsp.functions.InfoFunctionValue;
import lsp.LSP;
import lsp.LSPImpl;
import lsp.LSPPlan;
import lsp.LSPPlanImpl;
import lsp.LSPs;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.LogisticsSolutionElementImpl;
import lsp.LogisticsSolutionImpl;
import lsp.ShipmentAssigner;
import lsp.SolutionScheduler;
import lsp.replanning.LSPReplanningModuleImpl;
import lsp.resources.Resource;
import lsp.scoring.LSPScoringModuleImpl;
import lsp.shipment.LSPShipment;
import lsp.shipment.LSPShipmentImpl;
import lsp.usecase.CollectionCarrierAdapter;
import lsp.usecase.CollectionCarrierScheduler;
import lsp.usecase.DeterministicShipmentAssigner;
import lsp.usecase.SimpleForwardSolutionScheduler;

public class CostTrackerAndOfferTest {

	private Network network;
	private LSPDecorator offerLSP;
	private Carrier carrier;
	private Resource collectionAdapter;
	private LogisticsSolutionElement collectionElement;
	private LogisticsSolutionDecorator collectionSolution;
	private LinearOfferFactoryImpl offerFactory;
	private LinearCostTracker tracker;
	private LinearOfferVisitor linearVisitor;
	private LSPPlanDecorator collectionPlan;

	@Before
	public void initialize() {
		Config config = new Config();
		config.addCoreModules();
		Scenario scenario = ScenarioUtils.createScenario(config);
		new MatsimNetworkReader(scenario.getNetwork()).readFile("input\\lsp\\network\\2regions.xml");
		this.network = scenario.getNetwork();

		CollectionCarrierScheduler scheduler = new CollectionCarrierScheduler();
		Id<Carrier> carrierId = Id.create("CollectionCarrier", Carrier.class);
		Id<VehicleType> vehicleTypeId = Id.create("CollectionCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder vehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(vehicleTypeId);
		vehicleTypeBuilder.setCapacity(10);
		vehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
		vehicleTypeBuilder.setCostPerTimeUnit(0.38);
		vehicleTypeBuilder.setFixCost(49);
		vehicleTypeBuilder.setMaxVelocity(50 / 3.6);
		CarrierVehicleType collectionType = vehicleTypeBuilder.build();

		Id<Link> collectionLinkId = Id.createLinkId("(4 2) (4 3)");
		Link collectionLink = network.getLinks().get(collectionLinkId);

		Id<Vehicle> vollectionVehicleId = Id.createVehicleId("CollectionVehicle");
		CarrierVehicle carrierVehicle = CarrierVehicle.newInstance(vollectionVehicleId, collectionLink.getId());
		carrierVehicle.setVehicleType(collectionType);

		CarrierCapabilities.Builder capabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		capabilitiesBuilder.addType(collectionType);
		capabilitiesBuilder.addVehicle(carrierVehicle);
		capabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities capabilities = capabilitiesBuilder.build();
		carrier = CarrierImpl.newInstance(carrierId);
		carrier.setCarrierCapabilities(capabilities);

		Id<Resource> adapterId = Id.create("CollectionCarrierAdapter", Resource.class);
		CollectionCarrierAdapter.Builder adapterBuilder = CollectionCarrierAdapter.Builder.newInstance(adapterId,
				network);
		adapterBuilder.setCollectionScheduler(scheduler);
		adapterBuilder.setCarrier(carrier);
		adapterBuilder.setLocationLinkId(collectionLinkId);
		collectionAdapter = adapterBuilder.build();

		Id<LogisticsSolutionElement> elementId = Id.create("CollectionElement", LogisticsSolutionElement.class);
		LogisticsSolutionElementImpl.Builder collectionElementBuilder = LogisticsSolutionElementImpl.Builder
				.newInstance(elementId);
		collectionElementBuilder.setResource(collectionAdapter);
		collectionElement = collectionElementBuilder.build();

		Id<LogisticsSolution> collectionSolutionId = Id.create("CollectionSolution", LogisticsSolution.class);
		LogisticsSolutionWithOffers.Builder collectionSolutionBuilder = LogisticsSolutionWithOffers.Builder
				.newInstance(collectionSolutionId);
		collectionSolutionBuilder.addSolutionElement(collectionElement);
		collectionSolution = collectionSolutionBuilder.build();

		tracker = new LinearCostTracker(0.2);
		tracker.getEventHandlers().add(new TourStartHandler());
		tracker.getEventHandlers().add(new CollectionServiceHandler());
		tracker.getEventHandlers().add(new DistanceAndTimeHandler(network));

		collectionSolution.addSimulationTracker(tracker);

		offerFactory = new LinearOfferFactoryImpl(collectionSolution);
		collectionSolution.setOfferFactory(offerFactory);

		collectionPlan = new LSPPlanWithOfferTransferrer();
		collectionPlan.addSolution(collectionSolution);
		collectionPlan.setOfferTransferrer(new OfferTransferrerImpl());

		LSPWithOffers.Builder collectionLSPBuilder = LSPWithOffers.Builder.getInstance();
		collectionLSPBuilder.setInitialPlan(collectionPlan);
		Id<LSP> collectionLSPId = Id.create("CollectionLSP", LSP.class);
		collectionLSPBuilder.setId(collectionLSPId);
		ArrayList<Resource> resourcesList = new ArrayList<Resource>();
		resourcesList.add(collectionAdapter);

		SolutionScheduler simpleScheduler = new SimpleForwardSolutionScheduler(resourcesList);
		collectionLSPBuilder.setSolutionScheduler(simpleScheduler);
		offerLSP = collectionLSPBuilder.build();

		ArrayList<Link> linkList = new ArrayList<Link>(network.getLinks().values());
		Id<Link> toLinkId = collectionLinkId;

		for (int i = 1; i < 21; i++) {
			Id<LSPShipment> id = Id.create(i, LSPShipment.class);
			LSPShipmentImpl.Builder builder = LSPShipmentImpl.Builder.newInstance(id);
			Random random = new Random(1);
			int capacityDemand = random.nextInt(4);
			builder.setCapacityDemand(capacityDemand);

			while (true) {
				Collections.shuffle(linkList, random);
				Link pendingFromLink = linkList.get(0);
				if (pendingFromLink.getFromNode().getCoord().getX() <= 4000
						&& pendingFromLink.getFromNode().getCoord().getY() <= 4000
						&& pendingFromLink.getToNode().getCoord().getX() <= 4000
						&& pendingFromLink.getToNode().getCoord().getY() <= 4000) {
					builder.setFromLinkId(pendingFromLink.getId());
					break;
				}
			}

			builder.setToLinkId(toLinkId);
			TimeWindow endTimeWindow = TimeWindow.newInstance(0, (24 * 3600));
			builder.setEndTimeWindow(endTimeWindow);
			TimeWindow startTimeWindow = TimeWindow.newInstance(0, (24 * 3600));
			builder.setStartTimeWindow(startTimeWindow);
			builder.setServiceTime(capacityDemand * 60);
			LSPShipment shipment = builder.build();
			offerLSP.getShipments().add(shipment);
		}

		LSPWithOffersReplanner replanner = new LSPWithOffersReplanner(offerLSP);
		offerLSP.setReplanner(replanner);

		OfferUpdater updater = new OfferUpdaterImpl();
		offerLSP.setOfferUpdater(updater);
		replanner.setOfferUpdater(updater);
		linearVisitor = new LinearOfferVisitor(collectionSolution);
		updater.getOfferVisitors().add(linearVisitor);
		
		//offerLSP.scheduleSoultions();
		
		ArrayList<LSP> lspList = new ArrayList<LSP>();
		lspList.add(offerLSP);
		LSPs lsps = new LSPs(lspList);

		Controler controler = new Controler(config);

		CostTrackerTestModule module = new CostTrackerTestModule(lsps, new LSPReplanningModuleImpl(lsps), new LSPScoringModuleImpl(lsps));

		controler.addOverridingModule(module);
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(3);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		config.network().setInputFile("input\\lsp\\network\\2regions.xml");
		controler.run();
	}

	@Test
	public void testOfferUpdater() {
		for (Info trackerInfo : collectionSolution.getInfos()) {
			for (InfoFunctionValue value : trackerInfo.getFunction().getValues()) {
				System.out.println(value.getName() + " " + value.getValue().toString());
			}
		}
		assertTrue(tracker == collectionSolution.getSimulationTrackers().iterator().next());
		int numberOfHandlers = 0;
		for (EventHandler solutionHandler : collectionSolution.getEventHandlers()) {
			for (EventHandler trackerHandler : tracker.getEventHandlers()) {
				if (solutionHandler == trackerHandler) {
					numberOfHandlers++;
				}
			}
		}
		assertTrue(numberOfHandlers == tracker.getEventHandlers().size());

		for (Info solutionInfo : collectionSolution.getInfos()) {
			for (Info trackerInfo : tracker.getInfos()) {
				assertTrue(solutionInfo == trackerInfo);
			}
		}

		Offer offer = offerFactory.makeOffer(null, "linear");
		assertTrue(offerFactory.getLogisticsSolution() == collectionSolution);
		assertTrue(offerFactory.getLogisticsSolution().getLSP() == collectionSolution.getLSP());
		assertTrue(offerFactory.getLogisticsSolution().getLSP() == offerLSP);
		assertTrue(offerFactory.getLSP() == offerLSP);
		assertTrue(offer instanceof LinearOffer);
		LinearOffer linearOffer = (LinearOffer) offer;
		assertTrue(linearOffer.getSolution() == collectionSolution);
		assertTrue(linearOffer.getSolution().getLSP() == collectionSolution.getLSP());
		assertTrue(linearOffer.getSolution().getLSP() == offerLSP);
		assertTrue(linearOffer.getLsp() == offerLSP);

		for (Info trackerInfo : collectionSolution.getInfos()) {
			for (InfoFunctionValue value : trackerInfo.getFunction().getValues()) {
				if (value.getName() == "linear") {
					assertEquals(Double.parseDouble(value.getValue()), linearOffer.getLinear(), 0.0001);
				}
				if (value.getName() == "fixed") {
					assertEquals(Double.parseDouble(value.getValue()), linearOffer.getFix(), 0.1);
				}
			}
		}
	}

}
