/**
 * 
 */
package receiver.usecases;

import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.ExpBetaPlanChanger;
import org.matsim.core.replanning.selectors.KeepSelected;
import org.matsim.core.utils.misc.Time;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.replanning.OrderChanger;
import receiver.replanning.OrderSizeMutator;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;
import receiver.replanning.ServiceTimeMutator;

/**
 * Collection of receiver replanning strategies.
 * 
 * @author wlbean
 *
 */

/* TODO Currently the carrier does not reroute its plan every iteration, so it basically "ignores" any changes the receiver makes to his orders during each run. We should update the code to allow the receiver to only make changes every 10 iterations (or so) and then reroute the Carrier in jsprit after these changes were made. */

public class MyReceiverOrderStrategyManagerFactorImpl implements ReceiverOrderStrategyManagerFactory {

	@Override
	public GenericStrategyManager<ReceiverPlan, Receiver> createReceiverStrategyManager() {
		final GenericStrategyManager<ReceiverPlan, Receiver> stratMan = new GenericStrategyManager<>();
		stratMan.setMaxPlansPerAgent(5);
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanChanger<ReceiverPlan, Receiver>(1.));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 2.0);

		}
		
		/*
		 * Increase service duration with specified duration (mutationTime) until specified maximum service time (mutationRange) is reached. 
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new ServiceTimeMutator(Time.parseTime("00:30:00"), Time.parseTime("04:00:00"), true));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.5);
		}
		
		/* 
		 * Decreases service duration with specified duration (mutationTime) until specified minimum service time (mutationRange) is reached. 
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new ServiceTimeMutator(Time.parseTime("00:30:00"), Time.parseTime("0:30:00"), false));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.5);
		}
		
		/*
		 * Increases the number of weekly deliveries with 1 at a time (and thereby decreasing order quantity).
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new OrderSizeMutator(true));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.5);		
		}
		
		/*
		 * Decreases the number of weekly deliveries with 1 at time (and thereby increase order quantity).
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new OrderSizeMutator(false));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.5);		
		}
		
		/*TODO:
		 * 1. Consider a "time window wiggle" replanning strategy that searches 
		 *    randomly within the current time window, but also between different
		 *    time windows if there are multiple.
		 * 2. Consider/Add a ConfigModule.
		 */

		return stratMan;
	}
	
}
