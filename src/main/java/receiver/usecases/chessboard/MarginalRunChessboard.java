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
package receiver.usecases.chessboard;

import java.io.BufferedWriter;
import java.io.File;
import java.net.URL;
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

import receiver.ReceiverModule;
import receiver.ReceiverUtils;
import receiver.ReceiversWriter;
import receiver.collaboration.CollaborationUtils;
import receiver.replanning.ReceiverReplanningType;
import receiver.usecases.ReceiverScoreStats;

/**
 * Specific example for my (wlbean) thesis chapters 5 and 6.
 * @author jwjoubert, wlbean
 */

 class MarginalRunChessboard{
	final private static Logger LOG = Logger.getLogger( MarginalRunChessboard.class );
	final private static long SEED_BASE = 20180816l;
	final private static String DESCRIPTION = "marginal";

//	private static int replanInt;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int startRun = Integer.parseInt(args[0]);
		int endRun = Integer.parseInt(args[1]);
		int numberOfThreads = Integer.parseInt(args[2]);
		for(int i = startRun; i < endRun; i++) {
			run(i, numberOfThreads);
		}
	}


	public static void run(int run, int numberOfThreads) {

		String outputfolder = String.format("./output/" + DESCRIPTION + "/run_%03d/", run);
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
		Scenario sc = MarginalRun.run(marginalArgs );
		
		Scenario newSc = MarginalScenarioBuilder.createChessboardScenario(outputfolder, SEED_BASE*run, run, true);
		
		/* Write headings */
		BufferedWriter bw = IOUtils.getBufferedWriter(outputfolder + "/ReceiverStats" + run + ".csv");
		BaseRunReceiver.writeHeadings( bw );

		sc.getConfig().controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);

		Controler controler = new Controler(sc);

		/* Set up freight portion.To be repeated every iteration*/
		//FIXME
		sc.getConfig().controler().setOutputDirectory(outputfolder);
//		setupReceiverAndCarrierReplanning(sc);

		ReceiverChessboardUtils.setupCarriers(controler);
		ReceiverModule receiverModule = new ReceiverModule( ReceiverReplanningType.serviceTime );
		controler.addOverridingModule(receiverModule);

		/* TODO This stats must be set up automatically. */
		prepareFreightOutputDataAndStats(controler, run);

		controler.run();
	}


	@Deprecated
	private static void prepareFreightOutputDataAndStats( MatsimServices controler, int run) {

		/*
		 * Adapted from RunChessboard.java by sshroeder and gliedtke.
		 */
		final int statInterval = MarginalExperimentParameters.STAT_INTERVAL;
		//final LegHistogram freightOnly = new LegHistogram(20);

		// freightOnly.setPopulation(controler.getScenario().getPopulation());
		//freightOnly.setInclPop(false);
		
		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
		ReceiverScoreStats rScoreStats = new ReceiverScoreStats();

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
				BaseRunReceiver.recordReceiverStats( event, numberOfReceivers, controler, run );

			}
		});	

	}
}

