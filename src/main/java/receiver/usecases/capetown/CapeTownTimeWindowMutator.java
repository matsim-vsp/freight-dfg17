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

import java.util.Random;

import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;
import org.matsim.core.utils.misc.Time;

import receiver.Receiver;
import receiver.ReceiverPlan;
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
//		this.stepSize = stepSize*MatsimRandom.getLocalInstance().nextDouble();
		this.stepSize = stepSize;
	}

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {
	}

	@Override
	public void handlePlan(ReceiverPlan plan) {
		boolean status = (boolean) plan.getReceiver().getAttributes().getAttribute("collaborationStatus");
		plan.getAttributes().putAttribute("collaborationStatus", status);
		
		Receiver receiver = plan.getReceiver();
		//TimeWindow oldWindow = pickRandomTimeWindow(plan);
		TimeWindow oldWindow = plan.getTimeWindows().get(0);
		TimeWindow newWindow = wiggleTimeWindow(oldWindow, receiver);
		plan.getTimeWindows().remove(oldWindow);
		plan.getTimeWindows().add(newWindow);
	}
	
	public TimeWindow pickRandomTimeWindow(ReceiverPlan plan) {
		int item = MatsimRandom.getLocalInstance().nextInt(plan.getTimeWindows().size());
		return plan.getTimeWindows().get(item);
	}
	
	/**
	 * Randomly performs a perturbation to the given {@link TimeWindow}. The
	 * perturbations include increasing or decreasing either the {@link TimeWindow}'s
	 * start or end time.
	 *  
	 * @param tw
	 * @return
	 */
	public TimeWindow wiggleTimeWindow(TimeWindow tw, Receiver receiver) {
		
		if ((boolean) receiver.getSelectedPlan().getAttributes().getAttribute("collaborationStatus") == true){
			int move = MatsimRandom.getLocalInstance().nextInt(6);
			switch (move) {
			case 0:
				return extendTimeWindowDownwardsNight(tw, receiver);
			case 1:
				return extendTimeWindowUpwardsNight(tw, receiver);
			case 2:
				return contractTimeWindowBottomNight(tw, receiver);
			case 3:
				return contractTimeWindowTopNight(tw, receiver);
			case 4:
				return selectNightTimeWindow(receiver);
			default:
				throw new IllegalArgumentException("Cannot wiggle TimeWindow with move type '" + move + "'.");
			}
		} else {
		int move = MatsimRandom.getLocalInstance().nextInt(5);
		switch (move) {
		case 0:
			return extendTimeWindowDownwards(tw);
		case 1:
			return extendTimeWindowUpwards(tw);
		case 2:
			return contractTimeWindowBottom(tw);
		case 3:
			return contractTimeWindowTop(tw);
		case 4: 
			return selectDayTimeWindow(receiver);
		default:
			throw new IllegalArgumentException("Cannot wiggle TimeWindow with move type '" + move + "'.");
		}
		}
	}
	


