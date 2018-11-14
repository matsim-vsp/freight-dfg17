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
package receiver.usecases.capetown;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
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
import org.matsim.contrib.freight.carrier.ForwardingVehicleType;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;
import org.matsim.vehicles.VehicleType;
import receiver.Receiver;
import receiver.ReceiverAttributes;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.Receivers;
import receiver.ReceiversWriter;
import receiver.SSReorderPolicy;
import receiver.collaboration.MutableCoalition;
import receiver.product.Order;
import receiver.product.ProductType;
import receiver.product.ReceiverOrder;
import receiver.product.ReceiverProduct;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Various utilities for building receiver scenarios (for now).
 * 
 * @author jwjoubert, wlbean
 */
public class CapeTownScenarioBuilder {
	private final static Logger LOG = Logger.getLogger(CapeTownScenarioBuilder.class);


	/**
	 * Build the entire chessboard example.
	 */
	public static Scenario createCapeTownScenario( long seed, int run, boolean write) {
		MatsimRandom.reset(seed);
		//		int numberOfReceivers = CapeTownExperimentParameters.NUMBER_OF_RECEIVERS;
		Scenario sc = setupCapeTownScenario(seed, run);
		createCapeTownCarriersAndAddToScenario(sc);

		ReceiverUtils.setReplanInterval(CapeTownExperimentParameters.REPLAN_INTERVAL, sc );


		/* To split up our "real" facilities into a study and control group, we
		 * have to do that PRIOR to creating the receivers. */
		for(ActivityFacility facility : sc.getActivityFacilities().getFacilities().values()) {
//			if((boolean) facility.getAttributes().getAttribute("PnP") == true){
				if(facility.getAttributes().getAttribute("PnP") != null){
			if(facility.getId().equals(Id.create("0", ActivityFacility.class))) {
				/* Ignore the depot where the carrier resides. */
			} else{
				double rnd = MatsimRandom.getLocalInstance().nextDouble();
				if(rnd <= CapeTownExperimentParameters.PROPORTION_CORPORATE) {
					facility.getAttributes().putAttribute("corporate", true);
				} else {
					facility.getAttributes().putAttribute("corporate", false);
				}
			}
			}
		}

		/* Create the grand coalition receiver members and allocate orders. */
		createAndAddReceivers(sc);

		createReceiverOrders(sc);

		/* Let jsprit do its magic and route the given receiver orders. */
		generateCarrierPlan( sc );


		if(write) {
			writeFreightScenario(sc);
		}

		/* Link the carriers to the receivers. */
		ReceiverUtils.getReceivers( sc ).linkReceiverOrdersToCarriers( ReceiverUtils.getCarriers( sc ) );

		/* Add carrier and receivers to coalition */
		MutableCoalition coalition = new MutableCoalition();

		for (Carrier carrier : ReceiverUtils.getCarriers( sc ).getCarriers().values()){
			if (!coalition.getCarrierCoalitionMembers().contains(carrier)){
				coalition.addCarrierCoalitionMember(carrier);
			}
		}

		for (Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()){
			if ((boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()) == true){
				if (!coalition.getReceiverCoalitionMembers().contains(receiver)){
					coalition.addReceiverCoalitionMember(receiver);
				}
			} else {
				if (coalition.getReceiverCoalitionMembers().contains(receiver)){
					coalition.removeReceiverCoalitionMember(receiver);
				}
			}
		}

		ReceiverUtils.setCoalition( coalition, sc );
		
		return sc;
	}
	
