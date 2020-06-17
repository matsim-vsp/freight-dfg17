/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2020 by the members listed in the COPYING,        *
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
package receiver.usecases.chessboard.marginalv2;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import receiver.Receiver;

import java.util.concurrent.Callable;

/**
 * Executes a MATSim run for a given (freight) scenario, where the scenario
 * is cognisant of the receiver (being excluded) but calculates the marginal
 * contribution FOR THAT receiver.
 *
 * Instead of reading in the scenario elements from file, this implementation
 * aims to get the scenario passed as an argument.
 *
 * @author jwjoubert
 */
class MarginalCalculatorCallable implements Callable<Double> {
	final private Logger log = Logger.getLogger(MarginalCalculatorCallable.class);
	final private long seed;
	final private Scenario scenario;
	final private Id<Receiver> receiverId;

	MarginalCalculatorCallable(long seed, final Scenario scenario, Id<Receiver> receiverId){
		this.seed = seed;
		this.scenario = scenario;
		this.receiverId = receiverId;
	}

	@Override
	public Double call() throws Exception {
		/* Set up the input data. */

		/* Execute the pipe using the MarginalReceiverClass. */

		/* Get the receiver's cost from the output. */

		/* Cleanup */


		return null;
	}
}
