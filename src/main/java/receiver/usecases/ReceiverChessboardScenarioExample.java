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
package receiver.usecases;

import java.io.File;
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
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.CarrierVehicleType;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeWriter;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.vehicles.VehicleType;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;

import receiver.FreightScenario;
import receiver.MutableFreightScenario;
import receiver.Receiver;
import receiver.ReceiverImpl;
import receiver.ReceiverPlan;
import receiver.Receivers;
import receiver.collaboration.MutableCoalition;
import receiver.io.ReceiversWriter;
import receiver.product.Order;
import receiver.product.ProductType;
import receiver.product.ReceiverOrder;
import receiver.product.ReceiverProduct;
import receiver.reorderPolicy.SSReorderPolicy;

/**
 * Various utilities for building receiver scenarios (for now).
 * 
 * @author jwjoubert, wlbean
 */
public class ReceiverChessboardScenarioExample {
	private final static Logger LOG = Logger.getLogger(ReceiverChessboardScenarioExample.class);

	/**
	 * Build the entire chessboard example.
	 */
	public static MutableFreightScenario createChessboardScenario(long seed, int run, boolean write) {
		Scenario sc = setupChessboardScenario(seed, run);
		Carriers carriers = createChessboardCarriers(sc);
		
		MutableFreightScenario fs = new MutableFreightScenario(sc, carriers);
		fs.setReplanInterval(10);
		
		createAndAddChessboardReceivers(fs);
		createReceiverOrders(fs);

		/* Let jsprit do its magic and route the given receiver orders. */
		generateCarrierPlan(fs.getCarriers(), fs.getScenario().getNetwork());
		
		
		if(write) {
			writeFreightScenario(fs);
		}
		
		/* Link the carriers to the receivers. */
		fs.getReceivers().linkReceiverOrdersToCarriers(fs.getCarriers());
		
		/* Add carrier and receivers to coalition */
		MutableCoalition coalition = new MutableCoalition();
		
		for (Carrier carrier : fs.getCarriers().getCarriers().values()){
			coalition.addCarrierCoalitionMember(carrier);
		}
		
		for (Receiver receiver : fs.getReceivers().getReceivers().values()){
			if (receiver.getCollaborationStatus() == true){
				coalition.addReceiverCoalitionMember(receiver);
			}
		}
		
		fs.setCoalition(coalition);
		
		return fs;
	}
	
	/**
	 * FIXME Need to complete this. 
	 * @return
	 */
	public static Scenario setupChessboardScenario(long seed, int run) {
		Config config = ConfigUtils.createConfig();
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(100);
		config.controler().setMobsim("qsim");
		config.controler().setWriteSnapshotsInterval(1);
		config.global().setRandomSeed(seed);
		config.network().setInputFile("./input/usecases/chessboard/network/grid9x9.xml");
		config.controler().setOutputDirectory(String.format("./output/run_%03d/", run));

		Scenario sc = ScenarioUtils.loadScenario(config);
		return sc;
	}
	
	private static void writeFreightScenario(FreightScenario fs) {
		/* Write the necessary bits to file. */
		String outputFolder = fs.getScenario().getConfig().controler().getOutputDirectory();
		outputFolder += outputFolder.endsWith("/") ? "" : "/";
		new File(outputFolder).mkdirs();
		
		new ConfigWriter(fs.getScenario().getConfig()).write(outputFolder + "config.xml");
		new CarrierPlanXmlWriterV2(fs.getCarriers()).write(outputFolder + "carriers.xml");
		new ReceiversWriter(fs.getReceivers()).write(outputFolder + "receivers.xml");

		/* Write the vehicle types. FIXME This will have to change so that vehicle
		 * types lie at the Carriers level, and not per Carrier. In this scenario 
		 * there luckily is only a single Carrier. */
		new CarrierVehicleTypeWriter(CarrierVehicleTypes.getVehicleTypes(fs.getCarriers())).write(outputFolder + "carrierVehicleTypes.xml");
	}

