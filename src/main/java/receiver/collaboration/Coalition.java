/**
 * 
 */
package receiver.collaboration;

import java.util.Collection;

import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.utils.objectattributes.attributable.Attributable;

import receiver.Receiver;

/**
 * This is an interface to create and manage carrier-receiver coalitions.
 * 
 * @author wlbean
 *
 */

public interface Coalition extends Attributable {
	
	public void addReceiverCoalitionMember(Receiver receiver);
	
	public void addCarrierCoalitionMember(Carrier carrier);
	
	public void removeReceiverCoalitionMember(Receiver receiver);
	
	public void removeCarrierCoalitionMember(Carrier carrier);
	
	public Collection<Carrier> getCarrierCoalitionMembers();
	
	public Collection<Receiver> getReceiverCoalitionMembers();

	//public void addAttribute(String coalitionDesc, double cost);

	void setCoalitionCost(double cost);

	double getCoalitionCost();
	
}
