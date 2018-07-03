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

import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;
import receiver.ReceiverPlan;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

public class ServiceTimeMutator implements GenericPlanStrategyModule<ReceiverPlan> {
	private double time;
	private double range;
	boolean increase;
	
	/**
	 * This class changes the service time of a receivers' orders with the 
	 * specified time. If increase is true, the service time will increase 
	 * until the max duration (mutationRange) is reached. Conversely, if 
	 * increase is false, the service time will decrease until the minimum 
	 * duration (mutationRange) is reached.
	 * 
	 * TODO Maybe expand the descriptions below so it is easier to understand
	 * what exactly the parameters mean.
	 * 
	 * @param mutationTime
	 * @param mutationRange
	 * @param increase
	 */
	
	public ServiceTimeMutator(double mutationTime, double mutationRange, boolean increase){
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
			for(Order order: ro.getReceiverOrders()){
				
				double duration = order.getServiceDuration();
				if (increase == true){
						if ( duration < range){
							duration = duration + time;		
						}
				}
				else
					if ( duration > range){
					duration = duration - time;		
				}
				order.setServiceDuration(duration);
				
			}

		}
					
	}

	@Override
	public void finishReplanning() {
	}

}
