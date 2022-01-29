package solutionElementTests;

import static org.junit.Assert.assertTrue;

import lsp.LSPUtils;
import lsp.resources.LSPCarrierResource;
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

import lsp.LogisticsSolutionElement;
import lsp.resources.LSPResource;

public class DistributionElementTest {

	private LSPCarrierResource adapter;
	private LogisticsSolutionElement distributionElement;
	
	
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
		VehicleType distributionType = vehicleTypeBuilder.build();
		
		Id<Link> distributionLinkId = Id.createLinkId("(4 2) (4 3)");
		Id<Vehicle> distributionVehicleId = Id.createVehicleId("CollectionVehicle");
		CarrierVehicle carrierVehicle = CarrierVehicle.newInstance(distributionVehicleId, distributionLinkId);
		carrierVehicle.setType(distributionType);

		CarrierCapabilities.Builder capabilitiesBuilder = CarrierCapabilities.Builder.newInstance();
		capabilitiesBuilder.addType(distributionType);
		capabilitiesBuilder.addVehicle(carrierVehicle);
		capabilitiesBuilder.setFleetSize(FleetSize.INFINITE);
		CarrierCapabilities capabilities = capabilitiesBuilder.build();
		Carrier carrier = CarrierUtils.createCarrier(carrierId);
		carrier.setCarrierCapabilities(capabilities);
		
		
		Id<LSPResource> adapterId = Id.create("DistributionCarrierAdapter", LSPResource.class);
		UsecaseUtils.DistributionCarrierAdapterBuilder builder = UsecaseUtils.DistributionCarrierAdapterBuilder.newInstance(adapterId, network);
		builder.setDistributionScheduler(UsecaseUtils.createDefaultDistributionCarrierScheduler());
		builder.setCarrier(carrier);
		builder.setLocationLinkId(distributionLinkId);
		adapter = builder.build();
		
		Id<LogisticsSolutionElement> elementId = Id.create("DistributionElement", LogisticsSolutionElement.class);
		LSPUtils.LogisticsSolutionElementBuilder distributionBuilder = LSPUtils.LogisticsSolutionElementBuilder.newInstance(elementId );
		distributionBuilder.setResource(adapter);
		distributionElement = distributionBuilder.build();
	
	}
	
	@Test
	public void testDistributionElement() {
		assertTrue(distributionElement.getIncomingShipments()!= null);
		assertTrue(distributionElement.getIncomingShipments().getShipments() != null);
		assertTrue(distributionElement.getIncomingShipments().getSortedShipments().isEmpty());
		assertTrue(distributionElement.getInfos() != null);
		assertTrue(distributionElement.getInfos().isEmpty());
		assertTrue(distributionElement.getLogisticsSolution() == null);
		assertTrue(distributionElement.getNextElement() == null);
		assertTrue(distributionElement.getOutgoingShipments()!= null);
		assertTrue(distributionElement.getOutgoingShipments().getShipments() != null);
		assertTrue(distributionElement.getOutgoingShipments().getSortedShipments().isEmpty());
		assertTrue(distributionElement.getPreviousElement() == null);
		assertTrue(distributionElement.getResource() == adapter);
		assertTrue(distributionElement.getResource().getClientElements().iterator().next() == distributionElement);
	}
}
