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
import org.matsim.contrib.freight.carrier.CarrierPlanXmlReader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeReader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.utils.FreightUtils;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import receiver.Receiver;
import receiver.ReceiverUtils;
import receiver.ReceiversReader;

/**
 * Class to execute a single MATSim instance for a carrier/receiver scenario.
 */
class MarginalReceiverRun {
	private final static Logger LOG = Logger.getLogger(MarginalReceiverRun.class);
	private final static String DEFAULT_SEED = "20200305";
	private final static String DEFAULT_FOLDER = "./output/";
	private final static String DEFAULT_ID = "3";

	private final static String FILE_NETWORK = "network.xml";
	private final static String FILE_CARRIERS = "carriers.xml";
	private final static String FILE_CARRIER_VEHICLE_TYPES = "carrierVehicleTypes.xml";
	private final static String FILE_RECEIVERS = "receivers.xml";
	private final static String FILE_CONFIG = "config.xml";


	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[]{DEFAULT_SEED, DEFAULT_FOLDER, DEFAULT_ID};
		}
		run(args);
	}

	static void run(String[] args){
		LOG.info("Running with the following arguments: ");
		for(String s : args) {
			LOG.info(s);
		}

		long seed = Long.parseLong(args[0]);
		String folder = args[1];
		folder += folder.endsWith("/") ? "" : "/";
		String idString = args[2];
		Id<Receiver> receiverId = Id.create(idString, Receiver.class);

		/* Parse the entire scenario from file. This includes:
		* - network;
		* - carriers; and
		* - receivers */
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(folder + FILE_NETWORK);
		CarrierVehicleTypes carrierVehicleTypes = CarrierVehicleTypes.getVehicleTypes(FreightUtils.getCarriers(scenario));
		new CarrierVehicleTypeReader(carrierVehicleTypes).readFile(folder + FILE_CARRIER_VEHICLE_TYPES);

		new CarrierPlanXmlReader(FreightUtils.addOrGetCarriers(scenario), carrierVehicleTypes).readFile(folder + FILE_CARRIERS);
		new ReceiversReader(ReceiverUtils.getReceivers(scenario)).readFile(folder + FILE_RECEIVERS);



	}
}
