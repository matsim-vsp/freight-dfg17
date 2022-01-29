package demand.mutualReplanning;

import java.util.Collection;

import org.matsim.core.controler.events.ReplanningEvent;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;

public interface DemandReplanner {
	public void replan(Collection<LSPDecorator> lsps, ReplanningEvent event);
	public void addStrategy(DemandPlanStrategyImpl strategy);
	public void setDemandObject(DemandObject demandObject);
	public DemandObject getDemandObject();
}
