package receiver.usecases.capetown;

import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;

import receiver.ReceiverAttributes;
import receiver.ReceiverPlan;

/**
 * This is a class that changes a receiver's collaboration status during replanning.
 * @author wlbean
 *
 */

public class CapeTownCollaborationStatusMutator implements GenericPlanStrategyModule<ReceiverPlan> {

	/*
	 * A class that changes a receiver's collaboration status.
	 */

	public CapeTownCollaborationStatusMutator(){

	}

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {

	}

	@Override
	public void handlePlan(ReceiverPlan receiverPlan) {
		boolean newstatus;
		boolean grandMember = (boolean) receiverPlan.getReceiver().getAttributes().getAttribute(ReceiverAttributes.grandCoalitionMember.toString());
//		boolean status = (boolean) receiverPlan.getReceiver().getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.toString());
		boolean status = (boolean) receiverPlan.getCollaborationStatus();


		if (grandMember == true){
			if (status == true){
				newstatus = false;
//				/* Select a random day time window for non-collaborating receivers */
//				TimeWindow newWindow = CapeTownScenarioBuilder.selectRandomDayTimeStart(CapeTownExperimentParameters.TIME_WINDOW_DURATION);
//				TimeWindow oldWindow = receiverPlan.getTimeWindows().get(0);
//				/* Remove old and add new time windows */
//				receiverPlan.getTimeWindows().remove(oldWindow);
//				receiverPlan.getTimeWindows().add(newWindow);
			} else {
				newstatus = true;
////				/* Select a random night time window for collaborating receivers */
//				TimeWindow newWindow = CapeTownScenarioBuilder.selectRandomNightTimeStart(CapeTownExperimentParameters.TIME_WINDOW_DURATION, receiverPlan.getReceiver());
//				TimeWindow oldWindow = receiverPlan.getTimeWindows().get(0);
//				/* Remove old and add new time windows */
//				receiverPlan.getTimeWindows().remove(oldWindow);
//				receiverPlan.getTimeWindows().add(newWindow);
			}
		} else {
			newstatus = status;
		}
		
		receiverPlan.getReceiver().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), newstatus);
		receiverPlan.getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), newstatus);
		receiverPlan.setCollaborationStatus(newstatus);

	}
	
	@Override
	public void finishReplanning() {

	}

}
