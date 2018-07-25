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
package receiver.collaboration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.utils.objectattributes.attributable.Attributes;

import receiver.FreightScenario;
import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

/**
 * A proportional cost allocation between receivers and carriers.
 * 
 * Currently (Jun 2018) we only implement a version based on order quantity/volume. 
 * 
 * @author jwjoubert, wlbean
 */
public class ProportionalCostSharing implements ReceiverCarrierCostAllocation {
	final private Logger log = Logger.getLogger(ProportionalCostSharing.class);
	private Attributes attributes;
	private String descr = "Proportional sharing of costs between carrier(s) and receiver(s)";
	private double fee;
	
	/**
	 * Create a new proportional cost sharing instance where a fixed fee per tonne (as specified) is 
	 * charged by the carrier for non-collaborating receivers.
	 * @param fee
	 */
	
	public ProportionalCostSharing(double fee) {
		this.fee = fee;
	}
	

	@Override
	public Attributes getAttributes() {
		return this.attributes;
	}

	@Override
	public String getDescription() {
		return this.descr;
	}
	
	public void setDescription(String descr) {
		this.descr = descr;
	}


	@Override
	public FreightScenario allocateCoalitionCosts(FreightScenario scenario) {
		
		log.info("Performing proportional cost allocation based on volume.");
		
		/* Get all the the cross-referenced receiver-carriers. */
		log.info("   Cross-referencing all carrier-receiver relationships...");
		Map<Id<Carrier>, List<Id<Receiver>>> carrierCustomers = new HashMap<>();
		
		for(Receiver receiver : scenario.getReceivers().getReceivers().values()) {
			
			ReceiverPlan plan = receiver.getSelectedPlan();
			if (plan == null) {
				log.warn("Receiver plan not yet selected.");
				return scenario;
			}
			
			
			for(ReceiverOrder ro : plan.getReceiverOrders()) {
				Id<Carrier> carrierId = ro.getCarrierId();
				
				if(!carrierCustomers.containsKey(carrierId)) {
					carrierCustomers.put(carrierId, new ArrayList<>());
				}
				carrierCustomers.get(carrierId).add(receiver.getId());
			}
		}
		
		/* Calculate the proportional volume. */
		Map<Id<Carrier>, Map<Id<Receiver>, Double>> proportionalMap = new HashMap<>();
		log.info("   Calculating receivers' proportional volume from each carrier's perspective...");
		
		for(Id<Carrier> carriedId : carrierCustomers.keySet()) {
			//double allocatedCost = 0.0;
			double totalCoalitionVolume = 0.0;
			double totalVolume = 0.0;
			double fixedFeeVolume = 0.0;
			Carrier carrier = scenario.getCarriers().getCarriers().get(carriedId);
			
			
			/* Calculate this receiver's total volume with the carrier. */
			for(Id<Receiver> receiverId : carrierCustomers.get(carriedId)) {
				Receiver thisReceiver = scenario.getReceivers().getReceivers().get(receiverId);

				if(scenario.getCoalition().getCarrierCoalitionMembers().contains(thisReceiver) ==  true){
						ReceiverOrder ro = thisReceiver.getSelectedPlan().getReceiverOrder(carriedId);
						totalCoalitionVolume += getReceiverOrderTotal(ro);	
					} else {		

					ReceiverOrder ro = thisReceiver.getSelectedPlan().getReceiverOrder(carriedId);					
					fixedFeeVolume += getReceiverOrderTotal(ro);
					}				
				
				totalVolume += getReceiverOrderTotal(thisReceiver.getSelectedPlan().getReceiverOrder(carriedId));
			}
			

			
			/* Now calculate each receiver's proportion of the total volume. */ 
			for(Id<Receiver> receiverId : carrierCustomers.get(carriedId)) {
				Receiver thisReceiver = scenario.getReceivers().getReceivers().get(receiverId);
				
				if(scenario.getCoalition().getReceiverCoalitionMembers().contains(thisReceiver) == true){
		
					double thisVolume = 0.0;
					ReceiverOrder ro = thisReceiver.getSelectedPlan().getReceiverOrder(carriedId);
					
					thisVolume += getReceiverOrderTotal(ro);

					if(!proportionalMap.containsKey(carriedId)) {
						proportionalMap.put(carriedId, new HashMap<>());
					}
					
					/* The essence of the proportional assignment.*/
					proportionalMap.get(carriedId).put(receiverId, thisVolume / (totalCoalitionVolume + totalVolume));
					}							
			}
			
			scenario.getCoalition().setCoalitionCost(carrier.getSelectedPlan().getScore());
			
				/* Scoring carrier */

				double newscore = ((scenario.getCoalition().getCoalitionCost()+((fixedFeeVolume*fee)/1000))*totalVolume)/(totalCoalitionVolume + totalVolume);
				if (newscore < 0){
				carrier.getSelectedPlan().setScore(newscore);
				}
				else carrier.getSelectedPlan().setScore(0.0);
			
		
		

		
		/* Score the individual receiver plans. */
		log.info("  Scoring the individual receivers...");
		
		
		for(Receiver thisReceiver : scenario.getReceivers().getReceivers().values()) {
			
			ReceiverPlan plan = thisReceiver.getSelectedPlan();
				
				/* Score non-collaborating receivers and calculate the total cost allocated to them. */
				if(scenario.getCoalition().getReceiverCoalitionMembers().contains(thisReceiver) == false){
					
					double total = 0.0;	
			
					for(ReceiverOrder ro : plan.getReceiverOrders()) {

					double thisVolume = 0.0;
					thisVolume = getReceiverOrderTotal(ro);

					/* TODO We need to change the scoring of a receiver if he is not collaborating. Currently, the carrier charges a fixed cost per tonne
					 * regardless of its own cost. Where should this fixed rate per tonne be set? This is not the best place.
					 * 
					 */
					double cost = (thisVolume/1000)*-1*fee;
					ro.setScore(cost);
					total += cost;	

					}

				plan.setScore(total);
				
			} else {	
			
			/* Score the collaborating receiver plans. */

				double total = 0.0;			
					
				for(ReceiverOrder ro : plan.getReceiverOrders()) {
											
					double cost = (scenario.getCoalition().getCoalitionCost()+(fixedFeeVolume*fee)/1000) * proportionalMap.get(ro.getCarrierId()).get(thisReceiver.getId());
					ro.setScore(cost);
					total += cost;
				} 

				plan.setScore(total);
			}
				
		
		}
		
		/* TODO We should think about changing carrier scores, based on cost allocations...
		 * 
		 */
			

		}
		
		log.info("Done with proportional cost calculation.");
		return scenario;
	}


	private double getReceiverOrderTotal(ReceiverOrder ro) {
		double total = 0.0;
		for(Order order : ro.getReceiverOrders()) {
			total += order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity();
		}
		return total;
	}
	
	
	
	
}
