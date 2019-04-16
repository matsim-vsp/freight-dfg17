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
//		double finalend;
//
//		if ((boolean) receiver.getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS) == true) {
//			
//			if (newWindow.getStart() <= Time.parseTime("18:00:00")){
//				finalstart = Time.parseTime("18:00:00");
//			} else if (newWindow.getStart() >= Time.parseTime("30:00:00")){
//				finalstart = Time.parseTime("30:00:00") - MINIMUM_TIME_WINDOW;
//			} else finalstart = newWindow.getStart();
//
//			if (newWindow.getEnd() >= Time.parseTime("30:00:00")) {
//				finalle finalstart;
//		doubend = Time.parseTime("30:00:00");
//			} else if (newWindow.getEnd() <= Time.parseTime("18:00:00")) {
//				finalend = Time.parseTime("18:00:00") + MINIMUM_TIME_WINDOW;
//			} else finalend = newWindow.getEnd();
//			
//		} else {
//			
//			if (newWindow.getStart() >= Time.parseTime("18:00:00")  - MINIMUM_TIME_WINDOW) {
//				finalstart = Time.parseTime("18:00:00")  - MINIMUM_TIME_WINDOW;
//			} else if (newWindow.getStart() <= Time.parseTime("06:00:00")) {
//				finalstart = Time.parseTime("06:00:00");
//			} else finalstart = newWindow.getStart();
//
//
//			if (newWindow.getEnd() >= Time.parseTime("18:00:00")) {
//				finalend = Time.parseTime("18:00:00");
//			} else if (newWindow.getEnd() <= Time.parseTime("06:00:00")) {
//				finalend = Time.parseTime("06:00:00") + MINIMUM_TIME_WINDOW;
//			} else finalend = newWindow.getEnd();
//		}
		
//		TimeWindow finalWindow = TimeWindow.newInstance(finalstart, finalend);
		
		plan.getTimeWindows().remove(oldWindow);
		plan.getTimeWindows().add(newWindow);
//		plan.getTimeWindows().add(finalWindow);
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
		boolean status = (boolean) plan.getReceiver().getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS );
		int move = MatsimRandom.getLocalInstance().nextInt(4);
		switch (move) {
		case 0:
			return extendTimeWindowDownwards(tw, status);
		case 1:
			return extendTimeWindowUpwards(tw, status);
		case 2:
			return contractTimeWindowBottom(tw, status);
		case 3:
			return contractTimeWindowTop(tw, status);
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
	 * @return
	 */
	public TimeWindow extendTimeWindowDownwards(final TimeWindow tw, boolean status) {
		//double newLow = tw.getStart() - MatsimRandom.getLocalInstance().nextDouble()*this.stepSize;		
		double newLow;

		if (tw.getEnd() - (tw.getStart() - this.stepSize) <= MAXIMUM_TIME_WINDOW){
			newLow = tw.getStart() - this.stepSize;
		} else newLow = tw.getEnd() - MAXIMUM_TIME_WINDOW ;

//			if ((boolean) status == true) {
//				
//				if (newLow <= Time.parseTime("18:00:00")){
//					newLow = Time.parseTime("18:00:00");
//				} else if (newLow >= Time.parseTime("30:00:00")){
//					newLow = Time.parseTime("30:00:00") - MINIMUM_TIME_WINDOW;
//				
//			} else {
//				
//				if (newLow >= Time.parseTime("18:00:00")  - MINIMUM_TIME_WINDOW) {
//					newLow = Time.parseTime("18:00:00")  - MINIMUM_TIME_WINDOW;
//				} else if (newLow <= Time.parseTime("06:00:00")) {
//					newLow = Time.parseTime("06:00:00");
//				}
//
//			}
//			}
			
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
	public TimeWindow extendTimeWindowUpwards(final TimeWindow tw, boolean status) {
//		double newHigh = tw.getEnd() + MatsimRandom.getLocalInstance().nextDouble()*this.stepSize;
		double newHigh;

		if ((tw.getEnd() + this.stepSize) - tw.getStart() <= MAXIMUM_TIME_WINDOW){
			newHigh = tw.getEnd() + this.stepSize;
		} else newHigh = tw.getStart() + MAXIMUM_TIME_WINDOW ;
//
//			if (status == true) {
//	
//				if (newHigh >= Time.parseTime("30:00:00")) {
//					newHigh = Time.parseTime("30:00:00");
//				} else if (newHigh <= Time.parseTime("18:00:00")) {
//					newHigh = Time.parseTime("18:00:00") + MINIMUM_TIME_WINDOW;
//				}
//				
//			} else {
//	
//				if (newHigh >= Time.parseTime("18:00:00")) {
//					newHigh = Time.parseTime("18:00:00");
//				} else if (newHigh <= Time.parseTime("06:00:00")) {
//					newHigh = Time.parseTime("06:00:00") + MINIMUM_TIME_WINDOW;
//				}
//			}
			
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
	public TimeWindow contractTimeWindowBottom(final TimeWindow tw, boolean status) {
		double gap = Math.max(0, (tw.getEnd() - tw.getStart()) - MINIMUM_TIME_WINDOW);
		double step = Math.min(gap, stepSize);
		double newLow = tw.getStart() + step;

//		if ((boolean) status == true) {
//			
//			if (newLow <= Time.parseTime("18:00:00")){
//				newLow = Time.parseTime("18:00:00");
//			} else if (newLow >= Time.parseTime("30:00:00")){
//				newLow = Time.parseTime("30:00:00") - MINIMUM_TIME_WINDOW;
//			
//		} else {
//			
//			if (newLow >= Time.parseTime("18:00:00")  - MINIMUM_TIME_WINDOW) {
//				newLow = Time.parseTime("18:00:00")  - MINIMUM_TIME_WINDOW;
//			} else if (newLow <= Time.parseTime("06:00:00")) {
//				newLow = Time.parseTime("06:00:00");
//			}
//
//		}
//		}
		
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
	public TimeWindow contractTimeWindowTop(final TimeWindow tw, boolean status) {
		double gap = Math.max(0, (tw.getEnd() - tw.getStart()) - MINIMUM_TIME_WINDOW);
		double step = Math.min(gap, stepSize);
		//double newHigh = tw.getEnd() - MatsimRandom.getLocalInstance().nextDouble()*step;
		double newHigh = tw.getEnd() - step;

//		if (status == true) {
//			
//			if (newHigh >= Time.parseTime("30:00:00")) {
//				newHigh = Time.parseTime("30:00:00");
//			} else if (newHigh <= Time.parseTime("18:00:00")) {
//				newHigh = Time.parseTime("18:00:00") + MINIMUM_TIME_WINDOW;
//			}
//			
//		} else {
//
//			if (newHigh >= Time.parseTime("18:00:00")) {
//				newHigh = Time.parseTime("18:00:00");
//			} else if (newHigh <= Time.parseTime("06:00:00")) {
//				newHigh = Time.parseTime("06:00:00") + MINIMUM_TIME_WINDOW;
//			}
//		}
		
		return TimeWindow.newInstance(tw.getStart(), newHigh);
	}
	

	@Override
	public void finishReplanning() {
		
	}

}
