package receiver.replanning;

import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;

import receiver.ReceiverPlan;

public class CollaborationStatusChanger implements GenericPlanStrategyModule<ReceiverPlan> {

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handlePlan(ReceiverPlan plan) {
		boolean status = (boolean) plan.getCollaborationStatus();
		plan.getReceiver().getAttributes().putAttribute("collaborationStatus", status);		
	}

	@Override
	public void finishReplanning() {
		// TODO Auto-generated method stub
		
	}

}
