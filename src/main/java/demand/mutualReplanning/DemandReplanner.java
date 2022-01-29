package demand.mutualReplanning;

import java.util.Collection;

import org.matsim.core.controler.events.ReplanningEvent;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;

public interface DemandReplanner {
	void replan(Collection<LSPDecorator> lsps, ReplanningEvent event);
	void addStrategy(DemandPlanStrategyImpl strategy);
	void setDemandObject(DemandObject demandObject);
	DemandObject getDemandObject();
}
