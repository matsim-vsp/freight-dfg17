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

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.scoring.ScoringFunction;

/**
 * This keeps track of a single receiver during simulation.
 * 
 * @author wlbean
 */

class ReceiverAgent {

	private final Receiver receiver;
	private final ScoringFunction scorFunc;
	final private Logger log = Logger.getLogger(ReceiverAgent.class);
	//private Id<Receiver> id;


	public ReceiverAgent(Receiver receiver, ScoringFunction receiverScorFunc) {
		this.receiver = receiver;
		this.scorFunc = receiverScorFunc;
		//this.id = receiver.getId();		
	}


	/**
	 * Score the receiver agent's selected order. This score reflects the receiver 
	 * cost and is currently determined as the carrier's delivery cost to that 
	 * receiver (based on the proportion of this receiver's orders in all the 
	 * orders delivered by the carrier). This is not really realistic, and will 
	 * be changed in the future.
	 *
	 * FIXME: JWJ (23/6/2018): I'm not quite sure what the purpose of this 
	 * method is. I've updated it so that the plan's cost is simply the sum
	 * of all individual order's costs.
	 *
	 * @author wlbean, jwjoubert
	 */
	public void scoreSelectedPlan() {
		double cost;


		
		ReceiverPlan selectedPlan = receiver.getSelectedPlan();
		if (selectedPlan == null) {
			log.warn("Receiver plan not yet selected.");
			return;
		}

		cost = receiver.getSelectedPlan().getScore();
		
		scorFunc.addMoney(cost);
		scorFunc.finish();
		
		receiver.getSelectedPlan().setScore(scorFunc.getScore());
		receiver.getAttributes().putAttribute(ReceiverUtils.ATTR_RECEIVER_SCORE, scorFunc.getScore());
	}
	


	/**
	 * Returns the receiver agent's unique receiver id.
	 * @return
	 */

	public Id<Receiver> getId() {
		return receiver.getId();
	}


}
