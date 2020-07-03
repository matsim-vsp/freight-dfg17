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
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Iterator;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.FreightConfigGroup;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.controler.CarrierModule;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.contrib.freight.controler.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.controler.CarrierScoringFunctionFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;

import receiver.*;
import receiver.usecases.UsecasesCarrierScoringFunctionFactory;
import receiver.usecases.UsecasesCarrierStrategyManagerFactory;

/**
 *
 * @author jwjoubert
 */
public class ReceiverChessboardUtils {
	final private static Logger LOG = Logger.getLogger(ReceiverChessboardUtils.class);
	final public static int STATISTICS_INTERVAL = 50;
	
	public static void setupCarriers(Controler controler) {
		Carriers carriers = ReceiverUtils.getCarriers( controler.getScenario() );;

		BaseRunReceiver.setupCarrierReplanning(controler );

		/* Create a new instance of a carrier scoring function factory. */
		final CarrierScoringFunctionFactory cScorFuncFac = new UsecasesCarrierScoringFunctionFactory( controler.getScenario().getNetwork() );

		/* Create a new instance of a carrier plan strategy manager factory. */
		final CarrierPlanStrategyManagerFactory cStratManFac = new UsecasesCarrierStrategyManagerFactory( CarrierVehicleTypes.getVehicleTypes( carriers ),
			  controler.getScenario().getNetwork(), controler);

		FreightConfigGroup freightConfig = ConfigUtils.addOrGetModule( controler.getScenario().getConfig(), FreightConfigGroup.class );
		if ( true ){
			freightConfig.setTimeWindowHandling( FreightConfigGroup.TimeWindowHandling.enforceBeginnings );
		} else{
			freightConfig.setTimeWindowHandling( FreightConfigGroup.TimeWindowHandling.ignore );
		}

		CarrierModule carrierControler = new CarrierModule(carriers, cStratManFac, cScorFuncFac);
//		carrierControler.setPhysicallyEnforceTimeWindowBeginnings(true);
		controler.addOverridingModule(carrierControler);
	}


	/**
	 * Route the services that are allocated to the carrier and writes the initial carrier plans.
	 *
	 * @param carriers
	 * @param network
	 */
	public static void generateCarrierPlan(Carriers carriers, Network network, URL algorithmFile) {
		Carrier carrier = carriers.getCarriers().get(Id.create("Carrier1", Carrier.class));

		VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, network);

		NetworkBasedTransportCosts netBasedCosts = NetworkBasedTransportCosts.Builder.newInstance(network, carrier.getCarrierCapabilities().getVehicleTypes()).build();
		VehicleRoutingProblem vrp = vrpBuilder.setRoutingCost(netBasedCosts).build();

		//read and create a pre-configured algorithms to solve the vrp
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, algorithmFile);


		//solve the problem
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

		//get best (here, there is only one)
		VehicleRoutingProblemSolution solution = null;

		Iterator<VehicleRoutingProblemSolution> iterator = solutions.iterator();

		while(iterator.hasNext()){
			solution = iterator.next();
		}

		//create a carrierPlan from the solution
		CarrierPlan plan = MatsimJspritFactory.createPlan(carrier, solution);

		//route plan
		NetworkRouter.routePlan(plan, netBasedCosts);


		//assign this plan now to the carrier and make it the selected carrier plan
		carrier.setSelectedPlan(plan);
		

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
