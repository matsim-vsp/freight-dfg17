/**
 * 
 */
package receiver.collaboration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.TimeWindow;

import org.matsim.contrib.freight.utils.FreightUtils;
import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

/**
 * This is a carrier-receiver coalition cost sharing implementation based on the marginal cost sharing method.
 * @author wlbean
 *
 */
public final class MarginalCostSharing implements ReceiverCarrierCostAllocation {
	
	final private Logger log = Logger.getLogger(ProportionalCostSharing.class);
//	private Attributes attributes = new Attributes();
//	private String descr = "Marginal sharing of costs between carrier(s) and receiver(s)";
	private final double fee;
	private final Scenario sc;

	/**
	 * Create a new marginal cost sharing instance where a fixed fee per tonne (as specified) is 
	 * charged by the carrier for non-collaborating receivers.
	 */
	public MarginalCostSharing(double fee, Scenario sc){
		this.sc = sc;
		this.fee = fee;
	}


	/* TODO Currently assuming that the carrier has no choice but to be part of the coalition. This should 
	 * be changed when more than one carrier is involved.*/
	
	@Override
	public void allocateCoalitionCosts() {
		
		log.info("Performing marginal cost allocation based on volume.");

		/* Get all the carriers and count the number of grand coalition members. */
		log.info("Creating a list of carriers.");
		
		List<Id<Carrier>> carriers = new ArrayList<>();
		int counter = 0;
		for(Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()) {
			ReceiverPlan plan = receiver.getSelectedPlan();
			if (plan == null) {
				log.warn("Receiver plan not yet selected.");
			}
			
			/* Create a list of carriers.*/
			for(ReceiverOrder ro : plan.getReceiverOrders()) {
				Id<Carrier> carrierId = ro.getCarrierId();
				if(!carriers.contains(carrierId)) {
					carriers.add(carrierId);
				}
			}
			
			/* Count the number of grand coalition receiver members. */			
			boolean status = (boolean) receiver.getAttributes().getAttribute( ReceiverUtils.ATTR_GRANDCOALITION_MEMBER );
			if (status){
				counter += 1;
			}
		}

		/* Determine coalition scores and allocate carrier score. */
		Iterator<Id<Carrier>> iterator = carriers.iterator();
		while (iterator.hasNext()){

			Id<Carrier> carrierId = iterator.next();
			Carrier carrier = FreightUtils.getCarriers(sc).getCarriers().get(carrierId);
//			double fixedFeeVolume = 0.0;
//			
//			/* Determine the total volume of non-coalition members. */
//			for(Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()) {
//				if((boolean) receiver.getAttributes().getAttribute(ReceiverUtils.ATTR_GRANDCOALITION_MEMBER ) == false){
//					ReceiverOrder ro = receiver.getSelectedPlan().getReceiverOrder(carrierId);					
//					fixedFeeVolume += getReceiverOrderTotal(ro);
//				}								
//			}

			/*Adding attributes containing coalition details and cost in order to calculate marginal contribution later on. */
//			double cost = carrier.getSelectedPlan().getScore() - (fixedFeeVolume*fee*-1)/1000;
			double cost = carrier.getSelectedPlan().getScore();

			/* Capture the grand coalition score */
			if (counter == ReceiverUtils.getCoalition( sc ).getReceiverCoalitionMembers().size()){
				if ( ReceiverUtils.getCoalition( sc ).getAttributes().getAsMap().containsKey("C(N)")){
					ReceiverUtils.getCoalition( sc ).getAttributes().removeAttribute("C(N)");
				}
				ReceiverUtils.getCoalition( sc ).getAttributes().putAttribute("C(N)", cost);

			} else {

				/* Capture all the sub-coalition scores */
				String coalitionDesc = "C(N|{" ;
				
				for (Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()){
					if (!((boolean) receiver.getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS))){
						coalitionDesc =  coalitionDesc + receiver.getId().toString();
					}
				}
				coalitionDesc = coalitionDesc + "})";
				
				/* Remove old attribute of this sub-coalition before updating with new attribute.*/
				if ( ReceiverUtils.getCoalition( sc ).getAttributes().getAsMap().containsKey(coalitionDesc)){
					ReceiverUtils.getCoalition( sc ).getAttributes().removeAttribute(coalitionDesc);
				}
				ReceiverUtils.getCoalition( sc ).getAttributes().putAttribute(coalitionDesc, cost);

				/* Calculate carrier score */
//				double subScore = 0.0;
//				double grandScore = (double) ReceiverUtils.getCoalition( sc ).getAttributes().getAttribute("C(N)");
//				carrier.getSelectedPlan().setScore(grandScore - subScore);	
			}
		}

