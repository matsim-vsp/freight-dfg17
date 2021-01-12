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
import org.matsim.contrib.freight.utils.FreightUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.examples.ExamplesUtils;
import receiver.*;
import receiver.collaboration.CollaborationUtils;
import receiver.product.Order;
import receiver.product.ProductType;
import receiver.product.ReceiverOrder;
import receiver.product.ReceiverProduct;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Various utilities for building receiver scenarios (for now).
 * 
 * @author jwjoubert, wlbean
 */
class ProportionalScenarioBuilder {
	private final static Logger LOG = Logger.getLogger(ProportionalScenarioBuilder.class);
	private final static int NUMBER_OF_RECEIVERS = 60;
	

	/**
	 * Build the entire chessboard example.
	 */
	public static Scenario createChessboardScenario(String outputDirectory, long seed, int run, boolean write) {
		Scenario sc = setupChessboardScenario("grid9x9.xml", outputDirectory, seed, run);

        ConfigUtils.addOrGetModule(sc.getConfig(), ReceiverConfigGroup.class).setReceiverReplanningInterval(50);

        /* Create and add chessboard carriers. */
		MarginalScenarioBuilder.createChessboardCarriers(sc );
		
		/* Create the grand coalition receiver members and allocate orders. */
		createAndAddChessboardReceivers(sc);		
		
		/* Create the control group (not in the grand coalition) receivers and allocate orders. */
		createAndAddControlGroupReceivers(sc);

		createReceiverOrders(sc);

		/* Let jsprit do its magic and route the given receiver orders. */
//		generateCarrierPlan(ReceiverUtils.getCarriers( sc ), sc.getNetwork(), "./scenarios/chessboard/vrpalgo/initialPlanAlgorithm.xml");
		URL algoConfigFileName = IOUtils.extendUrl(sc.getConfig().getContext(), "initialPlanAlgorithm.xml" );
		ReceiverChessboardUtils.generateCarrierPlan(FreightUtils.getCarriers(sc), sc.getNetwork(), algoConfigFileName);
		
		if(write) {
			writeFreightScenario(sc);
		}
		
		/* Link the carriers to the receivers. */
		ReceiverUtils.getReceivers( sc ).linkReceiverOrdersToCarriers(FreightUtils.getCarriers(sc));
		
		CollaborationUtils.createCoalitionWithCarriersAndAddCollaboratingReceivers( sc );

		return sc;
	}



	/*
	 * Creates and adds a control group of receivers for experiments. These receivers will be allowed to replan, 
	 * but NOT be allowed to join the grand coalition. This group represents receivers that are unwilling to 
	 * collaborate in any circumstances.
	 */
	public static void createAndAddControlGroupReceivers(Scenario sc) {
		Network network = sc.getNetwork();
		Receivers receivers = ReceiverUtils.getReceivers( sc );
		
		for (int r = NUMBER_OF_RECEIVERS+1; r < (NUMBER_OF_RECEIVERS*2)+1 ; r++){
			Id<Link> result;
			Object[] linkIds = network.getLinks().keySet().toArray();
			int sample = MatsimRandom.getRandom().nextInt(linkIds.length );
			Object o = linkIds[sample];
			Id<Link> linkId = null;
			if(o instanceof Id<?>){
				linkId = (Id<Link>) o;
				result = linkId;
			} else{
				throw new RuntimeException("Oops, cannot find a correct link Id.");
			}
			Id<Link> receiverLocation = result;
			Receiver receiver = ReceiverUtils.newInstance(Id.create(Integer.toString(r), Receiver.class))
					.setLinkId(receiverLocation);
			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_GRANDCOALITION_MEMBER, false);
			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS, false);
		
			receivers.addReceiver(receiver);
		}		
//		ReceiverUtils.setReceivers( receivers, sc );	
	}

	/**
	 * FIXME Need to complete this. 
	 * @param inputNetwork
	 * @param outputDirectory
	 * @return
	 */
	public static Scenario setupChessboardScenario(String inputNetwork, String outputDirectory, long seed, int run) {
		URL context = ExamplesUtils.getTestScenarioURL( "freight-chessboard-9x9" );
		Config config = ConfigUtils.createConfig();
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(1500);
		config.controler().setMobsim("qsim");
		config.controler().setWriteSnapshotsInterval(50);
		config.global().setRandomSeed(seed);
		config.setContext(context);
		config.network().setInputFile(inputNetwork);
		config.controler().setOutputDirectory(outputDirectory);

		Scenario sc = ScenarioUtils.loadScenario(config);
		return sc;
	}
	
	public static void writeFreightScenario(Scenario sc) {
		/* Write the necessary bits to file. */
		String outputFolder = sc.getConfig().controler().getOutputDirectory();
		outputFolder += outputFolder.endsWith("/") ? "" : "/";
		new File(outputFolder).mkdirs();
		
		new ConfigWriter(sc.getConfig()).write(outputFolder + "config.xml");
		new CarrierPlanXmlWriterV2(FreightUtils.getCarriers(sc)).write(outputFolder + "carriers.xml");
//		new ReceiversWriter( ReceiverUtils.getReceivers( sc ) ).write(outputFolder + "receivers.xml");

		/* Write the vehicle types. FIXME This will have to change so that vehicle
		 * types lie at the Carriers level, and not per Carrier. In this scenario 
		 * there luckily is only a single Carrier. */
		new CarrierVehicleTypeWriter(CarrierVehicleTypes.getVehicleTypes(FreightUtils.getCarriers(sc))).write(outputFolder + "carrierVehicleTypes.xml");
	}


	/**
	 * Creates the product orders for the receiver agents in the simulation. Currently (28/08/18) all the receivers have the same orders 
	 * for experiments, but this must be adapted in the future to accept other parameters as inputs to enable different orders per receiver. 
	 * @param sc
	 */
	public static void createReceiverOrders( Scenario sc) {
		Carriers carriers = FreightUtils.getCarriers(sc);
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
			int tw = 6;
			String serdur = "02:00:00";
			int numDel = 5;
			
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
			
			/* Set the different service durations for experiments. */
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
			
//			
//			/* Set the different delivery frequencies for experiments. */
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
	 * Creates and adds the receivers that are part of the grand coalition. These receivers are allowed to replan
	 * their orders as well as decided to join or leave the coalition.
	 * @param fs
	 */
	public static void createAndAddChessboardReceivers( Scenario sc) {
		Network network = sc.getNetwork();

		Receivers receivers = new Receivers();
		
		receivers.setDescription("Chessboard");
		
		for (int r = 1; r < NUMBER_OF_RECEIVERS+1 ; r++){
			Id<Link> result;
			Object[] linkIds = network.getLinks().keySet().toArray();
			int sample = MatsimRandom.getRandom().nextInt(linkIds.length );
			Object o = linkIds[sample];
			Id<Link> linkId = null;
			if(o instanceof Id<?>){
				linkId = (Id<Link>) o;
				result = linkId;
			} else{
				throw new RuntimeException("Oops, cannot find a correct link Id.");
			}
			Id<Link> receiverLocation = result;
			Receiver receiver = ReceiverUtils.newInstance(Id.create(Integer.toString(r), Receiver.class))
					.setLinkId(receiverLocation);
			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_GRANDCOALITION_MEMBER, true);
			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS, true);
			receivers.addReceiver(receiver);
		}
		
		ReceiverUtils.setReceivers( receivers, sc );
	}


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
