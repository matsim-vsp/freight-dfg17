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
		plan.getReceiver().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), status);		
	}

	@Override
	public void finishReplanning() {
		// TODO Auto-generated method stub
		
	}

}