	/**
	 * Route the services that are allocated to the carrier.
	 * 
	 * @param carriers
	 * @param network
	 */
	public static void generateCarrierPlan(Carriers carriers, Network network) {
		Carrier carrier = carriers.getCarriers().get(Id.create("Carrier1", Carrier.class)); 

		VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, network);

		NetworkBasedTransportCosts netBasedCosts = NetworkBasedTransportCosts.Builder.newInstance(network, carrier.getCarrierCapabilities().getVehicleTypes()).build();
		VehicleRoutingProblem vrp = vrpBuilder.setRoutingCost(netBasedCosts).build();

		//read and create a pre-configured algorithms to solve the vrp
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "./input/usecases/chessboard/vrpalgo/initialPlanAlgorithm.xml");

		//solve the problem
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

		//get best (here, there is only one)
		VehicleRoutingProblemSolution solution = null;

		Iterator<VehicleRoutingProblemSolution> iterator = solutions.iterator();

		while(iterator.hasNext()){
			solution = iterator.next();
		}

		//create a carrierPlan from the solution 
		CarrierPlan plan = MatsimJspritFactory.createPlan(carrier, solution);

		//route plan 
		NetworkRouter.routePlan(plan, netBasedCosts);


		//assign this plan now to the carrier and make it the selected carrier plan
		carrier.setSelectedPlan(plan);

		//write out the carrierPlan to an xml-file
		//		new CarrierPlanXmlWriterV2(carriers).write(directory + "/input/carrierPlanned.xml");
	}


	public static void createReceiverOrders(FreightScenario fs) {
		Carriers carriers = fs.getCarriers();
		Receivers receivers = fs.getReceivers();
		
		Carrier carrierOne = carriers.getCarriers().get(Id.create("Carrier1", Carrier.class));

		/* Create generic product types with a description and required capacity (in kg per item)*/
		ProductType productTypeOne = receivers.createAndAddProductType(Id.create("P1", ProductType.class));
		productTypeOne.setProductDescription("Product 1");
		productTypeOne.setRequiredCapacity(1);

		ProductType productTypeTwo = receivers.createAndAddProductType(Id.create("P2", ProductType.class));
		productTypeTwo.setProductDescription("Product 2");
		productTypeTwo.setRequiredCapacity(2);

		/* Create receiver-specific products */
		Receiver receiverOne = receivers.getReceivers().get(Id.create("1", Receiver.class));
		ReceiverProduct receiverOneProductOne = createReceiverProduct(receiverOne, productTypeOne, 1000, 5000);
		ReceiverProduct receiverOneProductTwo = createReceiverProduct(receiverOne, productTypeTwo, 500, 2500);
		receiverOne.getProducts().add(receiverOneProductOne);
		receiverOne.getProducts().add(receiverOneProductTwo);

		Receiver receiverTwo = receivers.getReceivers().get(Id.create("2", Receiver.class));
		ReceiverProduct receiverTwoProductOne = createReceiverProduct(receiverTwo, productTypeOne, 1000, 5000);
		ReceiverProduct receiverTwoProductTwo = createReceiverProduct(receiverTwo, productTypeTwo, 500, 2500);
		receiverTwo.getProducts().add(receiverTwoProductOne);
		receiverTwo.getProducts().add(receiverTwoProductTwo);
		
	/*	Receiver receiverThree = receivers.getReceivers().get(Id.create("3", Receiver.class));
		ReceiverProduct receiverThreeProductOne = createReceiverProduct(receiverThree, productTypeOne, 1000, 5000);
		ReceiverProduct receiverThreeProductTwo = createReceiverProduct(receiverThree, productTypeTwo, 500, 2500);
		receiverThree.getProducts().add(receiverThreeProductOne);
		receiverThree.getProducts().add(receiverThreeProductTwo);

		Receiver receiverFour = receivers.getReceivers().get(Id.create("4", Receiver.class));
		ReceiverProduct receiverFourProductOne = createReceiverProduct(receiverFour, productTypeOne, 1000, 5000);
		ReceiverProduct receiverFourProductTwo = createReceiverProduct(receiverFour, productTypeTwo, 500, 2500);
		receiverFour.getProducts().add(receiverFourProductOne);
		receiverFour.getProducts().add(receiverFourProductTwo);
		
		Receiver receiverFive = receivers.getReceivers().get(Id.create("5", Receiver.class));
		ReceiverProduct receiverFiveProductOne = createReceiverProduct(receiverFive, productTypeOne, 1000, 5000);
		ReceiverProduct receiverFiveProductTwo = createReceiverProduct(receiverFive, productTypeTwo, 500, 2500);
		receiverFive.getProducts().add(receiverFiveProductOne);
		receiverFive.getProducts().add(receiverFiveProductTwo);
		
		Receiver receiverSix = receivers.getReceivers().get(Id.create("6", Receiver.class));
		ReceiverProduct receiverSixProductOne = createReceiverProduct(receiverSix, productTypeOne, 1000, 5000);
		ReceiverProduct receiverSixProductTwo = createReceiverProduct(receiverSix, productTypeTwo, 500, 2500);
		receiverSix.getProducts().add(receiverSixProductOne);
		receiverSix.getProducts().add(receiverSixProductTwo);

		Receiver receiverSeven = receivers.getReceivers().get(Id.create("7", Receiver.class));
		ReceiverProduct receiverSevenProductOne = createReceiverProduct(receiverSeven, productTypeOne, 1000, 5000);
		ReceiverProduct receiverSevenProductTwo = createReceiverProduct(receiverSeven, productTypeTwo, 500, 2500);
		receiverSeven.getProducts().add(receiverSevenProductOne);
		receiverSeven.getProducts().add(receiverSevenProductTwo);
		
		Receiver receiverEight = receivers.getReceivers().get(Id.create("8", Receiver.class));
		ReceiverProduct receiverEightProductOne = createReceiverProduct(receiverEight, productTypeOne, 1000, 5000);
		ReceiverProduct receiverEightProductTwo = createReceiverProduct(receiverEight, productTypeTwo, 500, 2500);
		receiverEight.getProducts().add(receiverEightProductOne);
		receiverEight.getProducts().add(receiverEightProductTwo);



		/* Generate and collate orders for the different receiver/order combination. */

		/* -----  Receiver 1. ----- */
		Order r1order1 = createProductOrder(Id.create("Order11",  Order.class), receiverOne, 
				receiverOneProductOne, Time.parseTime("01:00:00"));
			r1order1.setNumberOfWeeklyDeliveries(5);
		Order r1order2 = createProductOrder(Id.create("Order12",  Order.class), receiverOne, 
				receiverOneProductTwo, Time.parseTime("01:00:00"));
			r1order2.setNumberOfWeeklyDeliveries(5);
		Collection<Order> r1orders = new ArrayList<Order>();
		r1orders.add(r1order1);
		r1orders.add(r1order2);

		/* Combine product orders into single receiver order for a specific carrier. */
		ReceiverOrder receiver1order = new ReceiverOrder(receiverOne.getId(), r1orders, carrierOne.getId());
		ReceiverPlan receiverOnePlan = ReceiverPlan.Builder.newInstance(receiverOne)
				.addReceiverOrder(receiver1order)
				.addTimeWindow(selectRandomTimeStart(6))
				//.addTimeWindow(TimeWindow.newInstance(Time.parseTime("10:00"), Time.parseTime("14:00")))
				.build();
		receiverOne.setSelectedPlan(receiverOnePlan);

		/* Convert receiver orders to initial carrier services. */
		for(Order order : receiver1order.getReceiverOrders()){
			org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
					Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());
			
			if(receiverOnePlan.getTimeWindows().size() > 1) {
				LOG.warn("Multiple time windows set. Only the first is used");
			}
			CarrierService newService = serBuilder.setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
					setServiceStartTimeWindow(receiverOnePlan.getTimeWindows().get(0)).
					setServiceDuration(order.getServiceDuration()).
					build();
			carriers.getCarriers().get(receiver1order.getCarrierId()).getServices().add(newService);		
		}	


		/* -----  Receiver 2. ----- */
		Order r2order1 = createProductOrder(Id.create("Order21",  Order.class), receiverTwo, 
				receiverTwoProductOne, Time.parseTime("01:00:00"));
			r2order1.setNumberOfWeeklyDeliveries(5);
		Order r2order2 = createProductOrder(Id.create("Order22",  Order.class), receiverTwo, 
				receiverTwoProductTwo, Time.parseTime("01:00:00"));
			r2order2.setNumberOfWeeklyDeliveries(5);
		Collection<Order> r2orders = new ArrayList<Order>();
		r2orders.add(r2order1);
		r2orders.add(r2order2);

		/* Combine product orders into single receiver order for a specific carrier. */
	
		ReceiverOrder receiver2order = new ReceiverOrder(receiverTwo.getId(), r2orders, carrierOne.getId());
		ReceiverPlan receiverTwoPlan = ReceiverPlan.Builder.newInstance(receiverTwo)
				.addReceiverOrder(receiver2order)
				.addTimeWindow(selectRandomTimeStart(6))
				//.addTimeWindow(TimeWindow.newInstance(Time.parseTime("08:00"), Time.parseTime("12:00")))
				.build();
		receiverTwo.setSelectedPlan(receiverTwoPlan);

		/* Convert receiver orders to initial carrier services. */
		for(Order order : receiver2order.getReceiverOrders()){
			org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
					Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());
			
			if(receiverTwoPlan.getTimeWindows().size() > 1) {
				LOG.warn("Multiple time windows set. Only the first is used");
			}
			CarrierService newService = serBuilder.
					setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
					setServiceStartTimeWindow(receiverTwoPlan.getTimeWindows().get(0)).
					setServiceDuration(order.getServiceDuration()).
					build();
			carriers.getCarriers().get(receiver2order.getCarrierId()).getServices().add(newService);		
		}
		
		/* -----  Receiver 3. ----- */
		/*Order r3order1 = createProductOrder(Id.create("Order31",  Order.class), receiverThree, 
				receiverThreeProductOne, Time.parseTime("01:00:00"));
			r3order1.setNumberOfWeeklyDeliveries(5);
		Order r3order2 = createProductOrder(Id.create("Order32",  Order.class), receiverThree, 
				receiverThreeProductTwo, Time.parseTime("01:00:00"));
			r3order2.setNumberOfWeeklyDeliveries(5);
		Collection<Order> r3orders = new ArrayList<Order>();
		r3orders.add(r3order1);
		r3orders.add(r3order2);

		/* Combine product orders into single receiver order for a specific carrier. */
	/*	ReceiverOrder receiver3order = new ReceiverOrder(receiverThree.getId(), r3orders, carrierOne.getId());
		ReceiverPlan receiverThreePlan = ReceiverPlan.Builder.newInstance(receiverThree)
				.addReceiverOrder(receiver3order)
				.addTimeWindow(selectRandomTimeStart(6))
				//.addTimeWindow(TimeWindow.newInstance(Time.parseTime("10:00"), Time.parseTime("14:00")))
				.build();
		receiverThree.setSelectedPlan(receiverThreePlan);

		/* Convert receiver orders to initial carrier services. */
	/*	for(Order order : receiver3order.getReceiverOrders()){
			org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
					Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());
			
			if(receiverThreePlan.getTimeWindows().size() > 1) {
				LOG.warn("Multiple time windows set. Only the first is used");
			}
			CarrierService newService = serBuilder.setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
					setServiceStartTimeWindow(receiverThreePlan.getTimeWindows().get(0)).
					setServiceDuration(order.getServiceDuration()).
					build();
			carriers.getCarriers().get(receiver3order.getCarrierId()).getServices().add(newService);		
		}	
		
		/* -----  Receiver 4. ----- */
	/*	Order r4order1 = createProductOrder(Id.create("Order41",  Order.class), receiverFour, 
				receiverFourProductOne, Time.parseTime("01:00:00"));
			r4order1.setNumberOfWeeklyDeliveries(5);
		Order r4order2 = createProductOrder(Id.create("Order42",  Order.class), receiverFour, 
				receiverFourProductTwo, Time.parseTime("01:00:00"));
			r4order2.setNumberOfWeeklyDeliveries(5);
		Collection<Order> r4orders = new ArrayList<Order>();
		r4orders.add(r4order1);
		r4orders.add(r4order2);

		/* Combine product orders into single receiver order for a specific carrier. */
	/*	ReceiverOrder receiver4order = new ReceiverOrder(receiverFour.getId(), r4orders, carrierOne.getId());
		ReceiverPlan receiverFourPlan = ReceiverPlan.Builder.newInstance(receiverFour)
				.addReceiverOrder(receiver4order)
				.addTimeWindow(selectRandomTimeStart(6))
				//.addTimeWindow(TimeWindow.newInstance(Time.parseTime("10:00"), Time.parseTime("14:00")))
				.build();
		receiverFour.setSelectedPlan(receiverFourPlan);

		/* Convert receiver orders to initial carrier services. */