	public static Scenario createCapeTownScenarioWithPassengers( long seed, int run, boolean write) {
		MatsimRandom.reset(seed);
		//		int numberOfReceivers = CapeTownExperimentParameters.NUMBER_OF_RECEIVERS;
		Scenario sc = setupCapeTownScenarioWithPassengers(seed, run);
		createCapeTownCarriersAndAddToScenario(sc);

		ReceiverUtils.setReplanInterval(CapeTownExperimentParameters.REPLAN_INTERVAL, sc );


		/* To split up our "real" facilities into a study and control group, we
		 * have to do that PRIOR to creating the receivers. */
		
		for(ActivityFacility facility : sc.getActivityFacilities().getFacilities().values()) {
			if(facility.getAttributes().getAttribute("PnP") != null){
			if(facility.getId().equals(Id.create("0", ActivityFacility.class))) {
				/* Ignore the depot where the carrier resides. */
			} else{
				double rnd = MatsimRandom.getLocalInstance().nextDouble();
				if(rnd <= CapeTownExperimentParameters.PROPORTION_CORPORATE) {
					facility.getAttributes().putAttribute("corporate", true);
				} else {
					facility.getAttributes().putAttribute("corporate", false);
				}
			}
			}
		}

		/* Create the grand coalition receiver members and allocate orders. */
		createAndAddReceivers(sc);
		
		createReceiverOrders(sc);

		/* Let jsprit do its magic and route the given receiver orders. */
		
		sc.getConfig().facilities().setInputFile("./facilities_used.xml.gz");
		generateCarrierPlan( sc );


		if(write) {
			writeFreightScenario(sc);
		}

		/* Link the carriers to the receivers. */
		ReceiverUtils.getReceivers( sc ).linkReceiverOrdersToCarriers( ReceiverUtils.getCarriers( sc ) );

		/* Add carrier and receivers to coalition */
		MutableCoalition coalition = new MutableCoalition();

		for (Carrier carrier : ReceiverUtils.getCarriers( sc ).getCarriers().values()){
			if (!coalition.getCarrierCoalitionMembers().contains(carrier)){
				coalition.addCarrierCoalitionMember(carrier);
			}
		}

		for (Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()){
			if ((boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()) == true){
				if (!coalition.getReceiverCoalitionMembers().contains(receiver)){
					coalition.addReceiverCoalitionMember(receiver);
				}
			} else {
				if (coalition.getReceiverCoalitionMembers().contains(receiver)){
					coalition.removeReceiverCoalitionMember(receiver);
				}
			}
		}

		ReceiverUtils.setCoalition( coalition, sc );
		
		

		return sc;
	}


	private static Scenario setupCapeTownScenarioWithPassengers(long seed, int run) {
//		Config config = ConfigUtils.createConfig();
		String configFile = "scenarios/capeTown/config.xml";
        Config config = ConfigUtils.loadConfig(configFile);
//        Scenario scenario = ScenarioUtils.loadScenario(config);

		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(CapeTownExperimentParameters.NUM_ITERATIONS);
		config.controler().setMobsim("qsim");
		config.controler().setWriteSnapshotsInterval(CapeTownExperimentParameters.STAT_INTERVAL);
		config.global().setRandomSeed(seed);
		config.network().setInputFile("./network.xml.gz");
		config.controler().setOutputDirectory(String.format("./output/capetown/run_%03d/", run));		
		
		
//		config.facilities().setInputFile("./facilities.xml");
		config.facilities().setInputFile("./facilities_used.xml.gz");
		
		Scenario sc = ScenarioUtils.loadScenario(config);
		
		/* Create a list of the receiver locations. */
//		List<Id<ActivityFacility>> rIds = new ArrayList<>();
		List<ActivityFacility> rFac = new ArrayList<>();
		for(ActivityFacility fac : sc.getActivityFacilities().getFacilities().values()){
			if(fac.getAttributes().toString().contains("Pick")){
				rFac.add(fac);
			}
		}
//		for(Id<ActivityFacility> facId : sc.getActivityFacilities().getFacilities().keySet()){
//			if(facId.toString().startsWith("Pick")){
//				rIds.add(facId);
//			}
//		}
		for(ActivityFacility fac : rFac){
			fac.getAttributes().putAttribute("PnP", true);
		}
//		sc.getConfig().facilities().setInputFile("scenarios/capeTown/facilities_used.xml.gz");
		
		PopulationReader reader = new PopulationReader(sc);
		reader.readFile("scenarios/capeTown/population_020.xml.gz");
		
		
//		ObjectAttributesXmlReader reader2 = new ObjectAttributesXmlReader(sc.getPopulation().getPersonAttributes());
//		reader2.readFile("scenarios/capeTown/populationAttributes.xml.gz");

		return sc;
	}

