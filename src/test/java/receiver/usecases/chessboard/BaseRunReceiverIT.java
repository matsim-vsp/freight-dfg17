package receiver.usecases.chessboard;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import receiver.Receiver;
import receiver.ReceiverConfigGroup;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

import java.util.ArrayList;
import java.util.List;

public class BaseRunReceiverIT {
    private static final Logger log = Logger.getLogger(BaseRunReceiverIT.class);

    @Test
    public void test() {
        int runId = 20;
        BaseRunReceiver runReceiver = new BaseRunReceiver();
        // ---
        Scenario sc = runReceiver.prepareScenario(runId, 1);
        sc.getConfig().controler().setLastIteration(runId);
        ConfigUtils.addOrGetModule(sc.getConfig(), ReceiverConfigGroup.class).setReceiverReplanningInterval(1);
        ReceiverConfigGroup rcg = ConfigUtils.addOrGetModule(sc.getConfig(), ReceiverConfigGroup.class);

        // ---
        List<AbstractModule> abstractModules = new ArrayList<>();
        abstractModules.add(new AbstractModule() {
            @Override
            public void install() {
                this.addControlerListenerBinding().toInstance(new MyIterationEndsListener());
                this.addControlerListenerBinding().toInstance(new ShutdownListener() {
                    @Inject
                    Scenario scenario;

                    @Override
                    public void notifyShutdown(ShutdownEvent event) {
                        for (Receiver receiver : ReceiverUtils.getReceivers(scenario).getReceivers().values()) {
                            for (ReceiverOrder receiverOrder : receiver.getSelectedPlan().getReceiverOrders()) {
                                for (Order order : receiverOrder.getReceiverProductOrders()) {
                                    Assert.assertEquals(3600., order.getServiceDuration(), 1.);
                                }
                            }
                        }
                        for (Carrier carrier : ReceiverUtils.getCarriers(scenario).getCarriers().values()) {
                            Assert.assertEquals(-8692.92, carrier.getSelectedPlan().getScore(), 1.);
                        }
                    }
                });
            }
        });
        // ---
        runReceiver.prepareAndRunControler(runId, abstractModules);
    }

    @Test
    public void testZwo() {
        int runId = 10; // this should rather be something like 1000, but since it is not asserting anything at the end, there is no point in running it that
	    // long.  kai, sep'19
        BaseRunReceiver runReceiver = new BaseRunReceiver();
        // ---
        Scenario sc = runReceiver.prepareScenario(runId, 3);
        sc.getConfig().controler().setLastIteration(runId);
        ConfigUtils.addOrGetModule(sc.getConfig(), ReceiverConfigGroup.class).setReceiverReplanningInterval(1);

        // ---
        List<AbstractModule> abstractModules = new ArrayList<>();
        abstractModules.add(new AbstractModule() {
            @Override
            public void install() {
                this.addControlerListenerBinding().toInstance(new MyIterationEndsListener());
//				this.addControlerListenerBinding().toInstance( new ShutdownListener(){
//					@Inject Scenario scenario ;
//					@Override
//					public void notifyShutdown( ShutdownEvent event ){
//						for( Receiver receiver : ReceiverUtils.getReceivers( scenario ).getReceivers().values() ){
//							for( ReceiverOrder receiverOrder : receiver.getSelectedPlan().getReceiverOrders() ){
//								for( Order order : receiverOrder.getReceiverProductOrders() ){
//									Assert.assertEquals( 3600., order.getServiceDuration(), 1. ) ;
//								}
//							}
//						}
//						for( Carrier carrier : ReceiverUtils.getCarriers( scenario ).getCarriers().values() ){
//							Assert.assertEquals( -8692.92, carrier.getSelectedPlan().getScore() , 1. ) ;
//						}
//
//					}
//				} );
            }
        });
        // ---
        runReceiver.prepareAndRunControler(runId, abstractModules);
    }

    private static class MyIterationEndsListener implements IterationEndsListener {
        @Inject
        Scenario scenario;

        @Override
        public void notifyIterationEnds(IterationEndsEvent event) {
            System.out.flush();
            for (Receiver receiver : ReceiverUtils.getReceivers(scenario).getReceivers().values()) {
//							ReceiverPlan plan = receiver.getSelectedPlan();;
                for (ReceiverPlan plan : receiver.getPlans()) {
//								log.warn( plan.toString() );
                    StringBuilder strb = new StringBuilder();
                    strb.append("receiverId=").append(plan.getReceiver().getId());
                    if (plan.isSelected()) {
                        strb.append("; SELECTED");
                    } else {
                        strb.append("; not selected");
                    }
                    strb.append("; score=").append(plan.getScore());
                    strb.append("; orders=");
                    for (ReceiverOrder receiverOrder : plan.getReceiverOrders()) {
                        strb.append(receiverOrder.toString());
                    }
                    log.warn(strb.toString());
                }
            }
            log.warn("");
            for (Carrier carrier : ReceiverUtils.getCarriers(scenario).getCarriers().values()) {
                for (CarrierPlan plan : carrier.getPlans()) {
                    StringBuilder strb = new StringBuilder();
                    strb.append("carrierId=").append(plan.getCarrier().getId());
                    strb.append("; score=").append(plan.getScore());
                    strb.append("; tours=");
                    for (ScheduledTour tour : plan.getScheduledTours()) {
                        strb.append(tour);
                    }
                    log.warn(strb.toString());
                }
                // yyyyyy we are quite often getting two carriers here where there should only be one.
                // yyyyyy I have not yet really understood how the scoring works, in particular how and when it is passed on to the receivers.
            }
            System.err.flush();
        }
    }
}
