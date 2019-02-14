/* *********************************************************************** *
 * project: org.matsim.*
 * RunReceiver.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package receiver.usecases.capetown;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.router.NetworkRoutingProvider;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Time;
import receiver.usecases.chessboard.ReceiverChessboardUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Specific example for my (wlbean) thesis chapters 5 and 6.
 * @author jwjoubert, wlbean
 */

public class RunCapeTownReceiverWithPopulation {
	final private static Logger LOG = Logger.getLogger(RunCapeTownReceiverWithPopulation.class);
	final private static long SEED_BASE = 20180816L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RunCapeTownReceiver.main(args);
	}


	public static void run(int run) {
		LOG.info("Starting run " + run);
		String outputfolder = String.format("./output/capetown/run_%03d/", run);
		new File(outputfolder).mkdirs();
		Scenario sc = CapeTownScenarioBuilder.createCapeTownScenarioWithPassengers(SEED_BASE*run, run, true);
		
		/* Write headings */
		BufferedWriter bw = IOUtils.getBufferedWriter(sc.getConfig().controler().getOutputDirectory() + "/ReceiverStats" + run + ".csv");
		try {
			bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
					"iteration", 
					"receiver_id", 
					"score", 
					"timewindow_start", 
					"timewindow_end", 
					"order_id", 
					"volume", 	        				
					"frequency", 
					"serviceduration",
					"collaborate_p",
					"collaborate_r",
					"grandCoalitionMember"));
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot write initial headings");  
		} finally{
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot close receiver stats file");
			}
		}

		sc.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

		Controler controler = new Controler(sc);

		/* Set up freight portion. To be repeated every iteration*/
		/* FIXME This should be removed and used from ReceiverModule */
		RunCapeTownReceiver.setupReceiverAndCarrierReplanning(controler, outputfolder);

		/* Add travel time binding for "commercial" mode. */
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				this.addTravelTimeBinding("commercial").to(networkTravelTime());
				this.addTravelDisutilityFactoryBinding("commercial").to(carTravelDisutilityFactoryKey());
				this.addRoutingModuleBinding("commercial").toProvider(new NetworkRoutingProvider("commercial", "car"));
			}
		});
		/* Setup generic subpopulation replanning. */
		/* Generic strategy */
		Config config = controler.getConfig();
		StrategySettings changeExpBetaStrategySettings = new StrategySettings( );
		changeExpBetaStrategySettings.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
		changeExpBetaStrategySettings.setWeight(0.8);
		config.strategy().addStrategySettings(changeExpBetaStrategySettings);
		/* People subpopulation strategy. */
		StrategySettings peopleChangeExpBeta = new StrategySettings( );
		peopleChangeExpBeta.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta);
		peopleChangeExpBeta.setWeight(0.70);
		peopleChangeExpBeta.setSubpopulation("private");
		config.strategy().addStrategySettings(peopleChangeExpBeta);
		/* People subpopulation ReRoute. Switch off after a time. */
		StrategySettings peopleReRoute = new StrategySettings( );
		peopleReRoute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute);
		peopleReRoute.setWeight(0.10);
		peopleReRoute.setSubpopulation("private");
		peopleReRoute.setDisableAfter(85);
		config.strategy().addStrategySettings(peopleReRoute);
		/* People subpopulation Time allocation mutator AND ReRoute. Switch off after a time. */
		StrategySettings peopleTimeAndReroute = new StrategySettings( );
		peopleTimeAndReroute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.TimeAllocationMutator_ReRoute);
		peopleTimeAndReroute.setWeight(0.20);
		peopleTimeAndReroute.setSubpopulation("private");
		peopleTimeAndReroute.setDisableAfter(85);
		config.strategy().addStrategySettings(peopleTimeAndReroute);
		/* Commercial subpopulation strategy. */
		StrategySettings commercialStrategy = new StrategySettings( );
		commercialStrategy.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta);
		commercialStrategy.setWeight(0.85);
		commercialStrategy.setSubpopulation("commercial");
		config.strategy().addStrategySettings(commercialStrategy);
		/* Commercial subpopulation ReRoute. Switch off after a time. */
		StrategySettings commercialReRoute = new StrategySettings( );
		commercialReRoute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute);
		commercialReRoute.setWeight(0.15);
		commercialReRoute.setSubpopulation("commercial");
		commercialReRoute.setDisableAfter(85);
		config.strategy().addStrategySettings(commercialReRoute);
		
		config.qsim().setEndTime(Time.parseTime("48:00:00"));
		
		/* Take out all commercial vehicles. FIXME This must be sorted. */
		List<Id<Person>> ids = new ArrayList<>();
		for(Id<Person> pId : sc.getPopulation().getPersons().keySet()){
			if(pId.toString().startsWith("coct_c")){
				ids.add(pId);
			}
		}
		for(Id<Person> pId : ids){
			sc.getPopulation().removePerson(pId);
		}
		
		LOG.info("========> Removed " + ids.size() + " commercial vehicles!!");
		
		
		
		ReceiverChessboardUtils.setupCarriers(controler );
		CapeTownReceiverUtils.setupReceivers(controler);

		/* TODO This stats must be set up automatically. */
		RunCapeTownReceiver.prepareFreightOutputDataAndStats(controler, run );

		controler.run();
		
		/* Clean up iterations folder */
		File itersFolder = new File(outputfolder + "ITERS/");
		IOUtils.deleteDirectoryRecursively(itersFolder.toPath());
	}


}
