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
package receiver;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.scoring.ScoringFunction;

import receiver.collaboration.MarginalCostSharing;

/**
 * This keeps track of all receiver agents during simulation.
 * 
 * @author wlbean
 *
 */


 final class ReceiverTracker implements EventHandler {
//	@Inject Scenario sc;
	private final Scenario sc;
	
	//private final Receivers receivers;
	private final Collection<ReceiverAgent> receiverAgents = new ArrayList<ReceiverAgent>();

	public ReceiverTracker(ReceiverScoringFunctionFactory scorFuncFac, Scenario sc){
		this.sc = sc;
		createReceiverAgents(scorFuncFac);
	}

	/**
	 * Scores the selected receiver order.
	 */
	void scoreSelectedPlans() {

		/* FIXME this must be relocated to Module level, and configurable in ConfigGroup */
		MarginalCostSharing mcs = new MarginalCostSharing(1000, sc);
		mcs.allocateCoalitionCosts();
		
//		ProportionalCostSharing pcs = new ProportionalCostSharing(750.0, sc);
//		ProportionalCostSharing pcs = new ProportionalCostSharing(10000.0, sc);
//		ProportionalCostSharing pcs = new ProportionalCostSharing(1000, sc);
//		pcs.allocateCoalitionCosts();
//		
		for (Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()){
			ReceiverAgent rAgent = findReceiver(receiver.getId());
			rAgent.scoreSelectedPlan();
		}		
	}


	/**
	 * Creates the list of all receiver agents.
	 */
	private void createReceiverAgents(ReceiverScoringFunctionFactory scorFuncFac) {
		for (Receiver receiver: ReceiverUtils.getReceivers( sc ).getReceivers().values()){
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
