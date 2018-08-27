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
package receiver.marginalParallel;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import receiver.MutableFreightScenario;
import receiver.Receiver;
import receiver.collaboration.MutableCoalition;
import receiver.usecases.ReceiverChessboardScenarioExample;
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
		Id<Receiver> receiverId = null;
		if(!idString.equalsIgnoreCase("null")) {
			receiverId = Id.create(idString, Receiver.class);
		}
	
		/* Use the code (components) from ReceiverChessboardScenario */
		int numberOfReceivers = 60;
		String inputNetwork = folder + "input/network.xml";
		Scenario sc = ReceiverChessboardScenarioExample.setupChessboardScenario(inputNetwork, folder + "output/", seed, 1);
		Carriers carriers = ReceiverChessboardScenarioExample.createChessboardCarriers(sc);
		
		MutableFreightScenario fs = new MutableFreightScenario(sc, carriers);
		fs.setReplanInterval(50);
		
		ReceiverChessboardScenarioExample.createAndAddChessboardReceivers(fs, numberOfReceivers);
		ReceiverChessboardScenarioExample.createReceiverOrders(fs, numberOfReceivers);
		/* This is the portion that is unique HERE: remove ONE receiver. */
		if(receiverId != null) {
			fs.getReceivers().getReceivers().remove(receiverId);
		}

		/* Let jsprit do its magic and route the given receiver orders. */
		ReceiverChessboardScenarioExample.generateCarrierPlan(fs.getCarriers(), fs.getScenario().getNetwork(), folder + "input/algorithm.xml");
		ReceiverChessboardScenarioExample.writeFreightScenario(fs);
		
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
			if (receiver.getCollaborationStatus() == true){
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
		sc.getConfig().controler().setLastIteration(fs.getReplanInterval()-1);
		
		Controler controler = new Controler(sc);

		ReceiverChessboardUtils.setupCarriers(controler, fs);

		ReceiverChessboardUtils.setupReceivers(controler, fs);	

		controler.run();
	}
	
	
}
