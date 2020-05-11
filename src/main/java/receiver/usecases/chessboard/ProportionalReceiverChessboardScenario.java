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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierCapabilities;
import org.matsim.contrib.freight.carrier.CarrierCapabilities.FleetSize;
import org.matsim.contrib.freight.carrier.CarrierImpl;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.CarrierVehicleType;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeWriter;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.examples.ExamplesUtils;
import org.matsim.vehicles.VehicleType;

import receiver.*;
import receiver.collaboration.CollaborationUtils;
import receiver.product.Order;
import receiver.product.ProductType;
import receiver.product.ReceiverOrder;
import receiver.product.ReceiverProduct;

/**
 * Various utilities for building receiver scenarios (for now).
 * 
 * @author jwjoubert, wlbean
 */
class ProportionalReceiverChessboardScenario {
	private final static Logger LOG = Logger.getLogger(ProportionalReceiverChessboardScenario.class);
//	private final static int NUMBER_OF_RECEIVERS = 60;
	

	/**
	 * Build the entire chessboard example.
	 */
	public static Scenario createChessboardScenario( long seed, boolean write ) {
		MatsimRandom.reset(seed);

		Config config = setupChessboardConfig( seed);

		Scenario sc = ScenarioUtils.loadScenario(config);

		createChessboardCarriersAndAddToScenario(sc);

		ConfigUtils.addOrGetModule(sc.getConfig(), ReceiverConfigGroup.class).setReceiverReplanningInterval(ExperimentParameters.REPLAN_INTERVAL);

		/* Create the grand coalition receiver members and allocate orders. */
		BaseReceiverChessboardScenario.createAndAddChessboardReceivers(sc, ExperimentParameters.NUMBER_OF_RECEIVERS);	
		
		/* Create the control group (not in the grand coalition) receivers and allocate orders. */
		createAndAddControlGroupReceivers(sc);

		createReceiverOrders(sc);

		if(write) {
			writeFreightScenario(sc);
		}
		
		/* Link the carriers to the receivers. */
		ReceiverUtils.getReceivers( sc ).linkReceiverOrdersToCarriers( ReceiverUtils.getCarriers( sc ) );
		CollaborationUtils.createCoalitionWithCarriersAndAddCollaboratingReceivers( sc );
		return sc;
	}