//	public ReceiverOrder pickRandomReceiverOrder(ReceiverPlan plan) {
//		/* Randomly identify a (single) receiver order to adapt. */
//		Collection<ReceiverOrder> ro = plan.getReceiverOrders();
//		int item = MatsimRandom.getLocalInstance().nextInt(ro.size());
//		ReceiverOrder selectedRo = null;
//		Object o = ro.toArray()[item];
//		if(o instanceof ReceiverOrder) {
//			selectedRo = (ReceiverOrder) o;
//		} else {
//			throw new RuntimeException("Randomly selected 'ReceiverOrder' is of the wrong type: " + o.getClass().toString() );
//		}
//		return selectedRo;
//	}
	
	/**
	 * Decreases the {@link TimeWindow} start time by some random step size that
	 * is no more than the threshold specified at this {@link CapeTownTimeWindowMutator}'s
	 * instantiation.
	 * 
	 * @param tw
	 * @return
	 */
	public TimeWindow extendTimeWindowDownwardsNight(final TimeWindow tw, Receiver receiver) {
//		double newLow = tw.getStart() - MatsimRandom.getLocalInstance().nextDouble()*this.stepSize;		
//		double newLow;
//
//		if (tw.getEnd() - (tw.getStart() - this.stepSize) <= MAXIMUM_TIME_WINDOW){
//			newLow = tw.getStart() - this.stepSize;
//		} else newLow = tw.getEnd() - MAXIMUM_TIME_WINDOW ;		
//
//		if ((boolean) receiver.getAttributes().getAttribute("EarlyDeliveries") ==  true){
//			if (newLow > Time.parseTime("00:00:00")){
//				return TimeWindow.newInstance(newLow, tw.getEnd());
//			} else {
//				return TimeWindow.newInstance(Time.parseTime("00:00:00"), tw.getEnd());
//			}
//		} else if (newLow > Time.parseTime("16:00:00")){
//				return TimeWindow.newInstance(newLow, tw.getEnd());
//			} else {
//				return TimeWindow.newInstance(Time.parseTime("16:00:00"), tw.getEnd());
//			}
		
		double newLow;

		if (tw.getEnd() - (tw.getStart() - this.stepSize) <= MAXIMUM_TIME_WINDOW){
			newLow = tw.getStart() - this.stepSize;
		} else newLow = tw.getEnd() - MAXIMUM_TIME_WINDOW ;		
		
		if (newLow > Time.parseTime("18:00:00")){
			return TimeWindow.newInstance(newLow, tw.getEnd());
		}
		else {
			return TimeWindow.newInstance(Time.parseTime("18:00:00"), tw.getEnd());
		}
	}
	
	
	/**
	 * Increases the {@link TimeWindow} end time by some random step size that
	 * is no more than the threshold specified at this {@link CapeTownTimeWindowMutator}'s
	 * instantiation.
	 * 
	 * @param tw
	 * @return
	 */
	public TimeWindow extendTimeWindowUpwardsNight(final TimeWindow tw, Receiver receiver) {
//		double newHigh = tw.getEnd() + MatsimRandom.getLocalInstance().nextDouble()*this.stepSize;
//		double newHigh;
//
//		if ((tw.getEnd() + this.stepSize) - tw.getStart() <= MAXIMUM_TIME_WINDOW){
//			newHigh = tw.getEnd() + this.stepSize;
//		} else newHigh = tw.getStart() + MAXIMUM_TIME_WINDOW ;
//
//		if ((boolean) receiver.getAttributes().getAttribute("EarlyDeliveries") ==  true){
//			if (newHigh < Time.parseTime("16:00:00")){
//				return TimeWindow.newInstance(tw.getStart(), newHigh);
//			} else {
//				return TimeWindow.newInstance(tw.getStart(), Time.parseTime("16:00:00"));
//			}
//		} else if (newHigh < Time.parseTime("24:00:00")){
//				return TimeWindow.newInstance(tw.getStart(), newHigh);
//			} else {
//				return TimeWindow.newInstance(tw.getStart(),  Time.parseTime("24:00:00"));
//			} 
		
		double newHigh;

		if ((tw.getEnd() + this.stepSize) - tw.getStart() <= MAXIMUM_TIME_WINDOW){
			newHigh = tw.getEnd() + this.stepSize;
		} else newHigh = tw.getStart() + MAXIMUM_TIME_WINDOW ;
		
		if (newHigh < Time.parseTime("30:00:00")){
			return TimeWindow.newInstance(tw.getStart(), newHigh);
		}
		else {
			return TimeWindow.newInstance(tw.getStart(), Time.parseTime("30:00:00"));
		}
	}
	
	
	/**
	 * Increases the {@link TimeWindow} start time by some random step size that
	 * is no more than the threshold specified at this {@link CapeTownTimeWindowMutator}'s
	 * instantiation, provided the minimum {@link TimeWindow} width is maintained.
	 * 
	 * @param tw
	 * @return
	 */
	public TimeWindow contractTimeWindowBottomNight(final TimeWindow tw, Receiver receiver) {
//		double gap = Math.max(0, (tw.getEnd() - tw.getStart()) - MINIMUM_TIME_WINDOW);
//		double step = Math.min(gap, stepSize);
//		//double newLow = tw.getStart() + MatsimRandom.getLocalInstance().nextDouble()*step;
//		double newLow = tw.getStart() + step;
//
//		if ((boolean) receiver.getAttributes().getAttribute("EarlyDeliveries") ==  true){
//			if (newLow > Time.parseTime("00:00:00")){
//				return TimeWindow.newInstance(newLow, tw.getEnd());
//			}
//			else {
//				return TimeWindow.newInstance(Time.parseTime("00:00:00"), tw.getEnd());
//			}
//		} else {
//			if (newLow > Time.parseTime("16:00:00")){
//				return TimeWindow.newInstance(newLow, tw.getEnd());
//			} else {
//				return TimeWindow.newInstance(Time.parseTime("16:00:00"), tw.getEnd());
//			} 
//		}
		
		double gap = Math.max(0, (tw.getEnd() - tw.getStart()) - MINIMUM_TIME_WINDOW);
		double step = Math.min(gap, stepSize);
		//double newLow = tw.getStart() + MatsimRandom.getLocalInstance().nextDouble()*step;
		double newLow = tw.getStart() + step;
		
		if (newLow > Time.parseTime("18:00:00")){
			return TimeWindow.newInstance(newLow, tw.getEnd());
		}
		else {
			return TimeWindow.newInstance(Time.parseTime("18:00:00"), tw.getEnd());
		}
	}
	
	
	/**
	 * Decreases the {@link TimeWindow} end time by some random step size that
	 * is no more than the threshold specified at this {@link CapeTownTimeWindowMutator}'s
	 * instantiation, provided the minimum {@link TimeWindow} width is maintained.
	 * 
	 * @param tw
	 * @return
	 */
	public TimeWindow contractTimeWindowTopNight(final TimeWindow tw, Receiver receiver) {
//		double gap = Math.max(0, (tw.getEnd() - tw.getStart()) - MINIMUM_TIME_WINDOW);
//		double step = Math.min(gap, stepSize);
//		//double newHigh = tw.getEnd() - MatsimRandom.getLocalInstance().nextDouble()*step;
//		double newHigh = tw.getEnd() - step;
//
//		if ((boolean) receiver.getAttributes().getAttribute("EarlyDeliveries") ==  true){
//			if (newHigh < Time.parseTime("08:00:00")){
//				return TimeWindow.newInstance(tw.getStart(), newHigh);
//			} else {
//				return TimeWindow.newInstance(tw.getStart(), Time.parseTime("08:00:00"));
//			}
//		} else if (newHigh < Time.parseTime("24:00:00")){
//			return TimeWindow.newInstance(tw.getStart(), newHigh);
//		} else {
//			return TimeWindow.newInstance(tw.getStart(), Time.parseTime("24:00:00"));
//		}
		
		double gap = Math.max(0, (tw.getEnd() - tw.getStart()) - MINIMUM_TIME_WINDOW);
		double step = Math.min(gap, stepSize);
		//double newHigh = tw.getEnd() - MatsimRandom.getLocalInstance().nextDouble()*step;
		double newHigh = tw.getEnd() - step;

		if (newHigh < Time.parseTime("30:00:00")){
			return TimeWindow.newInstance(tw.getStart(), newHigh);
		}
		else {
			return TimeWindow.newInstance(tw.getStart(), Time.parseTime("30:00:00"));
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
		
		if (newLow > Time.parseTime("06:00:00")){
			return TimeWindow.newInstance(newLow, tw.getEnd());
		}
		else {
			return TimeWindow.newInstance(Time.parseTime("06:00:00"), tw.getEnd());
		}
		
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
//		double newHigh = tw.getEnd() + MatsimRandom.getLocalInstance().nextDouble()*this.stepSize;
		double newHigh;

		if ((tw.getEnd() + this.stepSize) - tw.getStart() <= MAXIMUM_TIME_WINDOW){
			newHigh = tw.getEnd() + this.stepSize;
		} else newHigh = tw.getStart() + MAXIMUM_TIME_WINDOW ;
		
		if (newHigh < Time.parseTime("18:00:00")){
			return TimeWindow.newInstance(tw.getStart(), newHigh);
		}
		else {
			return TimeWindow.newInstance(tw.getStart(), Time.parseTime("18:00:00"));
		}
		
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
		
		if (newLow > Time.parseTime("06:00:00")){
			return TimeWindow.newInstance(newLow, tw.getEnd());
		}
		else {
			return TimeWindow.newInstance(Time.parseTime("06:00:00"), tw.getEnd());
		}
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
		//double newHigh = tw.getEnd() - MatsimRandom.getLocalInstance().nextDouble()*step;
		double newHigh = tw.getEnd() - step;

		if (newHigh < Time.parseTime("18:00:00")){
			return TimeWindow.newInstance(tw.getStart(), newHigh);
		}
		else {
			return TimeWindow.newInstance(tw.getStart(), Time.parseTime("18:00:00"));
		}
	}
	
	private TimeWindow selectDayTimeWindow(Receiver receiver) {
		int min = 6;
		int max = 18;
//		Random randomTime = new Random();
		TimeWindow randomTimeWindow;
		int time = MatsimRandom.getRandom().nextInt(max - CapeTownExperimentParameters.TIME_WINDOW_DURATION - min + 1);
		if (time >= 0){
			int randomStart =  (min + time);
			randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
		} else {
			int randomStart = min;
			randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
		}
//		receiver.getAttributes().putAttribute("EarlyDeliveries", false);
		return randomTimeWindow;		
	}
	
	private TimeWindow selectNightTimeWindow(Receiver receiver) {
		int min = 18;
		int max = 30;
//		Random randomTime = new Random();
		TimeWindow randomTimeWindow;
		int time = MatsimRandom.getRandom().nextInt(max - CapeTownExperimentParameters.TIME_WINDOW_DURATION - min + 1);
		if (time >= 0){
			int randomStart =  (min + time);
			randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
		} else {
			int randomStart = min;
			randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
		}
//		receiver.getAttributes().putAttribute("EarlyDeliveries", false);
		return randomTimeWindow;
	}

//	private TimeWindow selectMorningTimeWindow(Receiver receiver) {
//		int min = 0;
//		int max = 8;
//		Random randomTime = new Random();
//		TimeWindow randomTimeWindow;
//		int time = randomTime.nextInt(max - CapeTownExperimentParameters.TIME_WINDOW_DURATION - min + 1);
//		if (time >= 0){
//			int randomStart =  (min + time);
//			randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
//		} else {
//			int randomStart = min;
//			randomTimeWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
//		}
//		receiver.getAttributes().putAttribute("EarlyDeliveries", true);
//		return randomTimeWindow;
//	}


	@Override
	public void finishReplanning() {
		
	}

}
