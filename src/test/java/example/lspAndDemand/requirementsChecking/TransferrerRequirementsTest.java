package example.lspAndDemand.requirementsChecking;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Random;

import demand.decoratedLSP.*;
import lsp.*;
import lsp.usecase.UsecaseUtils;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.carrier.CarrierCapabilities.FleetSize;
import org.matsim.core.config.Config;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import demand.demandObject.DemandObject;
import demand.demandObject.DemandObjectImpl;
import demand.offer.Offer;
import demand.offer.OfferFactoryImpl;
import demand.offer.OfferTransferrer;
import lsp.resources.LSPResource;
import lsp.shipment.Requirement;
import requirementsCheckerTests.NonsenseOffer;
import requirementsCheckerTests.RequirementsTransferrer;

public class TransferrerRequirementsTest {
	private LogisticsSolutionDecorator blueOfferSolution;
	private LogisticsSolutionDecorator redOfferSolution;
	private LSPDecorator offerLSP;
	private ArrayList<DemandObject> demandObjects;

	@Before
	public void initialize() {
		Config config = new Config();
        config.addCoreModules();
        Scenario scenario = ScenarioUtils.createScenario(config);
        new MatsimNetworkReader(scenario.getNetwork()).readFile("scenarios/2regions/2regions-network.xml");
		Network network = scenario.getNetwork();

		Id<Carrier> redCarrierId = Id.create("RedCarrier", Carrier.class);
		Id<VehicleType> vehicleTypeId = Id.create("CollectionCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder vehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(vehicleTypeId);
		vehicleTypeBuilder.setCapacity(10);
		vehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
		vehicleTypeBuilder.setCostPerTimeUnit(0.38);
		vehicleTypeBuilder.setFixCost(49);
		vehicleTypeBuilder.setMaxVelocity(50/3.6);
		org.matsim.vehicles.VehicleType collectionType = vehicleTypeBuilder.build();
		
		Id<Link> collectionLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> redVehicleId = Id.createVehicleId("RedVehicle");
		CarrierVehicle redVehicle = CarrierVehicle.newInstance(redVehicleId, collectionLinkId);
		redVehicle.setType( collectionType );

		CarrierCapabilities.Builder redCapabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		redCapabilitiesBuilder.addType(collectionType);
		redCapabilitiesBuilder.addVehicle(redVehicle);
		redCapabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities redCapabilities = redCapabilitiesBuilder.build();
		Carrier redCarrier = CarrierUtils.createCarrier( redCarrierId );
		redCarrier.setCarrierCapabilities(redCapabilities);
				
		Id<LSPResource> redAdapterId = Id.create("RedCarrierAdapter", LSPResource.class);
		UsecaseUtils.CollectionCarrierAdapterBuilder redAdapterBuilder = UsecaseUtils.CollectionCarrierAdapterBuilder.newInstance(redAdapterId, network);
		redAdapterBuilder.setCollectionScheduler(UsecaseUtils.createDefaultCollectionCarrierScheduler());
		redAdapterBuilder.setCarrier(redCarrier);
		redAdapterBuilder.setLocationLinkId(collectionLinkId);
		LSPResource redCollectionAdapter = redAdapterBuilder.build();
		
		Id<LogisticsSolutionElement> redElementId = Id.create("RedCollectionElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder redCollectionElementBuilder = LSPUtils.LogisticsSolutionElementBuilder.newInstance(redElementId );
		redCollectionElementBuilder.setResource(redCollectionAdapter);
		LogisticsSolutionElement redCollectionElement = redCollectionElementBuilder.build();
		
		Id<LogisticsSolution> redCollectionSolutionId = Id.create("RedCollectionSolution", LogisticsSolution.class);
		DecoratedLSPUtils.LogisticsSolutionDecoratorImpl_wOffersBuilder redOfferSolutionBuilder = DecoratedLSPUtils.LogisticsSolutionDecoratorImpl_wOffersBuilder.newInstance(redCollectionSolutionId);
		redOfferSolutionBuilder.addSolutionElement(redCollectionElement);
		redOfferSolution = redOfferSolutionBuilder.build();
		redOfferSolution.getInfos().add(new RedInfo() );
		OfferFactoryImpl redOfferFactory = new OfferFactoryImpl(redOfferSolution);
		redOfferFactory.addOffer(new requirementsCheckerTests.NonsenseOffer() );
		redOfferSolution.setOfferFactory(redOfferFactory);

		LSPPlanDecorator collectionPlan = new LSPPlanWithOfferTransferrer();
		collectionPlan.addSolution(redOfferSolution);

		Id<Carrier> blueCarrierId = Id.create("BlueCarrier", Carrier.class);
		Id<Vehicle> blueVehicleId = Id.createVehicleId("BlueVehicle");
		CarrierVehicle blueVehicle = CarrierVehicle.newInstance(blueVehicleId, collectionLinkId);
		blueVehicle.setType( collectionType );

		CarrierCapabilities.Builder blueCapabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		blueCapabilitiesBuilder.addType(collectionType);
		blueCapabilitiesBuilder.addVehicle(blueVehicle);
		blueCapabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities blueCapabilities = blueCapabilitiesBuilder.build();
		Carrier blueCarrier = CarrierUtils.createCarrier( blueCarrierId );
		blueCarrier.setCarrierCapabilities(blueCapabilities);
				
		Id<LSPResource> blueAdapterId = Id.create("BlueCarrierAdapter", LSPResource.class);
		UsecaseUtils.CollectionCarrierAdapterBuilder blueAdapterBuilder = UsecaseUtils.CollectionCarrierAdapterBuilder.newInstance(blueAdapterId, network);
		blueAdapterBuilder.setCollectionScheduler(UsecaseUtils.createDefaultCollectionCarrierScheduler());
		blueAdapterBuilder.setCarrier(blueCarrier);
		blueAdapterBuilder.setLocationLinkId(collectionLinkId);
		LSPResource blueCollectionAdapter = blueAdapterBuilder.build();
		
		Id<LogisticsSolutionElement> blueElementId = Id.create("BlueCollectionElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder blueCollectionElementBuilder = LSPUtils.LogisticsSolutionElementBuilder.newInstance(blueElementId );
		blueCollectionElementBuilder.setResource(blueCollectionAdapter);
		LogisticsSolutionElement blueCollectionElement = blueCollectionElementBuilder.build();
		
		Id<LogisticsSolution> blueCollectionSolutionId = Id.create("BlueCollectionSolution", LogisticsSolution.class);
		DecoratedLSPUtils.LogisticsSolutionDecoratorImpl_wOffersBuilder blueOfferSolutionBuilder = DecoratedLSPUtils.LogisticsSolutionDecoratorImpl_wOffersBuilder.newInstance(blueCollectionSolutionId);
		blueOfferSolutionBuilder.addSolutionElement(blueCollectionElement);
		blueOfferSolution = blueOfferSolutionBuilder.build();
		blueOfferSolution.getInfos().add(new BlueInfo() );
		OfferFactoryImpl blueOfferFactory = new OfferFactoryImpl(blueOfferSolution);
		blueOfferFactory.addOffer(new NonsenseOffer() );
		blueOfferSolution.setOfferFactory(blueOfferFactory);
		collectionPlan.addSolution(blueOfferSolution);

		OfferTransferrer transferrer = new RequirementsTransferrer();
		collectionPlan.setOfferTransferrer(transferrer);
		
		LSPWithOffers.Builder offerLSPBuilder = LSPWithOffers.Builder.newInstance();
		offerLSPBuilder.setInitialPlan(collectionPlan);
		Id<LSP> collectionLSPId = Id.create("CollectionLSP", LSP.class);
		offerLSPBuilder.setId(collectionLSPId);
		ArrayList<LSPResource> resourcesList = new ArrayList<>();
		resourcesList.add(redCollectionAdapter);
		resourcesList.add(blueCollectionAdapter);
			
		SolutionScheduler simpleScheduler = UsecaseUtils.createDefaultSimpleForwardSolutionScheduler(resourcesList);
		offerLSPBuilder.setSolutionScheduler(simpleScheduler);
		offerLSP = offerLSPBuilder.build();
		LSPPlanDecorator decorator = (LSPPlanDecorator)offerLSP.getSelectedPlan();
		
		demandObjects = new ArrayList<>();
	    
	    Random rand = new Random(1); 
	    
	    for(int i = 1; i < 11; i++) {
			DemandObjectImpl.Builder builder = DemandObjectImpl.Builder.newInstance(Id.create(i, DemandObject.class));
        	
        	boolean blue = rand.nextBoolean();
        	if (blue) {
        		builder.addRequirement(new BlueRequirement() );
        	}
        	else {
        		builder.addRequirement(new RedRequirement() );
        	}
        	
        	DemandObject demandObject = builder.build();
        	demandObjects.add(demandObject);
	    }	
	}
	   
	@Test
	  public void testRequirementsTransferrer() {
	    	for(DemandObject demandObject : demandObjects) {
	    		Offer offer = offerLSP.getOffer(demandObject, "nonsense", null);
	    		for(Requirement requirement : demandObject.getRequirements()) {
	    			if(requirement instanceof RedRequirement) {
						assertSame(offer.getSolution(), redOfferSolution);
	    			}
	    			if(requirement instanceof BlueRequirement) {
						assertSame(offer.getSolution(), blueOfferSolution);
	    			}
	    		}
	    	}
	 }






}
