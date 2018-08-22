/**
 * 
 */
package receiver.replanning;

import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.ExpBetaPlanChanger;
import org.matsim.core.replanning.selectors.KeepSelected;
import org.matsim.core.utils.misc.Time;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.replanning.OrderChanger;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;
import receiver.replanning.ServiceTimeMutator;

/**
 * This class implements a receiver reorder strategy that changes the delivery unloading time of its orders.
 * 
 * @author wlbean
 *
 */
public class ServiceTimeReceiverOrderStrategyManagerImpl implements ReceiverOrderStrategyManagerFactory{
	
	public ServiceTimeReceiverOrderStrategyManagerImpl(){		
	}

	@Override
	public GenericStrategyManager<ReceiverPlan, Receiver> createReceiverStrategyManager() {
		final GenericStrategyManager<ReceiverPlan, Receiver> stratMan = new GenericStrategyManager<>();
		stratMan.setMaxPlansPerAgent(4);
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanChanger<ReceiverPlan, Receiver>(1.));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 1.0);

		}
		
		/*
		 * Increase service duration with specified duration (mutationTime) until specified maximum service time (mutationRange) is reached. 
		 */
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new ServiceTimeMutator(Time.parseTime("01:00:00"), Time.parseTime("04:00:00"), true));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.5);
		}
		
		/* 
		 * Decreases service duration with specified duration (mutationTime) until specified minimum service time (mutationRange) is reached. 
		 */
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new ServiceTimeMutator(Time.parseTime("01:00:00"), Time.parseTime("01:00:00"), false));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.1);
		}
		
		
		return stratMan;
	}

}
