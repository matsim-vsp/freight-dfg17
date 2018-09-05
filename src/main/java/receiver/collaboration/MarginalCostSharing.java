/**
 * 
 */
package receiver.collaboration;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

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
 * This is a carrier-receiver coalition cost sharing implementation based on the marginal cost sharing method.
 * @author wlbean
 *
 */
public class MarginalCostSharing implements ReceiverCarrierCostAllocation {
	final private Logger log = Logger.getLogger(ProportionalCostSharing.class);
	private Attributes attributes = new Attributes();
	private String descr = "Marginal sharing of costs between carrier(s) and receiver(s)";
	private double fee;

	/**
	 * Create a new marginal cost sharing instance where a fixed fee per tonne (as specified) is 
	 * charged by the carrier for non-collaborating receivers.
	 * @param fee
	 */

	public MarginalCostSharing(double fee){
	}


	@Override
	public Attributes getAttributes() {
		return this.attributes;
	}

	@Override
	public String getDescription() {
		return this.descr;
	}

	/* TODO Currently assuming that the carrier has no choice but to be part of the coalition. This should 
	 * be changed when more than one carrier is involved.*/
	
	@Override
	public FreightScenario allocateCoalitionCosts(FreightScenario scenario) {		
		
		log.info("Performing proportional cost allocation based on volume.");

		/* Get all the carriers and count the number of grand coalition members. */
		log.info("Creating a list of carriers.");
		
		List<Id<Carrier>> carriers = new ArrayList<>();
		int counter = 0;		
		for(Receiver receiver : scenario.getReceivers().getReceivers().values()) {			
			ReceiverPlan plan = receiver.getSelectedPlan();
			if (plan == null) {
				log.warn("Receiver plan not yet selected.");
				return scenario;
			}
			
			/* Create a list of carriers.*/
			for(ReceiverOrder ro : plan.getReceiverOrders()) {
				Id<Carrier> carrierId = ro.getCarrierId();
				if(!carriers.contains(carrierId)) {
					carriers.add(carrierId);
				}
			}
			
			/* Count the number of grand coalition receiver members. */			
			boolean status = (boolean) receiver.getAttributes().getAttribute("grandCoalitionMember");			
			if (status == true){
				counter += 1;
			}
		}

		/* Determine coalition scores and allocate carrier score. */
		Iterator<Id<Carrier>> iterator = carriers.iterator();
		while (iterator.hasNext()){

			Id<Carrier> carrierId = iterator.next();
			Carrier carrier = scenario.getCarriers().getCarriers().get(carrierId);			
			double fixedFeeVolume = 0.0;
			
			/* Determine the total volume of non-coalition members. */
			for(Receiver receiver : scenario.getReceivers().getReceivers().values()) {				
				if(!scenario.getCoalition().getCarrierCoalitionMembers().contains(receiver)){
					ReceiverOrder ro = receiver.getSelectedPlan().getReceiverOrder(carrierId);					
					fixedFeeVolume += getReceiverOrderTotal(ro);
				}								
			}

			/*Adding attributes containing coalition details and cost in order to calculate marginal contribution later on. */
			double cost = carrier.getSelectedPlan().getScore() - (fixedFeeVolume*fee*-1)/1000;

			/* Capture the grand coalition score */			
			if (counter == scenario.getCoalition().getReceiverCoalitionMembers().size()){	
				if (scenario.getCoalition().getAttributes().getAsMap().containsKey("C({N})")){
					scenario.getCoalition().getAttributes().removeAttribute("C({N})");
				}
				scenario.getCoalition().getAttributes().putAttribute("C({N})", cost);

			} else {

				/* Capture all the sub-coalition scores */
				String coalitionDesc = "C({N}/{" ;

				for (Receiver receiver : scenario.getReceivers().getReceivers().values()){
					if (!scenario.getCoalition().getReceiverCoalitionMembers().contains(receiver)){					
						coalitionDesc =  coalitionDesc + receiver.getId().toString() + ",";
					}
				}
				coalitionDesc = coalitionDesc + "})";
				
				/* Remove old attribute of this sub-coalition before updating with new attribute.*/
				if (scenario.getCoalition().getAttributes().getAsMap().containsKey(coalitionDesc)){
					scenario.getCoalition().getAttributes().removeAttribute(coalitionDesc);
				}
				scenario.getCoalition().getAttributes().putAttribute(coalitionDesc, cost);

				/* Calculate carrier score */
				double subScore = 0.0;	
				double grandScore = (double) scenario.getCoalition().getAttributes().getAttribute("C({N})");
				carrier.getSelectedPlan().setScore(grandScore - subScore);	
			}
		}

		/* Calculate individual receiver scores.*/
		double grandScore = 0; 
		if (scenario.getCoalition().getAttributes().getAsMap().containsKey("C({N})")){
			grandScore = (double) scenario
					.getCoalition()
					.getAttributes()
					.getAttribute("C({N})");					
		}
		
		for (Receiver receiver : scenario.getReceivers().getReceivers().values()){
			/* 
			 * Checks to see if the receiver is part of the coalition, if so, allocate marginal cost, 
			 * if not allocate fixed fee per tonne.					 
			 */			
			if (scenario.getCoalition().getReceiverCoalitionMembers().contains(receiver)){
				double subScore = 0;
				if (scenario.getCoalition().getAttributes().getAsMap().containsKey("C({N}/{" + receiver.getId().toString() + ",})")){		
					subScore = (double) scenario
							.getCoalition()
							.getAttributes()
							.getAttribute("C({N}/{" + receiver.getId().toString() + ",})");
				}
				if (grandScore - subScore < 0){
					receiver.getSelectedPlan().setScore(grandScore - subScore);
				} else {
					receiver.getSelectedPlan().setScore(0.0);
				}

			} else {

				double fixedFeeScore = 0.0;					
				double thisVolume = 0.0;
				for (ReceiverOrder ro : receiver.getSelectedPlan().getReceiverOrders()){						
					thisVolume += getReceiverOrderTotal(ro);
				}
				fixedFeeScore = (thisVolume*fee*-1)/1000;					
				receiver.getSelectedPlan().setScore(fixedFeeScore);						
			}
		}
		return scenario;
	}
	
	/* Calculates the total volume of a ReceiverOrder. */
	private double getReceiverOrderTotal(ReceiverOrder ro) {
		double total = 0.0;
		for(Order order : ro.getReceiverProductOrders()) {
			total += order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity();
		}
		return total;
	}

}
