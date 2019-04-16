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

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeLoader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeReader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.utils.io.IOUtils;
import receiver.Receiver;
import receiver.ReceiverModule;
import receiver.ReceiverUtils;
import receiver.product.Order;
import receiver.product.ReceiverOrder;
import receiver.replanning.ReceiverReplanningType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

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


	public void run(int run) {
		LOG.info("Starting run " + run);
		prepareScenario( run, ExperimentParameters.NUMBER_OF_RECEIVERS );
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

//		ReceiverModule receiverModule = new ReceiverModule( ReceiverReplanningType.serviceTime );
		ReceiverModule receiverModule = new ReceiverModule();
		receiverModule.setReplanningType(ExperimentParameters.REPLANNING_STRATEGY );
		
		controler.addOverridingModule(receiverModule);

		prepareFreightOutputDataAndStats(controler);

		controler.run();
	}

	Scenario prepareScenario( int run, int numberOfReceivers ){
		outputfolder = String.format("./output/base/serdur/run_%03d/", run);
		new File(outputfolder).mkdirs();
		sc = BaseReceiverChessboardScenario.createChessboardScenario(SEED_BASE*run, run, numberOfReceivers, true );
		//		replanInt = mfs.getReplanInterval();
		sc.getConfig().controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles );
		sc.getConfig().controler().setOutputDirectory(outputfolder);

//		/* Write headings */
//		BufferedWriter bw = IOUtils.getBufferedWriter(sc.getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv");
//		writeHeadings( bw );
//		// (later code appends to this file!)

//		final Carriers carriers = ReceiverUtils.getCarriers( sc );
//		CarrierVehicleTypes types = new CarrierVehicleTypes();
//		new CarrierVehicleTypeReader(types).readFile(sc.getConfig().controler().getOutputDirectory()  + "carrierVehicleTypes.xml");
//		new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(types);

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

//	static void writeHeadings( BufferedWriter bw ){
//		try {
//			bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
//				  "iteration",
//				  "receiver_id",
//				  "score",
//				  "timewindow_start",
//				  "timewindow_end",
//				  "order_id",
//				  "volume",
//				  "frequency",
//				  "serviceduration",
//				  "collaborate",
//				  "grandCoalitionMember" ) );
//			bw.newLine();
//		} catch ( IOException e) {
//			e.printStackTrace();
//			throw new RuntimeException("Cannot write initial headings");
//		} finally{
//			try {
//				bw.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//				throw new RuntimeException("Cannot close receiver stats file");
//			}
//		}
//	}


	static void setupCarrierReplanning( MatsimServices controler ) {
//		controler.addControlerListener(new ReceiverResponseCarrierReplanning() );
	}


	/**
	 * FIXME This can be removed, like ReceiverScoreStats as soon as the Scenario can be injected into the ScoreStats class.
	 * @param controler
	 */
	@Deprecated
	static void prepareFreightOutputDataAndStats( MatsimServices controler) {
		CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", true);
		controler.addControlerListener(scoreStats);
	}

//	static void recordReceiverStats( IterationEndsEvent event, int numberOfReceivers, MatsimServices controler, int run ){
//		for(int i = 1; i < numberOfReceivers+1; i++) {
//			Receiver receiver = ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().get( Id.create(Integer.toString(i ), Receiver.class ) );
//			for ( ReceiverOrder rorder :  receiver.getSelectedPlan().getReceiverOrders()){
//				for ( Order order : rorder.getReceiverProductOrders()){
//					String score = receiver.getSelectedPlan().getScore().toString();
//					float start = (float) receiver.getSelectedPlan().getTimeWindows().get(0).getStart();
//					float end = (float) receiver.getSelectedPlan().getTimeWindows().get(0).getEnd();
//					float size = (float) (order.getDailyOrderQuantity()*order.getProduct().getProductType().getRequiredCapacity());
//					float freq = (float) order.getNumberOfWeeklyDeliveries();
//					float dur =  (float) order.getServiceDuration();
//					boolean status = (boolean) receiver.getAttributes().getAttribute( ReceiverUtils.ATTR_COLLABORATION_STATUS );
//					boolean member = (boolean) receiver.getAttributes().getAttribute( ReceiverUtils.ATTR_GRANDCOALITION_MEMBER );
//
//					BufferedWriter bw1 = IOUtils.getAppendingBufferedWriter(controler.getScenario().getConfig().controler().getOutputDirectory() + "/receiver_stats.csv");
//					try {
//						bw1.write(String.format("%d,%s,%s,%f,%f,%s,%f,%f,%f,%b,%b",
//							  event.getIteration(),
//							  receiver.getId(),
//							  score,
//							  start,
//							  end,
//							  order.getId(),
//							  size,
//							  freq,
//							  dur,
//							  status,
//							  member));
//						bw1.newLine();
//
//					} catch ( IOException e) {
//						e.printStackTrace();
//						throw new RuntimeException("Cannot write receiver stats");
//
//					} finally{
//						try {
//							bw1.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//							throw new RuntimeException("Cannot close receiver stats file");
//						}
//					}
//				}
//			}
//		}
//	}
}
