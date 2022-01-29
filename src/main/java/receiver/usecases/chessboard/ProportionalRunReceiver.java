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

package receiver.usecases.chessboard;

import java.io.File;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.contrib.freight.utils.FreightUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.utils.io.IOUtils;

import receiver.ReceiverModule;

/**
 * Specific example for my (wlbean) thesis chapters 5 and 6.
 * @author jwjoubert, wlbean
 */

class ProportionalRunReceiver{
	final private static Logger LOG = Logger.getLogger( ProportionalRunReceiver.class );
	final private static long SEED_BASE = 20180816l;
	private String outputfolder;
	private Scenario sc;

	public static void main(String[] args) {
		int startRun = Integer.parseInt(args[0]);
		int endRun = Integer.parseInt(args[1]);
		for(int i = startRun; i < endRun; i++) {
			new ProportionalRunReceiver().run(i );
		}
	}


	public void run(int run) {
		LOG.info("Starting run " + run);
		prepareScenario( run, ExperimentParameters.NUMBER_OF_RECEIVERS );
		prepareAndRunControler( run, null);
	}

	void prepareAndRunControler( int runId, Collection<AbstractModule> abstractModules ){
		Controler controler = new Controler(sc);
		if ( abstractModules!=null ){
			for( AbstractModule abstractModule : abstractModules ){
				controler.addOverridingModule( abstractModule );
			}
		}

		ReceiverChessboardUtils.setupCarriers(controler );

//		ReceiverModule receiverModule = new ReceiverModule( ReceiverReplanningType.serviceTime );
		ReceiverModule receiverModule = new ReceiverModule();
		receiverModule.setReplanningType(ExperimentParameters.REPLANNING_STRATEGY );
		
		controler.addOverridingModule(receiverModule);

		prepareFreightOutputDataAndStats(controler);

		controler.run();
		
		/* Clean up iterations folder */
		File itersFolder = new File(outputfolder + "ITERS/");
		IOUtils.deleteDirectoryRecursively(itersFolder.toPath());
	}

	void prepareScenario(int run, int numberOfReceivers ){
		outputfolder = String.format("./output/prop/serdur/run_%03d/", run);
		new File(outputfolder).mkdirs();
		sc = ProportionalReceiverChessboardScenario.createChessboardScenario(SEED_BASE*run, true );
		//		replanInt = mfs.getReplanInterval();
		sc.getConfig().controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles );
		sc.getConfig().controler().setOutputDirectory(outputfolder);

	}

//	static void setupCarrierReplanning( MatsimServices controler ) {
//		controler.addControlerListener(new ReceiverResponseCarrierReplanning() );
//

	/**
	 * FIXME This can be removed, like ReceiverScoreStats as soon as the Scenario can be injected into the ScoreStats class.
	 * @param controler
	 */
	@Deprecated
	static void prepareFreightOutputDataAndStats( MatsimServices controler) {
		CarrierScoreStats scoreStats = new CarrierScoreStats(FreightUtils.getCarriers(controler.getScenario()), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
		controler.addControlerListener(scoreStats);
	}
}
