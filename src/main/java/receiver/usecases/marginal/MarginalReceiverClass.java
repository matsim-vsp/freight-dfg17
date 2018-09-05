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
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;

import receiver.FreightScenario;
import receiver.MutableFreightScenario;
import receiver.Receiver;
import receiver.collaboration.MutableCoalition;
import receiver.usecases.ReceiverChessboardUtils;

/**
 *
 * @author jwjoubert
 */
public class MarginalReceiverClass {
	final private static Logger LOG = Logger.getLogger(MarginalReceiverClass.class);

	public static void main(String[] args) {
		long seed = Long.parseLong(args[0]);
		String folder = args[1];
		folder += folder.endsWith("/") ? "" : "/";
		
		String idString = args[2];
		Id<Receiver> receiverId = Id.create(idString, Receiver.class);
	
		/* Use the code (components) from ReceiverChessboardScenario */
		String inputNetwork = "input/network.xml";
		Scenario sc = MarginalScenarioBuilder.setupChessboardScenario(inputNetwork, "./output/", seed, 1);
		Carriers carriers = MarginalScenarioBuilder.createChessboardCarriers(sc);
		
		MutableFreightScenario fs = new MutableFreightScenario(sc, carriers);
		fs.setReplanInterval(5);
		
		MarginalScenarioBuilder.createAndAddChessboardReceivers(fs);
		MarginalScenarioBuilder.createAndAddControlGroupReceivers(fs);
		MarginalScenarioBuilder.createReceiverOrders(fs);
		/* This is the portion that is unique HERE: remove ONE receiver. */
		if(receiverId != Id.create("0", Receiver.class)) {
			fs.getReceivers().getReceivers().remove(receiverId);
		}

		/* Let jsprit do its magic and route the given receiver orders. */
		MarginalScenarioBuilder.generateCarrierPlan(fs.getCarriers(), fs.getScenario().getNetwork(),  "input/algorithm.xml");
		MarginalScenarioBuilder.writeFreightScenario(fs);
		
		/* Link the carriers to the receivers. */
		fs.getReceivers().linkReceiverOrdersToCarriers(fs.getCarriers());
		
		/* Add carrier and receivers to coalition */
		MutableCoalition coalition = new MutableCoalition();
		
		for (Carrier carrier : fs.getCarriers().getCarriers().values()){
			if (!coalition.getCarrierCoalitionMembers().contains(carrier)){
				coalition.addCarrierCoalitionMember(carrier);
			}
		}
		
		for (Receiver receiver : fs.getReceivers().getReceivers().values()){
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
		
 		fs.setCoalition(coalition);
		
		
		/* Make config changes relevant to the current marginal run. */
		sc.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
		sc.getConfig().controler().setLastIteration(fs.getReplanInterval());
		
		Controler controler = new Controler(sc);

		ReceiverChessboardUtils.setupCarriers(controler, fs);

		ReceiverChessboardUtils.setupReceivers(controler, fs);	
		
		prepareFreightOutputDataAndStats(controler, fs, 1);

		controler.run();
	}
	
	
	private static void prepareFreightOutputDataAndStats(MatsimServices controler, final FreightScenario fs, int run) {
		/*
		 * Adapted from RunChessboard.java by sshroeder and gliedtke.
		 */
		final int statInterval = fs.getReplanInterval();
		CarrierScoreStats scoreStats = new CarrierScoreStats(fs.getCarriers(), fs.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);

		controler.addControlerListener(scoreStats);

		controler.addControlerListener(new IterationEndsListener() {
			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				String dir = event.getServices().getControlerIO().getIterationPath(event.getIteration());
//				if((event.getIteration() + 1) % (statInterval) != 0) return;
				//write plans
				new CarrierPlanXmlWriterV2(fs.getCarriers()).write(dir + "/" + event.getIteration() + ".carrierPlans.xml.gz");
			}
		});
		
		controler.addControlerListener(new ShutdownListener() {
			@Override
			public void notifyShutdown(ShutdownEvent event) {
				int lastIteration = fs.getScenario().getConfig().controler().getLastIteration();
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
