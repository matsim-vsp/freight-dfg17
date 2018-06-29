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

public class OrderSizeMutator implements GenericPlanStrategyModule<ReceiverPlan> {
	private boolean increase;

	
	public OrderSizeMutator(boolean increase){
		this.increase = increase;
	}

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {

	}

	@Override
	public void handlePlan(ReceiverPlan receiverPlan) {
		
		/* Create list of receiver orders. */
		for (ReceiverOrder ro : receiverPlan.getReceiverOrders()){
		
			/* Increase or decrease the number of deliveries per week with specified value until either 1 day (in case of decrease) or 5 days (in case of increase) is reached.*/
			for(Order order: ro.getReceiverOrders()){
				
				double numDel = order.getNumberOfWeeklyDeliveries();
				double sdemand = order.getOrderQuantity();
				double random = MatsimRandom.getRandom().nextDouble();
				double newNumDel = order.getNumberOfWeeklyDeliveries();
				double demand = order.getOrderQuantity();
				double pdeliver;
			
				if (increase == true){
						if (numDel < 5){
							newNumDel = numDel + 1;
							pdeliver = newNumDel/5;
							if (random < pdeliver){
								sdemand = demand*(numDel/newNumDel);
							}
						}
				}
				
				if (increase == false){
					if (numDel > 1){					
						newNumDel = numDel - 1;
						pdeliver = newNumDel/5;
						if (random < pdeliver){
							sdemand = demand*(numDel/newNumDel);
						}
					}
				}
				
				order.setNumberOfWeeklyDeliveries(newNumDel);
				order.setOrderQuantity(sdemand);
			}
		}
	}
				

	@Override
	public void finishReplanning() {

	}

}
