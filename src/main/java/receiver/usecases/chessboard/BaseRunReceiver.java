/* *********************************************************************** *
 * project: org.matsim.*
 * RunReceiver.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 *
 */
package receiver.usecases.chessboard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.graphhopper.jsprit.core.util.Solutions;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.carrier.CarrierShipment;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.utils.io.IOUtils;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;

import receiver.*;
import receiver.product.Order;
import receiver.product.ReceiverOrder;

/**
 * Specific example for my (wlbean) thesis chapters 5 and 6.
 * @author jwjoubert, wlbean
 */

public class BaseRunReceiver{
	final private static Logger LOG = Logger.getLogger( BaseRunReceiver.class );
	final private static long SEED_BASE = 20180816l;
	private String outputfolder;
	private Scenario sc;
	//	private static int replanInt;

	public static void main(String[] args) {
		int startRun = Integer.parseInt(args[0]);
		int endRun = Integer.parseInt(args[1]);
		for(int i = startRun; i < endRun; i++) {
			new BaseRunReceiver().run(i );
		}
	}


	public  void run(int run) {
		LOG.info("Starting run " + run);
		Scenario scenario = prepareScenario( run );
		//		scenario.getConfig().global().setRandomSeed( run );
		prepareAndRunControler( run, null);
	}

	void prepareAndRunControler( int runId, Collection<AbstractModule> abstractModules ){
		Controler controler = new Controler(sc);
		if ( abstractModules!=null ){
			for( AbstractModule abstractModule : abstractModules ){
				controler.addOverridingModule( abstractModule );
			}
		}

		setupCarrierReplanning(controler, outputfolder );

		ReceiverChessboardUtils.setupCarriers(controler );

		ReceiverChessboardUtils.setupReceivers(controler);
		// (this one actually sets the receiver strategies!!!!)

		prepareFreightOutputDataAndStats(controler, runId);

		controler.run();
	}

