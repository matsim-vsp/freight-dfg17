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
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.utils.objectattributes.attributable.Attributes;

import com.google.inject.Inject;

import receiver.Receiver;
import receiver.ReceiverAttributes;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

/**
 * A proportional cost allocation between receivers and carriers.
 * 
 * Currently (Aug 2018) we only implement a version based on order quantity/volume. 
 * 
 * @author jwjoubert, wlbean
 */
public final class ProportionalCostSharing implements ReceiverCarrierCostAllocation {
//	@Inject Scenario sc;
	private Scenario sc;

	final private Logger log = Logger.getLogger(ProportionalCostSharing.class);
	private Attributes attributes;
	private String descr = "Proportional sharing of costs between carrier(s) and receiver(s)";
	private double fee;
	
	/**
	 * Create a new proportional cost sharing instance where a fixed fee per tonne (as specified) is 
	 * charged by the carrier for non-collaborating receivers.
	 * @param fee
	 */
	
	public ProportionalCostSharing(double fee, Scenario sc) {
		this.sc = sc;
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

	@Override
//	public MutableFreightScenario allocateCoalitionCosts( MutableFreightScenario scenario) {
	public void allocateCoalitionCosts() {
		
		log.info("Performing proportional cost allocation based on volume.");
		
		/* Get all the the cross-referenced receiver-carriers. */
		log.info("   Cross-referencing all carrier-receiver relationships...");
		Map<Id<Carrier>, List<Id<Receiver>>> carrierCustomers = new HashMap<>();
		
		for(Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()) {
		
			ReceiverPlan plan = receiver.getSelectedPlan();
			if (plan == null) {
				log.warn("Receiver plan not yet selected.");
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
		log.info("   Calculating receivers' proportional volume from each carrier's perspective....");
		
		double totalCoalitionVolume = 0.0;
		double totalCoalitionCost = 0.0;
//		int nrOfCarriers = 0;
		
		for(Id<Carrier> carriedId : carrierCustomers.keySet()) {
//			double carrierCoalitionVolume = 0.0;
//			nrOfCarriers += 1;
			double fixedFeeVolume = 0.0;
			Carrier carrier = ReceiverUtils.getCarriers( sc ).getCarriers().get(carriedId);

			/* Calculate this receiver's total volume with the carrier. */
			for(Id<Receiver> receiverId : carrierCustomers.get(carriedId)) {
				Receiver thisReceiver = ReceiverUtils.getReceivers( sc ).getReceivers().get(receiverId);
				
//				if( ReceiverUtils.getCoalition( sc ).getReceiverCoalitionMembers().contains(thisReceiver) ==  true){
				if ((boolean) thisReceiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()) == true){
					ReceiverOrder ro = thisReceiver.getSelectedPlan().getReceiverOrder(carriedId);
					totalCoalitionVolume += getReceiverOrderTotal(ro);
				} else {
					ReceiverOrder ro = thisReceiver.getSelectedPlan().getReceiverOrder(carriedId);					
					fixedFeeVolume += getReceiverOrderTotal(ro);
				}				
//				carrierCoalitionVolume += getReceiverOrderTotal(thisReceiver.getSelectedPlan().getReceiverOrder(carriedId));
			}
			
//			carrier.getAttributes().put("carrierCoalitionVolume", carrierCoalitionVolume);

			/* Now calculate each receiver's proportion of the total volume. */ 
			for(Id<Receiver> receiverId : carrierCustomers.get(carriedId)) {
				Receiver thisReceiver = ReceiverUtils.getReceivers( sc ).getReceivers().get(receiverId);
				
//				if( ReceiverUtils.getCoalition( sc ).getReceiverCoalitionMembers().contains(thisReceiver) == true){
				if ((boolean) thisReceiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()) == true){
					double thisVolume = 0.0;
					ReceiverOrder ro = thisReceiver.getSelectedPlan().getReceiverOrder(carriedId);

					thisVolume += getReceiverOrderTotal(ro);

					if(!proportionalMap.containsKey(carriedId)) {
						proportionalMap.put(carriedId, new HashMap<>());
					}

					/* The essence of the proportional assignment.*/
					proportionalMap.get(carriedId).put(receiverId, thisVolume / (totalCoalitionVolume));
					}							
			}
		

			/* Calculate the total coalition cost. */
			totalCoalitionCost = totalCoalitionCost + carrier.getSelectedPlan().getScore() - ((fixedFeeVolume*fee*-1)/1000);
		}
		/* Allocate the total coalition cost. */
		ReceiverUtils.getCoalition( sc ).setCoalitionCost(totalCoalitionCost);

		/* Scoring each carrier */
//		for(Id<Carrier> carriedId : carrierCustomers.keySet()) {
//			Carrier carrier = ReceiverUtils.getCarriers(sc).getCarriers().get(carriedId);
////			/* TODO This must be updated, because not all Carriers will deliver the same volume. This can be done by making
////			 * the carrier attributable, and introducing a carrierCoalitionVolume attribute that can be used to calculate
////			 * the carrier's proportion.
////			 */
////			//			double carrierCoalitionVolume = (double) carrier.getAttributes().get("carrierCoalitionVolume");
////			//			double newScore = scenario.getCoalition().getCoalitionCost()*((carrierCoalitionVolume*0.5)/totalCoalitionVolume);
//			double newScore = ReceiverUtils.getCoalition(sc).getCoalitionCost()*(((totalCoalitionVolume)*0.5)/totalCoalitionVolume);
//			if (newScore < 0){
//				carrier.getSelectedPlan().setScore(newScore);
//			}
//			else carrier.getSelectedPlan().setScore(0.0);		
//		}

			/* Score the individual receiver plans. */
			log.info("  Scoring the individual receivers...");
		
		for(Receiver thisReceiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()) {
				ReceiverPlan plan = thisReceiver.getSelectedPlan();

				/* Score non-collaborating receivers and calculate the total cost allocated to them. */
//			if( ReceiverUtils.getCoalition( sc ).getReceiverCoalitionMembers().contains(thisReceiver) == false){
			if ((boolean) thisReceiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString()) == false){
//				if (plan.getCollaborationStatus() == false){
					double total = 0.0;				
					for(ReceiverOrder ro : plan.getReceiverOrders()) {
						double thisVolume = 0.0;
						thisVolume = getReceiverOrderTotal(ro);		
						double cost = (thisVolume/1000)*-1*fee;
						ro.setScore(cost);
						total += cost;	
					}
					plan.setScore(total);		
					
				} else {		

					/* Score the collaborating receiver plans. */
					double total = 0.0;								
					for(ReceiverOrder ro : plan.getReceiverOrders()) {
						double cost = ReceiverUtils.getCoalition( sc ).getCoalitionCost() * proportionalMap.get(ro.getCarrierId()).get(thisReceiver.getId());
						ro.setScore(cost);
						total += cost;						
					} 
					if (total <= 0){
						plan.setScore(total);
					} else {
						plan.setScore(0.0);
					}					
				}
			}

		log.info("Done with proportional cost calculation.");
	}

	/* Calculate the total volume of a receiver order */
	private double getReceiverOrderTotal(ReceiverOrder ro) {
		double total = 0.0;
		for(Order order : ro.getReceiverProductOrders()) {
			total += order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity();
		}
		return total;
	}		
	
}
