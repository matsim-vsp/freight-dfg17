package receiver;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carriers;
import receiver.collaboration.Coalition;
import receiver.collaboration.MutableCoalition;

public class ReceiverUtils {
	private final static String REPLAN_INTERVAL = "replanInterval";
	private static final Logger log = Logger.getLogger( ReceiverUtils.class ) ;
	
	private ReceiverUtils(){} // do not instantiate
	
	private static final String CARRIERS_SCENARIO_ELEMENT = "Carriers";
	private static final String RECEIVERS_SCENARIO_ELEMENT = "Receivers" ;
	private static final String COALITION_SCENARIO_ELEMENT = "Coalition" ;
	
	/*
	 * Create a new instance of a receiver.
	 */
	public static Receiver newInstance( Id<Receiver> id){
		// this pattern allows to make the implementation package-protected. kai, sep'18
		
		return new ReceiverImpl(id);
	}
	
	public static Carriers getCarriers( final Scenario sc ) {
		return (Carriers) sc.getScenarioElement( CARRIERS_SCENARIO_ELEMENT );
	}
	
	public static void setReceivers( final Receivers receivers, final Scenario sc ) {
		sc.addScenarioElement( RECEIVERS_SCENARIO_ELEMENT, receivers );
	}
	
	public static Receivers getReceivers( final Scenario sc ) {
		Receivers receivers = (Receivers) sc.getScenarioElement( RECEIVERS_SCENARIO_ELEMENT );
		//		if(receivers == null) {
		//			log.error("No receivers were set. Returning new, empty receivers.");
		//			return new Receivers();
		//		}
		// yyyy I found the above.  I think that this is actually quite dangerous since it is returning the container, but not
		// memorizing it.  Changing it now.  kai, sep'18
		if ( receivers == null ) {
			log.error("No receivers were set. Returning new, empty receivers, AND memorizing them.");
			receivers = new Receivers() ;
			setReceivers( receivers, sc ) ;
		}
		return receivers;
	}
	
	public static void setCarriers( final Carriers carriers, final Scenario sc ) {
		sc.addScenarioElement( CARRIERS_SCENARIO_ELEMENT, carriers );
	}
	
	public static void setCoalition( final MutableCoalition coalition, final Scenario sc ) {
		sc.addScenarioElement( COALITION_SCENARIO_ELEMENT, coalition );
	}
	
	public static Coalition getCoalition( final Scenario sc ) {
		return (Coalition) sc.getScenarioElement( COALITION_SCENARIO_ELEMENT );
	}
	
	public static void setReplanInterval( final int interval, final Scenario sc ) {
		sc.addScenarioElement( REPLAN_INTERVAL, interval );
		// seems a bit overkill to do this as scenarioElement, but I can't think of a better solution right now.  Adding it into
		// the config would make it very public API, which maybe we don't want at this point. kai, sep'18
	}
	
	public static int getReplanInterval( final Scenario sc ) {
		return (int) sc.getScenarioElement( REPLAN_INTERVAL );
	}
	
}
