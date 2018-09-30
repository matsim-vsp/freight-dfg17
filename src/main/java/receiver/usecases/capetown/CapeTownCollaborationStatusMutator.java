package receiver.usecases.capetown;

import java.util.Random;

import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;

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
		boolean newstatus = false;
		boolean grandMember = (boolean) receiverPlan.getReceiver().getAttributes().getAttribute("grandCoalitionMember");
		boolean status = (boolean) receiverPlan.getReceiver().getAttributes().getAttribute("collaborationStatus");


		if (grandMember == true){
			if (status == true){
				newstatus = false;
				/* Select a random day time window for non-collaborating receivers */
				int min = 8;
				int max = 16;
				Random randomTime = new Random();
				int randomStart =  (min + randomTime.nextInt(max - CapeTownExperimentParameters.TIME_WINDOW_DURATION - min + 1));
				TimeWindow newWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
				TimeWindow oldWindow = receiverPlan.getTimeWindows().get(0);
				/* Remove old and add new time windows */
				receiverPlan.getTimeWindows().remove(oldWindow);
				receiverPlan.getTimeWindows().add(newWindow);
//				receiverPlan.getReceiver().getAttributes().putAttribute("EarlyDeliveries", false);
			} else if (status == false){
				newstatus = true;
				/* Select a random night time window for collaborating receivers */
				int min = 16;
				int max = 24;
				Random randomTime = new Random();
				int randomStart =  (min + randomTime.nextInt(max - CapeTownExperimentParameters.TIME_WINDOW_DURATION - min + 1));
				TimeWindow newWindow = TimeWindow.newInstance(randomStart*3600, randomStart*3600 + CapeTownExperimentParameters.TIME_WINDOW_DURATION*3600);
				TimeWindow oldWindow = receiverPlan.getTimeWindows().get(0);
				/* Remove old and add new time windows */
				receiverPlan.getTimeWindows().remove(oldWindow);
				receiverPlan.getTimeWindows().add(newWindow);
				receiverPlan.getReceiver().getAttributes().putAttribute("EarlyDeliveries", false);
			}
		} else {
			newstatus = status;
		}
		receiverPlan.getReceiver().getAttributes().putAttribute("collaborationStatus", newstatus);
		receiverPlan.getReceiver().getSelectedPlan().getAttributes().putAttribute("collaborationStatus", newstatus);

	}
	
	@Override
	public void finishReplanning() {

	}

}