/*		for(Order order : receiver4order.getReceiverOrders()){
			org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
					Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());
			
			if(receiverFourPlan.getTimeWindows().size() > 1) {
				LOG.warn("Multiple time windows set. Only the first is used");
			}
			CarrierService newService = serBuilder.setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
					setServiceStartTimeWindow(receiverFourPlan.getTimeWindows().get(0)).
					setServiceDuration(order.getServiceDuration()).
					build();
			carriers.getCarriers().get(receiver4order.getCarrierId()).getServices().add(newService);		
		}
		
		/* -----  Receiver 5. ----- */
/*		Order r5order1 = createProductOrder(Id.create("Order51",  Order.class), receiverFive, 
				receiverFiveProductOne, Time.parseTime("01:00:00"));
			r5order1.setNumberOfWeeklyDeliveries(5);
		Order r5order2 = createProductOrder(Id.create("Order52",  Order.class), receiverFive, 
				receiverFiveProductTwo, Time.parseTime("01:00:00"));
			r5order2.setNumberOfWeeklyDeliveries(5);
		Collection<Order> r5orders = new ArrayList<Order>();
		r5orders.add(r5order1);
		r5orders.add(r5order2);

		/* Combine product orders into single receiver order for a specific carrier. */
/*		ReceiverOrder receiver5order = new ReceiverOrder(receiverFive.getId(), r5orders, carrierOne.getId());
		ReceiverPlan receiverFivePlan = ReceiverPlan.Builder.newInstance(receiverFive)
				.addReceiverOrder(receiver5order)
				.addTimeWindow(selectRandomTimeStart(6))
				//.addTimeWindow(TimeWindow.newInstance(Time.parseTime("10:00"), Time.parseTime("14:00")))
				.build();
		receiverFive.setSelectedPlan(receiverFivePlan);

		/* Convert receiver orders to initial carrier services. */
