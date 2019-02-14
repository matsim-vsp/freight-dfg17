/* *********************************************************************** *
 * project: org.matsim.*
 * ReceiverUtils.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package receiver.usecases.chessboard;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.carrier.CarrierCapabilities.FleetSize;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.examples.ExamplesUtils;
import org.matsim.vehicles.VehicleType;
import receiver.*;
import receiver.collaboration.CollaborationUtils;
import receiver.product.Order;
import receiver.product.ProductType;
import receiver.product.ReceiverOrder;
import receiver.product.ReceiverProduct;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import static receiver.usecases.chessboard.BaseReceiverChessboardScenario.selectRandomLink;

/**
 * Various utilities for building receiver scenarios (for now).
 * 
 * @author jwjoubert, wlbean
 */
class MarginalScenarioBuilder {
	private final static Logger LOG = Logger.getLogger(MarginalScenarioBuilder.class);


	/**
	 * Build the entire chessboard example.
	 */
	public static Scenario createChessboardScenario( String outputDirectory, long seed, int run, boolean write) {
		Scenario sc = setupChessboardScenario("grid9x9.xml", outputDirectory, seed, run);
		
		ReceiverUtils.setReplanInterval(ExperimentParameters.REPLAN_INTERVAL, sc );
		
		/* Create and add the carrier agent(s). */
		createChessboardCarriers(sc);
		
		/* Create the grand coalition receiver members and allocate orders. */
		BaseReceiverChessboardScenario.createAndAddChessboardReceivers(sc, ExperimentParameters.NUMBER_OF_RECEIVERS );
		
		/* Create the control group (not in the grand coalition) receivers and allocate orders. */
		createAndAddControlGroupReceivers(sc);

		createReceiverOrders(sc);

		/* Let jsprit do its magic and route the given receiver orders. */		
		URL algoConfigFileName = IOUtils.newUrl( sc.getConfig().getContext(), "initialPlanAlgorithm.xml" );
		ReceiverChessboardUtils.generateCarrierPlan( ReceiverUtils.getCarriers( sc ), sc.getNetwork(), algoConfigFileName);
		
		
		if(write) {
			BaseReceiverChessboardScenario.writeFreightScenario(sc );
		}
		
		/* Link the carriers to the receivers. */
		ReceiverUtils.getReceivers( sc ).linkReceiverOrdersToCarriers( ReceiverUtils.getCarriers( sc ) );
		CollaborationUtils.createCoalitionWithCarriersAndAddCollaboratingReceivers( sc );
		return sc;
	}


	/**
	 * FIXME Need to complete this. 
	 * @return
	 */
	public static Scenario setupChessboardScenario(String inputNetwork, String outputDirectory, long seed, int run) {
		URL context = ExamplesUtils.getTestScenarioURL( "freight-chessboard-9x9" );
		Config config = ConfigUtils.createConfig();
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(ExperimentParameters.NUM_ITERATIONS);
		config.controler().setMobsim("qsim");
		config.controler().setWriteSnapshotsInterval(ExperimentParameters.STAT_INTERVAL);
		config.global().setRandomSeed(seed);
		config.setContext( context );
		config.network().setInputFile(inputNetwork);
		config.controler().setOutputDirectory(outputDirectory);

		Scenario sc = ScenarioUtils.loadScenario(config);
		return sc;
	}
	
