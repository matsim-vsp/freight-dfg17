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
package receiver.usecases.capetown;

import org.apache.log4j.Logger;
import org.matsim.core.controler.Controler;

import receiver.*;
import receiver.collaboration.Coalition;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;
import receiver.usecases.chessboard.ProportionalReceiverScoringFunctionFactoryImpl;
import receiver.usecases.chessboard.ReceiverChessboardUtils;

/**
 *
 * @author jwjoubert
 */
public class CapeTownReceiverUtils {
	final private static Logger LOG = Logger.getLogger(CapeTownReceiverUtils.class);




	static void setupReceivers( Controler controler ) {

		String outputfolder = controler.getScenario().getConfig().controler().getOutputDirectory();
		outputfolder += outputfolder.endsWith("/") ? "" : "/";
//		Receivers finalReceivers = new Receivers();
		Receivers finalReceivers = ReceiverUtils.getReceivers( controler.getScenario() );
//		new ReceiversReader(finalReceivers).readFile(outputfolder + "receivers.xml");
		finalReceivers = ReceiverUtils.getReceivers( controler.getScenario() );

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
		ReceiverChessboardUtils.setCoalitionFromReceiverAttributes( controler, coalition );
		LOG.info("Current number of receiver coalition members: " + coalition.getReceiverCoalitionMembers().size());
		LOG.info("Current number of carrier coalition members: " + coalition.getCarrierCoalitionMembers().size());
		LOG.info("Total number of receiver agents: " + Integer.toString( ReceiverUtils.getReceivers( controler.getScenario() ).getReceivers().size()));
		//		}


		/*
		 * Create a new instance of a receiver scoring function factory.
		 */
		final ReceiverScoringFunctionFactory rScorFuncFac = new ProportionalReceiverScoringFunctionFactoryImpl();

		/*
		 * Create a new instance of a receiver plan strategy manager factory.
		 */

		final ReceiverOrderStrategyManagerFactory rStratManFac = new CapeTownReceiverOrderStrategyManagerImpl();
		ReceiverModule receiverControler = new ReceiverModule(finalReceivers, rScorFuncFac, rStratManFac, controler.getScenario());
		controler.addOverridingModule(receiverControler);

	}

}
