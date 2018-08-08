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
	//private final Logger log = Logger.getLogger(MutableCoalition.class);
	private double coalitionCost = 0.0;
	private Attributes attributes = new Attributes();
	private ArrayList<Receiver> receiverMembers = new ArrayList<Receiver>();
	private ArrayList<Carrier> carrierMembers = new ArrayList<Carrier>();


	public MutableCoalition(){

	}
	
	@Override
	public Attributes getAttributes() {
		return attributes;
	}	
	

//	@Override
//	public void addAttribute(String attr, double cost) {
//		Object o = attributes.getAttribute(attr);
//		if(o != null) {
//			log.warn("The attribute '" + attr + "' has already bee set. Overwriting.");
//		}
//		attributes.putAttribute(attr, cost);		
//	}
	

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
