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
package receiver.usecases.chessboard;

import java.io.*;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.controler.CarrierModule;
import org.matsim.contrib.freight.replanning.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.scoring.CarrierScoringFunctionFactory;
import org.matsim.core.controler.Controler;

import receiver.ReceiverModule;
import receiver.ReceiverScoringFunctionFactory;
import receiver.ReceiverUtils;
import receiver.Receivers;
import receiver.collaboration.Coalition;
import receiver.replanning.*;
import receiver.usecases.UsecasesCarrierScoringFunctionFactory;
import receiver.usecases.UsecasesCarrierStrategyManagerFactory;
import receiver.usecases.UsecasesReceiverScoringFunctionFactory;

/**
 *
 * @author jwjoubert
 */
public class ReceiverChessboardUtils {
	final private static Logger LOG = Logger.getLogger(ReceiverChessboardUtils.class);

	public static void setupCarriers(Controler controler) {

		Carriers carriers = ReceiverUtils.getCarriers( controler.getScenario() );;

		BaseRunReceiver.setupCarrierReplanning(controler );

		/* Create a new instance of a carrier scoring function factory. */
		final CarrierScoringFunctionFactory cScorFuncFac = new UsecasesCarrierScoringFunctionFactory( controler.getScenario().getNetwork() );

		/* Create a new instance of a carrier plan strategy manager factory. */
		final CarrierPlanStrategyManagerFactory cStratManFac = new UsecasesCarrierStrategyManagerFactory( CarrierVehicleTypes.getVehicleTypes( carriers ),
			  controler.getScenario().getNetwork(), controler);

		CarrierModule carrierControler = new CarrierModule(carriers, cStratManFac, cScorFuncFac);
		carrierControler.setPhysicallyEnforceTimeWindowBeginnings(true);
		controler.addOverridingModule(carrierControler);

	}



	static void setupReceivers( Controler controler ) {
		Receivers receivers = ReceiverUtils.getReceivers( controler.getScenario() );
		final ReceiverScoringFunctionFactory rScorFuncFac = new UsecasesReceiverScoringFunctionFactory();

		ReceiverOrderStrategyManagerFactory rStratManFac = null ;

		/* FIXME This must be configurable from the ConfigGroup OUTSIDE the usecases */
		int selector = 1;
		switch (selector) {
			case 0:
				rStratManFac = new TimeWindowReceiverOrderStrategyManagerImpl();
				break ;
			case 1:
				rStratManFac = new ServiceTimeReceiverOrderStrategyManagerImpl();
				break ;
			case 2:
				rStratManFac = new NumDelReceiverOrderStrategyManagerImpl();
				break;
			default:
				Log.warn("No order strategy manager selected." );
		}

		ReceiverModule receiverControler = new ReceiverModule(receivers, rScorFuncFac, rStratManFac, controler.getScenario());

		controler.addOverridingModule(receiverControler);
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
	static void copyFile( File sourceFile, File destFile )  {
		if(!destFile.exists()) {
			try{
				destFile.createNewFile();
			} catch( IOException e ){
				e.printStackTrace();
			}
		}

		try( FileChannel source = new FileInputStream( sourceFile ).getChannel() ;
		     FileChannel destination = new FileOutputStream( destFile ).getChannel() ){
			destination.transferFrom( source, 0, source.size() );
		} catch( IOException e ){
			e.printStackTrace();
		}
	}


	/**
	 * Cleans a given file. If the file is a directory, it first cleans all
	 * its contained files (or folders).
	 * @param folder
	 */
	static void delete( File folder ){
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
