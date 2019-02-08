package receiver.collaboration;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.core.controler.MatsimServices;
import receiver.Receiver;
import receiver.ReceiverUtils;

public class CollaborationUtils{
	private CollaborationUtils(){} // do not instantiate

	public static MutableCoalition createCoalition(){
		return new MutableCoalition();
	}

	public static void setCoalitionFromReceiverAttributes( MatsimServices controler ){
		for ( Receiver receiver : ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().values()){
			if (receiver.getAttributes().getAttribute( ReceiverUtils.ATTR_COLLABORATION_STATUS ) != null){
				if ((boolean) receiver.getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS) == true){
					if (!ReceiverUtils.getCoalition( controler.getScenario() ).getReceiverCoalitionMembers().contains(receiver)){
						ReceiverUtils.getCoalition( controler.getScenario() ).addReceiverCoalitionMember(receiver);
					}
				} else {
					if ( ReceiverUtils.getCoalition( controler.getScenario() ).getReceiverCoalitionMembers().contains(receiver)){
						ReceiverUtils.getCoalition( controler.getScenario() ).removeReceiverCoalitionMember(receiver);
					}
				}
			}
		}
	}

	public static void createCoalitionWithCarriersAndAddCollaboratingReceivers(Scenario sc ){
		/* Add carrier and receivers to coalition */
		Coalition coalition = CollaborationUtils.createCoalition();

		for (Carrier carrier : ReceiverUtils.getCarriers( sc ).getCarriers().values()){
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
