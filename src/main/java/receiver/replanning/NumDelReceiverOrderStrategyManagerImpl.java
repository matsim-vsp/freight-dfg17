/**
 * 
 */
package receiver.replanning;

import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.ExpBetaPlanChanger;
import org.matsim.core.replanning.selectors.KeepSelected;
import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.replanning.OrderChanger;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;

/**
 * This class implements a receiver reorder strategy that changes the number of weekly deliveries preferred by receivers.
 * 
 * @author wlbean
 *
 */
public class NumDelReceiverOrderStrategyManagerImpl implements ReceiverOrderStrategyManagerFactory{
	
	public NumDelReceiverOrderStrategyManagerImpl(){		
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
		 * Increases the number of weekly deliveries with 1 at a time (and thereby decreasing order quantity).
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new OrderSizeMutator(true));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.1);		
		}
		
		/*
		 * Decreases the number of weekly deliveries with 1 at time (and thereby increase order quantity).
		 */
		
		{
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			strategy.addStrategyModule(new OrderSizeMutator(false));
			strategy.addStrategyModule(new OrderChanger());
			stratMan.addStrategy(strategy, null, 0.1);		
		}
		
		
		return stratMan;
	}

}
