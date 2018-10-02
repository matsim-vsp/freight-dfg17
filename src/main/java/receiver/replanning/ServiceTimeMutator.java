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
  
package receiver.replanning;

import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;
import receiver.ReceiverPlan;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

/**
 * Changes the service time of a receivers' orders.
 * 
 * @author wlbean
 */

public class ServiceTimeMutator implements GenericPlanStrategyModule<ReceiverPlan> {
	private double time;
	private double range;
	boolean increase;
	
	/**
	 * This class changes the service time of a receivers' orders with the 
	 * specified time. If {@link increase} is true, the service time will increase
	 * by {@link mutationTime} until the max duration {@link mutationRange} is 
	 * reached. Conversely, if increase is false, the service time will decrease 
	 * by {@link mutationTime} until the minimum duration {@link mutationRange} 
	 * is reached.
	 * 
	 * @param mutationTime
	 * @param mutationRange
	 * @param increase
	 */
	
	public ServiceTimeMutator(double mutationTime, double mutationRange, boolean increase){
//		this.time = mutationTime*MatsimRandom.getLocalInstance().nextDouble();
		this.time = mutationTime;
		this.range = mutationRange;
		this.increase = increase;
	}
	

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {
		
	}

	@Override
	public void handlePlan(ReceiverPlan receiverPlan) {
		
		/* Create list of receiver orders. */
		for (ReceiverOrder ro: receiverPlan.getReceiverOrders()){

			/* Increase or decrease the service time with specified value until range min or range max is reached.*/
			for(Order order: ro.getReceiverProductOrders()){

				double duration = order.getServiceDuration();
				
				if (increase == true){
					if ( duration + time <= range){
						duration = duration + time;		
					} else duration = range;
				}
				else 
					if (duration - time >= range){
						duration = duration - time;		
					} else duration = range;
				
				order.setServiceDuration(duration);
			}
		}

	}

	@Override
	public void finishReplanning() {
	}

}
