package adapterTests;

import static org.junit.Assert.*;

import java.util.ArrayList;

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

import lsp.resources.LSPCarrierResource;
import lsp.resources.LSPResource;



public class CollectionAdapterTest {

	//die Trackers sind ja erst ein Bestandteil des Scheduling bzw. Replanning und kommen hier noch nicht rein.
	//Man kann sie deshalb ja extra au�erhalb des Builders einsetzen.

	private org.matsim.vehicles.VehicleType collectionType;
	private CarrierVehicle collectionCarrierVehicle;
	private Carrier collectionCarrier;
	private LSPCarrierResource carrierResource;
	private Id<Link> collectionLinkId;
	private CarrierCapabilities capabilities;
	
	@Before
	public void initialize() {
		Config config = new Config();
        config.addCoreModules();
        Scenario scenario = ScenarioUtils.createScenario(config);
        new MatsimNetworkReader(scenario.getNetwork()).readFile("scenarios/2regions/2regions-network.xml");
		Network network = scenario.getNetwork();

		Id<Carrier> carrierId = Id.create("CollectionCarrier", Carrier.class);
		Id<VehicleType> vehicleTypeId = Id.create("CollectionCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder vehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(vehicleTypeId);
		vehicleTypeBuilder.setCapacity(10);
		vehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
		vehicleTypeBuilder.setCostPerTimeUnit(0.38);
		vehicleTypeBuilder.setFixCost(49);
		vehicleTypeBuilder.setMaxVelocity(50/3.6);
		collectionType = vehicleTypeBuilder.build();
		
		collectionLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> vollectionVehicleId = Id.createVehicleId("CollectionVehicle");
		collectionCarrierVehicle = CarrierVehicle.newInstance(vollectionVehicleId, collectionLinkId);
		collectionCarrierVehicle.setType( collectionType );

		CarrierCapabilities.Builder capabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		capabilitiesBuilder.addType(collectionType);
		capabilitiesBuilder.addVehicle(collectionCarrierVehicle);
		capabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		capabilities = capabilitiesBuilder.build();
		collectionCarrier = CarrierUtils.createCarrier( carrierId );
		collectionCarrier.setCarrierCapabilities(capabilities);
		
		
		Id<LSPResource> adapterId = Id.create("CollectionCarrierAdapter", LSPResource.class);
		UsecaseUtils.CollectionCarrierAdapterBuilder builder = UsecaseUtils.CollectionCarrierAdapterBuilder.newInstance(adapterId, network);
		builder.setCollectionScheduler(UsecaseUtils.createDefaultCollectionCarrierScheduler());
		builder.setCarrier(collectionCarrier);
		builder.setLocationLinkId(collectionLinkId);
		carrierResource = builder.build();
	}
	
	
	@Test
	public void testCollectionAdapter() {
		assertTrue(carrierResource.getClientElements() != null);
		assertTrue(carrierResource.getClientElements().isEmpty());
		assertTrue(LSPCarrierResource.class.isAssignableFrom(carrierResource.getClass()));
		if(LSPCarrierResource.class.isAssignableFrom(carrierResource.getClass())) {
			assertTrue(Carrier.class.isAssignableFrom(carrierResource.getClassOfResource()));
			assertTrue(carrierResource.getCarrier() == collectionCarrier);
		}
		assertTrue(carrierResource.getEndLinkId() == collectionLinkId);
		assertTrue(carrierResource.getStartLinkId() == collectionLinkId);
		assertTrue(carrierResource.getEventHandlers() != null);
		assertTrue(carrierResource.getEventHandlers().isEmpty());
		assertTrue(carrierResource.getInfos() != null);
		assertTrue(carrierResource.getInfos().isEmpty());
		assertTrue(carrierResource.getStartLinkId() == collectionLinkId);
		if(carrierResource.getCarrier() == collectionCarrier) {
			assertTrue(collectionCarrier.getCarrierCapabilities() == capabilities);
			assertTrue(Carrier.class.isAssignableFrom(collectionCarrier.getClass()));
			assertTrue(collectionCarrier.getPlans().isEmpty());
			assertTrue(collectionCarrier.getSelectedPlan() == null);
			assertTrue(collectionCarrier.getServices().isEmpty());
			assertTrue(collectionCarrier.getShipments().isEmpty());
			if(collectionCarrier.getCarrierCapabilities() == capabilities) {
				assertTrue(capabilities.getFleetSize() == FleetSize.INFINITE);
				assertFalse(capabilities.getVehicleTypes().isEmpty());
				ArrayList<VehicleType> types = new ArrayList<VehicleType>(capabilities.getVehicleTypes());
				if(types.size() ==1) {
					assertTrue(types.get(0) == collectionType);
					assertTrue( collectionType.getCapacity().getOther().intValue() == 10 );
					assertTrue( collectionType.getCostInformation().getPerDistanceUnit() == 0.0004 );
					assertTrue( collectionType.getCostInformation().getPerTimeUnit() == 0.38 );
					assertTrue( collectionType.getCostInformation().getFix() == 49 );
					assertTrue(collectionType.getMaximumVelocity() == (50/3.6));
					
				}
				ArrayList<CarrierVehicle> vehicles = new ArrayList<CarrierVehicle>(capabilities.getCarrierVehicles().values());
				if(vehicles.size() == 1) {
					assertTrue(vehicles.get(0) == collectionCarrierVehicle);
					assertTrue(collectionCarrierVehicle.getType() == collectionType);
					assertTrue(collectionCarrierVehicle.getLocation() == collectionLinkId);
				}
			}
		}
	}
}