		/* Calculate individual receiver scores.*/
		double grandScore = 0;
		if ( ReceiverUtils.getCoalition( sc ).getAttributes().getAsMap().containsKey("C(N)")){
			grandScore = (double) ReceiverUtils.getCoalition( sc )
					.getAttributes()
					.getAttribute("C(N)");					
		}
		
		for (Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()){
			double twCost = 0.0;
			double total = 0.0;
			ReceiverPlan plan = receiver.getSelectedPlan();
			TimeWindow tw = plan.getTimeWindows().get(0);
			//Calculate the receiver's timewindow cost for the selected plan.
			twCost = ((tw.getEnd()-tw.getStart())/3600)*((double) receiver.getAttributes().getAttribute(ReceiverUtils.ATTR_RECEIVER_TW_COST));
			
			/* 
			 * Checks to see if the receiver is part of the coalition, if so, allocate marginal cost, 
			 * if not allocate fixed fee per tonne.					 
			 */
//			if ( !ReceiverUtils.getCoalition( sc ).getReceiverCoalitionMembers().contains(receiver)){
//			double total = 0.0;
			if((boolean) receiver.getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS)){
				double subScore = 0;
				if ( ReceiverUtils.getCoalition( sc ).getAttributes().getAsMap().containsKey("C(N|{" + receiver.getId().toString() + "})")){
					subScore = (double) ReceiverUtils.getCoalition( sc )
							.getAttributes()
							.getAttribute("C(N|{" + receiver.getId().toString() + "})");
				}
				
				total = grandScore - subScore;
				
				plan.setScore( Math.min(total - twCost, twCost) );
				/*
				 * Setting the receiver order score as the receiver plan score.
				 * TODO This only works with one carrier and one receiver order....must be updated.
				 */
				for (ReceiverOrder ro : plan.getReceiverOrders()) {
					ro.setScore(Math.min(total - twCost, 0.0));
				}

			} else {
				
				for(ReceiverOrder ro : plan.getReceiverOrders()) {
					double volume = 0.0;
					double nonDeliveryCost = 0.0;
					for(Order order : ro.getReceiverProductOrders()) {
					if (order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity() != 0) {
						volume += order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity();
					} else {
						int countertwo = (int) order.getNumberOfWeeklyDeliveries();

						/*
						 * TODO This is currently hard coded with one experiment parameter values.
						 * This should be updated to determine the costs based on each experiment's
						 * unique parameters.
						 */
						
						if (countertwo == 4) {
							// (light vehicle fixed cost + carrierTimeCost) * 4/5 * 1
							nonDeliveryCost += 1702.50;
						} else if (countertwo == 3) {
							// (light vehicle fixed cost+ carrierTimeCost) * 3/5 * 2
							nonDeliveryCost += 2553.74;
						} else if (countertwo == 2) {
							// (light vehicle fixed cost + carrierTimeCost) * 2/5 * 3
							nonDeliveryCost += 2553.74;
						} else if (countertwo == 1) {
							// (heavy vehicle fixed cost + carrierTimeCost) * 1/5 * 4 
							nonDeliveryCost += 2851.30;
						} else {
							throw new IllegalArgumentException("Number of deliveries must be between 1 and 4 for non delivery fee.");
						}
					
					}
				}

				double cost = (volume /1000)*-1*fee  - nonDeliveryCost;
				ro.setScore(cost);
				total += cost;
				
				}
				plan.setScore(total - twCost);
			}
				
		
		log.warn("      Receiver '" + receiver.getId().toString() + "' score:" + plan.getScore());
		}
			
				
//				double fixedFeeScore = 0.0;					
//				double thisVolume = 0.0;
//				for (ReceiverOrder ro : receiver.getSelectedPlan().getReceiverOrders()){						
//					thisVolume += getReceiverOrderTotal(ro);
//				}
//				fixedFeeScore = (thisVolume*fee*-1)/1000;					
//				receiver.getSelectedPlan().setScore(fixedFeeScore);			
		log.info("Done with marginal cost calculation.");
	}
	
//	/* Calculates the total volume of a ReceiverOrder. */
//	private double getReceiverOrderTotal(ReceiverOrder ro) {
//		double total = 0.0;
//		for(Order order : ro.getReceiverProductOrders()) {
//			total += order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity();
//		}
//		return total;
//	}

}
