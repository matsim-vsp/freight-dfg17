package receiver.usecases.base;

import com.google.inject.Inject;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.Receivers;

import java.util.ArrayList;
import java.util.List;

public class RunReceiverTest{

	@Test
	public void run001(){
		int runId = 1 ;
		RunReceiver runReceiver = new RunReceiver();
		Scenario sc = runReceiver.prepareScenario(runId ) ;
		sc.getConfig().controler().setLastIteration( runId );
		List<AbstractModule> abstractModules = new ArrayList<>() ;
		abstractModules.add( new AbstractModule(){
			@Override
			public void install(){
				this.addControlerListenerBinding().toInstance( new IterationEndsListener(){
					@Inject Scenario scenario ;
					@Override public void notifyIterationEnds( IterationEndsEvent event ){
						System.err.println( "here10") ;
						Receivers receivers = ReceiverUtils.getReceivers( scenario );
						for( Receiver receiver : receivers.getReceivers().values() ){
							for( ReceiverPlan plan : receiver.getPlans() ){
								System.err.println( plan.toString() ) ;
							}
						}
						System.err.println( "here99") ;
					}
				} );
			}
		} ) ;
		runReceiver.prepareAndRunControler( runId, abstractModules );
	}
}