	/**
	 * Creates the carrier agents for the simulation.
	 * @param sc
	 * @return
	 */
	private static void createChessboardCarriersAndAddToScenario( Scenario sc ) {
		Id<Carrier> carrierId = Id.create("Carrier1", Carrier.class);
		Carrier carrier = CarrierImpl.newInstance(carrierId);
		Id<Link> carrierLocation = selectRandomLink(sc.getNetwork());

		CarrierCapabilities.Builder capBuilder = CarrierCapabilities.Builder.newInstance();
		CarrierCapabilities carrierCap = capBuilder.setFleetSize(FleetSize.INFINITE).build();
		carrier.setCarrierCapabilities(carrierCap);
		LOG.info("Created a carrier with capabilities.");

		/*
		 * Create the carrier vehicle types.
		 * TODO This might, potentially, be read from XML file.
		 */

		/* Heavy vehicle. */
		org.matsim.contrib.freight.carrier.CarrierVehicleType.Builder typeBuilderHeavy = CarrierVehicleType.Builder.newInstance(Id.create("heavy", VehicleType.class));
		org.matsim.vehicles.VehicleType typeHeavy = typeBuilderHeavy
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
		org.matsim.vehicles.VehicleType typeLight = typeBuilderLight
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
		carrier.getCarrierCapabilities().getCarrierVehicles().put(heavy.getId(), heavy);
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeHeavy);
		carrier.getCarrierCapabilities().getCarrierVehicles().put(light.getId(), light);
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeLight);
		LOG.info("Added different vehicle types to the carrier.");

		CarrierVehicleTypes types = new CarrierVehicleTypes();
		types.getVehicleTypes().put(typeLight.getId(), typeLight);
		types.getVehicleTypes().put(typeHeavy.getId(), typeHeavy);

		Carriers carriers = new Carriers();
		carriers.addCarrier(carrier);
		ReceiverUtils.setCarriers(carriers, sc);
	}


	/**
	 * Selects a random link in the network.
	 * @param network
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static Id<Link> selectRandomLink( Network network ){
		Object[] linkIds = network.getLinks().keySet().toArray();
		int sample = MatsimRandom.getRandom().nextInt(linkIds.length );
		Object o = linkIds[sample];
		Id<Link> linkId = null;
		if(o instanceof Id<?>){
			linkId = (Id<Link>) o;
			return linkId;
		} else{
			throw new RuntimeException("Oops, cannot find a correct link Id.");
		}
	}



	private static Config setupChessboardConfig( long seed) {
		URL context = ExamplesUtils.getTestScenarioURL( "freight-chessboard-9x9" );

		Config config = ConfigUtils.createConfig();

		config.setContext( context );

		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(ExperimentParameters.NUM_ITERATIONS);
		config.controler().setMobsim("qsim");
		config.controler().setWriteSnapshotsInterval(ExperimentParameters.STAT_INTERVAL);
		config.global().setRandomSeed(seed);
		config.network().setInputFile( "grid9x9.xml" );

		return config ;
	}



	/*
	 * Creates and adds a control group of receivers for experiments. These receivers will be allowed to replan, 
	 * but NOT be allowed to join the grand coalition. This group represents receivers that are unwilling to 
	 * collaborate in any circumstances.
	 */
	public static void createAndAddControlGroupReceivers(Scenario sc) {
		Network network = sc.getNetwork();
		Receivers receivers = ReceiverUtils.getReceivers( sc );
		
		for (int r = ExperimentParameters.NUMBER_OF_RECEIVERS+1; r < (ExperimentParameters.NUMBER_OF_RECEIVERS*2)+1 ; r++) {
			Id<Link> receiverLocation = selectRandomLink(network);
			Receiver receiver = ReceiverUtils.newInstance(Id.create(Integer.toString(r), Receiver.class))
					.setLinkId(receiverLocation);
			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_GRANDCOALITION_MEMBER, false);
			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS, false);
			receivers.addReceiver(receiver);
		}		
//		ReceiverUtils.setReceivers( receivers, sc );	
	}

