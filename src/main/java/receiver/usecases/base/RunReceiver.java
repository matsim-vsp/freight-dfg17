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
package receiver.usecases.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.utils.io.IOUtils;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;

import receiver.MutableFreightScenario;
import receiver.Receiver;
import receiver.ReceiversWriter;
import receiver.product.Order;
import receiver.product.ReceiverOrder;
import receiver.usecases.ReceiverChessboardUtils;
import receiver.usecases.ReceiverScoreStats;

/**
 * Specific example for my (wlbean) thesis chapters 5 and 6.
 * @author jwjoubert, wlbean
 */

public class RunReceiver {
	final private static Logger LOG = Logger.getLogger(RunReceiver.class);
	final private static long SEED_BASE = 20180816l;	
//	private static int replanInt;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numberOfRuns = Integer.parseInt(args[0]);
		for(int i = 0; i < numberOfRuns; i++) {
			run(i);
		}
	}


	public static void run(int run) {

		String outputfolder = String.format("./output/base/tw/run_%03d/", run);
		new File(outputfolder).mkdirs();
		MutableFreightScenario mfs = ReceiverChessboardScenario.createChessboardScenario(SEED_BASE*run, run, true);
//		replanInt = mfs.getReplanInterval();
		
		/* Write headings */
		BufferedWriter bw = IOUtils.getBufferedWriter(mfs.getScenario().getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv");
		try {
			bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
					"iteration", 
					"receiver_id", 
					"score", 
					"timewindow_start", 
					"timewindow_end", 
					"order_id", 
					"volume", 	        				
					"frequency", 
					"serviceduration",
					"collaborate",
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

		Scenario sc = mfs.getScenario();
		sc.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

		Controler controler = new Controler(sc);

		/* Set up freight portion.To be repeated every iteration*/
		MutableFreightScenario nmfs = receiverAndCarrierReplan(controler, mfs, outputfolder);
		
		ReceiverChessboardUtils.setupCarriers(controler, nmfs);
		ReceiverChessboardUtils.setupReceivers(controler, nmfs);

		/* TODO This stats must be set up automatically. */
		prepareFreightOutputDataAndStats(controler, nmfs, run);

		controler.run();
	}


	private static MutableFreightScenario receiverAndCarrierReplan( MatsimServices controler, MutableFreightScenario mfs, String outputFolder) {
		

		MutableFreightScenario fs = mfs;
		controler.addControlerListener(new IterationStartsListener() {

			//@Override
			public void notifyIterationStarts(IterationStartsEvent event) {

				if(event.getIteration() % mfs.getReplanInterval() != 0) {
					return;
				}

				/* Adds the receiver agents that are part of the current (sub)coalition. */
				for (Receiver receiver : mfs.getReceivers().getReceivers().values()){
					if (receiver.getAttributes().getAttribute("collaborationStatus") != null){
						if ((boolean) receiver.getAttributes().getAttribute("collaborationStatus") == true){
							if (!mfs.getCoalition().getReceiverCoalitionMembers().contains(receiver)){
								mfs.getCoalition().addReceiverCoalitionMember(receiver);
							}
						} else {
							if (mfs.getCoalition().getReceiverCoalitionMembers().contains(receiver)){
								mfs.getCoalition().removeReceiverCoalitionMember(receiver);
							}
						}
					}
				}

				/*
				 * Carrier replan with receiver changes.
				 */

				Carrier carrier = mfs.getCarriers().getCarriers().get(Id.create("Carrier1", Carrier.class)); 
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


				VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, mfs.getScenario().getNetwork());

				NetworkBasedTransportCosts netBasedCosts = NetworkBasedTransportCosts.Builder.newInstance(mfs.getScenario().getNetwork(), carrier.getCarrierCapabilities().getVehicleTypes()).build();
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

				//create a new carrierPlan from the solution 
				CarrierPlan newPlan = MatsimJspritFactory.createPlan(carrier, solution);

				//route plan 
				NetworkRouter.routePlan(newPlan, netBasedCosts);


				//assign this plan now to the carrier and make it the selected carrier plan
				carrier.setSelectedPlan(newPlan);

				//write out the carrierPlan to an xml-file
				new CarrierPlanXmlWriterV2(mfs.getCarriers()).write(mfs.getScenario().getConfig().controler().getOutputDirectory() + "../../../../input/carrierPlanned.xml");

				new CarrierPlanXmlWriterV2(mfs.getCarriers()).write(mfs.getScenario().getConfig().controler().getOutputDirectory() + "carriers.xml");
				new ReceiversWriter(mfs.getReceivers()).write(mfs.getScenario().getConfig().controler().getOutputDirectory() + "receivers.xml");

				fs.setCarriers(mfs.getCarriers());
				fs.setReceivers(mfs.getReceivers());
			}

		});		

		return fs;
	}


//	private static Carriers setupCarriers(Controler controler, FreightScenario fs) {
//		final Carriers carriers = new Carriers();							
//		new CarrierPlanXmlReaderV2(carriers).readFile(fs.getScenario().getConfig().controler().getOutputDirectory() + "carriers.xml");	
//		CarrierVehicleTypes types = new CarrierVehicleTypes();
//		new CarrierVehicleTypeReader(types).readFile(fs.getScenario().getConfig().controler().getOutputDirectory()  + "carrierVehicleTypes.xml");
//		new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(types);
//
//		Coalition coalition = fs.getCoalition();
//
//		for (Carrier carrier : fs.getCarriers().getCarriers().values()){
//			if (!coalition.getCarrierCoalitionMembers().contains(carrier)){
//				coalition.addCarrierCoalitionMember(carrier);
//			}
//		}
//
//		/* Create a new instance of a carrier scoring function factory. */
//		final CarrierScoringFunctionFactory cScorFuncFac = new MyCarrierScoringFunctionFactoryImpl(fs.getScenario().getNetwork());
//
//		/* Create a new instance of a carrier plan strategy manager factory. */
//		final CarrierPlanStrategyManagerFactory cStratManFac = new MyCarrierPlanStrategyManagerFactoryImpl(types, fs.getScenario().getNetwork(), controler);
//
//		CarrierModule carrierControler = new CarrierModule(carriers, cStratManFac, cScorFuncFac);
//		carrierControler.setPhysicallyEnforceTimeWindowBeginnings(true);
//		controler.addOverridingModule(carrierControler);
//
//		return carriers;
//	}


//	private static FreightScenario setupReceivers(Controler controler, MutableFreightScenario fsc, String outputfolder) {
//		
//		Receivers finalReceivers = new Receivers();
//		new ReceiversReader(finalReceivers).readFile(outputfolder + "receivers.xml");
//		finalReceivers = fsc.getReceivers();
//
//
//		/* 
//		 * Adds receivers to freight scenario.
//		 */		
//		finalReceivers.linkReceiverOrdersToCarriers(fsc.getCarriers());
//		fsc.setReceivers(finalReceivers);
//
//		Coalition coalition = fsc.getCoalition();
//
//		/* Adds collaborating receivers to the current (sub) coalition. */
//		for (Receiver receiver : fsc.getReceivers().getReceivers().values()){
//			if(receiver.getAttributes().getAttribute("collaborationStatus")!=null){
//				if ((boolean) receiver.getAttributes().getAttribute("collaborationStatus") == true){
//					if (!coalition.getReceiverCoalitionMembers().contains(receiver)){
//						coalition.addReceiverCoalitionMember(receiver);
//					}
//				}
//			}
//		}
//		
//		LOG.info("Current number of carrier coalition members: " + coalition.getCarrierCoalitionMembers().size());
//		LOG.info("Current number of receiver coalition members: " + coalition.getReceiverCoalitionMembers().size());
//		LOG.info("Total number of receiver agents: " + Integer.toString(fsc.getReceivers().getReceivers().size()));
//
//		/*
//		 * Create a new instance of a receiver scoring function factory.
//		 */
//		final ReceiverScoringFunctionFactory rScorFuncFac = new ProportionalReceiverScoringFunctionFactoryImpl();
//
//		//int selector = MatsimRandom.getLocalInstance().nextInt(3);
////		int selector = 0;
////		switch (selector) {
////			case 0: {
//				ReceiverOrderStrategyManagerFactory rStratManFac = new TimeWindowReceiverOrderStrategyManagerImpl();				
//				ReceiverModule receiverControler = new ReceiverModule(finalReceivers, rScorFuncFac, rStratManFac, fsc);
//				controler.addOverridingModule(receiverControler);
////			}
////			case 1: {
////				final ReceiverOrderStrategyManagerFactory rStratManFac = new ServiceTimeReceiverOrderStrategyManagerImpl();
////				ReceiverModule receiverControler = new ReceiverModule(finalReceivers, rScorFuncFac, rStratManFac, fsc);
////				controler.addOverridingModule(receiverControler);
////			}
////			case 2: {
////				final ReceiverOrderStrategyManagerFactory rStratManFac = new NumDelReceiverOrderStrategyManagerImpl();
////				ReceiverModule receiverControler = new ReceiverModule(finalReceivers, rScorFuncFac, rStratManFac, fsc);
////				controler.addOverridingModule(receiverControler); 
////			}
////			default: { 
////				Log.warn("No order strategy manager selected.");		
////			}
////		}
//		return fsc;
//	}


//	private static class MyCarrierPlanStrategyManagerFactoryImpl implements CarrierPlanStrategyManagerFactory {
//
//		/*
//		 * Adapted from RunChessboard.java by sschroeder and gliedtke.
//		 */
//		private Network network;
//		private MatsimServices controler;
//		private CarrierVehicleTypes types;
//
//		public MyCarrierPlanStrategyManagerFactoryImpl(final CarrierVehicleTypes types, final Network network, final MatsimServices controler) {
//			this.types = types;
//			this.network = network;
//			this.controler= controler;
//		}
//
//		@Override
//		public GenericStrategyManager<CarrierPlan, Carrier> createStrategyManager() {
//			TravelDisutility travelDis = TravelDisutilities.createBaseDisutility(types, controler.getLinkTravelTimes());
//			final LeastCostPathCalculator router = controler.getLeastCostPathCalculatorFactory().createPathCalculator(network,	travelDis, controler.getLinkTravelTimes());
//
//			final GenericStrategyManager<CarrierPlan, Carrier> strategyManager = new GenericStrategyManager<>();
//			strategyManager.setMaxPlansPerAgent(5);
//			{
//				GenericPlanStrategyImpl<CarrierPlan, Carrier> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanChanger<CarrierPlan, Carrier>(1.));
//				strategyManager.addStrategy(strategy, null, 1.0);
//			}
//
//			{
//				GenericPlanStrategyImpl<CarrierPlan, Carrier> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<CarrierPlan, Carrier>());
//				strategy.addStrategyModule(new TimeAllocationMutator());
//				ReRouteVehicles reRouteModule = new ReRouteVehicles(router, network, controler.getLinkTravelTimes(), 1.);
//				strategy.addStrategyModule(reRouteModule);
//				strategyManager.addStrategy(strategy, null, 0.5);
//			}
//			return strategyManager;
//		}
//	}


	private static void prepareFreightOutputDataAndStats( MatsimServices controler, final MutableFreightScenario fs, int run) {

		/*
		 * Adapted from RunChessboard.java by sshroeder and gliedtke.
		 */
		final int statInterval = fs.getReplanInterval();

		CarrierScoreStats scoreStats = new CarrierScoreStats(fs.getCarriers(), fs.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
		ReceiverScoreStats rScoreStats = new ReceiverScoreStats(fs, fs.getScenario().getConfig().controler().getOutputDirectory() + "/receiver_scores", true);

		controler.addControlerListener(scoreStats);
		controler.addControlerListener(rScoreStats);
		controler.addControlerListener(new IterationEndsListener() {

			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				String dir = event.getServices().getControlerIO().getIterationPath(event.getIteration());

				if((event.getIteration() + 1) % (statInterval) != 0) return;

				//write plans

				new CarrierPlanXmlWriterV2(fs.getCarriers()).write(dir + "/" + event.getIteration() + ".carrierPlans.xml");

				new ReceiversWriter(fs.getReceivers()).write(dir + "/" + event.getIteration() + ".receivers.xml");

				/* Record receiver stats */
				int numberOfReceivers = fs.getReceivers().getReceivers().size();
				for(int i = 1; i < numberOfReceivers+1; i++) {
					Receiver receiver = fs.getReceivers().getReceivers().get(Id.create(Integer.toString(i), Receiver.class));
					for (ReceiverOrder rorder :  receiver.getSelectedPlan().getReceiverOrders()){
						for (Order order : rorder.getReceiverProductOrders()){
							String score = receiver.getSelectedPlan().getScore().toString();
							float start = (float) receiver.getSelectedPlan().getTimeWindows().get(0).getStart();
							float end = (float) receiver.getSelectedPlan().getTimeWindows().get(0).getEnd();
							float size = (float) (order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity());
							float freq = (float) order.getNumberOfWeeklyDeliveries();
							float dur =  (float) order.getServiceDuration();
							boolean status = (boolean) receiver.getAttributes().getAttribute("collaborationStatus");
							boolean member = (boolean) receiver.getAttributes().getAttribute("grandCoalitionMember");

							BufferedWriter bw1 = IOUtils.getAppendingBufferedWriter(fs.getScenario().getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv");
							try {
								bw1.write(String.format("%d,%s,%s,%f,%f,%s,%f,%f,%f,%b,%b", 
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