/*		for(Order order : receiver5order.getReceiverOrders()){
			org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
					Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());
			
			if(receiverFivePlan.getTimeWindows().size() > 1) {
				LOG.warn("Multiple time windows set. Only the first is used");
			}
			CarrierService newService = serBuilder.setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
					setServiceStartTimeWindow(receiverFivePlan.getTimeWindows().get(0)).
					setServiceDuration(order.getServiceDuration()).
					build();
			carriers.getCarriers().get(receiver5order.getCarrierId()).getServices().add(newService);		
		}
		
		/* -----  Receiver 6. ----- */
/*		Order r6order1 = createProductOrder(Id.create("Order61",  Order.class), receiverSix, 
				receiverSixProductOne, Time.parseTime("01:00:00"));
			r6order1.setNumberOfWeeklyDeliveries(5);
		Order r6order2 = createProductOrder(Id.create("Order62",  Order.class), receiverSix, 
				receiverSixProductTwo, Time.parseTime("01:00:00"));
			r6order2.setNumberOfWeeklyDeliveries(5);
		Collection<Order> r6orders = new ArrayList<Order>();
		r6orders.add(r6order1);
		r6orders.add(r6order2);

		/* Combine product orders into single receiver order for a specific carrier. */
/*		ReceiverOrder receiver6order = new ReceiverOrder(receiverSix.getId(), r6orders, carrierOne.getId());
		ReceiverPlan receiverSixPlan = ReceiverPlan.Builder.newInstance(receiverSix)
				.addReceiverOrder(receiver6order)
				.addTimeWindow(selectRandomTimeStart(6))
				//.addTimeWindow(TimeWindow.newInstance(Time.parseTime("10:00"), Time.parseTime("14:00")))
				.build();
		receiverSix.setSelectedPlan(receiverSixPlan);

		/* Convert receiver orders to initial carrier services. */
