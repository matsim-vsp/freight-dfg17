package receiver.replanning;

public class ReplanningUtils {
	private ReplanningUtils(){} // do not instantiate

	// yyyy todo: make the strategy manager "infrastructure", i.e. have it standard for the receiver and make the studies add plan strategies as they want.  kai, jan'19
	// (although, in fact, the design that is used here is also not so bad)
	
	public static ReceiverOrderStrategyManagerFactory createNumDelReceiverOrderStrategyManagerImpl() {
		return new NumDelReceiverOrderStrategyManagerImpl();
	}

	public static ReceiverOrderStrategyManagerFactory createServiceTimeReceiverOrderStrategyManagerImpl(){
		return new ServiceTimeReceiverOrderStrategyManagerImpl();
	}

	public static ReceiverOrderStrategyManagerFactory createTimeWindowReceiverOrderStrategyManagerImpl(){
		return new TimeWindowReceiverOrderStrategyManagerImpl();
	}

}
