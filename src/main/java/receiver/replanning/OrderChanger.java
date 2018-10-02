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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierService.Builder;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;

import receiver.ReceiverPlan;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

/**
 * Rewrites a carrier's services after a receiver changed its plan.
 * 
 * @author wlbean
 *
 */

public class OrderChanger implements GenericPlanStrategyModule<ReceiverPlan> {

	/**
	 * This class rewrites a carrier's services after a receiver changed its plan.
	 * 
	 */

	public OrderChanger(){		
	}


	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {

	}

	@Override
	public void handlePlan(ReceiverPlan receiverPlan) {
//		boolean status = (boolean) receiverPlan.getCollaborationStatus();
//		receiverPlan.getReceiver().getAttributes().putAttribute("collaborationStatus", status);

		/* Create list of receiver orders. */
		for (ReceiverOrder ro: receiverPlan.getReceiverOrders()){
			int n = 0;
					
			for(Order order: ro.getReceiverProductOrders()){
				List<CarrierService> services = new ArrayList<>();
				List<CarrierService> servicesToRemove = new ArrayList<>();
				n = n + 1;
				/*  Check to see if a particular carrier service does indeed 
				 * belong to the receiver and then changes the service parameters 
				 * according to the receiver's order. Currently it compares carrier 
				 * service id with receiver order id. This might be changed in the 
				 * future. */
				Iterator<CarrierService> iterator = ro.getCarrier().getServices().iterator();	
				int counter = 0;
				
				while(iterator.hasNext()){					
					CarrierService service = iterator.next();
					if (service.getId().toString() == order.getId().toString()){	
						counter = 1;
					}
				}
				
				if (counter == 1){
					Iterator<CarrierService> iterator2 = ro.getCarrier().getServices().iterator();		
					
					while(iterator2.hasNext()){					
						CarrierService service = iterator2.next();
						CarrierService newService = null;
										
						/* Check to see if this unique order exists as a carrier service, if so check to see if this particular 
						 * carrier service is indeed this receiver's order. Before updating. */
						
					
						if (service.getId().toString() == order.getId().toString()){		
							Builder builder = CarrierService.Builder.newInstance(Id.create("Order" + receiverPlan.getReceiver().getId().toString() + Integer.toString(n), CarrierService.class), receiverPlan.getReceiver().getLinkId());
							newService = builder
									.setCapacityDemand((int) ((order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity())))
									/*TODO This only looks at the FIRST time window. 
									 * This may need revision once we handle multiple 
									 * time windows. */
									.setServiceStartTimeWindow(receiverPlan.getTimeWindows().get(0))
									.setServiceDuration(order.getServiceDuration())
									.build();
						} else {
							newService = service;
						}
							
						/*Check to see if the service has a 0 capacity demand, and if that is the case,
						 * remove it from the carrier's list of services.
						 */
						if (newService.getCapacityDemand() != 0) {
							services.add(newService);
	
						}
						
						servicesToRemove.add(service);
					}
					
					ro.getCarrier().getServices().removeAll(servicesToRemove);
					ro.getCarrier().getServices().addAll(services);
								
				} else if (counter == 0){
					
					CarrierService newService2 = null;				
					
					Builder builder = CarrierService.Builder.newInstance(Id.create("Order" + receiverPlan.getReceiver().getId().toString() + Integer.toString(n), CarrierService.class), receiverPlan.getReceiver().getLinkId());
					newService2 = builder
							.setCapacityDemand((int) ((order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity())))
							.setServiceStartTimeWindow(receiverPlan.getTimeWindows().get(0))
							.setServiceDuration(order.getServiceDuration())
							.build();
					
					if (newService2.getCapacityDemand() != 0) {
						ro.getCarrier().getServices().add(newService2);
					}
				}
	
				counter = 0;
				
			}
		}
	}

	@Override
	public void finishReplanning() {
	}

}
