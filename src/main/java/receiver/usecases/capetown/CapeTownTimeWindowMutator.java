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
package receiver.usecases.capetown;

import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;
import org.matsim.core.utils.misc.Time;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.replanning.TimeWindowMutator;

/**
 *
 * @author jwjoubert
 */
public class CapeTownTimeWindowMutator implements GenericPlanStrategyModule<ReceiverPlan> {
	final private double stepSize;
	final private double MINIMUM_TIME_WINDOW = Time.parseTime("02:00:00"); 
	final private double MAXIMUM_TIME_WINDOW = Time.parseTime("12:00:00"); 
	
	
	public CapeTownTimeWindowMutator(double stepSize) {
		this.stepSize = stepSize*MatsimRandom.getLocalInstance().nextDouble();
//		this.stepSize = stepSize;
	}

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {
	}

	@Override
	public void handlePlan(ReceiverPlan plan) {
//		boolean status = (boolean) plan.getReceiver().getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS );
		Object o = plan.getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS);
		if(o == null) {
			throw new IllegalArgumentException("Current plan has no status. This must likely be set first.");
		}
//		plan.setCollaborationStatus(status);

		/*FIXME The following should be checked: only coalition members are allowed
		 * after-hour deliveries. */
		Receiver receiver = plan.getReceiver();
		TimeWindow oldWindow = plan.getTimeWindows().get(0);
		TimeWindow newWindow = wiggleTimeWindow(oldWindow, receiver, plan);
		plan.getTimeWindows().remove(oldWindow);
		plan.getTimeWindows().add(newWindow);
	}
	
	
	/**
	 * Randomly performs a perturbation to the given {@link TimeWindow}. The
	 * perturbations include increasing or decreasing either the {@link TimeWindow}'s
	 * start or end time.
	 *  
	 * @param tw
	 * @param plan 
	 * @return
	 */
	public TimeWindow wiggleTimeWindow(TimeWindow tw, Receiver receiver, ReceiverPlan plan) {
		int move = MatsimRandom.getLocalInstance().nextInt(4);
		switch (move) {
		case 0:
			return extendTimeWindowDownwards(tw);
		case 1:
			return extendTimeWindowUpwards(tw);
		case 2:
			return contractTimeWindowBottom(tw);
		case 3:
			return contractTimeWindowTop(tw);
		default:
			throw new IllegalArgumentException("Cannot wiggle TimeWindow with move type '" + move + "'.");
		}
	}
	
	
	/**
	 * Decreases the {@link TimeWindow} start time by some random step size that
	 * is no more than the threshold specified at this {@link TimeWindowMutator}'s
	 * instantiation.
	 * 
	 * @param tw
	 * @param receiver 
	 * @return
	 */
	public TimeWindow extendTimeWindowDownwards(final TimeWindow tw) {
		//double newLow = tw.getStart() - MatsimRandom.getLocalInstance().nextDouble()*this.stepSize;		
		double newLow;

		if (tw.getEnd() - (tw.getStart() - this.stepSize) <= MAXIMUM_TIME_WINDOW){
			newLow = tw.getStart() - this.stepSize;
		} else newLow = tw.getEnd() - MAXIMUM_TIME_WINDOW ;		
		
		return TimeWindow.newInstance(newLow, tw.getEnd());
	}
	
	
	/**
	 * Increases the {@link TimeWindow} end time by some random step size that
	 * is no more than the threshold specified at this {@link TimeWindowMutator}'s
	 * instantiation.
	 * 
	 * @param tw
	 * @return
	 */
	public TimeWindow extendTimeWindowUpwards(final TimeWindow tw) {
		double newHigh;

		if ((tw.getEnd() + this.stepSize) - tw.getStart() <= MAXIMUM_TIME_WINDOW){
			newHigh = tw.getEnd() + this.stepSize;
		} else newHigh = tw.getStart() + MAXIMUM_TIME_WINDOW ;

		return TimeWindow.newInstance(tw.getStart(), newHigh);
	}
	
	
	/**
	 * Increases the {@link TimeWindow} start time by some random step size that
	 * is no more than the threshold specified at this {@link TimeWindowMutator}'s
	 * instantiation, provided the minimum {@link TimeWindow} width is maintained.
	 * 
	 * @param tw
	 * @return
	 */
	public TimeWindow contractTimeWindowBottom(final TimeWindow tw) {
		double gap = Math.max(0, (tw.getEnd() - tw.getStart()) - MINIMUM_TIME_WINDOW);
		double step = Math.min(gap, stepSize);
		//double newLow = tw.getStart() + MatsimRandom.getLocalInstance().nextDouble()*step;
		double newLow = tw.getStart() + step;
		
		return TimeWindow.newInstance(newLow, tw.getEnd());
	}
	
	
	/**
	 * Decreases the {@link TimeWindow} end time by some random step size that
	 * is no more than the threshold specified at this {@link TimeWindowMutator}'s
	 * instantiation, provided the minimum {@link TimeWindow} width is maintained.
	 * 
	 * @param tw
	 * @return
	 */
	public TimeWindow contractTimeWindowTop(final TimeWindow tw) {
		double gap = Math.max(0, (tw.getEnd() - tw.getStart()) - MINIMUM_TIME_WINDOW);
		double step = Math.min(gap, stepSize);
		double newHigh = tw.getEnd() - step;

		return TimeWindow.newInstance(tw.getStart(), newHigh);
	}
	

	@Override
	public void finishReplanning() {
		
	}

}
