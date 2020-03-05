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

import java.io.File;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;

import receiver.ReceiverModule;

/**
 * Specific example for my (wlbean) thesis chapters 5 and 6.
 * @author jwjoubert, wlbean
 */

 class MarginalRunReceiver {
	final private static Logger LOG = Logger.getLogger( MarginalRunReceiver.class );
	final private static long SEED_BASE = 20180816l;
//	private Scenario sc;
	private String outputFolder;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int startRun = Integer.parseInt(args[0]);
		int endRun = Integer.parseInt(args[1]);
		int numberOfThreads = Integer.parseInt(args[2]);
		for(int i = startRun; i < endRun; i++) {
//			run(i, numberOfThreads);
			new MarginalRunReceiver().run(i , numberOfThreads);
		}
	}


	public void run(int run, int numberOfThreads) {
		LOG.info("Starting run " + run);
		String outputFolder = String.format("./output/marg/serdur2/run_%03d/", run);
		new File(outputFolder).mkdirs();
		
		/* Before the main run starts, we need to calculate the marginal 
		 * contribution for each receiver. This is done, for now, by running
		 * a separate class. */
		LOG.info("Calculating the initial marginal cost for each receiver.");
		String[] marginalArgs = {
				"./scenarios/",
				outputFolder,
				"./target/freight-dfg17-0.0.1-SNAPSHOT-release.zip",
				String.valueOf(numberOfThreads),
				String.valueOf(SEED_BASE*run),
		};
		Scenario sc = MarginalRun.run(marginalArgs );
//		prepareScenario( run, ExperimentParameters.NUMBER_OF_RECEIVERS );
//		Coalition coalition = ReceiverUtils.getCoalition( sc );
		sc.getConfig().controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);

		Controler controler = new Controler(sc);

		/* Set up freight portion.To be repeated every iteration*/
		//FIXME
		sc.getConfig().controler().setOutputDirectory(outputFolder);
		
		ReceiverChessboardUtils.setupCarriers(controler);
		
		ReceiverModule receiverModule = new ReceiverModule();
		receiverModule.setReplanningType(ExperimentParameters.REPLANNING_STRATEGY );
		controler.addOverridingModule(receiverModule);

		/* TODO This stats must be set up automatically. */
		BaseRunReceiver.prepareFreightOutputDataAndStats(controler);

		controler.run();
	}


//	void prepareAndRunControler(int run, Collection<AbstractModule> abstractModules ) {
//		Controler controler = new Controler(sc);
//		if ( abstractModules!=null ){
//			for( AbstractModule abstractModule : abstractModules ){
//				controler.addOverridingModule( abstractModule );
//			}
//		}
//		
////		URL algoConfigFileName = IOUtils.newUrl( sc.getConfig().getContext(), "initialPlanAlgorithm.xml" );
//
//		ReceiverChessboardUtils.setupCarriers(controler );
////		ReceiverChessboardUtils.generateCarrierPlan( ReceiverUtils.getCarriers( sc ), sc.getNetwork(), algoConfigFileName);
//		
////		new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers(sc).write(sc.getConfig().controler().get + ".carrierPlans.xml.gz");
//		
//		ReceiverModule receiverModule = new ReceiverModule();
//		receiverModule.setReplanningType(ExperimentParameters.REPLANNING_STRATEGY );
//		
//		controler.addOverridingModule(receiverModule);
//
//		prepareFreightOutputDataAndStats(controler);
//
//		controler.run();
//		
//		/* Clean up iterations folder */
//		File itersFolder = new File(outputFolder + "ITERS/");
//		IOUtils.deleteDirectoryRecursively(itersFolder.toPath());
//		
//	}
//
//
//	private static void prepareFreightOutputDataAndStats(Controler controler) {
//		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
//		controler.addControlerListener(scoreStats);
//	}
//
//
//	Scenario prepareScenario(int run, int numberOfReceivers) {
//		outputFolder = String.format("./output/marg/freq5/run_%03d/", run);
////		new File(outputFolder).mkdirs();
//		sc = MarginalScenarioBuilder.createChessboardScenario(SEED_BASE*run, true );
//		sc.getConfig().controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles );
//		sc.getConfig().controler().setOutputDirectory(outputFolder);
//		
//		return sc;
//	}
}

