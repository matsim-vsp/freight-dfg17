package receiver.collaboration;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.utils.FreightUtils;
import receiver.Receiver;
import receiver.ReceiverUtils;

public class CollaborationUtils{
	private CollaborationUtils(){} // do not instantiate

	public static MutableCoalition createCoalition(){
		return new MutableCoalition();
	}

	public static void setCoalitionFromReceiverAttributes( Scenario sc ){
		for ( Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()){
			if (receiver.getAttributes().getAttribute( ReceiverUtils.ATTR_COLLABORATION_STATUS ) != null){
				if ((boolean) receiver.getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS)){
					if (!ReceiverUtils.getCoalition( sc ).getReceiverCoalitionMembers().contains(receiver)){
						ReceiverUtils.getCoalition( sc ).addReceiverCoalitionMember(receiver);
					}
				} else {
					if ( ReceiverUtils.getCoalition( sc ).getReceiverCoalitionMembers().contains(receiver)){
						ReceiverUtils.getCoalition( sc ).removeReceiverCoalitionMember(receiver);
					}
				}
			}
		}
	}

	public static void createCoalitionWithCarriersAndAddCollaboratingReceivers(Scenario sc ){
		/* Add carrier and receivers to coalition */
		Coalition coalition = CollaborationUtils.createCoalition();

		for (Carrier carrier : FreightUtils.getCarriers(sc).getCarriers().values()){
			if (!coalition.getCarrierCoalitionMembers().contains(carrier)){
				coalition.addCarrierCoalitionMember(carrier);
			}
		}

		for ( Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()){
			if ( (boolean) receiver.getAttributes().getAttribute( ReceiverUtils.ATTR_COLLABORATION_STATUS ) ){
				if (!coalition.getReceiverCoalitionMembers().contains(receiver)){
					coalition.addReceiverCoalitionMember(receiver);
				}
			} else {
				if (coalition.getReceiverCoalitionMembers().contains(receiver)){
					coalition.removeReceiverCoalitionMember(receiver);
				}
			}
		}

		ReceiverUtils.setCoalition( coalition, sc );
	}
}
