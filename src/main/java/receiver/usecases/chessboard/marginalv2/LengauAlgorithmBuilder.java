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

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

/**
 * Class to build a VRP algorithm config file.
 */
class LengauAlgorithmBuilder {
	final static String filename = "./output/algorithm.xml";

	public static void main(String[] args) {
		if(args.length == 0){
			args = new String[]{filename};
		}
		run(args);
	}

	public static void run(String[] args) {
		VehicleRoutingProblem vrp = null;
		VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
	}
}
