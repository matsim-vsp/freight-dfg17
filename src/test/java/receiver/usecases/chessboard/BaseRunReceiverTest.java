package receiver.usecases.chessboard;

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

public class BaseRunReceiverTest{

	@Test
	public void test(){
		int runId = 2 ;
		BaseRunReceiver runReceiver = new BaseRunReceiver();
		Scenario sc = runReceiver.prepareScenario(runId ) ;
		sc.getConfig().controler().setLastIteration( runId );
		ReceiverUtils.setReplanInterval( 1, sc );
		List<AbstractModule> abstractModules = new ArrayList<>() ;
		abstractModules.add( new AbstractModule(){
			@Override public void install(){
				this.addControlerListenerBinding().toInstance( new IterationEndsListener(){
					@Inject Scenario scenario ;
					@Override public void notifyIterationEnds( IterationEndsEvent event ){
						Receivers receivers = ReceiverUtils.getReceivers( scenario );
						for( Receiver receiver : receivers.getReceivers().values() ){
							for( ReceiverPlan plan : receiver.getPlans() ){
//								if ( plan.isSelected() ){
									System.err.println( plan.toString() );
//								}
							}
						}
					}
				} );
			}
		} ) ;
		runReceiver.prepareAndRunControler( runId, abstractModules );
	}
}
