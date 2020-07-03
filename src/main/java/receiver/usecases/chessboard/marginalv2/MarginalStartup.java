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

import org.matsim.api.core.v01.Scenario;
import receiver.usecases.chessboard.MarginalScenarioBuilder;

/**
 * Class to create the initial scenario, including the carriers receivers, and
 * the network and writing it to a folder.
 */
public class MarginalStartup {
	final private static String DEFAULT_LOCATION = "./output/";
	final private static String DEFAULT_SEED = "20200305";

	public static void main(String[] args) {
		if(args.length==0){
			args = new String[] {DEFAULT_LOCATION, DEFAULT_SEED};
		}
		run(args);
	}

	public static void run(String[] args) {
		String folder = args[0];
		long seed = Long.parseLong(args[1]);

		Scenario scenario = MarginalScenarioBuilder.createChessboardScenario(seed, true);
	}
}
