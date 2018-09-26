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
package receiver.usecases.marginal;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;

import receiver.Receiver;
import receiver.ReceiverUtils;
import receiver.collaboration.MutableCoalition;
import receiver.usecases.ReceiverChessboardUtils;

/**
 *
 * @author jwjoubert
 */
public class MarginalReceiverClass {
	final private static Logger LOG = Logger.getLogger(MarginalReceiverClass.class);

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
		Scenario sc = MarginalScenarioBuilder.setupChessboardScenario(inputNetwork, "./output/", seed, 1);
		sc.getConfig().controler().setLastIteration(50);
		
		ReceiverUtils.setReplanInterval(50, sc );
		
		MarginalScenarioBuilder.createChessboardCarriers(sc);
		MarginalScenarioBuilder.createAndAddChessboardReceivers(sc);
		MarginalScenarioBuilder.createAndAddControlGroupReceivers(sc);
		MarginalScenarioBuilder.createReceiverOrders(sc);
		/* This is the portion that is unique HERE: remove ONE receiver. */
		if(receiverId != Id.create("0", Receiver.class)) {
			ReceiverUtils.getReceivers( sc ).getReceivers().remove(receiverId);
		}

		/* Let jsprit do its magic and route the given receiver orders. */
		MarginalScenarioBuilder.generateCarrierPlan( ReceiverUtils.getCarriers( sc ), sc.getNetwork(),  "input/algorithm.xml");
		MarginalScenarioBuilder.writeFreightScenario(sc);
		
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
			if ((boolean) receiver.getAttributes().getAttribute("collaborationStatus") == true){
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
		
		
		/* Make config changes relevant to the current marginal run. */
		sc.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
		sc.getConfig().controler().setLastIteration( ReceiverUtils.getReplanInterval( sc ) );
		
		Controler controler = new Controler(sc);

		ReceiverChessboardUtils.setupCarriers(controler);

		ReceiverChessboardUtils.setupReceivers(controler);	
		
		prepareFreightOutputDataAndStats(controler, 1);

		controler.run();
	}
	
	
	private static void prepareFreightOutputDataAndStats( MatsimServices controler, int run) {
		/*
		 * Adapted from RunChessboard.java by sshroeder and gliedtke.
		 */
//		final int statInterval = ReceiverUtils.getReplanInterval( controler.getScenario() );
		final int statInterval = 1;
		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);

		controler.addControlerListener(scoreStats);

		controler.addControlerListener(new IterationEndsListener() {
			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				String dir = event.getServices().getControlerIO().getIterationPath(event.getIteration());
				if((event.getIteration() + 1) % (statInterval) != 0) return;
				//write plans
				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(dir + "/" + event.getIteration() + ".carrierPlans.xml.gz");
			}
		});
		
		controler.addControlerListener(new ShutdownListener() {
			@Override
			public void notifyShutdown(ShutdownEvent event) {
				int lastIteration = controler.getScenario().getConfig().controler().getLastIteration();
				String outputDir = controler.getConfig().controler().getOutputDirectory();
				outputDir += outputDir.endsWith("/") ? "" : "/";
				File f1 = new File(outputDir + "ITERS/it." + lastIteration + "/" + lastIteration + ".carrierPlans.xml.gz");
				File f2 = new File(outputDir + "output_carrierPlans.xml.gz");
				try {
					ReceiverChessboardUtils.copyFile(f1, f2);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Cannot copy output carrier plans.");
				}
			}
		});
		

	}

	
	
}
