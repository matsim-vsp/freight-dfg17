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
	private Attributes attributes;
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
		
		/* Get all the carriers. */
		log.info("Creating a list of carriers.");
		List<Id<Carrier>> carriers = new ArrayList<>();
		
		for(Receiver receiver : scenario.getReceivers().getReceivers().values()) {
			
			ReceiverPlan plan = receiver.getSelectedPlan();
			if (plan == null) {
				log.warn("Receiver plan not yet selected.");
				return scenario;
			}
			
			for(ReceiverOrder ro : plan.getReceiverOrders()) {
				Id<Carrier> carrierId = ro.getCarrierId();
				
				if(!carriers.contains(carrierId)) {
					carriers.add(carrierId);
				}
			}
		}

				
		Iterator<Id<Carrier>> iterator = carriers.iterator();
		while (iterator.hasNext()){
			
			Id<Carrier> carrierId = iterator.next();
			Carrier carrier = scenario.getCarriers().getCarriers().get(carrierId);
				
		/*
		 * Adding attributes containing coalition details and cost in order to calculate marginal contribution later on.
		 */
		
		/* TODO Working with only one carrier...must be adapted to accommodate more than one. */
		
		double cost = carrier.getSelectedPlan().getScore();
		double grandScore = 0.0;
		
		/* Capture the grand coalition score */
		if (scenario.getReceivers().getReceivers().size() == scenario.getCoalition().getReceiverCoalitionMembers().size()){			
			scenario.getCoalition().addAttribute("c({N})", cost);
			grandScore = cost;
			
		} else {
			/* Capture all the sub-coalition scores */
			
			String coalitionDesc = "C({N}/{" ;
			
			for (Receiver receiver : scenario.getReceivers().getReceivers().values()){
				
				if (scenario.getCoalition().getReceiverCoalitionMembers().contains(receiver) == false){					
					coalitionDesc =  coalitionDesc + receiver.getId().toString() + ",";
				}
			}
			
			coalitionDesc = coalitionDesc + "})";
			scenario.getCoalition().addAttribute(coalitionDesc, cost);
			
			/* Calculate carrier score */
			double subScore = 0.0;	
			carrier.getSelectedPlan().setScore(grandScore - subScore);	
		}
		}
		
		
		/* Calculate individual receiver scores.*/
				
		for (Receiver receiver : scenario.getReceivers().getReceivers().values()){
				
			/* 
			 * Checks to see if the receiver is part of the coalition, if so, allocate marginal cost, 
			 * if not allocate fixed fee per tonne.	
			 * 		 
			 */
			
				if (scenario.getCoalition().getReceiverCoalitionMembers().contains(receiver) == true){
					
					double subScore = 0.0;
					
					if (scenario.getCoalition().getAttributes().getAttribute("C({N}/{" + receiver.getId().toString() + ",})") != null){		
						subScore = (double) scenario
							.getCoalition()
							.getAttributeValue("C({N}/{" + receiver.getId().toString() + ",})");	
					}
					
										
					double grandScore = 0.0; 
					
					if (scenario.getCoalition().getAttributes().getAttribute("C({N})") != null){
						grandScore = (double) scenario.getCoalition().getAttributeValue("C({N})");
					}
					
					
					receiver.getSelectedPlan().setScore(grandScore - subScore);
					
				} else {
					
					double fixedFeeScore = 0.0;					
					double thisVolume = 0.0;
					
					for (ReceiverOrder ro : receiver.getSelectedPlan().getReceiverOrders()){						
						thisVolume = getReceiverOrderTotal(ro);
					}
					
					fixedFeeScore = (thisVolume*fee*-1)/1000;					
					receiver.getSelectedPlan().setScore(fixedFeeScore);						
			}
			
		
		}
		
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
