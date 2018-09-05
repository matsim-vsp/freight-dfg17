/**
 * 
 */
package receiver.replanning;

import javax.inject.Inject;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.BestPlanSelector;
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
	@Inject Scenario sc;
	
	public ServiceTimeReceiverOrderStrategyManagerImpl(){		
	}

	@Override
	public GenericStrategyManager<ReceiverPlan, Receiver> createReceiverStrategyManager() {
		final GenericStrategyManager<ReceiverPlan, Receiver> stratMan = new GenericStrategyManager<>();
		stratMan.setMaxPlansPerAgent(5);
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanChanger<ReceiverPlan, Receiver>(1.0));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.5);
			stratMan.addChangeRequest((int) (sc.getConfig().controler().getLastIteration()*0.9), strategy, null, 0.0);

		}
		
		/*
		 * Increase service duration with specified duration (mutationTime) until specified maximum service time (mutationRange) is reached. 
		 */
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> increaseStrategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			increaseStrategy.addStrategyModule(new ServiceTimeMutator(Time.parseTime("01:00:00"), Time.parseTime("04:00:00"), true));
			increaseStrategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(increaseStrategy, null, 0.15);
			stratMan.addChangeRequest((int) (sc.getConfig().controler().getLastIteration()*0.9), increaseStrategy, null, 0.0);
		}
		
		/* 
		 * Decreases service duration with specified duration (mutationTime) until specified minimum service time (mutationRange) is reached. 
		 */
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> decreaseStrategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			decreaseStrategy.addStrategyModule(new ServiceTimeMutator(Time.parseTime("01:00:00"), Time.parseTime("01:00:00"), false));
			decreaseStrategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(decreaseStrategy, null, 0.15);
			stratMan.addChangeRequest((int) (sc.getConfig().controler().getLastIteration()*0.9), decreaseStrategy, null, 0.0);
		}
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new BestPlanSelector<ReceiverPlan,Receiver>());
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.2);
		}
		
		
		return stratMan;
	}

}
