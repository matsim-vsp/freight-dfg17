/**
 * 
 */
package receiver.collaboration;

import java.util.ArrayList;
import java.util.Collection;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.utils.objectattributes.attributable.Attributes;

import receiver.Receiver;

/**
 * This class implements a coalition between carriers and receivers.
 * 
 * @author wlbean
 *
 */
public class MutableCoalition implements Coalition {
	private double coalitionCost = 0.0;
	private Attributes attributes;
	private ArrayList<Receiver> receiverMembers = new ArrayList<Receiver>();
	private ArrayList<Carrier> carrierMembers = new ArrayList<Carrier>();


	public MutableCoalition(){

	}
	
	@Override
	public Attributes getAttributes() {
		return attributes;
	}	
	

	@Override
	public void addAttribute(String attr, double cost) {
		attributes.putAttribute(attr, cost);		
	}
	
	@Override
	public double getAttributeValue(String attr) {
		return (double) attributes.getAttribute(attr);
	}


	@Override
	public double getCoalitionCost() {
		return this.coalitionCost;
	}


	@Override
	public void setCoalitionCost(double cost) {
		this.coalitionCost = cost;

	}

	@Override
	public void addReceiverCoalitionMember(Receiver receiver) {
		receiverMembers.add(receiver);
	}


	@Override
	public void addCarrierCoalitionMember(Carrier carrier) {
		carrierMembers.add(carrier);		
	}

	@Override
	public void removeReceiverCoalitionMember(Receiver receiver) {
		receiverMembers.remove(receiver);		
	}

	@Override
	public void removeCarrierCoalitionMember(Carrier carrier) {
		carrierMembers.remove(carrier);		
	}

	@Override
	public Collection<Carrier> getCarrierCoalitionMembers() {
		return this.carrierMembers;
	}

	@Override
	public Collection<Receiver> getReceiverCoalitionMembers() {
		return this.receiverMembers;
	}




}