/*		for(Order order : receiver6order.getReceiverOrders()){
			org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
					Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());
			
			if(receiverSixPlan.getTimeWindows().size() > 1) {
				LOG.warn("Multiple time windows set. Only the first is used");
			}
			CarrierService newService = serBuilder.setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
					setServiceStartTimeWindow(receiverSixPlan.getTimeWindows().get(0)).
					setServiceDuration(order.getServiceDuration()).
					build();
			carriers.getCarriers().get(receiver6order.getCarrierId()).getServices().add(newService);		
		}	


		/* -----  Receiver 7. ----- */
	/*	Order r7order1 = createProductOrder(Id.create("Order71",  Order.class), receiverSeven, 
				receiverSevenProductOne, Time.parseTime("01:00:00"));
			r7order1.setNumberOfWeeklyDeliveries(5);
		Order r7order2 = createProductOrder(Id.create("Order72",  Order.class), receiverSeven, 
				receiverSevenProductTwo, Time.parseTime("01:00:00"));
			r7order2.setNumberOfWeeklyDeliveries(5);
		Collection<Order> r7orders = new ArrayList<Order>();
		r7orders.add(r7order1);
		r7orders.add(r7order2);

		/* Combine product orders into single receiver order for a specific carrier. */
	
	/*	ReceiverOrder receiver7order = new ReceiverOrder(receiverSeven.getId(), r7orders, carrierOne.getId());
		ReceiverPlan receiverSevenPlan = ReceiverPlan.Builder.newInstance(receiverSeven)
				.addReceiverOrder(receiver7order)
				.addTimeWindow(selectRandomTimeStart(6))
				//.addTimeWindow(TimeWindow.newInstance(Time.parseTime("08:00"), Time.parseTime("12:00")))
				.build();
		receiverSeven.setSelectedPlan(receiverSevenPlan);

		/* Convert receiver orders to initial carrier services. */