//	/**
//	 * FIXME Need to complete this. 
//	 * @param inputNetwork
//	 * @param outputDirectory
//	 * @return
//	 */
//	public static Scenario setupChessboardScenario(String inputNetwork, String outputDirectory, long seed, int run) {
//		URL context = ExamplesUtils.getTestScenarioURL( "freight-chessboard-9x9" );
//		Config config = ConfigUtils.createConfig();
//		config.controler().setFirstIteration(0);
//		config.controler().setLastIteration(1500);
//		config.controler().setMobsim("qsim");
//		config.controler().setWriteSnapshotsInterval(50);
//		config.global().setRandomSeed(seed);
//		config.setContext(context);
//		config.network().setInputFile(inputNetwork);
//		config.controler().setOutputDirectory(outputDirectory);
//
//		Scenario sc = ScenarioUtils.loadScenario(config);
//		return sc;
//	}
	
	public static void writeFreightScenario(Scenario sc) {
		/* Write the necessary bits to file. */
		String outputFolder = sc.getConfig().controler().getOutputDirectory();
		outputFolder += outputFolder.endsWith("/") ? "" : "/";
		new File(outputFolder).mkdirs();
		
		new ConfigWriter(sc.getConfig()).write(outputFolder + "config.xml");
		new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( sc ) ).write(outputFolder + "carriers.xml");
		new ReceiversWriter( ReceiverUtils.getReceivers( sc ) ).write(outputFolder + "receivers.xml");

		/* Write the vehicle types. FIXME This will have to change so that vehicle
		 * types lie at the Carriers level, and not per Carrier. In this scenario 
		 * there luckily is only a single Carrier. */
		new CarrierVehicleTypeWriter(CarrierVehicleTypes.getVehicleTypes( ReceiverUtils.getCarriers( sc ) )).write(outputFolder + "carrierVehicleTypes.xml");
	}


	/**
	 * Creates the product orders for the receiver agents in the simulation. Currently (28/08/18) all the receivers have the same orders 
	 * for experiments, but this must be adapted in the future to accept other parameters as inputs to enable different orders per receiver. 
	 * @param sc
	 */
	public static void createReceiverOrders( Scenario sc) {
		Carriers carriers = ReceiverUtils.getCarriers( sc );
		Receivers receivers = ReceiverUtils.getReceivers( sc );
		Carrier carrierOne = carriers.getCarriers().get(Id.create("Carrier1", Carrier.class));

		/* Try and get the first Carrier vehicle so that we can get its origina link.
		 * FIXME We want the carrier's location to rather be an attribute of the
		 * Carrier, but currently (Feb 19, JWJ) Carrier is not Attributable.
		 */
		Iterator<CarrierVehicle> vehicles = carrierOne.getCarrierCapabilities().getCarrierVehicles().values().iterator();
		if( !vehicles.hasNext() ) {
			throw new RuntimeException("Must have vehicles to get origin link!");
		}
		Id<Link> carrierOriginLinkId = vehicles.next().getLocation();

		/* Create generic product types with a description and required capacity (in kg per item). */
		ProductType productTypeOne = receivers.createAndAddProductType(Id.create("P1", ProductType.class), carrierOriginLinkId);
		productTypeOne.setDescription("Product 1");
		productTypeOne.setRequiredCapacity(1);

		ProductType productTypeTwo = receivers.createAndAddProductType(Id.create("P2", ProductType.class), carrierOriginLinkId);
		productTypeTwo.setDescription("Product 2");
		productTypeTwo.setRequiredCapacity(2);
		
		for ( int r = 1 ; r < ReceiverUtils.getReceivers( sc ).getReceivers().size()+1 ; r++){
			
			int tw = ExperimentParameters.TIME_WINDOW_DURATION;
//			if (r <= 3){
//				tw = 2;
//			} else if (r <= 6){
//				tw = 12;
//			} else if (r <= 9){
//				tw = 2;
//			} else tw = 12;
			int numDel = ExperimentParameters.NUM_DELIVERIES;
			String serdur = ExperimentParameters.SERVICE_TIME;

			/* Create receiver-specific products */
			Receiver receiver = receivers.getReceivers().get(Id.create(Integer.toString(r), Receiver.class));

			ReceiverProduct receiverProductOne = createReceiverProduct( receiver, productTypeOne, 1000, 5000 );
			receiver.addProduct(receiverProductOne);

//			ReceiverProduct receiverProductTwo = createReceiverProduct( receiver, productTypeTwo, 500, 2500 );
//			receiver.addProduct( receiverProductTwo );

			/* Generate and collate orders for the different receiver/order combination. */
			Collection<Order> rOrders = new ArrayList<Order>();
			{
				Order rOrder1 = createProductOrder( Id.create( "Order" + Integer.toString( r ) + "1", Order.class ), receiver,
					  receiverProductOne, Time.parseTime( serdur ) );
				rOrder1.setNumberOfWeeklyDeliveries( numDel );
				rOrder1.setDailyOrderQuantity(rOrder1.getOrderQuantity()/rOrder1.getNumberOfWeeklyDeliveries());
				rOrders.add( rOrder1 );
			}
//			{
//				Order rOrder2 = createProductOrder( Id.create( "Order" + Integer.toString( r ) + "2", Order.class ), receiver,
//					  receiverProductTwo, Time.parseTime( serdur ) );
//				rOrder2.setNumberOfWeeklyDeliveries( numDel );
//				rOrders.add( rOrder2 );
//			}

			/* Combine product orders into single receiver order for a specific carrier. */
			ReceiverOrder receiverOrder = new ReceiverOrder(receiver.getId(), rOrders, carrierOne.getId());
			boolean collaborationStatus = (boolean) receiver.getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS);
			ReceiverPlan receiverPlan = ReceiverPlan.Builder.newInstance(receiver, collaborationStatus)
					.addReceiverOrder(receiverOrder)
//					// setting the time window start time of ALL receivers to 08:00 for time window experiments.
					.addTimeWindow(TimeWindow.newInstance(6*3600,(6*3600+tw*3600)))
					// selecting a random time window start time.
//					.addTimeWindow(BaseReceiverChessboardScenario.selectRandomTimeStart(tw))
					.build();
			receiver.setSelectedPlan(receiverPlan);
			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_RECEIVER_TW_COST, ExperimentParameters.TIME_WINDOW_HOURLY_COST);

			/* Convert receiver orders to initial carrier services. */
