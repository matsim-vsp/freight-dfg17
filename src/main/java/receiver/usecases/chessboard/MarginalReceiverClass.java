/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.utils.io.IOUtils;
import receiver.Receiver;
import receiver.ReceiverConfigGroup;
import receiver.ReceiverModule;
import receiver.ReceiverUtils;
import receiver.collaboration.CollaborationUtils;

import java.io.File;
import java.net.URL;

/**
 *
 * @author jwjoubert
 */
 class MarginalReceiverClass {
	final private static Logger LOG = Logger.getLogger(MarginalReceiverClass.class);
	static String folder;

	public static void main(String[] args) {
		LOG.info("Running with the following arguments: ");
		for(String s : args) {
			LOG.info(s);
		}
		
		long seed = Long.parseLong(args[0]);
		String folder = args[1];
		folder += folder.endsWith("/") ? "" : "/";
		String idString = args[2];
		Id<Receiver> receiverId = Id.create(idString, Receiver.class);
	
		/* Use the code (components) from ReceiverChessboardScenario */
		String inputNetwork = "input/network.xml";
		Scenario sc = MarginalScenarioBuilder.setupChessboardScenario(seed);
		sc.getConfig().controler().setLastIteration(ExperimentParameters.REPLAN_INTERVAL);

        ConfigUtils.addOrGetModule(sc.getConfig(), ReceiverConfigGroup.class).setReceiverReplanningInterval(ExperimentParameters.REPLAN_INTERVAL);

        MarginalScenarioBuilder.createChessboardCarriers(sc);
		ProportionalScenarioBuilder.createAndAddChessboardReceivers(sc );
		MarginalScenarioBuilder.createAndAddControlGroupReceivers(sc);
		MarginalScenarioBuilder.createReceiverOrders(sc);
		/* This is the portion that is unique HERE: remove ONE receiver. */
		if(receiverId != Id.create("0", Receiver.class)) {
			ReceiverUtils.getReceivers( sc ).getReceivers().remove(receiverId);
		}

		/* Let jsprit do its magic and route the given receiver orders. */
//		MarginalScenarioBuilder.generateCarrierPlan( ReceiverUtils.getCarriers( sc ), sc.getNetwork(),  "input/algorithm.xml");
//		URL algoConfigFileName = IOUtils.newUrl( sc.getConfig().getContext(), "algorithm.xml" );
		URL algoConfigFileName = IOUtils.extendUrl( sc.getConfig().getContext(), "initialPlanAlgorithm.xml" );
		ReceiverChessboardUtils.generateCarrierPlan( ReceiverUtils.getCarriers( sc ), sc.getNetwork(),  algoConfigFileName);
		
		BaseReceiverChessboardScenario.writeFreightScenario(sc );
		
		/* Link the carriers to the receivers. */
		ReceiverUtils.getReceivers( sc ).linkReceiverOrdersToCarriers( ReceiverUtils.getCarriers( sc ) );
		
		CollaborationUtils.createCoalitionWithCarriersAndAddCollaboratingReceivers( sc );


		/* Make config changes relevant to the current marginal run. */
		sc.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
		sc.getConfig().controler().setLastIteration( ExperimentParameters.REPLAN_INTERVAL );

		Controler controler = new Controler(sc);

		ReceiverChessboardUtils.setupCarriers(controler);

		ReceiverModule receiverModule = new ReceiverModule();
		controler.addOverridingModule(receiverModule);

		prepareFreightOutputDataAndStats(controler, 1);

		controler.run();
	}
	
	
	private static void prepareFreightOutputDataAndStats( MatsimServices controler, int run) {
		/*
		 * Adapted from RunChessboard.java by sshroeder and gliedtke.
		 */
//		final int statInterval = ReceiverUtils.getReplanInterval( controler.getScenario() );
		final int statInterval = ExperimentParameters.STAT_INTERVAL;
		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);

		controler.addControlerListener(scoreStats);

		controler.addControlerListener(new IterationEndsListener() {
			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				String dir = event.getServices().getControlerIO().getIterationPath(event.getIteration());
				if((event.getIteration() + 1) % (statInterval) != 0) return;
				//write plans
				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(dir + "/" + event.getIteration() + ".carrierPlans.xml.gz");
				LOG.info("Writing carrier plans to: " + dir + "/" + event.getIteration() + ".carrierPlans.xml.gz");
			}
		});
		
		controler.addControlerListener(new ShutdownListener() {
			@Override
			public void notifyShutdown(ShutdownEvent event) {
				int lastIteration = controler.getScenario().getConfig().controler().getLastIteration();
				String outputDir =  "./output/";
				outputDir += outputDir.endsWith("/") ? "" : "/";
				LOG.info("Reading carrier plans from: " + outputDir + "ITERS/it." + lastIteration + "/" + lastIteration + ".carrierPlans.xml.gz");
				File f1 = new File(outputDir + "ITERS/it." + lastIteration + "/" + lastIteration + ".carrierPlans.xml.gz");
				File f2 = new File(outputDir + "output_carrierPlans.xml.gz");
				ReceiverChessboardUtils.copyFile(f1, f2);
			}
		});
		

	}

	
	
}