	/**
	 * FIXME Need to complete this. 
	 * @return
	 */
	public static Scenario setupCapeTownScenario(long seed, int run) {		
		Config config = ConfigUtils.createConfig();
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(CapeTownExperimentParameters.NUM_ITERATIONS);
		config.controler().setMobsim("qsim");
		config.controler().setWriteSnapshotsInterval(CapeTownExperimentParameters.STAT_INTERVAL);
		config.global().setRandomSeed(seed);
		config.network().setInputFile("./scenarios/capeTown/network.xml.gz");
		config.controler().setOutputDirectory(String.format("./output/capetown/run_%03d/", run));		
		config.facilities().setInputFile("./scenarios/capeTown/facilities.xml.gz");
		
		Scenario sc = ScenarioUtils.loadScenario(config);
		for (Id<ActivityFacility> facility : sc.getActivityFacilities().getFacilities().keySet()){
			sc.getActivityFacilities().getFacilityAttributes().putAttribute(facility.toString(), "PnP", true);
		}

		return sc;
	}

	

	public static void writeFreightScenario( Scenario sc ) {
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
	 * Route the services that are allocated to the carrier and writes the initial carrier plans.
	 * 
	 * @param carriers
	 * @param network
	 */
	public static void generateCarrierPlan(Scenario sc ) {
		Carrier carrier = ReceiverUtils.getCarriers(sc).getCarriers().get(Id.create("Carrier1", Carrier.class)); 

		VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, sc.getNetwork());

		NetworkBasedTransportCosts netBasedCosts = NetworkBasedTransportCosts.Builder.newInstance(sc.getNetwork(), carrier.getCarrierCapabilities().getVehicleTypes()).build();
		VehicleRoutingProblem vrp = vrpBuilder.setRoutingCost(netBasedCosts).build();

		//read and create a pre-configured algorithms to solve the vrp
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "./scenarios/chessboard/vrpalgo/initialPlanAlgorithm.xml");

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

	}

	/**
	 * Creates the product orders for the receiver agents in the simulation. Currently (28/08/18) all the receivers have the same orders 
	 * for experiments, but this must be adapted in the future to accept other parameters as inputs to enable different orders per receiver. 
	 * @param fs
	 */
	public static void createReceiverOrders( Scenario sc ) {
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
		
		ProductType productTypeThree = receivers.createAndAddProductType(Id.create("P3", ProductType.class));
		productTypeThree.setDescription("Product 3");
		productTypeThree.setRequiredCapacity(3);

		for ( int r = 1 ; r < ReceiverUtils.getReceivers( sc ).getReceivers().size()+1 ; r++){
			int tw = CapeTownExperimentParameters.TIME_WINDOW_DURATION;
			String serdur = CapeTownExperimentParameters.SERVICE_TIME;
			int numDel = CapeTownExperimentParameters.NUM_DELIVERIES;

			/* Create receiver-specific products */
			Receiver receiver = receivers.getReceivers().get(Id.create(Integer.toString(r), Receiver.class));
			
			ReceiverProduct receiverProductOne;
			ReceiverProduct receiverProductTwo;
			ReceiverProduct receiverProductThree;
			
			if((boolean) receiver.getAttributes().getAttribute("corporate") == true){
				receiverProductOne = createReceiverProduct(receiver, productTypeOne, 1000, 9000);
				receiverProductTwo = createReceiverProduct(receiver, productTypeTwo, 750, 4500);
				receiverProductThree = createReceiverProduct(receiver, productTypeThree, 1000, 6000);
				receiver.getProducts().add(receiverProductOne);
				receiver.getProducts().add(receiverProductTwo);
				receiver.getProducts().add(receiverProductThree);						
			} else {
				receiverProductOne = createReceiverProduct(receiver, productTypeOne, 750, 6000);
				receiverProductTwo = createReceiverProduct(receiver, productTypeTwo, 500, 3000);
				receiverProductThree = createReceiverProduct(receiver, productTypeThree, 750, 4000);
				receiver.getProducts().add(receiverProductOne);
				receiver.getProducts().add(receiverProductTwo);
				receiver.getProducts().add(receiverProductThree);
			}

			/* Generate and collate orders for the different receiver/order combination. */
			Order rOrder1 = createProductOrder(Id.create("Order"+Integer.toString(r)+"1",  Order.class), receiver, 
					receiverProductOne, Time.parseTime(serdur));
			rOrder1.setNumberOfWeeklyDeliveries(numDel);
			Order rOrder2 = createProductOrder(Id.create("Order"+Integer.toString(r)+"2",  Order.class), receiver, 
					receiverProductTwo, Time.parseTime(serdur));
			rOrder2.setNumberOfWeeklyDeliveries(numDel);
			Order rOrder3 = createProductOrder(Id.create("Order"+Integer.toString(r)+"3",  Order.class), receiver, 
					receiverProductThree, Time.parseTime(serdur));
			rOrder3.setNumberOfWeeklyDeliveries(numDel);
			Collection<Order> rOrders = new ArrayList<Order>();
			rOrders.add(rOrder1);
			rOrders.add(rOrder2);
			rOrders.add(rOrder3);

			/* Combine product orders into single receiver order for a specific carrier. */
			if ((boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()) == true){
				ReceiverOrder receiverOrder = new ReceiverOrder(receiver.getId(), rOrders, carrierOne.getId());
				ReceiverPlan receiverPlan = ReceiverPlan.Builder.newInstance(receiver, true)
						.addReceiverOrder(receiverOrder)
						.addTimeWindow(selectRandomNightTimeStart(tw, receiver))
//						.addTimeWindow(selectRandomDayTimeStart(tw))
						.build();
//				receiverPlan.setCollaborationStatus(true); 
				receiver.setSelectedPlan(receiverPlan);
				receiver.getSelectedPlan().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), true);

				/* Convert receiver orders to initial carrier services. */
				for(Order order : receiverOrder.getReceiverProductOrders()){
					order.setDailyOrderQuantity(order.getOrderQuantity()/order.getNumberOfWeeklyDeliveries());
					org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
							Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());

					if(receiverPlan.getTimeWindows().size() > 1) {
						LOG.warn("Multiple time windows set. Only the first is used");
					}

					CarrierService newService = serBuilder
							.setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
							setServiceStartTimeWindow(receiverPlan.getTimeWindows().get(0)).
							setServiceDuration(order.getServiceDuration()).
							build();
					carriers.getCarriers().get(receiverOrder.getCarrierId()).getServices().add(newService);	
				}

			} else {

				ReceiverOrder receiverOrder = new ReceiverOrder(receiver.getId(), rOrders, carrierOne.getId());
				ReceiverPlan receiverPlan = ReceiverPlan.Builder.newInstance(receiver,false)
						.addReceiverOrder(receiverOrder)
						.addTimeWindow(selectRandomDayTimeStart(tw))
						.build();
//				receiverPlan.setCollaborationStatus();
				receiver.setSelectedPlan(receiverPlan);
				receiver.getSelectedPlan().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), false);

				/* Convert receiver orders to initial carrier services. */
				for(Order order : receiverOrder.getReceiverProductOrders()){
					order.setDailyOrderQuantity(order.getOrderQuantity()/order.getNumberOfWeeklyDeliveries());
					org.matsim.contrib.freight.carrier.CarrierService.Builder serBuilder = CarrierService.
							Builder.newInstance(Id.create(order.getId(),CarrierService.class), order.getReceiver().getLinkId());

					if(receiverPlan.getTimeWindows().size() > 1) {
						LOG.warn("Multiple time windows set. Only the first is used");
					}

					CarrierService newService = serBuilder.
							setCapacityDemand((int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity()))).
							setServiceStartTimeWindow(receiverPlan.getTimeWindows().get(0)).
							setServiceDuration(order.getServiceDuration()).
							build();
					carriers.getCarriers().get(receiverOrder.getCarrierId()).getServices().add(newService);	
				}
			}

		}

	}

	/**
	 * Creates and adds the receivers that are part of the grand coalition. These receivers are allowed to replan
	 * their orders as well as decided to join or leave the coalition.
	 * @param fs
	 * @param numberOfReceivers
	 */
	public static void createAndAddReceivers( Scenario sc) {

		Receivers receivers = new Receivers();
		receivers.setDescription("Corporate");
		int receiverIndex = 1;
		for(ActivityFacility facility : sc.getActivityFacilities().getFacilities().values()) {
			/* Only use PnP facility locations for receivers. */
			if(facility.getAttributes().getAttribute("PnP") != null){
//			if((boolean) facility.getAttributes().getAttribute("PnP") == true){
				if(facility.getId().equals(Id.create("0", ActivityFacility.class))) {
					/* Ignore the depot where carrier resides. */
				} else {
					if((boolean) facility.getAttributes().getAttribute("corporate")) {
						/* Corporates */
						Link receiverLink = FacilitiesUtils.decideOnLink(facility, sc.getNetwork());
						Receiver receiver = ReceiverUtils.newInstance(Id.create(Integer.toString(receiverIndex++), Receiver.class))
								.setLinkId(receiverLink.getId());
						receiver.getAttributes().putAttribute(ReceiverAttributes.grandCoalitionMember.toString(), true);
						double rnd2 = MatsimRandom.getLocalInstance().nextDouble();
						if(rnd2 <= 0.75) {
							receiver.getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), true);	
						} else {
							receiver.getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), false);	
						}
						//					receiver.getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), false);	
						receiver.getAttributes().putAttribute("corporate", true);
						receivers.addReceiver(receiver);
					} else {
						/* Franchises */
						Link receiverLink = FacilitiesUtils.decideOnLink(facility, sc.getNetwork());
						Receiver receiver = ReceiverUtils.newInstance(Id.create(Integer.toString(receiverIndex++), Receiver.class))
								.setLinkId(receiverLink.getId());
						receiver.getAttributes().putAttribute(ReceiverAttributes.grandCoalitionMember.toString(), false);
						receiver.getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), false);
						receiver.getAttributes().putAttribute("corporate", false);
						receivers.addReceiver(receiver);
					}
				}
			}
		}

		ReceiverUtils.setReceivers( receivers, sc );
	}


	/**
	 * Creates the carrier agents for the simulation.
	 * @param sc
	 * @return
	 */
	public static void createCapeTownCarriersAndAddToScenario(Scenario sc) {
		Id<Carrier> carrierId = Id.create("Carrier1", Carrier.class);
		Carrier carrier = CarrierImpl.newInstance(carrierId);

		/* Set its "real" location. */
		Link carrierLink = FacilitiesUtils.decideOnLink(sc.getActivityFacilities().getFacilities().get(Id.create("0", ActivityFacility.class)), sc.getNetwork());
		Id<Link> carrierLocation = carrierLink.getId();
		org.matsim.contrib.freight.carrier.CarrierCapabilities.Builder capBuilder = CarrierCapabilities.Builder.newInstance();
		CarrierCapabilities carrierCap = capBuilder.setFleetSize(FleetSize.INFINITE).build();
		carrier.setCarrierCapabilities(carrierCap);						
		LOG.info("Created a carrier with capabilities.");	

		/*
		 * Create the carrier vehicle types. 
		 * TODO This might, potentially, be read from XML file. 
		 */

		/* Heavy vehicle (28 tonnes). */
		org.matsim.contrib.freight.carrier.CarrierVehicleType.Builder typeBuilderHeavy = CarrierVehicleType.Builder.newInstance(Id.create("heavy", VehicleType.class));
		CarrierVehicleType typeHeavy = typeBuilderHeavy
				.setCapacity(26000)
				.setFixCost(3097)
				.setCostPerDistanceUnit(8.99E-3)
				.setCostPerTimeUnit(0.2072)
				.build();		
		org.matsim.contrib.freight.carrier.CarrierVehicle.Builder carrierHVehicleBuilder = CarrierVehicle.Builder.newInstance(Id.createVehicleId("heavy"), carrierLocation);
		CarrierVehicle heavy = carrierHVehicleBuilder
				.setEarliestStart(Time.parseTime("06:00:00"))
				.setLatestEnd(Time.parseTime("30:00:00"))
				.setType(typeHeavy)
				.setTypeId(typeHeavy.getId())
				.build();
		
		/* Medium vehicle (14 tonnes). */		
		org.matsim.contrib.freight.carrier.CarrierVehicleType.Builder typeBuilderMedium = CarrierVehicleType.Builder.newInstance(Id.create("medium", VehicleType.class));
		CarrierVehicleType typeMedium = typeBuilderMedium
				.setCapacity(14000)
				.setFixCost(2893)
				.setCostPerDistanceUnit(8.32E-3)
				.setCostPerTimeUnit(0.2072)
				.build();
		org.matsim.contrib.freight.carrier.CarrierVehicle.Builder carrierMVehicleBuilder = CarrierVehicle.Builder.newInstance(Id.createVehicleId("medium"), carrierLocation);
		CarrierVehicle medium = carrierMVehicleBuilder
				.setEarliestStart(Time.parseTime("06:00:00"))
				.setLatestEnd(Time.parseTime("30:00:00"))
				.setType(typeMedium)
				.setTypeId(typeMedium.getId())
				.build();


		/* Light vehicle (8 tonnes). */
		org.matsim.contrib.freight.carrier.CarrierVehicleType.Builder typeBuilderLight = CarrierVehicleType.Builder.newInstance(Id.create("light", VehicleType.class));
		CarrierVehicleType typeLight = typeBuilderLight
				.setCapacity(8000)
				.setFixCost(1887)
				.setCostPerDistanceUnit(6.21E-3)
				.setCostPerTimeUnit(0.1083)
				.build();
		org.matsim.contrib.freight.carrier.CarrierVehicle.Builder carrierLVehicleBuilder = CarrierVehicle.Builder.newInstance(Id.createVehicleId("light"), carrierLocation);
		CarrierVehicle light = carrierLVehicleBuilder
				.setEarliestStart(Time.parseTime("06:00:00"))
				.setLatestEnd(Time.parseTime("30:00:00"))
				.setType(typeLight)
				.setTypeId(typeLight.getId())
				.build();

		/* Assign vehicles to carrier. */
		carrier.getCarrierCapabilities().getCarrierVehicles().add(heavy);
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeHeavy);
		carrier.getCarrierCapabilities().getCarrierVehicles().add(medium);
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeMedium);		
		carrier.getCarrierCapabilities().getCarrierVehicles().add(light);	
		carrier.getCarrierCapabilities().getVehicleTypes().add(typeLight);
		LOG.info("Added different vehicle types to the carrier.");

		CarrierVehicleTypes types = new CarrierVehicleTypes();
		types.getVehicleTypes().put(typeLight.getId(), typeLight);
		types.getVehicleTypes().put(typeMedium.getId(), typeMedium);
		types.getVehicleTypes().put(typeHeavy.getId(), typeHeavy);

		Carriers carriers = new Carriers();
		carriers.addCarrier(carrier);
		ReceiverUtils.setCarriers(carriers, sc);
	}


