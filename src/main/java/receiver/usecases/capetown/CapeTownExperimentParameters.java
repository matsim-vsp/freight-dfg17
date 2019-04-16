package receiver.usecases.capetown;

import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacility;

import receiver.replanning.ReceiverReplanningType;

/**
 * Class to just help with setting experimental parameters, which are called 
 * from multiple places, in ONE location.
 * 
 * @author wlbean
 */
public class CapeTownExperimentParameters {

	public static int NUM_ITERATIONS = 1000;
	
	public static int STAT_INTERVAL = 20;
	
	public static int REPLAN_INTERVAL = 20;
	
//	public static int NUMBER_OF_RECEIVERS = 5;
	
	public static int TIME_WINDOW_DURATION = 4;
	
	public static String SERVICE_TIME = "02:00:00";
	
	public static int NUM_DELIVERIES = 5;
	
	public static double PROPORTION_CORPORATE = 0.4;
	
	public static Id<ActivityFacility> DEPOT_ID = Id.create("0", ActivityFacility.class);
	
	public static String DAY_START = "06:00:00";

	public static String DAY_END = "30:00:00";
	
	public static double TIME_WINDOW_HOURLY_COST = 10.0;
	
	public static ReceiverReplanningType REPLANNING_STRATEGY = ReceiverReplanningType.afterHoursTimeWindow;
}
