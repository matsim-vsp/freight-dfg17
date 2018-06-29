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

import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierService.Builder;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;

import receiver.ReceiverPlan;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

public class OrderChanger implements GenericPlanStrategyModule<ReceiverPlan> {
	
	/**
	 * This class rewrites a carrier's services after a receiver changed its plan.
	 */
	
	public OrderChanger(){		
	}
	

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {

	}

	@Override
	public void handlePlan(ReceiverPlan receiverPlan) {
		
		/* Create list of receiver orders. */
		for (ReceiverOrder ro: receiverPlan.getReceiverOrders()){
			
			for(Order order: ro.getReceiverOrders()){
				List<CarrierService> services = new ArrayList<>(ro.getCarrier().getServices().size());
				List<CarrierService> servicesToRemove = new ArrayList<>(ro.getCarrier().getServices().size());
				
			/*  Check to see if a particular carrier service does indeed belong to the receiver and then changes the service parameters according to the receiver's order. Currently it compares carrier service id with receiver order id. This might be changed in the future. */
					
			Iterator<CarrierService> iterator = ro.getCarrier().getServices().iterator();		
			while(iterator.hasNext()){
				CarrierService newService = null;
				CarrierService service = iterator.next();
				
				/* Check to see if this carrier service is indeed this receiver's order. Before updating. */
				Builder builder = CarrierService.Builder.newInstance(service.getId(), service.getLocationLinkId());
				if (service.getId().toString() == order.getId().toString()){
				newService = builder.setCapacityDemand((int) (order.getOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity())).setServiceStartTimeWindow(receiverPlan.getReceiver().getTimeWindows().get(0)).setServiceDuration(order.getServiceDuration()).build();

				}
				else
				newService = builder.setCapacityDemand(service.getCapacityDemand()).setServiceStartTimeWindow(service.getServiceStartTimeWindow()).setServiceDuration(service.getServiceDuration()).build();
				
				services.add(newService);
				servicesToRemove.add(service);
			}
			
			ro.getCarrier().getServices().removeAll(servicesToRemove);
			ro.getCarrier().getServices().addAll(services);
		}

		}
	}

	@Override
	public void finishReplanning() {
	}

}
