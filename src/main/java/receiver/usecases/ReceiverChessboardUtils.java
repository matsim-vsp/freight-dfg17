/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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
package receiver.usecases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlReaderV2;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeLoader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeReader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.controler.CarrierModule;
import org.matsim.contrib.freight.replanning.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.replanning.modules.ReRouteVehicles;
import org.matsim.contrib.freight.replanning.modules.TimeAllocationMutator;
import org.matsim.contrib.freight.scoring.CarrierScoringFunctionFactory;
import org.matsim.contrib.freight.usecases.chessboard.TravelDisutilities;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.ExpBetaPlanChanger;
import org.matsim.core.replanning.selectors.KeepSelected;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;

import receiver.FreightScenario;
import receiver.MutableFreightScenario;
import receiver.Receiver;
import receiver.ReceiverModule;
import receiver.Receivers;
import receiver.collaboration.Coalition;
import receiver.io.ReceiversReader;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;
import receiver.replanning.TimeWindowReceiverOrderStrategyManagerImpl;
import receiver.scoring.ReceiverScoringFunctionFactory;

/**
 *
 * @author jwjoubert
 */
public class ReceiverChessboardUtils {
	final private static Logger LOG = Logger.getLogger(ReceiverChessboardUtils.class);

	
	public static void setupCarriers(Controler controler, MutableFreightScenario fs) {
		final Carriers carriers = new Carriers();							
		new CarrierPlanXmlReaderV2(carriers).readFile(fs.getScenario().getConfig().controler().getOutputDirectory() + "carriers.xml");	
		CarrierVehicleTypes types = new CarrierVehicleTypes();
		new CarrierVehicleTypeReader(types).readFile(fs.getScenario().getConfig().controler().getOutputDirectory()  + "carrierVehicleTypes.xml");
		new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(types);

		/* FIXME We added this null check because, essentially, the use of 
		 * coalitions should be optional. We must eventually find a way to be
		 * able to configure this in a more elegant way. */
		Coalition coalition = fs.getCoalition();
		if(coalition != null) {
			for (Carrier carrier : fs.getCarriers().getCarriers().values()){
				if (!coalition.getCarrierCoalitionMembers().contains(carrier)){
					coalition.addCarrierCoalitionMember(carrier);
				}
			}
		}

		/* Create a new instance of a carrier scoring function factory. */
		final CarrierScoringFunctionFactory cScorFuncFac = new MyCarrierScoringFunctionFactoryImpl(fs.getScenario().getNetwork());

		/* Create a new instance of a carrier plan strategy manager factory. */
		final CarrierPlanStrategyManagerFactory cStratManFac = new MyCarrierPlanStrategyManagerFactoryImpl(types, fs.getScenario().getNetwork(), controler);

		CarrierModule carrierControler = new CarrierModule(carriers, cStratManFac, cScorFuncFac);
		carrierControler.setPhysicallyEnforceTimeWindowBeginnings(true);
		controler.addOverridingModule(carrierControler);
		
		fs.setCarriers(carriers);
	}
	
	
	
