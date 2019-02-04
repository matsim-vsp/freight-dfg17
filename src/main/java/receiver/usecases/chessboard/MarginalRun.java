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

package receiver.usecases.chessboard;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;

import receiver.Receiver;
import receiver.ReceiverUtils;
import receiver.ReceiversWriter;

/**
 * Executes the receiver run by starting off with calculating the grand
 * coalition score, and the different marginals (for each receiver), and then
 * start with the overall run.
 *  
 * @author jwjoubert
 */
 class MarginalRun{
	final private static Logger LOG = Logger.getLogger( MarginalRun.class );
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LOG.info("Start marginal run...");
		run(args);
		LOG.info("Completed.");
	}
	
	
	/**
	 * Executes the calculation of marginal contributions. 
	 * 
	 *  <ol>
	 *  	<li> absolute path to the input directory of the freight-dfg17 repo;
	 *  
	 *  </ol>
	 * 
	 * @param args
	 */
	public static Scenario run( String[] args) {
		String inputPath = args[0];
		inputPath += inputPath.endsWith("/") ? "" : File.separator;
		String outputPath = args[1];
		outputPath += outputPath.endsWith("/") ? "" : File.separator;
		String release = args[2];
		int numberOfThreads = Integer.parseInt(args[3]);
		long seed = Long.parseLong(args[4]);
		
		checkInputPath(inputPath, release);
		
		/* Set up the parallel infrastructure. */
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		
		/* Calculate grand coalition cost. */
		LOG.info("Building base freight scenario");
		Scenario sc = MarginalScenarioBuilder.createChessboardScenario(inputPath + "output", seed, 1, false);
		double grandCoalitionCost = Double.NEGATIVE_INFINITY;
		MarginalCalculateCallable cmcGrand = new MarginalCalculateCallable(seed, inputPath, outputPath, release, Id.create("0", Receiver.class ));
		Future<Double> grandJob = executor.submit(cmcGrand);
		executor.shutdown();
		while(!executor.isTerminated()) {
		}
		try {
			grandCoalitionCost = grandJob.get();
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Cannot get the grand coalition cost.");
		}
		ReceiverUtils.getCoalition( sc ).getAttributes().putAttribute("C(N)", grandCoalitionCost);
		
		/* Calculate the marginal contribution for each receiver. */
		LOG.info("Calculate the marginal contributions for each receiver...");
		executor = Executors.newFixedThreadPool(numberOfThreads);
		Map<Id<Receiver>, Future<Double>> jobs = new TreeMap<>();
		
		for(Receiver receiver : ReceiverUtils.getReceivers( sc ).getReceivers().values()) {
			/* Only execute a marginal calculation run for those in the grand coalition. */
			if((boolean) receiver.getAttributes().getAttribute( ReceiverUtils.ATTR_COLLABORATION_STATUS )) {
				MarginalCalculateCallable cmc = new MarginalCalculateCallable(seed, inputPath, outputPath, release, receiver.getId());
				Future<Double> job = executor.submit(cmc);
				jobs.put(receiver.getId(), job);
			} else {
				/*TODO Allocate a fixed cost... based on the grandCoalitionCost?! */
			}
		}
		executor.shutdown();
		while(!executor.isTerminated()) {
		}
		LOG.info("Done calculating the marginals.");
		
		/* Write the receivers, along with the newly added "marginal" attribute. */
		LOG.info("Consolidate marginals and write to file.");
		for(Id<Receiver> rId : jobs.keySet()) {
			try {
				double cost = jobs.get(rId).get();
				String attr = String.format("C(N|{%s})", rId.toString());
				ReceiverUtils.getReceivers( sc ).getReceivers().get(rId).getAttributes().putAttribute(attr, cost);
				ReceiverUtils.getCoalition( sc ).getAttributes().putAttribute(attr, cost);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not get the marginal contribution for receiver " + rId.toString());
			}
		}
		
		/* TODO Check if we really need to write the receivers to file. */
		String receiversFilename = outputPath + "receivers.xml.gz";
		new ReceiversWriter( ReceiverUtils.getReceivers( sc ) ).write(receiversFilename );
		
		return sc;
	}
	
	
	/**
	 * Ensure that the input network and algorithm files are indeed available.
	 * 
	 * @param folder
	 */
	private static void checkInputPath(String folder, String release) {
		File gridNetwork = new File(folder + "/chessboard/network/grid9x9.xml");
		if(!gridNetwork.exists()) {
			throw new RuntimeException("The given input path '" + folder + "' does not contain a grid9x9.xml network");
		}

		File algorithm = new File(folder + "/chessboard/vrpalgo/initialPlanAlgorithm.xml");
		if(!algorithm.exists()) {
			throw new RuntimeException("The given input path '" + folder + "' does not contain an initialPlanAlgorithm.xml file");
		}

		File releaseFile = new File(release);
		if(!releaseFile.exists()) {
			throw new RuntimeException("The given release.zip path '" + release + "' does not exist.");
		}
	}

}
