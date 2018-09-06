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
package receiver.wlbCapeTown;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesWriter;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

/**
 * Class to parse the Pick-n-Pay outlets in the City of Cape Town from 
 * OpenStreetMap data.
 * 
 * @author jwjoubert
 */
public class ParsePnP {
	final private static Logger LOG = Logger.getLogger(ParsePnP.class);
	final private static CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(
			TransformationFactory.WGS84, TransformationFactory.HARTEBEESTHOEK94_LO19);
	final private static Coord DISTRIBUTION_CENTER_COORD = ct.transform(CoordUtils.createCoord(18.542431, -34.002989));
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		run(args);
	}
	
	public static void run(String[] args) {
		LOG.info("Starting to parse the PnP data from OSM file...");
		String pbfFile = args[0];
		String facilitiesFile = args[1];
		
		PbfReader pr = new PbfReader(new File(pbfFile ), 4);
		PnpSink sink = new PnpSink();
		pr.setSink(sink);
		pr.run();
		Map<Long, Coord> map = sink.getCoordMap();
		
		/* Create and write facilities */
		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		ActivityFacilities facilities = sc.getActivityFacilities();
		ActivityFacility dc = facilities.getFactory().createActivityFacility(Id.create(0l, ActivityFacility.class), DISTRIBUTION_CENTER_COORD);
		facilities.addActivityFacility(dc);
		for(Long l : map.keySet()) {
			Coord c = ct.transform(map.get(l));
			ActivityFacility facility = facilities.getFactory().createActivityFacility(Id.create(l, ActivityFacility.class), c);
			facilities.addActivityFacility(facility);
		}
		new FacilitiesWriter(facilities).write(facilitiesFile);
		
		LOG.info("Done parsing PnP.");
	}

	
	
	
	
	
}