	Scenario prepareScenario( int run ){
		outputfolder = String.format("./output/base/tw/run_%03d/", run);
		new File(outputfolder).mkdirs();
		sc = BaseReceiverChessboardScenario.createChessboardScenario(SEED_BASE*run, run, true );
		//		replanInt = mfs.getReplanInterval();

		//		/* Write headings */
		//		BufferedWriter bw = IOUtils.getBufferedWriter(sc.getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv");
		//		writeHeadings( bw );
		// yy the above was nowhere used.  kai, jan'19

		sc.getConfig().controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles );
		return sc;
	}

	static void writeHeadings( BufferedWriter bw ){
		try {
			bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
				  "iteration",
				  "receiver_id",
				  "score",
				  "timewindow_start",
				  "timewindow_end",
				  "order_id",
				  "volume",
				  "frequency",
				  "serviceduration",
				  "collaborate",
				  ReceiverAttributes.grandCoalitionMember.name() ) );
			bw.newLine();
		} catch ( IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot write initial headings");
		} finally{
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot close receiver stats file");
			}
		}
	}


	private static void setupCarrierReplanning( MatsimServices controler, String outputFolder ) {
		controler.addControlerListener(new IterationStartsListener() {

			@Override
			public void notifyIterationStarts(IterationStartsEvent event) {

				// replan only in replanning iteration:
				if(event.getIteration() % ReceiverUtils.getReplanInterval( controler.getScenario() ) != 0) {
					return;
				}

				// Adds the receiver agents that are part of the current (sub)coalition.
				setCoalitionFromReceiverAttributes( controler );

				// clean out plans, services, shipments from carriers:
				Map<Id<Carrier>, Carrier> carriers = ReceiverUtils.getCarriers( controler.getScenario() ).getCarriers();
				for( Carrier carrier : carriers.values() ){
					/* Remove all existing carrier plans. */
					ArrayList<CarrierPlan> carrierPlans = new ArrayList<>( carrier.getPlans() );
					for( CarrierPlan plan : carrierPlans ){
						carrier.removePlan( plan );
					}
					// yyyy todo: replace above by carrier.clearPlans() (not yet in master).  kai, jan'19

					carrier.getShipments().clear();
					carrier.getServices().clear();
				}

				// re-fill the carriers from the receiver orders:
				Map<Id<Receiver>, Receiver> receivers = ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers();
				int nn = 0 ;
				for( Receiver receiver : receivers.values() ){
					ReceiverPlan receiverPlan = receiver.getSelectedPlan();
					for( ReceiverOrder receiverOrder : receiverPlan.getReceiverOrders() ){
						for( Order order : receiverOrder.getReceiverProductOrders() ){
							nn++ ;
							CarrierShipment.Builder builder = CarrierShipment.Builder.newInstance(
								  Id.create("Order" + receiverPlan.getReceiver().getId().toString() + Integer.toString(nn), CarrierShipment.class),
								  order.getProduct().getProductType().getOriginLinkId(),
								  order.getReceiver().getLinkId(),
								  (int) (Math.round(order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity())) );
							CarrierShipment newShipment = builder
													  .setDeliveryServiceTime( order.getServiceDuration() )
													  .setDeliveryTimeWindow( receiverPlan.getTimeWindows().get( 0 ) )
													  // TODO This only looks at the FIRST time window. This may need revision once we handle multiple
													  // time windows.
													  .build();
							if (newShipment.getSize() != 0) {
								receiverOrder.getCarrier().getShipments().add(newShipment );
							}
						}
					}
				}

				for( Carrier carrier : carriers.values() ){
					// for all carriers, re-run jsprit:

					VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, controler.getScenario().getNetwork());

					NetworkBasedTransportCosts netBasedCosts = NetworkBasedTransportCosts.Builder.newInstance(controler.getScenario().getNetwork(), carrier.getCarrierCapabilities().getVehicleTypes()).build();
					VehicleRoutingProblem vrp = vrpBuilder.setRoutingCost(netBasedCosts).build();

					//read and create a pre-configured algorithms to solve the vrp
					URL algoConfigFileName = IOUtils.newUrl( controler.getScenario().getConfig().getContext(), "initialPlanAlgorithm.xml" );
					VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, algoConfigFileName);

					//solve the problem
					Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

					//create a new carrierPlan from the best solution
					CarrierPlan newPlan = MatsimJspritFactory.createPlan(carrier, Solutions.bestOf( solutions ) );

					//route plan
					NetworkRouter.routePlan(newPlan, netBasedCosts);

					//assign this plan now to the carrier and make it the selected carrier plan
					carrier.setSelectedPlan(newPlan);

				}

				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(controler.getScenario().getConfig().controler().getOutputDirectory() + "carriers.xml");
				new ReceiversWriter( ReceiverUtils.getReceivers( controler.getScenario() ) ).write(controler.getScenario().getConfig().controler().getOutputDirectory() + "receivers.xml");

			}

		});
	}

	public static void setCoalitionFromReceiverAttributes( MatsimServices controler ){
		for ( Receiver receiver : ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().values()){
			if (receiver.getAttributes().getAttribute( ReceiverAttributes.collaborationStatus.name() ) != null){
				if ((boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.collaborationStatus.name()) == true){
					if (!ReceiverUtils.getCoalition( controler.getScenario() ).getReceiverCoalitionMembers().contains(receiver)){
						ReceiverUtils.getCoalition( controler.getScenario() ).addReceiverCoalitionMember(receiver);
					}
				} else {
					if ( ReceiverUtils.getCoalition( controler.getScenario() ).getReceiverCoalitionMembers().contains(receiver)){
						ReceiverUtils.getCoalition( controler.getScenario() ).removeReceiverCoalitionMember(receiver);
					}
				}
			}
		}
	}


	private static void prepareFreightOutputDataAndStats( MatsimServices controler, int run) {

		/*
		 * Adapted from RunChessboard.java by sshroeder and gliedtke.
		 */
		final int statInterval = ReceiverUtils.getReplanInterval( controler.getScenario() );

		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
		ReceiverScoreStats rScoreStats = new ReceiverScoreStats(controler.getScenario().getConfig().controler().getOutputDirectory() + "/receiver_scores", true);

		controler.addControlerListener(scoreStats);
		controler.addControlerListener(rScoreStats);
		controler.addControlerListener(new IterationEndsListener() {

			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				String dir = event.getServices().getControlerIO().getIterationPath(event.getIteration());

				if((event.getIteration() + 1) % (statInterval) != 0) return;

				//write plans

				new CarrierPlanXmlWriterV2( ReceiverUtils.getCarriers( controler.getScenario() ) ).write(dir + "/" + event.getIteration() + ".carrierPlans.xml");

				new ReceiversWriter( ReceiverUtils.getReceivers( controler.getScenario() ) ).write(dir + "/" + event.getIteration() + ".receivers.xml");

				/* Record receiver stats */
				int numberOfReceivers = ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().size();
				recordReceiverStats( event, numberOfReceivers, controler, run );

			}
		});

	}

	public static void recordReceiverStats( IterationEndsEvent event, int numberOfReceivers, MatsimServices controler, int run ){
		for(int i = 1; i < numberOfReceivers+1; i++) {
			Receiver receiver = ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().get( Id.create(Integer.toString(i ), Receiver.class ) );
			for ( ReceiverOrder rorder :  receiver.getSelectedPlan().getReceiverOrders()){
				for ( Order order : rorder.getReceiverProductOrders()){
					String score = receiver.getSelectedPlan().getScore().toString();
					float start = (float) receiver.getSelectedPlan().getTimeWindows().get(0).getStart();
					float end = (float) receiver.getSelectedPlan().getTimeWindows().get(0).getEnd();
					float size = (float) (order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity());
					float freq = (float) order.getNumberOfWeeklyDeliveries();
					float dur =  (float) order.getServiceDuration();
					boolean status = (boolean) receiver.getAttributes().getAttribute( ReceiverAttributes.collaborationStatus.name() );
					boolean member = (boolean) receiver.getAttributes().getAttribute(ReceiverAttributes.grandCoalitionMember.name());

					BufferedWriter bw1 = IOUtils.getAppendingBufferedWriter(controler.getScenario().getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv" );
					try {
						bw1.write(String.format("%d,%s,%s,%f,%f,%s,%f,%f,%f,%b,%b",
							  event.getIteration(),
							  receiver.getId(),
							  score,
							  start,
							  end,
							  order.getId(),
							  size,
							  freq,
							  dur,
							  status,
							  member));
						bw1.newLine();

					} catch ( IOException e) {
						e.printStackTrace();
						throw new RuntimeException("Cannot write receiver stats");

					} finally{
						try {
							bw1.close();
						} catch (IOException e) {
							e.printStackTrace();
							throw new RuntimeException("Cannot close receiver stats file");
						}
					}
				}
			}
		}
	}
}
