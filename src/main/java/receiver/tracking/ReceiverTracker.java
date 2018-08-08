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
package receiver.tracking;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.scoring.ScoringFunction;

import receiver.FreightScenario;
import receiver.Receiver;
import receiver.collaboration.MarginalCostSharing;
import receiver.collaboration.ProportionalCostSharing;
import receiver.mobsim.ReceiverAgent;
import receiver.scoring.ReceiverScoringFunctionFactory;

/**
 * This keeps track of all receiver agents during simulation.
 * 
 * @author wlbean
 *
 */
public class ReceiverTracker implements EventHandler {
	private final FreightScenario fsc;
	//private final Receivers receivers;
	private final Collection<ReceiverAgent> receiverAgents = new ArrayList<ReceiverAgent>();

	public ReceiverTracker(FreightScenario fsc, ReceiverScoringFunctionFactory scorFuncFac){
		this.fsc = fsc;
		createReceiverAgents(scorFuncFac);
	}

	/**
	 * Scores the selected receiver order.
	 */
	public void scoreSelectedPlans() {
		
		
		//MarginalCostSharing mcs = new MarginalCostSharing(350);
		//mcs.allocateCoalitionCosts(fsc);
		
		ProportionalCostSharing pcs = new ProportionalCostSharing(350);
		pcs.allocateCoalitionCosts(fsc);
		
		for (Receiver receiver : fsc.getReceivers().getReceivers().values()){
			ReceiverAgent rAgent = findReceiver(receiver.getId());
			rAgent.scoreSelectedPlan();
		}		
	}


	/**
	 * Creates the list of all receiver agents.
	 */
	private void createReceiverAgents(ReceiverScoringFunctionFactory scorFuncFac) {
		for (Receiver receiver: fsc.getReceivers().getReceivers().values()){
			ScoringFunction receiverScorFunc = scorFuncFac.createScoringFunction(receiver);
			ReceiverAgent rAgent = new ReceiverAgent(receiver, receiverScorFunc);
			receiverAgents.add(rAgent);
		}
	}


	/**
	 * Find a particular receiver agent in the list of receiver agents.
	 */
	private ReceiverAgent findReceiver(Id<Receiver> id) {
		for (ReceiverAgent rAgent : receiverAgents){
			if (rAgent.getId().equals(id)){
				return rAgent;
			}
		}
		return null;
	}


}