/*		for(Order order : receiver7order.getReceiverOrders()){
			org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
					Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());
			
			if(receiverSevenPlan.getTimeWindows().size() > 1) {
				LOG.warn("Multiple time windows set. Only the first is used");
			}
			CarrierService newService = serBuilder.
					setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
					setServiceStartTimeWindow(receiverSevenPlan.getTimeWindows().get(0)).
					setServiceDuration(order.getServiceDuration()).
					build();
			carriers.getCarriers().get(receiver7order.getCarrierId()).getServices().add(newService);		
		}
		
		/* -----  Receiver 8. ----- */
/*		Order r8order1 = createProductOrder(Id.create("Order81",  Order.class), receiverEight, 
				receiverEightProductOne, Time.parseTime("01:00:00"));
			r8order1.setNumberOfWeeklyDeliveries(5);
		Order r8order2 = createProductOrder(Id.create("Order82",  Order.class), receiverEight, 
				receiverEightProductTwo, Time.parseTime("01:00:00"));
			r8order2.setNumberOfWeeklyDeliveries(5);
		Collection<Order> r8orders = new ArrayList<Order>();
		r8orders.add(r8order1);
		r8orders.add(r8order2);

		/* Combine product orders into single receiver order for a specific carrier. */
/*		ReceiverOrder receiver8order = new ReceiverOrder(receiverEight.getId(), r8orders, carrierOne.getId());
		ReceiverPlan receiverEightPlan = ReceiverPlan.Builder.newInstance(receiverEight)
				.addReceiverOrder(receiver8order)
				.addTimeWindow(selectRandomTimeStart(6))
				//.addTimeWindow(TimeWindow.newInstance(Time.parseTime("10:00"), Time.parseTime("14:00")))
				.build();
		receiverEight.setSelectedPlan(receiverEightPlan);

		/* Convert receiver orders to initial carrier services. */
