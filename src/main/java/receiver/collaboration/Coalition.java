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
	
	void addReceiverCoalitionMember(Receiver receiver);
	
	void addCarrierCoalitionMember(Carrier carrier);
	
	void removeReceiverCoalitionMember(Receiver receiver);
	
	void removeCarrierCoalitionMember(Carrier carrier);
	
	Collection<Carrier> getCarrierCoalitionMembers();
	
	Collection<Receiver> getReceiverCoalitionMembers();

	//public void addAttribute(String coalitionDesc, double cost);

	void setCoalitionCost(double cost);

	double getCoalitionCost();
	
}
