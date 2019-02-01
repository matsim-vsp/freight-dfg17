package receiver.usecases.chessboard;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.Receivers;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BaseRunReceiverIT{
	private static final Logger log = Logger.getLogger( BaseRunReceiverIT.class ) ;

	@Test
	public void test(){
		int runId = 20	 ;
		BaseRunReceiver runReceiver = new BaseRunReceiver();
		// ---
		Scenario sc = runReceiver.prepareScenario(runId ) ;
		sc.getConfig().controler().setLastIteration( runId );
		ReceiverUtils.setReplanInterval( 1, sc );

//		Receivers receivers = ReceiverUtils.getReceivers( sc );
//		int cnt = 0 ;
//		for( Receiver receiver : receivers.getReceivers().values() ){
//		Iterator<Map.Entry<Id<Receiver>, Receiver>> it;
//		for ( it = receivers.getReceivers().entrySet().iterator() ; it.hasNext() ; ) {
//			Receiver receiver = it.next().getValue();;
//			cnt++ ;
//			if ( cnt > 1 ) {
//				it.remove();
//			}
//			else{
//				for( ReceiverPlan plan : receiver.getPlans() ){
//					for( ReceiverOrder rcvOrder : plan.getReceiverOrders() ){
//						for( Order order : rcvOrder.getReceiverProductOrders() ){
//							order.setServiceDuration( 2. * 3600. );
//						}
//					}
//				}
//			}
//		}

		// ---
		List<AbstractModule> abstractModules = new ArrayList<>() ;
		abstractModules.add( new AbstractModule(){
			@Override
			public void install(){
				this.addControlerListenerBinding().toInstance( new IterationEndsListener(){
					@Inject Scenario scenario ;
					@Override public void notifyIterationEnds( IterationEndsEvent event ){
						System.out.flush();
						for( Receiver receiver : ReceiverUtils.getReceivers( scenario ).getReceivers().values() ){
//							ReceiverPlan plan = receiver.getSelectedPlan();;
							for( ReceiverPlan plan : receiver.getPlans() ){
//								log.warn( plan.toString() );
								StringBuilder strb = new StringBuilder(  ) ;
								strb.append( "receiverId=" ).append( plan.getReceiver().getId() ) ;
								if ( plan.isSelected() ) {
									strb.append("; SELECTED") ;
								} else {
									strb.append("; not selected") ;
								}
								strb.append("; score=").append( plan.getScore() ) ;
								strb.append("; orders=") ;
								for( ReceiverOrder receiverOrder : plan.getReceiverOrders() ){
									strb.append( receiverOrder.toString() ) ;
								}
								log.warn( strb.toString() ) ;
							}
						}
						log.warn("") ;
						for( Carrier carrier : ReceiverUtils.getCarriers( scenario ).getCarriers().values() ){
							for( CarrierPlan plan : carrier.getPlans() ){
								StringBuilder strb = new StringBuilder();
								strb.append( "carrierId=" ).append( plan.getCarrier().getId() ) ;
								strb.append( "; score=" ).append( plan.getScore() ) ;
								strb.append("; tours=") ;
								for( ScheduledTour tour : plan.getScheduledTours() ){
									strb.append( tour ) ;
								}
								log.warn( strb.toString() ) ;
							}
						}
						System.err.flush();
					}
				} );
				this.addControlerListenerBinding().toInstance( new ShutdownListener(){
					@Inject Scenario scenario ;
					@Override
					public void notifyShutdown( ShutdownEvent event ){
						for( Receiver receiver : ReceiverUtils.getReceivers( scenario ).getReceivers().values() ){
							for( ReceiverOrder receiverOrder : receiver.getSelectedPlan().getReceiverOrders() ){
								for( Order order : receiverOrder.getReceiverProductOrders() ){
									Assert.assertEquals( 3600., order.getServiceDuration(), 1. ) ;
								}
							}
						}
						for( Carrier carrier : ReceiverUtils.getCarriers( scenario ).getCarriers().values() ){
							Assert.assertEquals( -8692.92, carrier.getSelectedPlan().getScore() , 1. ) ;
						}

					}
				} );
			}
		} ) ;
		// ---
		runReceiver.prepareAndRunControler( runId, abstractModules );
	}
}
