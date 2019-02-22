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

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.utils.io.IOUtils;
import receiver.ReceiverModule;
import receiver.ReceiverUtils;
import receiver.replanning.ReceiverReplanningType;

import java.io.BufferedWriter;
import java.io.File;

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
		
//		/* Write headings */
//		BufferedWriter bw = IOUtils.getBufferedWriter(outputfolder + "/ReceiverStats" + run + ".csv");
//		BaseRunReceiver.writeHeadings( bw );

		sc.getConfig().controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);

		Controler controler = new Controler(sc);

		/* Set up freight portion.To be repeated every iteration*/
		//FIXME
		sc.getConfig().controler().setOutputDirectory(outputfolder);
//		setupReceiverAndCarrierReplanning(sc);

		ReceiverChessboardUtils.setupCarriers(controler);
		ReceiverModule receiverModule = new ReceiverModule();
		controler.addOverridingModule(receiverModule);

		/* TODO This stats must be set up automatically. */
		BaseRunReceiver.prepareFreightOutputDataAndStats(controler);

		controler.run();
	}
}

