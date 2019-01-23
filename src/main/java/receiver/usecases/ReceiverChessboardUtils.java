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
import org.jfree.util.Log;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
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

import receiver.*;
import receiver.collaboration.Coalition;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;
import receiver.replanning.ReplanningUtils;

/**
 *
 * @author jwjoubert
 */
public class ReceiverChessboardUtils {
	final private static Logger LOG = Logger.getLogger(ReceiverChessboardUtils.class);


	public static void setupCarriers(Controler controler) {
//		final Carriers carriers = new Carriers();							
		final Carriers carriers = ReceiverUtils.getCarriers( controler.getScenario() );							
//		new CarrierPlanXmlReaderV2(carriers).readFile(controler.getScenario().getConfig().controler().getOutputDirectory() + "carriers.xml");	
		CarrierVehicleTypes types = new CarrierVehicleTypes();
		new CarrierVehicleTypeReader(types).readFile(controler.getScenario().getConfig().controler().getOutputDirectory()  + "carrierVehicleTypes.xml");
		new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(types);

		/* FIXME We added this null check because, essentially, the use of 
		 * coalitions should be optional. We must eventually find a way to be
		 * able to configure this in a more elegant way. */
		Coalition coalition = ReceiverUtils.getCoalition( controler.getScenario() );
		if(coalition != null) {
			for (Carrier carrier : ReceiverUtils.getCarriers( controler.getScenario() ).getCarriers().values()){
				if (!coalition.getCarrierCoalitionMembers().contains(carrier)){
					coalition.addCarrierCoalitionMember(carrier);
				}
			}
		}

		/* Create a new instance of a carrier scoring function factory. */
		final CarrierScoringFunctionFactory cScorFuncFac = new MyCarrierScoringFunctionFactoryImpl(controler.getScenario().getNetwork());

		/* Create a new instance of a carrier plan strategy manager factory. */
		final CarrierPlanStrategyManagerFactory cStratManFac = new MyCarrierPlanStrategyManagerFactoryImpl(types, controler.getScenario().getNetwork(), controler);

		CarrierModule carrierControler = new CarrierModule(carriers, cStratManFac, cScorFuncFac);
		carrierControler.setPhysicallyEnforceTimeWindowBeginnings(true);
		controler.addOverridingModule(carrierControler);

//		ReceiverUtils.setCarriers( carriers, controler.getScenario() );
	}



	public static void setupReceivers(Controler controler) {

//		String outputfolder = controler.getScenario().getConfig().controler().getOutputDirectory();
//		outputfolder += outputfolder.endsWith("/") ? "" : "/";
//		Receivers finalReceivers = new Receivers();
		Receivers finalReceivers = ReceiverUtils.getReceivers( controler.getScenario() );
//		new ReceiversReader(finalReceivers).readFile(outputfolder + "receivers.xml");
//		finalReceivers = ReceiverUtils.getReceivers( controler.getScenario() );

		/* 
		 * Adds receivers to freight scenario.
		 */
		finalReceivers.linkReceiverOrdersToCarriers( ReceiverUtils.getCarriers( controler.getScenario() ) );
//		ReceiverUtils.setReceivers( finalReceivers, controler.getScenario() );

		/* FIXME We added this null check because, essentially, the use of 
		 * coalitions should be optional. We must eventually find a way to be
		 * able to configure this in a more elegant way. */
		Coalition coalition = ReceiverUtils.getCoalition( controler.getScenario() );
		//		if(coalition != null) {
		setCoalitionFromReceiverAttributes( controler, coalition );
		LOG.info("Current number of receiver coalition members: " + coalition.getReceiverCoalitionMembers().size());
		LOG.info("Current number of carrier coalition members: " + coalition.getCarrierCoalitionMembers().size());
		LOG.info("Total number of receiver agents: " + Integer.toString( ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().size()));
		//		}


		/*
		 * Create a new instance of a receiver scoring function factory.
		 */
		final ReceiverScoringFunctionFactory rScorFuncFac = new ProportionalReceiverScoringFunctionFactoryImpl();

		/*
		 * Create a new instance of a receiver plan strategy manager factory..
		 */
		//int selector = MatsimRandom.getLocalInstance().nextInt(3);
		ReceiverOrderStrategyManagerFactory rStratManFac = null ;
		int selector = 1;
		switch (selector) {
			case 0:
				rStratManFac = ReplanningUtils.createTimeWindowReceiverOrderStrategyManagerImpl();
				break ;
			case 1:
				rStratManFac = ReplanningUtils.createServiceTimeReceiverOrderStrategyManagerImpl();
				break ;
			case 2:
				rStratManFac = ReplanningUtils.createNumDelReceiverOrderStrategyManagerImpl();
				break;
			default:
				Log.warn("No order strategy manager selected." );
		}
		ReceiverModule receiverControler = new ReceiverModule(finalReceivers, rScorFuncFac, rStratManFac, controler.getScenario());
		controler.addOverridingModule(receiverControler);
	}

