package demand.demandObject;

import java.util.Collection;

import demand.offer.Offer;

public interface DemandPlanGenerator {

	DemandPlan createDemandPlan(Collection<Offer> offers);
	void setDemandObject(DemandObject demandObject);
}
