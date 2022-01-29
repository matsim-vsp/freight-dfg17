/**
 * 
 */
package receiver.usecases.capetown;

import javax.inject.Inject;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.ExpBetaPlanChanger;
import org.matsim.core.utils.misc.Time;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.replanning.CollaborationStatusChanger;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;

/**
 * This class implements a receiver reorder strategy that changes the delivery time window of its orders.
 * 
 * @author wlbean
 *
 */
public class CapeTownReceiverOrderStrategyManagerImpl implements ReceiverOrderStrategyManagerFactory{
	@Inject Scenario sc;
	
	public CapeTownReceiverOrderStrategyManagerImpl(){
	}

	@Override
	public GenericStrategyManager<ReceiverPlan, Receiver> createReceiverStrategyManager() {
		final GenericStrategyManager<ReceiverPlan, Receiver> stratMan = new GenericStrategyManager<>();
		stratMan.setMaxPlansPerAgent(5);
		
		{
//			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new BestPlanSelector<ReceiverPlan, Receiver>());
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>( new ExpBetaPlanChanger<>( 1.) );
			strategy.addStrategyModule(new CollaborationStatusChanger());
			stratMan.addStrategy(strategy, null, 0.7);
//			stratMan.addChangeRequest((int) (sc.getConfig().controler().getLastIteration()*0.9), strategy, null, 0.0);
		}
		
		/*
		 * Increases or decreases the time window start or time window end times.
		 */
		
		{
//			GenericPlanStrategyImpl<ReceiverPlan, Receiver> timeStrategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> timeStrategy = new GenericPlanStrategyImpl<>( new ExpBetaPlanChanger<>( 1.) );
			timeStrategy.addStrategyModule(new CapeTownTimeWindowMutator(Time.parseTime("01:00:00")));
			stratMan.addStrategy(timeStrategy, null, 0.3);
			stratMan.addChangeRequest((int) (sc.getConfig().controler().getLastIteration()*0.9), timeStrategy, null, 0.0);
		}		
		
		
		/* Replanning for grand coalition receivers.*/
		
		{			
//			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
			GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>( new ExpBetaPlanChanger<>( 1.) );
			strategy.addStrategyModule(new CapeTownCollaborationStatusMutator());
			stratMan.addStrategy(strategy, null, 0.2);
			stratMan.addChangeRequest((int) Math.round((sc.getConfig().controler().getLastIteration())*0.9), strategy, null, 0.0);			
		}

		
		return stratMan;
	}

}