//	/**
//	 * Selects a random link in the network.
//	 * @param network
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	private static Id<Link> selectRandomLink(Network network){
//		Object[] linkIds = network.getLinks().keySet().toArray();
//		int sample = MatsimRandom.getRandom().nextInt(linkIds.length);
//		Object o = linkIds[sample];
//		Id<Link> linkId = null;
//		if(o instanceof Id<?>){
//			linkId = (Id<Link>) o;
//			return linkId;
//		} else{
//			throw new RuntimeException("Oops, cannot find a correct link Id.");
//		}
//	}

	/**
	 * Selects a random link in the network.
	 * @param network
	 * @return
	 */
	//	@SuppressWarnings("unchecked")
	//	private static Id<Link> selectRandomLink(Scenario sc){
	//		ActivityFacilities facilities = sc.getActivityFacilities();
	//		int sample = MatsimRandom.getRandom().nextInt(facilities.getFacilities().size());
	//		ActivityFacility facility = facilities.getFacilities().get(sample);
	//		Id<Link> locationId = null;
	//		return locationId;
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

	public static TimeWindow selectRandomDayTimeStart(int tw) {
		int min = 6;
		int max = 18;
//		Random randomTime = new Random();
		int randomStart =  (min +
				MatsimRandom.getRandom().nextInt(max - tw - min + 1));
		final TimeWindow randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + tw*3600);
		return randomTimeWindow;
	}

	public static TimeWindow selectRandomNightTimeStart(int tw, Receiver receiver) {
		int min = 18;
		int max = 30;
//		Random randomTime = new Random();
		int randomStart =  (min +
				MatsimRandom.getRandom().nextInt(max - tw - min + 1));
		final TimeWindow randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + tw*3600);
		return randomTimeWindow;
	}


	



}
