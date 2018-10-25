package receiver.replanning;

import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;

import receiver.ReceiverAttributes;
import receiver.ReceiverPlan;

/**
 * This is a class that changes a receiver's collaboration status during replanning.
 * @author wlbean
 *
 */

public class CollaborationStatusMutator implements GenericPlanStrategyModule<ReceiverPlan> {
	
	/*
	 * A class that changes a receiver's collaboration status.
	 */
	
public CollaborationStatusMutator(){
		
	}

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {

	}

	@Override
	public void handlePlan(ReceiverPlan receiverPlan) {
		boolean newstatus;
		boolean grandMember = (boolean) receiverPlan.getReceiver().getAttributes().getAttribute(ReceiverAttributes.grandCoalitionMember.toString());
//		boolean status = receiverPlan.getCollaborationStatus();
		boolean status = (boolean) receiverPlan.getReceiver().getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString());
		

		if (grandMember == true){
			if (status == true){
				newstatus = false;
			} else {
				newstatus = true;
			}
		} else newstatus = status;

		receiverPlan.getReceiver().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), newstatus);
		receiverPlan.getReceiver().getSelectedPlan().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), newstatus);
		receiverPlan.setCollaborationStatus(newstatus);
		

	}

	@Override
	public void finishReplanning() {

	}

}
