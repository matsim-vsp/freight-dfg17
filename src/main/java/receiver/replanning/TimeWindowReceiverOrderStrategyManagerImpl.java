/**
 * 
 */
package receiver.replanning;

import javax.inject.Inject;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.BestPlanSelector;
import org.matsim.core.replanning.selectors.ExpBetaPlanSelector;
import org.matsim.core.replanning.selectors.KeepSelected;
import org.matsim.core.utils.misc.Time;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.replanning.OrderChanger;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;

/**
 * This class implements a receiver reorder strategy that changes the delivery time window of its orders.
 * 
 * @author wlbean
 *
 */
public class TimeWindowReceiverOrderStrategyManagerImpl implements ReceiverOrderStrategyManagerFactory{
	@Inject Scenario sc;
	
	public TimeWindowReceiverOrderStrategyManagerImpl(){		
	}

	@Override
	public GenericStrategyManager<ReceiverPlan, Receiver> createReceiverStrategyManager() {
		final GenericStrategyManager<ReceiverPlan, Receiver> stratMan = new GenericStrategyManager<>();
		stratMan.setMaxPlansPerAgent(5);
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanSelector<ReceiverPlan, Receiver>(1.0));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.5);
			stratMan.addChangeRequest((int) (sc.getConfig().controler().getLastIteration()*0.9), strategy, null, 0.0);

		}
		
		/*
		 * Increases or decreases the time window start or time window end times.
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> timeStrategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			timeStrategy.addStrategyModule(new TimeWindowMutator(Time.parseTime("02:00:00")));
			timeStrategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(timeStrategy, null, 0.3);	
			stratMan.addChangeRequest((int) (sc.getConfig().controler().getLastIteration()*0.9), timeStrategy, null, 0.0);
		}		
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new BestPlanSelector<ReceiverPlan,Receiver>());
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.2);

		}
		
		return stratMan;
	}

}