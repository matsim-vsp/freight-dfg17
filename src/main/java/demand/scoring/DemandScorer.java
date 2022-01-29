package demand.scoring;

import demand.demandObject.DemandObject;
import lsp.scoring.Scorer;

public interface DemandScorer extends Scorer{

	double scoreCurrentPlan(DemandObject demandObject);
	void setDemandObject(DemandObject demandObject);
}