	public static void setCoalitionFromReceiverAttributes( Controler controler, Coalition coalition ){
		for ( Receiver receiver : ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().values()){
			if(receiver.getAttributes().getAttribute( ReceiverAttributes.collaborationStatus.name() )!=null){
				if ( (boolean) receiver.getAttributes().getAttribute( ReceiverAttributes.collaborationStatus.name() ) ){
					if (!coalition.getReceiverCoalitionMembers().contains(receiver)){
						coalition.addReceiverCoalitionMember(receiver);
					}
				}
			}
		}
	}

	public static class MyCarrierPlanStrategyManagerFactoryImpl implements CarrierPlanStrategyManagerFactory {

		/*
		 * Adapted from RunChessboard.java by sschroeder and gliedtke.
		 */
		private Network network;
		private MatsimServices controler;
		private CarrierVehicleTypes types;

		MyCarrierPlanStrategyManagerFactoryImpl( final CarrierVehicleTypes types, final Network network, final MatsimServices controler ) {
			this.types = types;
			this.network = network;
			this.controler= controler;
		}

		@Override
		public GenericStrategyManager<CarrierPlan, Carrier> createStrategyManager() {
			return getCarrierPlanCarrierGenericStrategyManager( types, controler, network );
		}

		static GenericStrategyManager<CarrierPlan, Carrier> getCarrierPlanCarrierGenericStrategyManager( CarrierVehicleTypes types, MatsimServices controler,
																		 Network network ){
			TravelDisutility travelDis = TravelDisutilities.createBaseDisutility( types, controler.getLinkTravelTimes() );
			final LeastCostPathCalculator router = controler.getLeastCostPathCalculatorFactory().createPathCalculator(
				  network,	travelDis, controler.getLinkTravelTimes() );

			final GenericStrategyManager<CarrierPlan, Carrier> strategyManager = new GenericStrategyManager<>();
			strategyManager.setMaxPlansPerAgent(5);
			{
				GenericPlanStrategyImpl<CarrierPlan, Carrier> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanChanger<CarrierPlan, Carrier>(1.));
				strategyManager.addStrategy(strategy, null, 1.0);
			}

			{
				GenericPlanStrategyImpl<CarrierPlan, Carrier> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<CarrierPlan, Carrier>());
				strategy.addStrategyModule(new TimeAllocationMutator() );
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
			source = new FileInputStream( sourceFile ).getChannel();
			destination = new FileOutputStream( destFile ).getChannel();
			destination.transferFrom( source, 0, source.size() );
		}
		finally {
			if( source != null) {
				source.close();
			}
			if( destination != null) {
				destination.close();
			}
		}
	}


	/**
	 * Cleans a given file. If the file is a directory, it first cleans all
	 * its contained files (or folders).
	 * @param folder
	 */
	public static void delete(File folder){
		if(folder.isDirectory()){
			File[] contents = folder.listFiles();
			for(File file : contents){
				delete(file);
			}
			folder.delete();
		} else{
			folder.delete();
		}
	}


}
