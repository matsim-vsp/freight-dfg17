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



public class MainRunAdapterTest {

	private org.matsim.vehicles.VehicleType mainRunType;
	private Id<Link> fromLinkId;
	private Id<Link> toLinkId;
	private CarrierVehicle carrierVehicle;
	private CarrierCapabilities capabilities;
	private Carrier carrier;
	private LSPCarrierResource mainRunAdapter;
	
	@Before
	public void initialize() {
		Config config = new Config();
        config.addCoreModules();
        Scenario scenario = ScenarioUtils.createScenario(config);
        new MatsimNetworkReader(scenario.getNetwork()).readFile("scenarios/2regions/2regions-network.xml");
		Network network = scenario.getNetwork();


		Id<Carrier> carrierId = Id.create("MainRunCarrier", Carrier.class);
		Id<VehicleType> vehicleTypeId = Id.create("MainRunCarrierVehicleType", VehicleType.class);
		CarrierVehicleType.Builder vehicleTypeBuilder = CarrierVehicleType.Builder.newInstance(vehicleTypeId);
		vehicleTypeBuilder.setCapacity(30);
		vehicleTypeBuilder.setCostPerDistanceUnit(0.0008);
		vehicleTypeBuilder.setCostPerTimeUnit(0.38);
		vehicleTypeBuilder.setFixCost(120);
		vehicleTypeBuilder.setMaxVelocity(50/3.6);
		mainRunType = vehicleTypeBuilder.build();
				
		toLinkId = Id.createLinkId("(14 2) (14 3)");
		fromLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> collectionVehicleId = Id.createVehicleId("MainRunVehicle");
		carrierVehicle = CarrierVehicle.newInstance(collectionVehicleId, fromLinkId);
		carrierVehicle.setType( mainRunType );


		CarrierCapabilities.Builder capabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		capabilitiesBuilder.addType(mainRunType);
		capabilitiesBuilder.addVehicle(carrierVehicle);
		capabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		capabilities = capabilitiesBuilder.build();
		carrier = CarrierUtils.createCarrier(carrierId);
		carrier.setCarrierCapabilities(capabilities);


		Id<LSPResource> mainRunId = Id.create("MainRunAdapter", LSPResource.class);
        UsecaseUtils.MainRunCarrierAdapterBuilder mainRunBuilder = UsecaseUtils.MainRunCarrierAdapterBuilder.newInstance(mainRunId, network);
        mainRunBuilder.setMainRunCarrierScheduler(UsecaseUtils.createDefaultMainRunCarrierScheduler());
        mainRunBuilder.setFromLinkId(Id.createLinkId("(4 2) (4 3)"));
        mainRunBuilder.setToLinkId(Id.createLinkId("(14 2) (14 3)"));
        mainRunBuilder.setCarrier(carrier);
        mainRunAdapter =  mainRunBuilder.build();
	
	}
	
	@Test
	public void testMainRunAdapter() {
		assertTrue(mainRunAdapter.getClientElements() != null);
		assertTrue(mainRunAdapter.getClientElements().isEmpty());
		assertTrue(LSPCarrierResource.class.isAssignableFrom(mainRunAdapter.getClass()));
		if(LSPCarrierResource.class.isAssignableFrom(mainRunAdapter.getClass())) {
			assertTrue(Carrier.class.isAssignableFrom(mainRunAdapter.getClassOfResource()));
			assertTrue(mainRunAdapter.getCarrier() == carrier);
		}
		assertTrue(mainRunAdapter.getEndLinkId() == toLinkId);
		assertTrue(mainRunAdapter.getStartLinkId() == fromLinkId);
		assertTrue(mainRunAdapter.getEventHandlers() != null);
		assertTrue(mainRunAdapter.getEventHandlers().isEmpty());
		assertTrue(mainRunAdapter.getInfos() != null);
		assertTrue(mainRunAdapter.getInfos().isEmpty());
		if(mainRunAdapter.getCarrier() == carrier) {
			assertTrue(carrier.getCarrierCapabilities() == capabilities);
			assertTrue(Carrier.class.isAssignableFrom(carrier.getClass()));
			assertTrue(carrier.getPlans().isEmpty());
			assertTrue(carrier.getSelectedPlan() == null);
			assertTrue(carrier.getServices().isEmpty());
			assertTrue(carrier.getShipments().isEmpty());
			if(carrier.getCarrierCapabilities() == capabilities) {
				assertTrue(capabilities.getFleetSize() == FleetSize.INFINITE);
				assertFalse(capabilities.getVehicleTypes().isEmpty());
				ArrayList<VehicleType> types = new ArrayList<VehicleType>(capabilities.getVehicleTypes());
				if(types.size() ==1) {
					assertTrue(types.get(0) == mainRunType);
					assertTrue( mainRunType.getCapacity().getOther().intValue() == 30 );
					assertTrue( mainRunType.getCostInformation().getPerDistanceUnit() == 0.0008 );
					assertTrue( mainRunType.getCostInformation().getPerTimeUnit() == 0.38 );
					assertTrue( mainRunType.getCostInformation().getFix() == 120 );
					assertTrue(mainRunType.getMaximumVelocity() == (50/3.6));					
				}
				ArrayList<CarrierVehicle> vehicles = new ArrayList<CarrierVehicle>(capabilities.getCarrierVehicles().values());
				if(vehicles.size() == 1) {
					assertTrue(vehicles.get(0) == carrierVehicle);
					assertTrue(carrierVehicle.getType() == mainRunType);
					assertTrue(carrierVehicle.getLocation() == fromLinkId);
				}
			}
		}
	
	
	}
}
