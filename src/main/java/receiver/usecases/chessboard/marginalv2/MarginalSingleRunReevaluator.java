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

import java.io.File;

/**
 * Running the marginal contribution, but re-evaluating the marginal contribution
 * of receivers each time they replan.
 */
class MarginalSingleRunReevaluator {
	final private Logger LOG = Logger.getLogger(MarginalSingleRunReevaluator.class);

	public static void main(String[] args) {
		String inputFolder = args[0];
		String outputFolder = args[1];
		new MarginalSingleRunReevaluator().run(inputFolder, outputFolder);
	}

	public void run(String inputFolder, String outputfolder){
		boolean output = new File(outputfolder).mkdirs();
		if(!output){
			LOG.error("Could not create output folder " + outputfolder);
		}
	}
}
