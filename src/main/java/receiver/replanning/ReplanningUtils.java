package receiver.replanning;

public class ReplanningUtils {
	private ReplanningUtils(){} // do not instantiate
	
	public static NumDelReceiverOrderStrategyManagerImpl createNumDelReceiverOrderStrategyManagerImpl() {
		return new NumDelReceiverOrderStrategyManagerImpl();
	}
}
