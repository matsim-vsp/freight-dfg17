package receiver.usecases.base;

import com.google.inject.Inject;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.Receivers;

import java.util.ArrayList;
import java.util.List;

public class RunReceiverIT{

	@Test
	public void test(){
		int runId = 100 ;
		RunReceiver runReceiver = new RunReceiver();
		Scenario sc = runReceiver.prepareScenario(runId ) ;
		sc.getConfig().controler().setLastIteration( runId );
		ReceiverUtils.setReplanInterval( 1, sc );
		List<AbstractModule> abstractModules = new ArrayList<>() ;
		abstractModules.add( new AbstractModule(){
			@Override
			public void install(){
				this.addControlerListenerBinding().toInstance( new IterationEndsListener(){
					@Inject Scenario scenario ;
					@Override public void notifyIterationEnds( IterationEndsEvent event ){

						Receivers receivers = ReceiverUtils.getReceivers( scenario );
						for( Receiver receiver : receivers.getReceivers().values() ){
							for( ReceiverPlan plan : receiver.getPlans() ){
								System.err.println( plan.toString() );
							}
						}

						for( Carrier carrier : ReceiverUtils.getCarriers( scenario ).getCarriers().values() ){
							for( CarrierPlan plan : carrier.getPlans() ){
								System.err.println( plan.toString() ) ;
							}
						}

					}
				} );
			}
		} ) ;
		runReceiver.prepareAndRunControler( runId, abstractModules );
	}
}
