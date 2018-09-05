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

import javax.inject.Inject;

import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.controler.events.ScoringEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.ReplanningListener;
import org.matsim.core.controler.listener.ScoringListener;
import org.matsim.core.replanning.GenericStrategyManager;

import receiver.replanning.ReceiverOrderStrategyManagerFactory;

/**
 * This controller ensures that each receiver receives a cost (score) per order at the end of each iteration and replans its orders based on the cost of the previous iteration and past iterations.
 * 
 * @author wlbean
 *
 */
class ReceiverControlerListener implements ScoringListener,
ReplanningListener, BeforeMobsimListener {

	private Receivers receivers;
	private ReceiverOrderStrategyManagerFactory stratManFac;
	private ReceiverScoringFunctionFactory scorFuncFac;
	private ReceiverTracker tracker;
	private MutableFreightScenario fsc;
	@Inject EventsManager eMan;

	/**
	 * This creates a new receiver controler listener for receivers with replanning abilities.
	 * @param receivers
	 * @param stratManFac
	 */

	@Inject
	ReceiverControlerListener(Receivers receivers, ReceiverOrderStrategyManagerFactory stratManFac, ReceiverScoringFunctionFactory scorFuncFac, MutableFreightScenario fsc){
		this.receivers = receivers;
		this.stratManFac = stratManFac;
		this.scorFuncFac = scorFuncFac;
		this.fsc = fsc;
	}


	@Override
	public void notifyReplanning(final ReplanningEvent event) {

		if (stratManFac == null){
			return;
		}

		GenericStrategyManager<ReceiverPlan, Receiver> stratMan = stratManFac.createReceiverStrategyManager();

		Collection<HasPlansAndId<ReceiverPlan, Receiver>> receiverCollection = new ArrayList<>();
		Collection<HasPlansAndId<ReceiverPlan, Receiver>> receiverControlCollection = new ArrayList<>();

		for(Receiver receiver : receivers.getReceivers().values()){
			
			if ((event.getIteration() - 1) % fsc.getReplanInterval() == 0) {
					receiver.setInitialCost(receiver.getSelectedPlan().getScore());
				}

			/*
			 * Checks to see if a receiver is part of the grand coalition, if not, the receiver are not allowed to join 
			 * a coalition at any time. If the receiver is willing to collaborate, the receiver will be allowed to leave
			 * and join coalitions.
			 */
			boolean status = (boolean) receiver.getAttributes().getAttribute("grandCoalitionMember");
			if(status == true){	
				receiverCollection.add(receiver);
			} else receiverControlCollection.add(receiver);
		}
		
		/* Replanning for grand coalition receivers.*/
		GenericStrategyManager<ReceiverPlan, Receiver> collaborationStratMan = stratMan;
//		GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
//		strategy.addStrategyModule(new CollaborationStatusMutator());
//		collaborationStratMan.addStrategy(strategy, null, 0.2);
//		collaborationStratMan.addChangeRequest((int) Math.round((fsc.getScenario().getConfig().controler().getLastIteration())*0.8), strategy, null, 0.0);
		
		if (event.getIteration() % fsc.getReplanInterval() != 0) {
			return;
		} 
		
		/* Run replanning for non-collaborating receivers */
		stratMan.run(receiverControlCollection, null, event.getIteration(), event.getReplanningContext());		
		
		/* Run replanning for grand coalition receivers.*/
		collaborationStratMan.run(receiverCollection, null, event.getIteration(), event.getReplanningContext());	
	}



	/*
	 * Determines the order cost at the end of each iteration.
	 */

	@Override
	public void notifyScoring(ScoringEvent event) {
		if (event.getIteration() == 0) {
			this.tracker.scoreSelectedPlans();
		} 

		if ((event.getIteration()+1) % fsc.getReplanInterval() == 0) {
			this.tracker.scoreSelectedPlans();
		} else {		
			for (Receiver receiver : receivers.getReceivers().values()){
				double score = (double) receiver.getAttributes().getAttribute("score");
				receiver.getSelectedPlan().setScore(score);
			}
		}
	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		tracker = new ReceiverTracker(fsc, scorFuncFac);
		eMan.addHandler(tracker);		
	}

}