	public static void setupReceivers(Controler controler, MutableFreightScenario fsc) {

		String outputfolder = fsc.getScenario().getConfig().controler().getOutputDirectory();
		outputfolder += outputfolder.endsWith("/") ? "" : "/";
		Receivers finalReceivers = new Receivers();
		new ReceiversReader(finalReceivers).readFile(outputfolder + "receivers.xml");
		finalReceivers = fsc.getReceivers();

		/* 
		 * Adds receivers to freight scenario.
		 */		
		finalReceivers.linkReceiverOrdersToCarriers(fsc.getCarriers());
		fsc.setReceivers(finalReceivers);

		/* FIXME We added this null check because, essentially, the use of 
		 * coalitions should be optional. We must eventually find a way to be
		 * able to configure this in a more elegant way. */
		Coalition coalition = fsc.getCoalition();
//		if(coalition != null) {
			for (Receiver receiver : fsc.getReceivers().getReceivers().values()){
				if (receiver.getCollaborationStatus() == true){
					if (!coalition.getReceiverCoalitionMembers().contains(receiver)){
						coalition.addReceiverCoalitionMember(receiver);
					}
				}
			}
			LOG.info("Current number of receiver coalition members: " + coalition.getReceiverCoalitionMembers().size());
			LOG.info("Current number of carrier coalition members: " + coalition.getCarrierCoalitionMembers().size());
//		}


		/*
		 * Create a new instance of a receiver scoring function factory.
		 */
		final ReceiverScoringFunctionFactory rScorFuncFac = new ProportionalReceiverScoringFunctionFactoryImpl();

		/*
		 * Create a new instance of a receiver plan strategy manager factory that allows grand coalition members 
		 * to join of leave a sub-coalition after 200 iterations (when all the sub-coalition scores were calculated 
		 * for the eight receivers in this scenario).
		 */
		//int selector = MatsimRandom.getLocalInstance().nextInt(3);
//		int selector = 0;
//		switch (selector) {
//			case 0: {
				final ReceiverOrderStrategyManagerFactory rStratManFac = new TimeWindowReceiverOrderStrategyManagerImpl();
	
//				/* change the receiver plan strategy manager after all coalition scores were calculated. */
//				if (controler.getIterationNumber() == (fsc.getReceivers().getReceivers().size() + 2)){
//					GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
//					strategy.addStrategyModule(new CollaborationStatusMutator());
//					rStratManFac.createReceiverStrategyManager().addStrategy(strategy, null, 0.2);
//				}

				ReceiverModule receiverControler = new ReceiverModule(finalReceivers, rScorFuncFac, rStratManFac, fsc);
				controler.addOverridingModule(receiverControler);
//			}
//			case 1: {
//				final ReceiverOrderStrategyManagerFactory rStratManFac = new ServiceTimeReceiverOrderStrategyManagerImpl();
//	
//				/* change the receiver plan strategy manager after all coalition scores were calculated. */
//				if (controler.getIterationNumber() == (fsc.getReceivers().getReceivers().size() + 2)){
//					GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
//					strategy.addStrategyModule(new CollaborationStatusMutator());
//					rStratManFac.createReceiverStrategyManager().addStrategy(strategy, null, 0.2);
//				}
//	
//				ReceiverModule receiverControler = new ReceiverModule(finalReceivers, rScorFuncFac, rStratManFac, fsc);
//				controler.addOverridingModule(receiverControler);
//			}
//			case 2: {
//				final ReceiverOrderStrategyManagerFactory rStratManFac = new NumDelReceiverOrderStrategyManagerImpl();
//	
//				/* change the receiver plan strategy manager after all coalition scores were calculated. */
//				if (controler.getIterationNumber() == (fsc.getReceivers().getReceivers().size() + 2)){
//					GenericPlanStrategyImpl<ReceiverPlan, Receiver> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<ReceiverPlan, Receiver>());
//					strategy.addStrategyModule(new CollaborationStatusMutator());
//					rStratManFac.createReceiverStrategyManager().addStrategy(strategy, null, 0.2);
////				}
//	
//				ReceiverModule receiverControler = new ReceiverModule(finalReceivers, rScorFuncFac, rStratManFac, fsc);
//				controler.addOverridingModule(receiverControler); 
//			}
//			default: { 
//				Log.warn("No order strategy manager selected.");		
//			}
//		}
	}
	
	
	
	
	
	private static class MyCarrierPlanStrategyManagerFactoryImpl implements CarrierPlanStrategyManagerFactory {

		/*
		 * Adapted from RunChessboard.java by sschroeder and gliedtke.
		 */
		private Network network;
		private MatsimServices controler;
		private CarrierVehicleTypes types;

		public MyCarrierPlanStrategyManagerFactoryImpl(final CarrierVehicleTypes types, final Network network, final MatsimServices controler) {
			this.types = types;
			this.network = network;
			this.controler= controler;
		}

		@Override
		public GenericStrategyManager<CarrierPlan, Carrier> createStrategyManager() {
			TravelDisutility travelDis = TravelDisutilities.createBaseDisutility(types, controler.getLinkTravelTimes());
			final LeastCostPathCalculator router = controler.getLeastCostPathCalculatorFactory().createPathCalculator(network,	travelDis, controler.getLinkTravelTimes());

			final GenericStrategyManager<CarrierPlan, Carrier> strategyManager = new GenericStrategyManager<>();
			strategyManager.setMaxPlansPerAgent(5);
			{
				GenericPlanStrategyImpl<CarrierPlan, Carrier> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanChanger<CarrierPlan, Carrier>(1.));
				strategyManager.addStrategy(strategy, null, 1.0);
			}

			{
				GenericPlanStrategyImpl<CarrierPlan, Carrier> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<CarrierPlan, Carrier>());
				strategy.addStrategyModule(new TimeAllocationMutator());
				ReRouteVehicles reRouteModule = new ReRouteVehicles(router, network, controler.getLinkTravelTimes(), 1.);
				strategy.addStrategyModule(reRouteModule);
				strategyManager.addStrategy(strategy, null, 0.5);
			}
			return strategyManager;
		}
	}
	
	
	/**
	 * Copies a file from one location to another.
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	/* The SuppressWarnings was added because Eclipse complains that file 
	 * streams are not closed... but they are. */
	@SuppressWarnings("resource")
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}


	
}
