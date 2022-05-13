package demand.scoring;

import demand.demandObject.DemandObject;

public interface DemandScorer {

	double scoreCurrentPlan(DemandObject demandObject);
	void setDemandObject(DemandObject demandObject);
}
