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
package receiver.marginalParallel;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;

import receiver.FreightScenario;
import receiver.Receiver;
import receiver.io.ReceiversWriter;
import receiver.usecases.ReceiverChessboardScenarioExample;

/**
 * Executes the receiver run by starting off with calculating the grand
 * coalition score, and the different marginals (for each receiver), and then
 * start with the overall run.
 *  
 * @author jwjoubert
 */
public class RunMarginal {
	final private static Logger LOG = Logger.getLogger(RunMarginal.class);
	

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
	public static void run(String[] args) {
		String inputPath = args[0];
		inputPath += inputPath.endsWith("/") ? "" : "/";
		String outputPath = args[1];
		outputPath += outputPath.endsWith("/") ? "" : "/";
		String release = args[2];
		int numberOfThreads = Integer.parseInt(args[3]);
		long seed = Long.parseLong(args[4]);
		
		checkInputPath(inputPath, release);
		
		
		/* Calculate grand coalition cost. */
		LOG.info("Building base freight scenario");
		FreightScenario fs = ReceiverChessboardScenarioExample.createChessboardScenario(inputPath + "output", seed, 1, false);
		//TODO Run the grand coalition.
		
		/* Calculate the marginal contribution for each receiver. */
		LOG.info("Calculate the marginal contributions for each receiver...");
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		Map<Id<Receiver>, Future<Double>> jobs = new TreeMap<>();
		
		for(Receiver receiver : fs.getReceivers().getReceivers().values()) {
			CalculateMarginalCallable cmc = new CalculateMarginalCallable(seed, inputPath, outputPath, release, receiver.getId());
			Future<Double> job = executor.submit(cmc);
			jobs.put(receiver.getId(), job);
		}
		executor.shutdown();
		while(!executor.isTerminated()) {
		}
		LOG.info("Done calculating the marginals.");
		
		/* Write the receivers, along with the newly added "marginal" attribute. */
		LOG.info("Consolidate marginals and write to file.");
		for(Id<Receiver> rId : jobs.keySet()) {
			try {
				double marginal = jobs.get(rId).get();
				fs.getReceivers().getReceivers().get(rId).getAttributes().putAttribute("marginal", marginal);
				
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not get the marginal contribution for receiver " + rId.toString());
			}
		}
		String receiversFilename = inputPath + "receivers.xml.gz";
		new ReceiversWriter(fs.getReceivers()).write(receiversFilename );
		
		/* If we can do it from here, start the rest of the run. */
		
	}
	
	
	/**
	 * Ensure that the input network and algorithm files are indeed available.
	 * 
	 * @param folder
	 */
	private static void checkInputPath(String folder, String release) {
		File gridNetwork = new File(folder + "usecases/chessboard/network/grid9x9.xml");
		if(!gridNetwork.exists()) {
			throw new RuntimeException("The given input path '" + folder + "' does not contain a grid9x9.xml network");
		}

		File algorithm = new File(folder + "usecases/chessboard/vrpalgo/initialPlanAlgorithm.xml");
		if(!algorithm.exists()) {
			throw new RuntimeException("The given input path '" + folder + "' does not contain an initialPlanAlgorithm.xml file");
		}

		File releaseFile = new File(release);
		if(!releaseFile.exists()) {
			throw new RuntimeException("The given release.zip path '" + release + "' does not exist.");
		}
	}

}
