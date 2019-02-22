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

package receiver.collaboration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.TimeWindow;

import receiver.Receiver;
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
	private Scenario sc;

	final private Logger log = Logger.getLogger(ProportionalCostSharing.class);
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
	public void allocateCoalitionCosts() {

		log.info("Performing proportional cost allocation based on volume.");

		/* Get all the the cross-referenced receiver-carriers. */
		log.info("   Cross-referencing all carrier-receiver relationships...");
		Map<Id<Carrier>, List<Receiver>> carrierCustomers = new HashMap<>();

		final Map<Id<Receiver>, Receiver> receivers = ReceiverUtils.getReceivers( sc ).getReceivers();
		for(Receiver receiver : receivers.values()) {

			ReceiverPlan plan = receiver.getSelectedPlan();
			if (plan == null) {
				log.warn("Receiver plan not yet selected.");
			}

			for(ReceiverOrder ro : plan.getReceiverOrders()) {
				Id<Carrier> carrierId = ro.getCarrierId();

				if(!carrierCustomers.containsKey(carrierId)) {
					carrierCustomers.put(carrierId, new ArrayList<>());
				}
				carrierCustomers.get(carrierId).add(receiver);
			}
		}

		/* Calculate the proportional volume. */
		Map<Id<Carrier>, Map<Id<Receiver>, Double>> proportionalMap = new HashMap<>();
		log.info("   Calculating receivers' proportional volume from each carrier's perspective....");

		double totalCoalitionVolume = 0.0;
		double totalCoalitionScore = 0.0;

		for( Map.Entry<Id<Carrier>, List<Receiver>> entry : carrierCustomers.entrySet() ){
			// (go through the carriers one by one)

			Id<Carrier> carrierId = entry.getKey() ;
			Carrier carrier = ReceiverUtils.getCarriers( sc ).getCarriers().get(carrierId);
			final List<Receiver> receiverList = entry.getValue();

			double fixedFeeVolume = 0.0;

			/* Calculate this receiver's total volume with the carrier. */
			for(Receiver thisReceiver : receiverList ) {

				if ( (boolean) thisReceiver.getAttributes().getAttribute( ReceiverUtils.ATTR_COLLABORATION_STATUS ) ){
					ReceiverOrder ro = thisReceiver.getSelectedPlan().getReceiverOrder(carrierId);
					totalCoalitionVolume += getReceiverOrderTotal(ro);
				} else {
					ReceiverOrder ro = thisReceiver.getSelectedPlan().getReceiverOrder(carrierId);
					fixedFeeVolume += getReceiverOrderTotal(ro);
					/*TODO Why not? We need the fixedFeeVolume later (JWJ, Feb19). Come back and check if we find exceptions!! */
					throw new RuntimeException( "I don't want these." ) ;
				}
				//				carrierCoalitionVolume += getReceiverOrderTotal(thisReceiver.getSelectedPlan().getReceiverOrder(carrierId));
			}

			//			carrier.getAttributes().put("carrierCoalitionVolume", carrierCoalitionVolume);

			/* Now calculate each receiver's proportion of the total volume. */
			for(Receiver thisReceiver : receiverList ) {

				//				if( ReceiverUtils.getCoalition( sc ).getReceiverCoalitionMembers().contains(thisReceiver) == true){
				if ( (boolean) thisReceiver.getAttributes().getAttribute( ReceiverUtils.ATTR_COLLABORATION_STATUS ) ){
					ReceiverOrder ro = thisReceiver.getSelectedPlan().getReceiverOrder(carrierId);

					if(!proportionalMap.containsKey(carrierId)) {
						proportionalMap.put(carrierId, new HashMap<>());
					}

					/* The essence of the proportional assignment.*/
					proportionalMap.get(carrierId).put(thisReceiver.getId(), getReceiverOrderTotal(ro) / (totalCoalitionVolume) );
				}
			}


			/* Calculate the total coalition cost. */
			totalCoalitionScore = totalCoalitionScore + carrier.getSelectedPlan().getScore() + ((fixedFeeVolume*fee)/1000);
			// (The above is what I found.  In principle, the carrier first collects the fixed volume fees.  Whatever carrier cost remains, is passed on to the coalition.
			// The sign convention, however, is odd: the score is typically negative.  So I guess to compensate, the fixedFeeVolume is "-" AND has an additional "*-1".
			// In the end, we will have a negative coalition cost, which has to be interpreted as a positive cost, but again a negative score.  ???  kai, jan'19)


		}
		/* Allocate the total coalition cost. */
		final Coalition coalition = ReceiverUtils.getCoalition( sc );
		coalition.setCoalitionCost(-totalCoalitionScore );
		log.warn("      Total coalition score: " + totalCoalitionScore);

//		log.warn("totalCoalitionScore=" + totalCoalitionScore) ;
//		System.exit(-1) ;

		/* Scoring each carrier */
		//		for(Id<Carrier> carriedId : carrierCustomers.keySet()) {
		//			Carrier carrier = scenario.getCarriers().getCarriers().get(carriedId);
		//			/* TODO This must be updated, because not all Carriers will deliver the same volume. This can be done by making
		//			 * the carrier attributable, and introducing a carrierCoalitionVolume attribute that can be used to calculate
		//			 * the carrier's proportion.
		//			 */
		//			//			double carrierCoalitionVolume = (double) carrier.getAttributes().get("carrierCoalitionVolume");
		//			//			double newScore = scenario.getCoalition().getCoalitionCost()*((carrierCoalitionVolume*0.5)/totalCoalitionVolume);
		//			double newScore = scenario.getCoalition().getCoalitionCost()*(((totalCoalitionVolume/nrOfCarriers)*0.5)/totalCoalitionVolume);
		//			if (newScore < 0){
		//				carrier.getSelectedPlan().setScore(newScore);
		//			}
		//			else carrier.getSelectedPlan().setScore(0.0);
		//		}

		/* Score the individual receiver plans. */
		log.info("  Scoring the individual receivers...");

		for(Receiver thisReceiver : receivers.values()) {
			ReceiverPlan plan = thisReceiver.getSelectedPlan();
			TimeWindow tw = plan.getTimeWindows().get(0);
			//Calculate the receiver's timewindow cost for the selected plan.
			double twCost = ((tw.getEnd()-tw.getStart())/3600)*((double) thisReceiver.getAttributes().getAttribute(ReceiverUtils.ATTR_RECEIVER_TW_COST));

			/* Score non-collaborating receivers and calculate the total cost allocated to them. */
			//			if( ReceiverUtils.getCoalition( sc ).getReceiverCoalitionMembers().contains(thisReceiver) == false){
			if ( !((boolean) thisReceiver.getAttributes().getAttribute( ReceiverUtils.ATTR_COLLABORATION_STATUS )) ){
				double total = 0.0;
				for(ReceiverOrder ro : plan.getReceiverOrders()) {
					double cost = (getReceiverOrderTotal( ro ) /1000)*-1*fee;
					ro.setScore(cost);
					total += cost;
				}
				
				/*
				 * Include the total order cost and allocate to receiver. Also add an hourly time window cost for the receiver, 
				 * assuming that receivers must hire at least one employee to be available at receiving for deliveries every 
				 * hour of the delivery time window.
				  */
				
				plan.setScore(total + twCost);

			} else {

				/* Score the collaborating receiver plans. */
				double total = 0.0;
				for(ReceiverOrder ro : plan.getReceiverOrders()) {
					double score = -coalition.getCoalitionCost() * proportionalMap.get(ro.getCarrierId() ).get(thisReceiver.getId() );
					ro.setScore(score);
					total += score;
				}
				/* Fixed fee delivers may cover (more than) Carrier costs. However,
				 * the Carrier will not PAY the receiver because they collaborate. */
				plan.setScore( Math.min(total, 0.0) );
			}
			log.warn("      Receiver '" + thisReceiver.getId().toString() + "' score:" + plan.getScore());

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
