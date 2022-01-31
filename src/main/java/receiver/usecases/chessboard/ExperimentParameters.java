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

import receiver.replanning.ReceiverReplanningType;

/**
 * Class to help with setting experimental parameters, which are called 
 * from multiple places, in ONE location.
 * 
 * @author jwjoubert, wlbean
 */
class ExperimentParameters {
	
	public static final int NUM_ITERATIONS = 200;
	
	public static final int STAT_INTERVAL = 1;
	
	public static final int REPLAN_INTERVAL = 10;
	
	public static final int NUMBER_OF_RECEIVERS = 5;
	
	public static final int TIME_WINDOW_DURATION = 12;
	
	public static final String SERVICE_TIME = "02:00:00";
	
	public static final int NUM_DELIVERIES = 5;
	
	public static final String DAY_START = "06:00:00";

	public static final String DAY_END = "18:00:00";
	
	public static final double TIME_WINDOW_HOURLY_COST = 0.0;
	
	public static final ReceiverReplanningType REPLANNING_STRATEGY = ReceiverReplanningType.serviceTime;
}
