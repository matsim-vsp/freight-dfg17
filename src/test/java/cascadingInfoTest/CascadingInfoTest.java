package cascadingInfoTest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import lsp.*;
import lsp.replanning.LSPReplanningUtils;
import lsp.scoring.LSPScoringModulsUtils;
import lsp.shipment.ShipmentUtils;
import lsp.usecase.*;
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
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import lsp.controler.LSPModule;
import org.matsim.contrib.freight.controler.LSPEventUtils;
import lsp.resources.LSPResource;
import lsp.shipment.LSPShipment;


public class CascadingInfoTest {
	private Network network;
	private LSP collectionLSP;	
	private Carrier carrier;
	private LSPResource collectionAdapter;
	private LogisticsSolutionElement collectionElement;
	private LogisticsSolution collectionSolution;
	private AverageTimeInfo elementInfo;
	private AverageTimeInfo solutionInfo;
	private AverageTimeTracker timeTracker;
	
	@Before
	public void initialize() {
		
		Config config = new Config();
        config.addCoreModules();
        Scenario scenario = ScenarioUtils.createScenario(config);
        new MatsimNetworkReader(scenario.getNetwork()).readFile("scenarios/2regions/2regions-network.xml");
        this.network = scenario.getNetwork();

		Id<Carrier> carrierId = Id.create("CollectionCarrier", Carrier.class);
		Id<VehicleType> vehicleTypeId = Id.create("CollectionCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder vehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(vehicleTypeId);
		vehicleTypeBuilder.setCapacity(10);
		vehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
		vehicleTypeBuilder.setCostPerTimeUnit(0.38);
		vehicleTypeBuilder.setFixCost(49);
		vehicleTypeBuilder.setMaxVelocity(50/3.6);
		org.matsim.vehicles.VehicleType collectionType = vehicleTypeBuilder.build();
		
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
		
		
		Id<LSPResource> adapterId = Id.create("CollectionCarrierAdapter", LSPResource.class);
		UsecaseUtils.CollectionCarrierAdapterBuilder adapterBuilder = UsecaseUtils.CollectionCarrierAdapterBuilder.newInstance(adapterId, network);
		adapterBuilder.setCollectionScheduler(UsecaseUtils.createDefaultCollectionCarrierScheduler());
		adapterBuilder.setCarrier(carrier);
		adapterBuilder.setLocationLinkId(collectionLinkId);
		collectionAdapter = adapterBuilder.build();
		timeTracker = new AverageTimeTracker();
		collectionAdapter.addSimulationTracker(timeTracker);
		
		
		Id<LogisticsSolutionElement> elementId = Id.create("CollectionElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder collectionElementBuilder = LSPUtils.LogisticsSolutionElementBuilder.newInstance(elementId );
		collectionElementBuilder.setResource(collectionAdapter);
		collectionElement = collectionElementBuilder.build();
		
		elementInfo = new AverageTimeInfo();
		elementInfo.addPredecessorInfo(collectionAdapter.getInfos().iterator().next());
		collectionElement.getInfos().add(elementInfo);
		
		Id<LogisticsSolution> collectionSolutionId = Id.create("CollectionSolution", LogisticsSolution.class);
		LSPUtils.LogisticsSolutionBuilder collectionSolutionBuilder = LSPUtils.LogisticsSolutionBuilder.newInstance(collectionSolutionId );
		collectionSolutionBuilder.addSolutionElement(collectionElement);
		collectionSolution = collectionSolutionBuilder.build();
		
		solutionInfo = new AverageTimeInfo();
		solutionInfo.addPredecessorInfo(collectionElement.getInfos().iterator().next());
		collectionElement.getInfos().add(solutionInfo);
		
		ShipmentAssigner assigner = UsecaseUtils.createDeterministicShipmentAssigner();
		LSPPlan collectionPlan = LSPUtils.createLSPPlan();
		collectionPlan.setAssigner(assigner);
		collectionPlan.addSolution(collectionSolution);
	
		LSPUtils.LSPBuilder collectionLSPBuilder = LSPUtils.LSPBuilder.getInstance();
		collectionLSPBuilder.setInitialPlan(collectionPlan);
		Id<LSP> collectionLSPId = Id.create("CollectionLSP", LSP.class);
		collectionLSPBuilder.setId(collectionLSPId);
		ArrayList<LSPResource> resourcesList = new ArrayList<LSPResource>();
		resourcesList.add(collectionAdapter);
		
		SolutionScheduler simpleScheduler = UsecaseUtils.createDefaultSimpleForwardSolutionScheduler(resourcesList);
		collectionLSPBuilder.setSolutionScheduler(simpleScheduler);
		collectionLSP = collectionLSPBuilder.build();
	
		ArrayList <Link> linkList = new ArrayList<Link>(network.getLinks().values());
	    Id<Link> toLinkId = collectionLinkId;
	
	        
	    for(int i = 1; i < 11; i++) {
        	Id<LSPShipment> id = Id.create(i, LSPShipment.class);
        	ShipmentUtils.LSPShipmentBuilder builder = ShipmentUtils.LSPShipmentBuilder.newInstance(id );
        	Random random = new Random(1);
        	int capacityDemand = 1 + random.nextInt(4);
        	builder.setCapacityDemand(capacityDemand);
        	
        	while(true) {
        		Collections.shuffle(linkList, random);
        		Link pendingFromLink = linkList.get(0);
        		if(pendingFromLink.getFromNode().getCoord().getX() <= 4000 &&
        		   pendingFromLink.getFromNode().getCoord().getY() <= 4000 &&
        		   pendingFromLink.getToNode().getCoord().getX() <= 4000 &&
        		   pendingFromLink.getToNode().getCoord().getY() <= 4000) {
        		   builder.setFromLinkId(pendingFromLink.getId());
        		   break;	
        		}	
        	}
        	
        	builder.setToLinkId(toLinkId);
        	TimeWindow endTimeWindow = TimeWindow.newInstance(0,(24*3600));
        	builder.setEndTimeWindow(endTimeWindow);
        	TimeWindow startTimeWindow = TimeWindow.newInstance(0,(24*3600));
        	builder.setStartTimeWindow(startTimeWindow);
        	builder.setServiceTime(capacityDemand * 60);
        	LSPShipment shipment = builder.build();
        	collectionLSP.assignShipmentToLSP(shipment);
        }
		
	    collectionLSP.scheduleSoultions();
		
		ArrayList<LSP> lspList = new ArrayList<LSP>();
		lspList.add(collectionLSP);
		LSPs lsps = new LSPs(lspList);
		
		Controler controler = new Controler(config);
		
		LSPModule module = new LSPModule(lsps, LSPReplanningUtils.createDefaultLSPReplanningModule(lsps), LSPScoringModulsUtils.createDefaultLSPScoringModule(lsps), LSPEventUtils.getStandardEventCreators());

		controler.addOverridingModule(module);
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(0);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		config.network().setInputFile("scenarios/2regions/2regions-network.xml");
		controler.run();
	}

	@Test
	public void testCascadingInfos() {
		assertTrue(timeTracker.getInfos().iterator().next() == elementInfo.getPredecessorInfos().iterator().next());
		assertTrue(timeTracker.getInfos().iterator().next() instanceof AverageTimeInfo);
		AverageTimeInfo resourceInfo = (AverageTimeInfo) timeTracker.getInfos().iterator().next();
		assertTrue(resourceInfo.getFunction() instanceof AverageTimeInfoFunction);
		AverageTimeInfoFunction resourceInfoFunction = (AverageTimeInfoFunction) resourceInfo.getFunction();
		assertTrue(resourceInfoFunction.getValues().size() == 1);
		assertTrue(resourceInfoFunction.getValues().iterator().next() instanceof AverageTimeInfoFunctionValue);
		AverageTimeInfoFunctionValue averageResourceValue = (AverageTimeInfoFunctionValue) resourceInfoFunction.getValues().iterator().next();
		assertTrue(elementInfo.getFunction() instanceof AverageTimeInfoFunction);
		AverageTimeInfoFunction averageElementFunction = (AverageTimeInfoFunction) elementInfo.getFunction();
		assertTrue(averageElementFunction.getValues().size() == 1);
		assertTrue(averageElementFunction.getValues().iterator().next() instanceof AverageTimeInfoFunctionValue);
		AverageTimeInfoFunctionValue averageElementValue = (AverageTimeInfoFunctionValue) averageElementFunction.getValues().iterator().next();
		assertTrue(averageElementValue.getValue() > 0);
		assertTrue(averageElementFunction.getValues().iterator().next().getValue() instanceof Double);
		assertTrue(solutionInfo.getFunction() instanceof AverageTimeInfoFunction);
		assertTrue(solutionInfo.getPredecessorInfos().iterator().next() == elementInfo);
		AverageTimeInfoFunction averageSolutionFunction = (AverageTimeInfoFunction) solutionInfo.getFunction();
		assertTrue(averageSolutionFunction.getValues().size() == 1);
		assertTrue(averageSolutionFunction.getValues().iterator().next() instanceof AverageTimeInfoFunctionValue);
		AverageTimeInfoFunctionValue averageSolutionValue = (AverageTimeInfoFunctionValue) averageSolutionFunction.getValues().iterator().next();
		assertTrue(averageSolutionValue.getValue() > 0);
		assertTrue(averageElementValue.getValue() == averageResourceValue.getValue());
		assertTrue(averageElementValue.getValue() == averageSolutionValue.getValue());
	}

}
