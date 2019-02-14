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
import org.matsim.contrib.freight.carrier.*;
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
import receiver.collaboration.Coalition;
import receiver.collaboration.CollaborationUtils;
import receiver.product.Order;
import receiver.product.ReceiverOrder;
import receiver.replanning.ReceiverReplanningType;
import receiver.replanning.ReceiverResponseCarrierReplanning;
import receiver.usecases.ReceiverScoreStats;

/**
 * Specific example for my (wlbean) thesis chapters 5 and 6.
 * @author jwjoubert, wlbean
 */

class BaseRunReceiver{
	final private static Logger LOG = Logger.getLogger( BaseRunReceiver.class );
	final private static long SEED_BASE = 20180816l;
	private String outputfolder;
	private Scenario sc;

	public static void main(String[] args) {
		int startRun = Integer.parseInt(args[0]);
		int endRun = Integer.parseInt(args[1]);
		for(int i = startRun; i < endRun; i++) {
			new BaseRunReceiver().run(i );
		}
	}


	public  void run(int run) {
		LOG.info("Starting run " + run);
		prepareScenario( run, 5 );
		prepareAndRunControler( run, null);
	}

	void prepareAndRunControler( int runId, Collection<AbstractModule> abstractModules ){
		Controler controler = new Controler(sc);
		if ( abstractModules!=null ){
			for( AbstractModule abstractModule : abstractModules ){
				controler.addOverridingModule( abstractModule );
			}
		}

		ReceiverChessboardUtils.setupCarriers(controler );

		ReceiverModule receiverModule = new ReceiverModule( ReceiverReplanningType.serviceTime );
		controler.addOverridingModule(receiverModule);

		prepareFreightOutputDataAndStats(controler, runId);

		controler.run();
	}

	Scenario prepareScenario( int run, int numberOfReceivers ){
		outputfolder = String.format("./output/base/tw/run_%03d/", run);
		new File(outputfolder).mkdirs();
		sc = BaseReceiverChessboardScenario.createChessboardScenario(SEED_BASE*run, run, numberOfReceivers, true );
		//		replanInt = mfs.getReplanInterval();
		sc.getConfig().controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles );


		/* Write headings */
		BufferedWriter bw = IOUtils.getBufferedWriter(sc.getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv");
		writeHeadings( bw );
		// (later code appends to this file!)

		final Carriers carriers = ReceiverUtils.getCarriers( sc );
		CarrierVehicleTypes types = new CarrierVehicleTypes();
		new CarrierVehicleTypeReader(types).readFile(sc.getConfig().controler().getOutputDirectory()  + "carrierVehicleTypes.xml");
		new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(types);

		/* FIXME We added this null check because, essentially, the use of
		 * coalitions should be optional. We must eventually find a way to be
		 * able to configure this in a more elegant way. */
//		Coalition coalition = ReceiverUtils.getCoalition( sc );
//		if(coalition != null) {
//			for (Carrier carrier : carriers.getCarriers().values()){
//				if (!coalition.getCarrierCoalitionMembers().contains(carrier)){
//					coalition.addCarrierCoalitionMember(carrier);
//				}
//			}
//		}


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
				  ReceiverUtils.ATTR_GRANDCOALITION_MEMBER ) );
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


	static void setupCarrierReplanning( MatsimServices controler ) {
//		controler.addControlerListener(new ReceiverResponseCarrierReplanning() );
	}


	private static void prepareFreightOutputDataAndStats( MatsimServices controler, int run) {

		/*
		 * Adapted from RunChessboard.java by sshroeder and gliedtke.
		 */
		final int statInterval = ReceiverUtils.getReplanInterval( controler.getScenario() );

		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
		ReceiverScoreStats rScoreStats = new ReceiverScoreStats();

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

	static void recordReceiverStats( IterationEndsEvent event, int numberOfReceivers, MatsimServices controler, int run ){
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
					boolean status = (boolean) receiver.getAttributes().getAttribute( ReceiverUtils.ATTR_COLLABORATION_STATUS );
					boolean member = (boolean) receiver.getAttributes().getAttribute( ReceiverUtils.ATTR_GRANDCOALITION_MEMBER );

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
