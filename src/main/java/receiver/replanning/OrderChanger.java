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
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.CarrierShipment;
import org.matsim.contrib.freight.carrier.CarrierShipment.Builder;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;

import com.google.inject.Inject;

import receiver.ReceiverPlan;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

/**
 * Rewrites a carrier's services after a receiver changed its plan.
 * 
 * @author wlbean
 *
 */

public final class OrderChanger implements GenericPlanStrategyModule<ReceiverPlan> {
	
	@Inject Scenario sc;
	

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
//		boolean status = (boolean) receiverPlan.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString());
//		receiverPlan.getReceiver().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), status);
//		receiverPlan.setCollaborationStatus(status);
		
		/* TODO Find a way to identify the appropriate origin link Id. */
		/*FIXME This is extremely dangerous because we hard-code the facility 
		 * Id of the distribution centre. The implication is that we most 
		 * probably have to introduce the origin of each product to either
		 * ProductType or ReceiverProduct, likely the latter. */

		/* Create list of receiver orders. */
		for (ReceiverOrder ro: receiverPlan.getReceiverOrders()){
			int n = 0;
					
			for(Order order: ro.getReceiverProductOrders()){
//				List<CarrierService> services = new ArrayList<>();
//				List<CarrierService> servicesToRemove = new ArrayList<>();
				
				List<CarrierShipment> shipments = new ArrayList<>();
				List<CarrierShipment> shipmentsToRemove = new ArrayList<>();
				
				
				n = n + 1;
				/*  Check to see if a particular carrier service does indeed 
				 * belong to the receiver and then changes the service parameters 
				 * according to the receiver's order. Currently it compares carrier 
				 * service id with receiver order id. This might be changed in the 
				 * future. */
//				Iterator<CarrierService> iterator = ro.getCarrier().getServices().iterator();	
				Iterator<CarrierShipment> iterator = ro.getCarrier().getShipments().iterator();
				int counter = 0;
				
				CarrierShipment shipment = null;
				while(iterator.hasNext() && counter == 0){					
//					CarrierService service = iterator.next();
					shipment = iterator.next();
//					if (service.getId().toString() == order.getId().toString()){	
					if (shipment.getId().toString() == order.getId().toString()){	
						counter = 1;
					}
				}
				
				if (counter == 1){
//					Iterator<CarrierService> iterator2 = ro.getCarrier().getServices().iterator();		
					Iterator<CarrierShipment> iterator2 = ro.getCarrier().getShipments().iterator();		
					
					while(iterator2.hasNext()){					
//						CarrierService service = iterator2.next();
//						CarrierService newService = null;
						shipment = iterator2.next();
						CarrierShipment newShipment = null;
										
						/* Check to see if this unique order exists as a carrier service, if so check to see if this particular 
						 * carrier service is indeed this receiver's order. Before updating. */
						double numDel = order.getNumberOfWeeklyDeliveries();
						double sdemand;
						double random = MatsimRandom.getLocalInstance().nextDouble();
						double weekdemand = order.getOrderQuantity();
						double pdeliver = numDel/5;
													
							if (random <= pdeliver){
								sdemand = weekdemand/numDel;
							} else sdemand = 0;
							order.setDailyOrderQuantity(sdemand);
						
						
						if (shipment.getId().toString() == order.getId().toString()){		
//						if (service.getId().toString() == order.getId().toString()){		
//							Builder builder = CarrierService.Builder.newInstance(Id.create("Order" + receiverPlan.getReceiver().getId().toString() + Integer.toString(n), CarrierService.class), receiverPlan.getReceiver().getLinkId());
//							newService = builder
//									.setCapacityDemand((int) ((order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity())))
//									/*TODO This only looks at the FIRST time window. 
//									 * This may need revision once we handle multiple 
//									 * time windows. */
//									.setServiceStartTimeWindow(receiverPlan.getTimeWindows().get(0))
//									.setServiceDuration(order.getServiceDuration())
//									.build();
							
							Builder builder = CarrierShipment.Builder.newInstance(
									Id.create("Order" + receiverPlan.getReceiver().getId().toString() + Integer.toString(n), CarrierShipment.class),
									shipment.getFrom(), 
									receiverPlan.getReceiver().getLinkId(), 
									(int) ((order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity())));
							newShipment = builder
									.setDeliveryServiceTime(order.getServiceDuration())
//									/*TODO This only looks at the FIRST time window. 
//									 * This may need revision once we handle multiple 
//									 * time windows. */
									.setDeliveryTimeWindow(receiverPlan.getTimeWindows().get(0))
									.build();
							
						} else {
							newShipment = shipment;
						}
							
						/*Check to see if the service has a 0 capacity demand, and if that is the case,
						 * remove it from the carrier's list of services.
						 */
						if (newShipment.getSize() != 0) {
							shipments.add(shipment);
	
						}
						
						shipmentsToRemove.add(shipment);
					}
					
					ro.getCarrier().getShipments().removeAll(shipmentsToRemove);
					ro.getCarrier().getShipments().addAll(shipments);
								
				} else if (counter == 0){
					CarrierShipment newShipment2 = null;	

					Builder builder = CarrierShipment.Builder.newInstance(
							Id.create("Order" + receiverPlan.getReceiver().getId().toString() + Integer.toString(n), CarrierShipment.class),
							order.getProduct().getProductType().getOriginLinkId(),
							order.getReceiver().getLinkId(), 
							(int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity())));
					newShipment2 = builder
							.setDeliveryTimeWindow(shipment.getDeliveryTimeWindow())
							.setDeliveryServiceTime(shipment.getDeliveryServiceTime())
							.build();
					
					if (newShipment2.getSize() != 0) {
						ro.getCarrier().getShipments().add(newShipment2);
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
