package adapterTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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



public class DistributionAdapterTest {
		
		//die Trackers sind ja erst ein Bestandteil des Scheduling bzw. Replanning und kommen hier noch nicht rein.
		//Man kann sie deshalb ja extra au�erhalb des Builders einsetzen.

	private org.matsim.vehicles.VehicleType distributionType;
		private CarrierVehicle distributionCarrierVehicle;
		private CarrierCapabilities capabilities;
		private Carrier distributionCarrier;
		private LSPCarrierResource distributionAdapter;
		private Id<Link> distributionLinkId;
		
		@Before
		public void initialize() {
			Config config = new Config();
	        config.addCoreModules();
	        Scenario scenario = ScenarioUtils.createScenario(config);
	        new MatsimNetworkReader(scenario.getNetwork()).readFile("scenarios/2regions/2regions-network.xml");
			Network network = scenario.getNetwork();

			Id<Carrier> carrierId = Id.create("DistributionCarrier", Carrier.class);
			Id<VehicleType> vehicleTypeId = Id.create("DistributionCarrierVehicleType", VehicleType.class);
			CarrierVehicleType.Builder vehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(vehicleTypeId);
			vehicleTypeBuilder.setCapacity(10);
			vehicleTypeBuilder.setCostPerDistanceUnit(0.0004);
			vehicleTypeBuilder.setCostPerTimeUnit(0.38);
			vehicleTypeBuilder.setFixCost(49);
			vehicleTypeBuilder.setMaxVelocity(50/3.6);
			distributionType = vehicleTypeBuilder.build();
			
			distributionLinkId = Id.createLinkId("(4 2) (4 3)");
			Id<Vehicle> distributionVehicleId = Id.createVehicleId("DistributionVehicle");
			distributionCarrierVehicle = CarrierVehicle.newInstance(distributionVehicleId, distributionLinkId);
			distributionCarrierVehicle.setType( distributionType );

			CarrierCapabilities.Builder capabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
			capabilitiesBuilder.addType(distributionType);
			capabilitiesBuilder.addVehicle(distributionCarrierVehicle);
			capabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
			capabilities = capabilitiesBuilder.build();
			distributionCarrier = CarrierUtils.createCarrier( carrierId );
			distributionCarrier.setCarrierCapabilities(capabilities);
			
			
			Id<LSPResource> adapterId = Id.create("DistributionCarrierAdapter", LSPResource.class);
			UsecaseUtils.DistributionCarrierAdapterBuilder builder = UsecaseUtils.DistributionCarrierAdapterBuilder.newInstance(adapterId, network);
			builder.setDistributionScheduler(UsecaseUtils.createDefaultDistributionCarrierScheduler());
			builder.setCarrier(distributionCarrier);
			builder.setLocationLinkId(distributionLinkId);
			distributionAdapter = builder.build();
		}


		@Test
		public void testCollectionAdapter() {
			assertTrue(distributionAdapter.getClientElements() != null);
			assertTrue(distributionAdapter.getClientElements().isEmpty());
			assertTrue(LSPCarrierResource.class.isAssignableFrom(distributionAdapter.getClass()));
			if(LSPCarrierResource.class.isAssignableFrom(distributionAdapter.getClass())) {
				assertTrue(Carrier.class.isAssignableFrom(distributionAdapter.getClassOfResource()));
				assertTrue(distributionAdapter.getCarrier() == distributionCarrier);
			}
			assertTrue(distributionAdapter.getEndLinkId() == distributionLinkId);
			assertTrue(distributionAdapter.getStartLinkId() == distributionLinkId);
			assertTrue(distributionAdapter.getEventHandlers() != null);
			assertTrue(distributionAdapter.getEventHandlers().isEmpty());
			assertTrue(distributionAdapter.getInfos() != null);
			assertTrue(distributionAdapter.getInfos().isEmpty());
			assertTrue(distributionAdapter.getStartLinkId() == distributionLinkId);
			if(distributionAdapter.getCarrier() == distributionCarrier) {
				assertTrue(distributionCarrier.getCarrierCapabilities() == capabilities);
				assertTrue(Carrier.class.isAssignableFrom(distributionCarrier.getClass()));
				assertTrue(distributionCarrier.getPlans().isEmpty());
				assertTrue(distributionCarrier.getSelectedPlan() == null);
				assertTrue(distributionCarrier.getServices().isEmpty());
				assertTrue(distributionCarrier.getShipments().isEmpty());
				if(distributionCarrier.getCarrierCapabilities() == capabilities) {
					assertTrue(capabilities.getFleetSize() == FleetSize.INFINITE);
					assertFalse(capabilities.getVehicleTypes().isEmpty());
					ArrayList<VehicleType> types = new ArrayList<>( capabilities.getVehicleTypes() );
					if(types.size() ==1) {
						assertTrue(types.get(0) == distributionType);
						assertTrue( distributionType.getCapacity().getOther().intValue() == 10 );
						assertTrue( distributionType.getCostInformation().getPerDistanceUnit() == 0.0004 );
						assertTrue( distributionType.getCostInformation().getPerTimeUnit() == 0.38 );
						assertTrue( distributionType.getCostInformation().getFix() == 49 );
						assertTrue(distributionType.getMaximumVelocity() == (50/3.6));
						
					}
					ArrayList<CarrierVehicle> vehicles = new ArrayList<CarrierVehicle>(capabilities.getCarrierVehicles().values());
					if(vehicles.size() == 1) {
						assertTrue(vehicles.get(0) == distributionCarrierVehicle);
						assertTrue(distributionCarrierVehicle.getType() == distributionType);
						assertTrue(distributionCarrierVehicle.getLocation() == distributionLinkId);
					}
				}
			}
		}


}
