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
package receiver.usecases.marginal;

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
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.utils.io.IOUtils;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;

import receiver.Receiver;
import receiver.ReceiverUtils;
import receiver.ReceiversWriter;
import receiver.product.Order;
import receiver.product.ReceiverOrder;
import receiver.usecases.ReceiverChessboardUtils;
import receiver.usecases.ReceiverScoreStats;

/**
 * Specific example for my (wlbean) thesis chapters 5 and 6.
 * @author jwjoubert, wlbean
 */

public class RunChessboardMarginal {
	final private static Logger LOG = Logger.getLogger(RunChessboardMarginal.class);
	final private static long SEED_BASE = 20180816l;	
//	private static int replanInt;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numberOfRuns = Integer.parseInt(args[0]);
		int numberOfThreads = Integer.parseInt(args[1]);
		for(int i = 0; i < numberOfRuns; i++) {
			run(i, numberOfThreads);
		}
	}


	public static void run(int run, int numberOfThreads) {

		String outputfolder = String.format("./output/marginal/run_%03d/", run);
		new File(outputfolder).mkdirs();
		
		/* Before the main run starts, we need to calculate the marginal 
		 * contribution for each receiver. This is done, for now, by running
		 * a separate class. */
		LOG.info("Calculating the initial marginal cost for each receiver.");
		String[] marginalArgs = {
				"./scenarios/",
				outputfolder,
				"./target/freight-dfg17-0.0.1-SNAPSHOT-release.zip",
				String.valueOf(numberOfThreads),
				String.valueOf(SEED_BASE),
		};
		Scenario sc = RunMarginal.run(marginalArgs);
		
		Scenario newSc = MarginalScenarioBuilder.createChessboardScenario(outputfolder, SEED_BASE*run, run, true);
		
		/* Write headings */
		BufferedWriter bw = IOUtils.getBufferedWriter(outputfolder + "/ReceiverStats" + run + ".csv");
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

		sc.getConfig().controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);

		Controler controler = new Controler(sc);

		/* Set up freight portion.To be repeated every iteration*/
		//FIXME
		sc.getConfig().controler().setOutputDirectory(outputfolder);
		setupReceiverAndCarrierReplanning(controler);

		ReceiverChessboardUtils.setupCarriers(controler);
		ReceiverChessboardUtils.setupReceivers(controler);	
//
//
		/* TODO This stats must be set up automatically. */
		prepareFreightOutputDataAndStats(controler, run);

		controler.run();
	}


	private static void setupReceiverAndCarrierReplanning( MatsimServices controler) {

		controler.addControlerListener(new IterationStartsListener() {

			//@Override
			public void notifyIterationStarts(IterationStartsEvent event) {
				
				if(event.getIteration() % ReceiverUtils.getReplanInterval( controler.getScenario() ) != 0) {
					return;
				}

				/* Adds the receiver agents that are part of the current (sub)coalition. */
				for (Receiver receiver : ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().values()){
					if (receiver.getAttributes().getAttribute("collaborationStatus") != null){
						if ((boolean) receiver.getAttributes().getAttribute("collaborationStatus") == true){
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
				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(controler.getScenario().getConfig().controler().getOutputDirectory() + "../../../input/carrierPlanned.xml");
				
				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(controler.getScenario().getConfig().controler().getOutputDirectory() + "carriers.xml");
				new ReceiversWriter( ReceiverUtils.getReceivers( controler.getScenario() ) ).write(controler.getScenario().getConfig().controler().getOutputDirectory() + "receivers.xml");
				
				//FIXME As in base case, this need not be required anymore.
//				ReceiverUtils.setCarriers( ReceiverUtils.getCarriers( mfs.getScenario() ), fs.getScenario() );
//				ReceiverUtils.setReceivers( ReceiverUtils.getReceivers( mfs.getScenario() ), fs.getScenario() );
			}

		});		
	}

	private static void prepareFreightOutputDataAndStats( MatsimServices controler, int run) {

		/*
		 * Adapted from RunChessboard.java by sshroeder and gliedtke.
		 */
		final int statInterval = ReceiverUtils.getReplanInterval( controler.getScenario() ) *2;
		//final LegHistogram freightOnly = new LegHistogram(20);

		// freightOnly.setPopulation(controler.getScenario().getPopulation());
		//freightOnly.setInclPop(false);
		
		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
		ReceiverScoreStats rScoreStats = new ReceiverScoreStats(controler.getScenario().getConfig().controler().getOutputDirectory() + "/receiver_scores", true);

		//controler.getEvents().addHandler(freightOnly);
		controler.addControlerListener(scoreStats);
		controler.addControlerListener(rScoreStats);
		controler.addControlerListener(new IterationEndsListener() {

			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				String dir = event.getServices().getControlerIO().getIterationPath(event.getIteration());

				if((event.getIteration() + 1) % (statInterval) != 0) return;

				//write plans
				
				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(dir + "/" + event.getIteration() + ".carrierPlans.xml.gz");
				
				new ReceiversWriter( ReceiverUtils.getReceivers( controler.getScenario() ) ).write(dir + "/" + event.getIteration() + ".receivers.xml.gz");

				/* Record receiver stats */
				int numberOfReceivers = ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().size();
				for(int i = 1; i < numberOfReceivers+1; i++) {
					Receiver receiver = ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().get(Id.create(Integer.toString(i), Receiver.class));
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

							BufferedWriter bw1 = IOUtils.getAppendingBufferedWriter(controler.getScenario().getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv");
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



/*	} finally{
				try {
					bw1.close();
					} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Cannot close receiver stats file");
						}
					}       		

				//write stats
				//freightOnly.writeGraphic(dir + "/" + event.getIteration() + ".legHistogram_freight.png");
				//freightOnly.reset(event.getIteration());
			}
		});	

	}
}*/