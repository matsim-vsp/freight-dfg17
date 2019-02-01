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
import receiver.ReceiverModule;
import receiver.ReceiverScoringFunctionFactory;
import receiver.ReceiverUtils;
import receiver.Receivers;
import receiver.collaboration.Coalition;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;
import receiver.usecases.UsecasesReceiverScoringFunctionFactory;

/**
 *
 * @author jwjoubert
 */
public class CapeTownReceiverUtils {
	final private static Logger LOG = Logger.getLogger(CapeTownReceiverUtils.class);



	public static void setupReceivers(Controler controler) {

		Receivers finalReceivers = ReceiverUtils.getReceivers( controler.getScenario() );

		finalReceivers.linkReceiverOrdersToCarriers( ReceiverUtils.getCarriers( controler.getScenario() ) );
		// (presumably done twice, just to be sure)

		Coalition coalition = ReceiverUtils.getCoalition( controler.getScenario() );
		ReceiverUtils.setCoalitionFromReceiverAttributes( controler.getScenario(), coalition );
		// (presumably done twice, just to be sure)

		final ReceiverScoringFunctionFactory rScorFuncFac = new UsecasesReceiverScoringFunctionFactory();

		final ReceiverOrderStrategyManagerFactory rStratManFac = new CapeTownReceiverOrderStrategyManagerImpl();

		ReceiverModule receiverControler = new ReceiverModule(finalReceivers, rScorFuncFac, rStratManFac, controler.getScenario());

		controler.addOverridingModule(receiverControler);
	}

}
