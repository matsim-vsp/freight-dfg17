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
		boolean status = receiverPlan.getCollaborationStatus();


		if (grandMember == true){
			if (status == true){
				newstatus = false;
				/* Select a random day time window for non-collaborating receivers */
//				int min = 6;
//				int max = 18;
//				Random randomTime = new Random();
//				int randomStart =  (min + randomTime.nextInt(max - CapeTownExperimentParameters.TIME_WINDOW_DURATION - min + 1));
//				TimeWindow newWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
				TimeWindow newWindow = CapeTownScenarioBuilder.selectRandomDayTimeStart(CapeTownExperimentParameters.TIME_WINDOW_DURATION);
				TimeWindow oldWindow = receiverPlan.getTimeWindows().get(0);
				/* Remove old and add new time windows */
				receiverPlan.getTimeWindows().remove(oldWindow);
				receiverPlan.getTimeWindows().add(newWindow);
//				receiverPlan.getReceiver().getAttributes().putAttribute("EarlyDeliveries", false);
			} else {
				newstatus = true;
				/* Select a random night time window for collaborating receivers */
//				int min = 16;
//				int max = 24;
//				Random randomTime = new Random();
//				int randomStart =  (min + randomTime.nextInt(max - CapeTownExperimentParameters.TIME_WINDOW_DURATION - min + 1));
//				TimeWindow newWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
				TimeWindow newWindow = CapeTownScenarioBuilder.selectRandomNightTimeStart(CapeTownExperimentParameters.TIME_WINDOW_DURATION, receiverPlan.getReceiver());
				TimeWindow oldWindow = receiverPlan.getTimeWindows().get(0);
				/* Remove old and add new time windows */
				receiverPlan.getTimeWindows().remove(oldWindow);
				receiverPlan.getTimeWindows().add(newWindow);
//				receiverPlan.getReceiver().getAttributes().putAttribute("EarlyDeliveries", false);
			}
		} else {
			newstatus = status;
		}
		
		receiverPlan.getReceiver().getAttributes().putAttribute(ReceiverAttributes.collaborationStatus.toString(), newstatus);
		receiverPlan.setCollaborationStatus(newstatus);

	}
	
	@Override
	public void finishReplanning() {

	}

}
