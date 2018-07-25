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
import receiver.replanning.TimeWindowMutator;

/**
 * Collection of receiver replanning strategies.
 * 
 * @author wlbean
 *
 */


public class MyReceiverOrderStrategyManagerFactorImpl implements ReceiverOrderStrategyManagerFactory {

	
	public MyReceiverOrderStrategyManagerFactorImpl(){
	}

	@Override
	public GenericStrategyManager<ReceiverPlan, Receiver> createReceiverStrategyManager() {
		final GenericStrategyManager<ReceiverPlan, Receiver> stratMan = new GenericStrategyManager<>();
		stratMan.setMaxPlansPerAgent(5);
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanChanger<ReceiverPlan, Receiver>(1.));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.8);

		}
		
		/*
		 * Increase service duration with specified duration (mutationTime) until specified maximum service time (mutationRange) is reached. 
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new ServiceTimeMutator(Time.parseTime("00:30:00"), Time.parseTime("04:00:00"), true));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.1);
		}
		
		/* 
		 * Decreases service duration with specified duration (mutationTime) until specified minimum service time (mutationRange) is reached. 
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new ServiceTimeMutator(Time.parseTime("00:30:00"), Time.parseTime("0:30:00"), false));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.1);
		}
		
		/*
		 * Increases the number of weekly deliveries with 1 at a time (and thereby decreasing order quantity).
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new OrderSizeMutator(true));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.0);		
		}
		
		/*
		 * Decreases the number of weekly deliveries with 1 at time (and thereby increase order quantity).
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new OrderSizeMutator(false));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.0);		
		}
		
		/*
		 * Increases or decreases the time window start or time window end times.
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new TimeWindowMutator(Time.parseTime("02:00:00")));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.0);		
		}

		return stratMan;
	}
	
}
