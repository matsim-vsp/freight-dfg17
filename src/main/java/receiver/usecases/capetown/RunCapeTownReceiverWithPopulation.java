/* *********************************************************************** *
 * project: org.matsim.*
 * RunReceiver.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.router.NetworkRoutingProvider;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Time;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;

import receiver.Receiver;
import receiver.ReceiverAttributes;
import receiver.ReceiverUtils;
import receiver.ReceiversWriter;
import receiver.product.Order;
import receiver.product.ReceiverOrder;
import receiver.usecases.ReceiverScoreStats;

/**
 * Specific example for my (wlbean) thesis chapters 5 and 6.
 * @author jwjoubert, wlbean
 */

public class RunCapeTownReceiverWithPopulation {
	final private static Logger LOG = Logger.getLogger(RunCapeTownReceiverWithPopulation.class);
	final private static long SEED_BASE = 20180816l;	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int startRun = Integer.parseInt(args[0]);
		int endRun = Integer.parseInt(args[1]);
		for(int i = startRun; i < endRun; i++) {
			run(i);
		}
	}


	public static void run(int run) {
		LOG.info("Starting run " + run);
		String outputfolder = String.format("./output/capetown/run_%03d/", run);
		new File(outputfolder).mkdirs();
		Scenario sc = CapeTownScenarioBuilder.createCapeTownScenarioWithPassengers(SEED_BASE*run, run, true);
		
		/* Write headings */
		BufferedWriter bw = IOUtils.getBufferedWriter(sc.getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv");
		try {
			bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
					"iteration", 
					"receiver_id", 
					"score", 
					"timewindow_start", 
					"timewindow_end", 
					"order_id", 
					"volume", 	        				
					"frequency", 
					"serviceduration",
					"collaborate_p",
					"collaborate_r",
					"grandCoalitionMember"));
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot write initial headings");  
		} finally{
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot close receiver stats file");
			}
		}

		sc.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

		Controler controler = new Controler(sc);

		/* Set up freight portion. To be repeated every iteration*/
		setupReceiverAndCarrierReplanning(controler, outputfolder);
		/* Add travel time binding for "commercial" mode. */
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				this.addTravelTimeBinding("commercial").to(networkTravelTime());
				this.addTravelDisutilityFactoryBinding("commercial").to(carTravelDisutilityFactoryKey());
				this.addRoutingModuleBinding("commercial").toProvider(new NetworkRoutingProvider("commercial", "car"));
			}
		});
		/* Setup generic subpopulation replanning. */
		/* Generic strategy */
		Config config = controler.getConfig();
		StrategySettings changeExpBetaStrategySettings = new StrategySettings(ConfigUtils.createAvailableStrategyId(config));
		changeExpBetaStrategySettings.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
		changeExpBetaStrategySettings.setWeight(0.8);
		config.strategy().addStrategySettings(changeExpBetaStrategySettings);
		/* People subpopulation strategy. */
		StrategySettings peopleChangeExpBeta = new StrategySettings(ConfigUtils.createAvailableStrategyId(config));
		peopleChangeExpBeta.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
		peopleChangeExpBeta.setWeight(0.70);
		peopleChangeExpBeta.setSubpopulation("private");
		config.strategy().addStrategySettings(peopleChangeExpBeta);
		/* People subpopulation ReRoute. Switch off after a time. */
		StrategySettings peopleReRoute = new StrategySettings(ConfigUtils.createAvailableStrategyId(config));
		peopleReRoute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute.toString());
		peopleReRoute.setWeight(0.10);
		peopleReRoute.setSubpopulation("private");
		peopleReRoute.setDisableAfter(85);
		config.strategy().addStrategySettings(peopleReRoute);
		/* People subpopulation Time allocation mutator AND ReRoute. Switch off after a time. */
		StrategySettings peopleTimeAndReroute = new StrategySettings(ConfigUtils.createAvailableStrategyId(config));
		peopleTimeAndReroute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.TimeAllocationMutator_ReRoute.toString());
		peopleTimeAndReroute.setWeight(0.20);
		peopleTimeAndReroute.setSubpopulation("private");
		peopleTimeAndReroute.setDisableAfter(85);
		config.strategy().addStrategySettings(peopleTimeAndReroute);
		/* Commercial subpopulation strategy. */
		StrategySettings commercialStrategy = new StrategySettings(ConfigUtils.createAvailableStrategyId(config));
		commercialStrategy.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
		commercialStrategy.setWeight(0.85);
		commercialStrategy.setSubpopulation("commercial");
		config.strategy().addStrategySettings(commercialStrategy);
		/* Commercial subpopulation ReRoute. Switch off after a time. */
		StrategySettings commercialReRoute = new StrategySettings(ConfigUtils.createAvailableStrategyId(config));
		commercialReRoute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute.toString());
		commercialReRoute.setWeight(0.15);
		commercialReRoute.setSubpopulation("commercial");
		commercialReRoute.setDisableAfter(85);
		config.strategy().addStrategySettings(commercialReRoute);
		
		config.qsim().setEndTime(Time.parseTime("48:00:00"));
		
		/* Take out all commercial vehicles. FIXME This must be sorted. */
		List<Id<Person>> ids = new ArrayList<>();
		for(Id<Person> pId : sc.getPopulation().getPersons().keySet()){
			if(pId.toString().startsWith("coct_c")){
				ids.add(pId);
			}
		}
		for(Id<Person> pId : ids){
			sc.getPopulation().removePerson(pId);
		}
		
		LOG.info("========> Removed " + ids.size() + " commercial vehicles!!");
		
		
		
		CapeTownReceiverUtils.setupCarriers(controler);
		CapeTownReceiverUtils.setupReceivers(controler);

		/* TODO This stats must be set up automatically. */
		prepareFreightOutputDataAndStats(controler, run);

		controler.run();
	}


	private static void setupReceiverAndCarrierReplanning( MatsimServices controler, String outputFolder) {
		controler.addControlerListener(new IterationStartsListener() {

			//@Override
			public void notifyIterationStarts(IterationStartsEvent event) {
				
				if(event.getIteration() % ReceiverUtils.getReplanInterval( controler.getScenario() ) != 0) {
					return;
				}

				/* Adds the receiver agents that are part of the current (sub)coalition. */
				for (Receiver receiver : ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().values()){
					if (receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()) != null){
						if ((boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()) == true){
							if (!ReceiverUtils.getCoalition( controler.getScenario() ).getReceiverCoalitionMembers().contains(receiver)){
								ReceiverUtils.getCoalition( controler.getScenario() ).addReceiverCoalitionMember(receiver);
							}
						} else {
							if ( ReceiverUtils.getCoalition( controler.getScenario() ).getReceiverCoalitionMembers().contains(receiver)){
								ReceiverUtils.getCoalition( controler.getScenario() ).removeReceiverCoalitionMember(receiver);
							}
						}
					}
				}

				/*
				 * Carrier replan with receiver changes.
				 */
				
				Carrier carrier = ReceiverUtils.getCarriers( controler.getScenario() ).getCarriers().get(Id.create("Carrier1", Carrier.class));
				ArrayList<CarrierPlan> carrierPlans = new ArrayList<CarrierPlan>();

				/* Remove all existing carrier plans. */

				for (CarrierPlan plan : carrier.getPlans()){
					carrierPlans.add(plan);
				}

				Iterator<CarrierPlan> planIterator = carrierPlans.iterator();
				while (planIterator.hasNext()){
					CarrierPlan plan = planIterator.next();							
					carrier.removePlan(plan);
				}


				VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, controler.getScenario().getNetwork());

				NetworkBasedTransportCosts netBasedCosts = NetworkBasedTransportCosts.Builder.newInstance(controler.getScenario().getNetwork(), carrier.getCarrierCapabilities().getVehicleTypes()).build();
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

				//create a new carrierPlan from the solution 
				CarrierPlan newPlan = MatsimJspritFactory.createPlan(carrier, solution);

				//route plan 
				NetworkRouter.routePlan(newPlan, netBasedCosts);


				//assign this plan now to the carrier and make it the selected carrier plan
				carrier.setSelectedPlan(newPlan);
	
				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(controler.getScenario().getConfig().controler().getOutputDirectory() + "carriers.xml");
				new ReceiversWriter( ReceiverUtils.getReceivers( controler.getScenario() ) ).write(controler.getScenario().getConfig().controler().getOutputDirectory() + "receivers.xml");

			}

		});		
	}

	private static void prepareFreightOutputDataAndStats( MatsimServices controler, int run) {

		/*
		 * Adapted from RunChessboard.java by sshroeder and gliedtke.
		 */
		final int statInterval = CapeTownExperimentParameters.STAT_INTERVAL;
		
		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
		ReceiverScoreStats rScoreStats = new ReceiverScoreStats(controler.getScenario().getConfig().controler().getOutputDirectory() + "/receiver_scores", true);

		controler.addControlerListener(scoreStats);
		controler.addControlerListener(rScoreStats);
		controler.addControlerListener(new VehicleTypeListener(controler.getScenario(), run));
		controler.addControlerListener(new IterationEndsListener() {

			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				String dir = event.getServices().getControlerIO().getIterationPath(event.getIteration());

				if((event.getIteration() + 1) % (statInterval) != 0) return;
				
				for(int i = 1; i < (ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().size()+1); i++) {
					Receiver receiver = ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().get(Id.create(Integer.toString(i), Receiver.class));
//					receiver.getSelectedPlan().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), (boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()));
					receiver.getSelectedPlan().setCollaborationStatus((boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()));					}


				//write plans
				
				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(dir + "/" + event.getIteration() + ".carrierPlans.xml");
				
				new ReceiversWriter( ReceiverUtils.getReceivers( controler.getScenario() ) ).write(dir + "/" + event.getIteration() + ".receivers.xml");

				/* Record receiver stats */
				int numberOfReceivers = ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().size();
				for(int i = 1; i < numberOfReceivers+1; i++) {
					Receiver receiver = ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().get(Id.create(Integer.toString(i), Receiver.class));
//					if(event.getIteration() == (CapeTownExperimentParameters.REPLAN_INTERVAL-1)){
//						receiver.getSelectedPlan().setCollaborationStatus((boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString())); 
//					}
					for (ReceiverOrder rorder :  receiver.getSelectedPlan().getReceiverOrders()){
						for (Order order : rorder.getReceiverProductOrders()){
							String score = receiver.getSelectedPlan().getScore().toString();
							float start = (float) receiver.getSelectedPlan().getTimeWindows().get(0).getStart();
							float end = (float) receiver.getSelectedPlan().getTimeWindows().get(0).getEnd();
							float size = (float) (order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity());
							float freq = (float) order.getNumberOfWeeklyDeliveries();
							float dur =  (float) order.getServiceDuration();
							boolean status = (boolean) receiver.getSelectedPlan().getCollaborationStatus();
							boolean status2 = (boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString());							
							boolean member = (boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.grandCoalitionMember.toString());

							BufferedWriter bw1 = IOUtils.getAppendingBufferedWriter(controler.getScenario().getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv");
							try {
								bw1.write(String.format("%d,%s,%s,%f,%f,%s,%f,%f,%f,%b,%b,%b", 
										event.getIteration(), 
										receiver.getId(), 
										score, 
										start, 
										end,
										order.getId(), 
										size,
										freq,
										dur,
										status,
										status2,
										member));											 							
								bw1.newLine();

							} catch (IOException e) {
								e.printStackTrace();
								throw new RuntimeException("Cannot write receiver stats");    

							} finally{
								try {
									bw1.close();
								} catch (IOException e) {
									e.printStackTrace();
									throw new RuntimeException("Cannot close receiver stats file");
								}
							}	
						}	
					}
				}

			}
		});	

	}
}
