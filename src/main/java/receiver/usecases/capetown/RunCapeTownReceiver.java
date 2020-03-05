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

import java.io.File;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import org.matsim.core.utils.io.IOUtils;
import receiver.ReceiverConfigGroup;
import receiver.ReceiverUtils;
import receiver.usecases.chessboard.ReceiverChessboardUtils;

/**
 * Specific example for my (wlbean) thesis chapter 7.
 * @author jwjoubert, wlbean
 */

public class RunCapeTownReceiver {
	final private static Logger LOG = Logger.getLogger(RunCapeTownReceiver.class);
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
		String outputfolder = String.format("./output/capetown/caseNP/run_%03d/", run);
		new File(outputfolder).mkdirs();
		Scenario sc = CapeTownScenarioBuilder.createCapeTownScenario(SEED_BASE*run, run, true);

		sc.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
		sc.getConfig().controler().setOutputDirectory(outputfolder);

		Controler controler = new Controler(sc);

		/* Set up freight portion. To be repeated every iteration.
		* FIXME should be replaced by ReceiverModule. */
		setupReceiverAndCarrierReplanning(controler, outputfolder);

		ReceiverChessboardUtils.setupCarriers(controler );
		CapeTownReceiverUtils.setupReceivers(controler);

		/* TODO This stats must be set up automatically. */
		prepareFreightOutputDataAndStats(controler, run);

		controler.run();

		/* Clean up iterations folder */
		File itersFolder = new File(outputfolder + "ITERS/");
		IOUtils.deleteDirectoryRecursively(itersFolder.toPath());
	}


	static void setupReceiverAndCarrierReplanning(MatsimServices controler, String outputFolder) {
//		controler.addControlerListener(new IterationStartsListener() {
//
//			//@Override
//			public void notifyIterationStarts(IterationStartsEvent event) {
//
//				if(event.getIteration() % ReceiverUtils.getReplanInterval( controler.getScenario() ) != 0) {
//					return;
//				}
//
//				/* Adds the receiver agents that are part of the current (sub)coalition. */
//				CollaborationUtils.setCoalitionFromReceiverAttributes( controler.getScenario() );
//
//				/*
//				 * Carrier replan with receiver changes.
//				 */
//
//				Carrier carrier = ReceiverUtils.getCarriers( controler.getScenario() ).getCarriers().get(Id.create("Carrier1", Carrier.class));
//				ArrayList<CarrierPlan> carrierPlans = new ArrayList<CarrierPlan>();
//
//				/* Remove all existing carrier plans. */
//
//				for (CarrierPlan plan : carrier.getPlans()){
//					carrierPlans.add(plan);
//				}
//
//				Iterator<CarrierPlan> planIterator = carrierPlans.iterator();
//				while (planIterator.hasNext()){
//					CarrierPlan plan = planIterator.next();							
//					carrier.removePlan(plan);
//				}
//
//
//				VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, controler.getScenario().getNetwork());
//
//				NetworkBasedTransportCosts netBasedCosts = NetworkBasedTransportCosts.Builder.newInstance(controler.getScenario().getNetwork(), carrier.getCarrierCapabilities().getVehicleTypes()).build();
//				VehicleRoutingProblem vrp = vrpBuilder.setRoutingCost(netBasedCosts).build();
//
//				//read and create a pre-configured algorithms to solve the vrp
//				VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "./scenarios/chessboard/vrpalgo/initialPlanAlgorithm.xml");
//
//				//solve the problem
//				Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
//
//				//get best (here, there is only one)
//				VehicleRoutingProblemSolution solution = null;
//
//				Iterator<VehicleRoutingProblemSolution> iterator = solutions.iterator();
//
//				while(iterator.hasNext()){
//					solution = iterator.next();
//				}
//
//				//create a new carrierPlan from the solution 
//				CarrierPlan newPlan = MatsimJspritFactory.createPlan(carrier, solution);
//
//				//route plan 
//				NetworkRouter.routePlan(newPlan, netBasedCosts);
//
//
//				//assign this plan now to the carrier and make it the selected carrier plan
//				carrier.setSelectedPlan(newPlan);
//
//				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(controler.getScenario().getConfig().controler().getOutputDirectory() + "carriers.xml");
//				new ReceiversWriter( ReceiverUtils.getReceivers( controler.getScenario() ) ).write(controler.getScenario().getConfig().controler().getOutputDirectory() + "receivers.xml");
//
//			}
//
//		});		
	}

	static void prepareFreightOutputDataAndStats( MatsimServices controler, int run ) {

		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);

		controler.addControlerListener(scoreStats);
		controler.addControlerListener(new VehicleTypeListener(controler.getScenario(), run));
	}
}