	/*
	 * Creates and adds a control group of receivers for experiments. These receivers will be allowed to replan, 
	 * but NOT be allowed to join the grand coalition. This group represents receivers that are unwilling to 
	 * collaborate in any circumstances.
	 */
	public static void createAndAddControlGroupReceivers( Scenario sc) {
		Network network = sc.getNetwork();
		Receivers receivers = ReceiverUtils.getReceivers( sc );
		
		for (int r = ExperimentParameters.NUMBER_OF_RECEIVERS+1; r < (ExperimentParameters.NUMBER_OF_RECEIVERS*2)+1 ; r++){
			Id<Link> receiverLocation = selectRandomLink(network);
			Receiver receiver = ReceiverUtils.newInstance(Id.create(Integer.toString(r), Receiver.class))
					.setLinkId(receiverLocation);
			receiver.getAttributes().putAttribute( ReceiverUtils.ATTR_GRANDCOALITION_MEMBER, false );
			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS, false);
		
			receivers.addReceiver(receiver);
		}
//		ReceiverUtils.setReceivers( receivers, sc );
	}



	/**
	 * Creates the product orders for the receiver agents in the simulation. Currently (28/08/18) all the receivers have the same orders 
	 * for experiments, but this must be adapted in the future to accept other parameters as inputs to enable different orders per receiver. 
	 */
	public static void createReceiverOrders( Scenario sc) {
		Carriers carriers = ReceiverUtils.getCarriers( sc );
		Receivers receivers = ReceiverUtils.getReceivers( sc );
		Carrier carrierOne = carriers.getCarriers().get(Id.create("Carrier1", Carrier.class));

		/* Create generic product types with a description and required capacity (in kg per item). */
		ProductType productTypeOne = receivers.createAndAddProductType(Id.create("P1", ProductType.class));
		productTypeOne.setDescription("Product 1");
		productTypeOne.setRequiredCapacity(1);

		ProductType productTypeTwo = receivers.createAndAddProductType(Id.create("P2", ProductType.class));
		productTypeTwo.setDescription("Product 2");
		productTypeTwo.setRequiredCapacity(2);
		
		for ( int r = 1 ; r < receivers.getReceivers().size()+1 ; r++){
			int tw = ExperimentParameters.TIME_WINDOW_DURATION;
			String serdur = ExperimentParameters.SERVICE_TIME;
			int numDel = ExperimentParameters.NUM_DELIVERIES;
			
			/* Set the different time window durations for experiments. */
//			if (r <= 10){
//				tw = 2;
//			} else if (r <= 20){
//				tw = 4;
//			} else if (r <= 30){
//				tw = 6;
//			} else if (r <= 40){
//				tw = 8;
//			} else if (r <= 50){
//				tw = 10;
//			} else if (r <= 60){
//				tw = 12;
//			} else if (r <= 70){
//				tw = 2;
//			} else if (r <= 80){
//				tw = 4;
//			} else if (r <= 90){
//				tw = 6;
//			} else if (r <= 100){
//				tw = 8;
//			} else if (r <= 110){
//				tw = 10;
//			} else tw = 12;
			
//			/* Set the different service durations for experiments. */
//			if (r <= 15){
//				serdur = "01:00:00";
//			} else if (r <= 30){
//				serdur = "02:00:00";
//			} else if (r <= 45){
//				serdur = "03:00:00";
//			} else if (r <=60) {
//				serdur = "04:00:00";
//			} else if (r <= 75){
//				serdur = "01:00:00";
//			} else if (r <= 90){
//				serdur = "02:00:00";
//			} else if (r <= 105){
//				serdur = "03:00:00";
//			} else serdur = "04:00:00";
			
			/* Set the different delivery frequencies for experiments. */
			if (r <= 12){
				numDel = 1;
			} else if (r <= 24){
				numDel = 2;
			} else if (r <= 36){
				numDel = 3;
			} else if (r <= 48){
				numDel = 4;
			} else if (r <= 60){
				numDel = 5;
			} else if (r <= 72){
				numDel = 1;
			} else if (r <= 84){
				numDel = 2;
			} else if (r <= 96){
				numDel = 3;
			} else if (r <= 108){
				numDel = 4;
			} else numDel = 5;
	
			/* Create receiver-specific products */
			Receiver receiver = receivers.getReceivers().get(Id.create(Integer.toString(r), Receiver.class));
			ReceiverProduct receiverProductOne = createReceiverProduct(receiver, productTypeOne, 1000, 5000);
			ReceiverProduct receiverProductTwo = createReceiverProduct(receiver, productTypeTwo, 500, 2500);
			receiver.addProduct(receiverProductOne);
			receiver.addProduct(receiverProductTwo);

			/* Generate and collate orders for the different receiver/order combination. */
			Order rOrder1 = createProductOrder(Id.create("Order"+Integer.toString(r)+"1",  Order.class), receiver, 
					receiverProductOne, Time.parseTime(serdur));
			rOrder1.setNumberOfWeeklyDeliveries(numDel);
			Order rOrder2 = createProductOrder(Id.create("Order"+Integer.toString(r)+"2",  Order.class), receiver, 
					receiverProductTwo, Time.parseTime(serdur));
			rOrder2.setNumberOfWeeklyDeliveries(numDel);
			Collection<Order> rOrders = new ArrayList<Order>();
			rOrders.add(rOrder1);
			rOrders.add(rOrder2);

			/* Combine product orders into single receiver order for a specific carrier. */
			ReceiverOrder receiverOrder = new ReceiverOrder(receiver.getId(), rOrders, carrierOne.getId());
			boolean collaborationStatus = (boolean) receiver.getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS);
			ReceiverPlan receiverPlan = ReceiverPlan.Builder.newInstance(receiver, collaborationStatus)
					.addReceiverOrder(receiverOrder)
					.addTimeWindow( BaseReceiverChessboardScenario.selectRandomTimeStart(tw ) )
//					.addTimeWindow(TimeWindow.newInstance(Time.parseTime("12:00:00"), Time.parseTime("12:00:00") + tw*3600))
					.build();
			receiver.setSelectedPlan(receiverPlan);

			/* Convert receiver orders to initial carrier services. */
			BaseReceiverChessboardScenario.convertReceiverOrdersToInitialCarrierServices( carriers, receiverOrder, receiverPlan );
		}

	}

	/**
	 * Creates the carrier agents for the simulation.
	 * @param sc
	 * @return
	 */
	public static void createChessboardCarriers(Scenario sc) {
		Id<Carrier> carrierId = Id.create("Carrier1", Carrier.class);
		Carrier carrier = CarrierImpl.newInstance(carrierId);
		Id<Link> carrierLocation = selectRandomLink(sc.getNetwork() );

		org.matsim.contrib.freight.carrier.CarrierCapabilities.Builder capBuilder = CarrierCapabilities.Builder.newInstance();
		CarrierCapabilities carrierCap = capBuilder.setFleetSize(FleetSize.INFINITE).build();
		carrier.setCarrierCapabilities(carrierCap);						
		LOG.info("Created a carrier with capabilities.");	

		/*
		 * Create the carrier vehicle types. 
		 * TODO This might, potentially, be read from XML file. 
		 */

		/* Heavy vehicle. */
		org.matsim.contrib.freight.carrier.CarrierVehicleType.Builder typeBuilderHeavy = CarrierVehicleType.Builder.newInstance(Id.create("heavy", VehicleType.class));
		CarrierVehicleType typeHeavy = typeBuilderHeavy
				.setCapacity(14000)
				.setFixCost(2604)
				.setCostPerDistanceUnit(7.34E-3)
				.setCostPerTimeUnit(0.171)
				.build();
		org.matsim.contrib.freight.carrier.CarrierVehicle.Builder carrierHVehicleBuilder = CarrierVehicle.Builder.newInstance(Id.createVehicleId("heavy"), carrierLocation);
		CarrierVehicle heavy = carrierHVehicleBuilder
				.setEarliestStart(Time.parseTime(ExperimentParameters.DAY_START))
				.setLatestEnd(Time.parseTime(ExperimentParameters.DAY_END))
				.setType(typeHeavy)
				.setTypeId(typeHeavy.getId())
				.build();

		/* Light vehicle. */
		org.matsim.contrib.freight.carrier.CarrierVehicleType.Builder typeBuilderLight = CarrierVehicleType.Builder.newInstance(Id.create("light", VehicleType.class));
		CarrierVehicleType typeLight = typeBuilderLight
				.setCapacity(3000)
				.setFixCost(1168)
				.setCostPerDistanceUnit(4.22E-3)
				.setCostPerTimeUnit(0.089)
				.build();
		org.matsim.contrib.freight.carrier.CarrierVehicle.Builder carrierLVehicleBuilder = CarrierVehicle.Builder.newInstance(Id.createVehicleId("light"), carrierLocation);
		CarrierVehicle light = carrierLVehicleBuilder
				.setEarliestStart(Time.parseTime(ExperimentParameters.DAY_START))
				.setLatestEnd(Time.parseTime(ExperimentParameters.DAY_END))
				.setType(typeLight)
				.setTypeId(typeLight.getId())
				.build();

		/* Assign vehicles to carrier. */
		carrier.getCarrierCapabilities().getCarrierVehicles().add(heavy);
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeHeavy);
		carrier.getCarrierCapabilities().getCarrierVehicles().add(light);	
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeLight);
		LOG.info("Added different vehicle types to the carrier.");

		/* FIXME we do nothing with this */
		CarrierVehicleTypes types = new CarrierVehicleTypes();
		types.getVehicleTypes().put(typeLight.getId(), typeLight);
		types.getVehicleTypes().put(typeHeavy.getId(), typeHeavy);

		Carriers carriers = new Carriers();
		carriers.addCarrier(carrier);
		
		ReceiverUtils.setCarriers(carriers, sc);
	}


	/**
	/**
	 * This method assigns a specific product type to a receiver, and allocates that receiver's order
	 * policy.
	 * 
	 * TODO This must be made more generic so that multiple policies can be considered. Currently (2018/04
	 * this is hard-coded to be a min-max (s,S) reordering policy.
	 *  
	 * @param receiver
	 * @param productType
	 * @param minLevel
	 * @param maxLevel
	 * @return 
	 */
	private static ReceiverProduct createReceiverProduct(Receiver receiver, ProductType productType, int minLevel, int maxLevel) {
		ReceiverProduct.Builder builder = ReceiverProduct.Builder.newInstance();
		ReceiverProduct rProd = builder
				.setReorderingPolicy(new SSReorderPolicy(minLevel, maxLevel))
				.setProductType(productType)
				.build();
		return rProd;
	}

	/**
	 * Create a receiver order for different products.
	 * @param number
	 * @param receiver
	 * @param receiverProduct
	 * @param serviceTime
	 * @return
	 */
	private static Order createProductOrder(Id<Order> number, Receiver receiver, ReceiverProduct receiverProduct, double serviceTime) {
		Order.Builder builder = Order.Builder.newInstance(number, receiver, receiverProduct);
		Order order = builder
				.calculateOrderQuantity()
				.setServiceTime(serviceTime)
				.build();

		return order;
	}

}