/*		for(Order order : receiver8order.getReceiverOrders()){
			org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
					Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());
			
			if(receiverEightPlan.getTimeWindows().size() > 1) {
				LOG.warn("Multiple time windows set. Only the first is used");
			}
			CarrierService newService = serBuilder.setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
					setServiceStartTimeWindow(receiverEightPlan.getTimeWindows().get(0)).
					setServiceDuration(order.getServiceDuration()).
					build();
			carriers.getCarriers().get(receiver8order.getCarrierId()).getServices().add(newService);		
		}	*/
		
		
	}

	public static void createAndAddChessboardReceivers(MutableFreightScenario fs) {
		Network network = fs.getScenario().getNetwork();

		/* Create first receiver */
		Id<Link> receiverOneLocation = selectRandomLink(network);
		Receiver receiverOne = ReceiverImpl.newInstance(Id.create("1", Receiver.class))
				.setLinkId(receiverOneLocation)
				.setCollaborationStatus(true);
//				.addTimeWindow(TimeWindow.newInstance(Time.parseTime("10:00"), Time.parseTime("14:00")));
		
		/* FIXME Add a toString() method. */
		
		/* Create second receiver */
		Id<Link> receiverTwoLocation = selectRandomLink(network);
		Receiver receiverTwo = ReceiverImpl.newInstance(Id.create("2", Receiver.class))
				.setLinkId(receiverTwoLocation)
				.setCollaborationStatus(false);
//				.addTimeWindow(TimeWindow.newInstance(Time.parseTime("08:00"), Time.parseTime("12:00")));
		
		/* Create third receiver */
/*		Id<Link> receiverThreeLocation = selectRandomLink(network);
		Receiver receiverThree = ReceiverImpl.newInstance(Id.create("3", Receiver.class))
				.setLinkId(receiverThreeLocation)
				.setCollaborationStatus(true);
		
		/* Create fourth receiver */
	/*	Id<Link> receiverFourLocation = selectRandomLink(network);
		Receiver receiverFour = ReceiverImpl.newInstance(Id.create("4", Receiver.class))
				.setLinkId(receiverFourLocation)
				.setCollaborationStatus(true);
		
		/* Create fifth receiver */
	/*	Id<Link> receiverFiveLocation = selectRandomLink(network);
		Receiver receiverFive = ReceiverImpl.newInstance(Id.create("5", Receiver.class))
				.setLinkId(receiverFiveLocation)
				.setCollaborationStatus(false);
		
		/* Create sixth receiver */
/*		Id<Link> receiverSixLocation = selectRandomLink(network);
		Receiver receiverSix = ReceiverImpl.newInstance(Id.create("6", Receiver.class))
				.setLinkId(receiverSixLocation)
				.setCollaborationStatus(false);
		
		/* Create seventh receiver */
/*		Id<Link> receiverSevenLocation = selectRandomLink(network);
		Receiver receiverSeven = ReceiverImpl.newInstance(Id.create("7", Receiver.class))
				.setLinkId(receiverSevenLocation)
				.setCollaborationStatus(false);
		
		/* Create eighth receiver */
	/*	Id<Link> receiverEightLocation = selectRandomLink(network);
		Receiver receiverEight = ReceiverImpl.newInstance(Id.create("8", Receiver.class))
				.setLinkId(receiverEightLocation)
				.setCollaborationStatus(false);*/

		Receivers receivers = new Receivers();
		
		receivers.setDescription("Chessboard");
		/* Add a dummy example attribute */
		receivers.getAttributes().putAttribute("date", "2018/04/20");
		
		receivers.addReceiver(receiverOne);
		receivers.addReceiver(receiverTwo);
	/*	receivers.addReceiver(receiverThree);
		receivers.addReceiver(receiverFour);
		receivers.addReceiver(receiverFive);
		receivers.addReceiver(receiverSix);
		receivers.addReceiver(receiverSeven);
		receivers.addReceiver(receiverEight);*/
		fs.setReceivers(receivers);
	}

	public static Carriers createChessboardCarriers(Scenario sc) {
		Id<Carrier> carrierId = Id.create("Carrier1", Carrier.class);
		Carrier carrier = CarrierImpl.newInstance(carrierId);
		Id<Link> carrierLocation = selectRandomLink(sc.getNetwork());

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
		CarrierVehicleType typeHeavy = typeBuilderHeavy.setCapacity(14000).setFixCost(2604).setCostPerDistanceUnit(7.34E-3).setCostPerTimeUnit(0.171).build();
		org.matsim.contrib.freight.carrier.CarrierVehicle.Builder carrierHVehicleBuilder = CarrierVehicle.Builder.newInstance(Id.createVehicleId("heavy"), carrierLocation);
		CarrierVehicle heavy = carrierHVehicleBuilder.setEarliestStart(Time.parseTime("06:00:00")).setLatestEnd(Time.parseTime("18:00:00")).setType(typeHeavy).setTypeId(typeHeavy.getId()).build();

		/* Light vehicle. */
		org.matsim.contrib.freight.carrier.CarrierVehicleType.Builder typeBuilderLight = CarrierVehicleType.Builder.newInstance(Id.create("light", VehicleType.class));
		CarrierVehicleType typeLight = typeBuilderLight
				.setCapacity(3000).setFixCost(1168)
				.setCostPerDistanceUnit(4.22E-3)
				.setCostPerTimeUnit(0.089)
				.build();
		org.matsim.contrib.freight.carrier.CarrierVehicle.Builder carrierLVehicleBuilder = CarrierVehicle.Builder.newInstance(Id.createVehicleId("light"), carrierLocation);
		CarrierVehicle light = carrierLVehicleBuilder
				.setEarliestStart(Time.parseTime("06:00:00"))
				.setLatestEnd(Time.parseTime("18:00:00"))
				.setType(typeLight)
				.setTypeId(typeLight.getId())
				.build();

		/* Assign vehicles to carrier. */
		carrier.getCarrierCapabilities().getCarrierVehicles().add(heavy);
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeHeavy);
		carrier.getCarrierCapabilities().getCarrierVehicles().add(light);	
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeLight);
		LOG.info("Added different vehicle types to the carrier.");

		CarrierVehicleTypes types = new CarrierVehicleTypes();
		types.getVehicleTypes().put(typeLight.getId(), typeLight);
		types.getVehicleTypes().put(typeHeavy.getId(), typeHeavy);

		Carriers carriers = new Carriers();
		carriers.addCarrier(carrier);
		return carriers;
	}


	/**
	 * Selects a random link in the network.
	 * @param network
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Id<Link> selectRandomLink(Network network){
		Object[] linkIds = network.getLinks().keySet().toArray();
		int sample = MatsimRandom.getRandom().nextInt(linkIds.length);
		Object o = linkIds[sample];
		//System.out.println("random link "+ o);
		Id<Link> linkId = null;
		if(o instanceof Id<?>){
			linkId = (Id<Link>) o;
			return linkId;
		} else{
			throw new RuntimeException("Oops, cannot find a correct link Id.");
		}
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
		ReceiverProduct rProd = builder.setReorderingPolicy(new SSReorderPolicy(minLevel, maxLevel)).setProductType(productType).build();
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
		//order.setDailyOrderQuantity(order.getOrderQuantity()/order.getNumberOfWeeklyDeliveries());
		
//		LOG.info("Created an order of type " + order.getOrderName() 
//		+ " for receiver " + order.getReceiver().getId() + " with order quantity of " 
//		+ order.getOrderQuantity() + " tonnes, to " + order.getReceiver().getLinkId().toString() 
//		+ ", and service duration of " + Time.writeTime(order.getServiceDuration()) + ".");
		return order;
	}

	private static TimeWindow selectRandomTimeStart(int tw) {
		int min = 06;
		int max = 18;
		Random randomTime = new Random();
		int randomStart =  (min +
				randomTime.nextInt(max - tw - min + 1));
		final TimeWindow randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + tw*3600);
		return randomTimeWindow;
	}

}