//			BaseReceiverChessboardScenario.convertReceiverOrdersToInitialCarrierServices( carriers, receiverOrder, receiverPlan );
			BaseReceiverChessboardScenario.convertReceiverOrdersToInitialCarrierShipments(carriers, receiverOrder, receiverPlan);
		}

	}

//	/**
//	 * Use shipments instead of services.
//	 * @param carriers
//	 * @param receiverOrder
//	 * @param receiverPlan
//	 */
//	private static void convertReceiverOrdersToInitialCarrierShipments(Carriers carriers, ReceiverOrder receiverOrder,
//			ReceiverPlan receiverPlan) {
//		for( Order order : receiverOrder.getReceiverProductOrders()){
//			CarrierService.Builder serBuilder = CarrierService.
//					Builder.newInstance( Id.create(order.getId(),CarrierService.class ), order.getReceiver().getLinkId() );
//
//			if(receiverPlan.getTimeWindows().size() > 1) {
//				LOG.warn("Multiple time windows set. Only the first is used" );
//			}
//
//			CarrierService newService = serBuilder
//					.setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
//					setServiceStartTimeWindow(receiverPlan.getTimeWindows().get(0)).
//					setServiceDuration(order.getServiceDuration()).
//					build();
//			carriers.getCarriers().get(receiverOrder.getCarrierId()).getServices().add(newService);
//		}
//	}
//
//
//	private static TimeWindow selectRandomTimeStart(int tw) {
//		int min = 06;
//		int max = 18;
////		Random randomTime = new Random();
//		Random randomTime = MatsimRandom.getLocalInstance(); // overkill, but easiest to retrofit.  kai, jan'19
//		int randomStart =  (min +
//				randomTime.nextInt(max - tw - min + 1 ));
//		return TimeWindow.newInstance(randomStart*3600, randomStart*3600 + tw *3600 );
//	}


	/**
	 * Creates and adds the receivers that are part of the grand coalition. These receivers are allowed to replan
	 * their orders as well as decided to join or leave the coalition.
	 * @param numberOfReceivers 
	 * @param fs
	 */
//	public static void createAndAddChessboardReceivers( Scenario sc) {
//		Network network = sc.getNetwork();
//
//		Receivers receivers = new Receivers();
//
//		receivers.setDescription("Chessboard");
//
//		for (int r = 1; r < ExperimentParameters.NUMBER_OF_RECEIVERS+1 ; r++){
//			Id<Link> receiverLocation = selectRandomLink(network);
//			Receiver receiver = ReceiverUtils.newInstance(Id.create(Integer.toString(r), Receiver.class)).setLinkId(receiverLocation);
//			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_GRANDCOALITION_MEMBER, true);
//			receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS, true);
//			receivers.addReceiver(receiver);
//		}
//
//		ReceiverUtils.setReceivers( receivers, sc );
//	}


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
