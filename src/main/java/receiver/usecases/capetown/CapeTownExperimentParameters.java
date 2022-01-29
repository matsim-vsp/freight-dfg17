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

	public static final int NUM_ITERATIONS = 1000;
	
	public static final int STAT_INTERVAL = 20;
	
	public static final int REPLAN_INTERVAL = 20;
	
//	public static int NUMBER_OF_RECEIVERS = 5;
	
	public static final int TIME_WINDOW_DURATION = 4;
	
	public static final String SERVICE_TIME = "02:00:00";
	
	public static final int NUM_DELIVERIES = 5;
	
	public static final double PROPORTION_CORPORATE = 0.4;
	
	public static final Id<ActivityFacility> DEPOT_ID = Id.create("0", ActivityFacility.class);
	
	public static final String DAY_START = "06:00:00";

	public static final String DAY_END = "30:00:00";
	
	public static final double TIME_WINDOW_HOURLY_COST = 10.0;
	
	public static ReceiverReplanningType REPLANNING_STRATEGY = ReceiverReplanningType.afterHoursTimeWindow;
}
