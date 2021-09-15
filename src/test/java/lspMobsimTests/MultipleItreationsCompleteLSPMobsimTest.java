package lspMobsimTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import lsp.*;
import lsp.replanning.LSPReplanningUtils;
import lsp.scoring.LSPScoringUtils;
import lsp.shipment.*;
import lsp.usecase.*;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.carrier.CarrierCapabilities.FleetSize;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import lsp.controler.LSPModule;
import org.matsim.contrib.freight.events.eventsCreator.LSPEventCreatorUtils;
import lsp.resources.LSPResource;

public class MultipleItreationsCompleteLSPMobsimTest {
	private Network network;
	private LogisticsSolution completeSolution;
	private ShipmentAssigner assigner;
	private LSPPlan completePlan;
	private SolutionScheduler simpleScheduler;
	private LSP completeLSP;	
		
	@Before
	public void initialize() {
		Config config = new Config();
        config.addCoreModules();
        Scenario scenario = ScenarioUtils.createScenario(config);
        new MatsimNetworkReader(scenario.getNetwork()).readFile("scenarios/2regions/2regions-network.xml");
        this.network = scenario.getNetwork();


		Id<Carrier> collectionCarrierId = Id.create("CollectionCarrier", Carrier.class);
		Id<VehicleType> collectionVehicleTypeId = Id.create("CollectionCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder collectionVehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(collectionVehicleTypeId);
		collectionVehicleTypeBuilder.setCapacity(10);
		collectionVehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
		collectionVehicleTypeBuilder.setCostPerTimeUnit(0.38);
		collectionVehicleTypeBuilder.setFixCost(49);
		collectionVehicleTypeBuilder.setMaxVelocity(50/3.6);
		org.matsim.vehicles.VehicleType collectionType = collectionVehicleTypeBuilder.build();
		
		Id<Link> collectionLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> collectionVehicleId = Id.createVehicleId("CollectionVehicle");
		CarrierVehicle collectionCarrierVehicle = CarrierVehicle.newInstance(collectionVehicleId, collectionLinkId);
		collectionCarrierVehicle.setType( collectionType );

		CarrierCapabilities.Builder collectionCapabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		collectionCapabilitiesBuilder.addType(collectionType);
		collectionCapabilitiesBuilder.addVehicle(collectionCarrierVehicle);
		collectionCapabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities collectionCapabilities = collectionCapabilitiesBuilder.build();
		Carrier collectionCarrier = CarrierUtils.createCarrier( collectionCarrierId );
		collectionCarrier.setCarrierCapabilities(collectionCapabilities);
		
		
		Id<LSPResource> collectionAdapterId = Id.create("CollectionCarrierAdapter", LSPResource.class);
		UsecaseUtils.CollectionCarrierAdapterBuilder collectionAdapterBuilder = UsecaseUtils.CollectionCarrierAdapterBuilder.newInstance(collectionAdapterId, network);
		collectionAdapterBuilder.setCollectionScheduler(UsecaseUtils.createDefaultCollectionCarrierScheduler());
		collectionAdapterBuilder.setCarrier(collectionCarrier);
		collectionAdapterBuilder.setLocationLinkId(collectionLinkId);
		LSPResource collectionAdapter = collectionAdapterBuilder.build();
		
		Id<LogisticsSolutionElement> collectionElementId = Id.create("CollectionElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder collectionBuilder = LSPUtils.LogisticsSolutionElementBuilder.newInstance(collectionElementId );
		collectionBuilder.setResource(collectionAdapter);
		LogisticsSolutionElement collectionElement = collectionBuilder.build();
		
		UsecaseUtils.ReloadingPointSchedulerBuilder firstReloadingSchedulerBuilder =  UsecaseUtils.ReloadingPointSchedulerBuilder.newInstance();
        firstReloadingSchedulerBuilder.setCapacityNeedFixed(10);
        firstReloadingSchedulerBuilder.setCapacityNeedLinear(1);
       
        
        Id<LSPResource> firstReloadingId = Id.create("ReloadingPoint1", LSPResource.class);
        Id<Link> firstReloadingLinkId = Id.createLinkId("(4 2) (4 3)");
        
        UsecaseUtils.ReloadingPointBuilder firstReloadingPointBuilder = UsecaseUtils.ReloadingPointBuilder.newInstance(firstReloadingId, firstReloadingLinkId);
		firstReloadingPointBuilder.setReloadingScheduler(firstReloadingSchedulerBuilder.build());
        LSPResource firstReloadingPointAdapter = firstReloadingPointBuilder.build();
        
        Id<LogisticsSolutionElement> firstReloadingElementId = Id.create("FirstReloadElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder firstReloadingElementBuilder = LSPUtils.LogisticsSolutionElementBuilder.newInstance(firstReloadingElementId );
		firstReloadingElementBuilder.setResource(firstReloadingPointAdapter);
		LogisticsSolutionElement firstReloadElement = firstReloadingElementBuilder.build();
		
		Id<Carrier> mainRunCarrierId = Id.create("MainRunCarrier", Carrier.class);
		Id<VehicleType> mainRunVehicleTypeId = Id.create("MainRunCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder mainRunVehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(mainRunVehicleTypeId);
		mainRunVehicleTypeBuilder.setCapacity(30);
		mainRunVehicleTypeBuilder.setCostPerDistanceUnit(0.0002);
		mainRunVehicleTypeBuilder.setCostPerTimeUnit(0.38);
		mainRunVehicleTypeBuilder.setFixCost(120);
		mainRunVehicleTypeBuilder.setMaxVelocity(50/3.6);
		org.matsim.vehicles.VehicleType mainRunType = mainRunVehicleTypeBuilder.build();
				
		
		Id<Link> fromLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> mainRunVehicleId = Id.createVehicleId("MainRunVehicle");
		CarrierVehicle mainRunCarrierVehicle = CarrierVehicle.newInstance(mainRunVehicleId, fromLinkId);
		mainRunCarrierVehicle.setType( mainRunType );


		CarrierCapabilities.Builder mainRunCapabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		mainRunCapabilitiesBuilder.addType(mainRunType);
		mainRunCapabilitiesBuilder.addVehicle(mainRunCarrierVehicle);
		mainRunCapabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities mainRunCapabilities = mainRunCapabilitiesBuilder.build();
		Carrier mainRunCarrier = CarrierUtils.createCarrier( mainRunCarrierId );
		mainRunCarrier.setCarrierCapabilities(mainRunCapabilities);


		Id<LSPResource> mainRunId = Id.create("MainRunAdapter", LSPResource.class);
        UsecaseUtils.MainRunCarrierAdapterBuilder mainRunAdapterBuilder = UsecaseUtils.MainRunCarrierAdapterBuilder.newInstance(mainRunId, network);
        mainRunAdapterBuilder.setMainRunCarrierScheduler(UsecaseUtils.createDefaultMainRunCarrierScheduler());
        mainRunAdapterBuilder.setFromLinkId(Id.createLinkId("(4 2) (4 3)"));
        mainRunAdapterBuilder.setToLinkId(Id.createLinkId("(14 2) (14 3)"));
        mainRunAdapterBuilder.setCarrier(mainRunCarrier);
        LSPResource mainRunAdapter = mainRunAdapterBuilder.build();
	
        Id<LogisticsSolutionElement> mainRunElementId = Id.create("MainRunElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder mainRunBuilder = LSPUtils.LogisticsSolutionElementBuilder.newInstance(mainRunElementId );
		mainRunBuilder.setResource(mainRunAdapter);
		LogisticsSolutionElement mainRunElement = mainRunBuilder.build();
		
		UsecaseUtils.ReloadingPointSchedulerBuilder secondSchedulerBuilder =  UsecaseUtils.ReloadingPointSchedulerBuilder.newInstance();
        secondSchedulerBuilder.setCapacityNeedFixed(10);
        secondSchedulerBuilder.setCapacityNeedLinear(1);

		Id<LSPResource> secondReloadingId = Id.create("ReloadingPoint2", LSPResource.class);
        Id<Link> secondReloadingLinkId = Id.createLinkId("(14 2) (14 3)");
        
        UsecaseUtils.ReloadingPointBuilder secondReloadingPointBuilder = UsecaseUtils.ReloadingPointBuilder.newInstance(secondReloadingId, secondReloadingLinkId);
       
        secondReloadingPointBuilder.setReloadingScheduler(secondSchedulerBuilder.build());
        LSPResource secondReloadingPointAdapter = secondReloadingPointBuilder.build();
        
        Id<LogisticsSolutionElement> secondReloadingElementId = Id.create("SecondReloadElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder secondReloadingElementBuilder = LSPUtils.LogisticsSolutionElementBuilder.newInstance(secondReloadingElementId );
		secondReloadingElementBuilder.setResource(secondReloadingPointAdapter);
		LogisticsSolutionElement secondReloadElement = secondReloadingElementBuilder.build();

		Id<Carrier> distributionCarrierId = Id.create("DistributionCarrier", Carrier.class);
		Id<VehicleType> distributionVehicleTypeId = Id.create("DistributionCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder dsitributionVehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(distributionVehicleTypeId);
		dsitributionVehicleTypeBuilder.setCapacity(10);
		dsitributionVehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
		dsitributionVehicleTypeBuilder.setCostPerTimeUnit(0.38);
		dsitributionVehicleTypeBuilder.setFixCost(49);
		dsitributionVehicleTypeBuilder.setMaxVelocity(50/3.6);
		org.matsim.vehicles.VehicleType distributionType = dsitributionVehicleTypeBuilder.build();
		
		Id<Link> distributionLinkId = Id.createLinkId("(14 2) (14 3)");
		Id<Vehicle> distributionVehicleId = Id.createVehicleId("DistributionVehicle");
		CarrierVehicle distributionCarrierVehicle = CarrierVehicle.newInstance(distributionVehicleId, distributionLinkId);
		distributionCarrierVehicle.setType( distributionType );

		CarrierCapabilities.Builder capabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		capabilitiesBuilder.addType(distributionType);
		capabilitiesBuilder.addVehicle(distributionCarrierVehicle);
		capabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities distributionCapabilities = capabilitiesBuilder.build();
		Carrier carrier = CarrierUtils.createCarrier( distributionCarrierId );
		carrier.setCarrierCapabilities(distributionCapabilities);
		
		
		Id<LSPResource> distributionAdapterId = Id.create("DistributionCarrierAdapter", LSPResource.class);
		UsecaseUtils.DistributionCarrierAdapterBuilder distributionAdapterBuilder = UsecaseUtils.DistributionCarrierAdapterBuilder.newInstance(distributionAdapterId, network);
		distributionAdapterBuilder.setDistributionScheduler(UsecaseUtils.createDefaultDistributionCarrierScheduler());
		distributionAdapterBuilder.setCarrier(carrier);
		distributionAdapterBuilder.setLocationLinkId(distributionLinkId);
		LSPResource distributionAdapter = distributionAdapterBuilder.build();
		
		Id<LogisticsSolutionElement> distributionElementId = Id.create("DistributionElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder distributionBuilder = LSPUtils.LogisticsSolutionElementBuilder.newInstance(distributionElementId );
		distributionBuilder.setResource(distributionAdapter);
		LogisticsSolutionElement distributionElement =    distributionBuilder.build();
		
		collectionElement.setNextElement(firstReloadElement);
		firstReloadElement.setPreviousElement(collectionElement);
		firstReloadElement.setNextElement(mainRunElement);
		mainRunElement.setPreviousElement(firstReloadElement);
		mainRunElement.setNextElement(secondReloadElement);
		secondReloadElement.setPreviousElement(mainRunElement);
		secondReloadElement.setNextElement(distributionElement);
		distributionElement.setPreviousElement(secondReloadElement);
		
		Id<LogisticsSolution> solutionId = Id.create("SolutionId", LogisticsSolution.class);
		LSPUtils.LogisticsSolutionBuilder completeSolutionBuilder = LSPUtils.LogisticsSolutionBuilder.newInstance(solutionId );
		completeSolutionBuilder.addSolutionElement(collectionElement);
		completeSolutionBuilder.addSolutionElement(firstReloadElement);
		completeSolutionBuilder.addSolutionElement(mainRunElement);
		completeSolutionBuilder.addSolutionElement(secondReloadElement);
		completeSolutionBuilder.addSolutionElement(distributionElement);
		completeSolution = completeSolutionBuilder.build();
		
		assigner = UsecaseUtils.createDeterministicShipmentAssigner();
		completePlan = LSPUtils.createLSPPlan();
		completePlan.setAssigner(assigner);
		completePlan.addSolution(completeSolution);
		
		LSPUtils.LSPBuilder completeLSPBuilder = LSPUtils.LSPBuilder.getInstance();
		completeLSPBuilder.setInitialPlan(completePlan);
		Id<LSP> collectionLSPId = Id.create("CollectionLSP", LSP.class);
		completeLSPBuilder.setId(collectionLSPId);
		ArrayList<LSPResource> resourcesList = new ArrayList<LSPResource>();
		resourcesList.add(collectionAdapter);
		resourcesList.add(firstReloadingPointAdapter);
		resourcesList.add(mainRunAdapter);
		resourcesList.add(secondReloadingPointAdapter);
		resourcesList.add(distributionAdapter);


		simpleScheduler = UsecaseUtils.createDefaultSimpleForwardSolutionScheduler(resourcesList);
		simpleScheduler.setBufferTime(300);
		completeLSPBuilder.setSolutionScheduler(simpleScheduler);
		completeLSP = completeLSPBuilder.build();
	
		ArrayList <Link> linkList = new ArrayList<Link>(network.getLinks().values());
		//Random rand = new Random(1);
		int numberOfShipments = new Random().nextInt(50);
		
		for(int i = 1; i < 1 +  numberOfShipments; i++) {
	        	Id<LSPShipment> id = Id.create(i, LSPShipment.class);
	        	ShipmentUtils.LSPShipmentBuilder builder = ShipmentUtils.LSPShipmentBuilder.newInstance(id );
	        	int capacityDemand =  1 + new Random().nextInt(4);
	        	builder.setCapacityDemand(capacityDemand);
	        	
	        	while(true) {
	        		Collections.shuffle(linkList);
	        		Link pendingToLink = linkList.get(0);
	        		if((pendingToLink.getFromNode().getCoord().getX() <= 18000 &&
	        			pendingToLink.getFromNode().getCoord().getY() <= 4000 &&
	        			pendingToLink.getFromNode().getCoord().getX() >= 14000 &&       			
	        			pendingToLink.getToNode().getCoord().getX() <= 18000 &&
	        			pendingToLink.getToNode().getCoord().getY() <= 4000  &&
	        			pendingToLink.getToNode().getCoord().getX() >= 14000	)) {
	        		   builder.setToLinkId(pendingToLink.getId());
	        		   break;	
	        		}
	        	
	        	}
	        	
	        	while(true) {
	        		Collections.shuffle(linkList);
	        		Link pendingFromLink = linkList.get(0);
	        		if(pendingFromLink.getFromNode().getCoord().getX() <= 4000 &&
	        		   pendingFromLink.getFromNode().getCoord().getY() <= 4000 &&
	        		   pendingFromLink.getToNode().getCoord().getX() <= 4000 &&
	        		   pendingFromLink.getToNode().getCoord().getY() <= 4000    ) {
	        		   builder.setFromLinkId(pendingFromLink.getId());
	        		   break;	
	        		}
	        	
	        	}
	        	
	        	TimeWindow endTimeWindow = TimeWindow.newInstance(0,(24*3600));
	        	builder.setEndTimeWindow(endTimeWindow);
	        	TimeWindow startTimeWindow = TimeWindow.newInstance(0,(24*3600));
	        	builder.setStartTimeWindow(startTimeWindow);
	        	builder.setDeliveryServiceTime(capacityDemand * 60 );
	        	LSPShipment shipment = builder.build();
	        	completeLSP.assignShipmentToLSP(shipment);
	        }
		completeLSP.scheduleSoultions();
		
		ArrayList<LSP> lspList = new ArrayList<LSP>();
		lspList.add(completeLSP);
		LSPs lsps = new LSPs(lspList);
		
		Controler controler = new Controler(config);
		
		LSPModule module = new LSPModule(lsps, LSPReplanningUtils.createDefaultLSPReplanningModule(lsps), LSPScoringUtils.createDefaultLSPScoringModule(lsps ), LSPEventCreatorUtils.getStandardEventCreators());

		controler.addOverridingModule(module);
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(1 + new Random().nextInt(10));
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		config.network().setInputFile("scenarios/2regions/2regions-network.xml");
		controler.run();
	}

	@Test
	public void testCompleteLSPMobsim() {
		for(LSPShipment shipment : completeLSP.getShipments()) {
			assertFalse(shipment.getLog().getPlanElements().isEmpty());
			ArrayList<ShipmentPlanElement> scheduleElements = new ArrayList<ShipmentPlanElement>(shipment.getShipmentPlan().getPlanElements().values());
			Collections.sort(scheduleElements, new ShipmentPlanElementComparator());
			ArrayList<ShipmentPlanElement> logElements = new ArrayList<ShipmentPlanElement>(shipment.getLog().getPlanElements().values());
			Collections.sort(logElements, new ShipmentPlanElementComparator());
		
			for(ShipmentPlanElement scheduleElement : scheduleElements){
				ShipmentPlanElement logElement = logElements.get(scheduleElements.indexOf(scheduleElement));
				assertTrue(scheduleElement.getElementType() == logElement.getElementType());
				assertTrue(scheduleElement.getResourceId() == logElement.getResourceId());
				assertTrue(scheduleElement.getSolutionElement() == logElement.getSolutionElement());
				assertEquals(scheduleElement.getStartTime(), logElement.getStartTime(), 300);
			}
		}
	}
}

