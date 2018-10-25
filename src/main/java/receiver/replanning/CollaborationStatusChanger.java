package receiver.replanning;

import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;

import receiver.ReceiverAttributes;
import receiver.ReceiverPlan;

public class CollaborationStatusChanger implements GenericPlanStrategyModule<ReceiverPlan> {

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handlePlan(ReceiverPlan plan) {
		boolean status = plan.getCollaborationStatus();
//		boolean status = (boolean) plan.getReceiver().getSelectedPlan().getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString());
		plan.getReceiver().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), status);		
//		boolean status = (boolean) plan.getReceiver().getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString());	
//		plan.setCollaborationStatus(status);
	}

	@Override
	public void finishReplanning() {
		// TODO Auto-generated method stub
		
	}

}
