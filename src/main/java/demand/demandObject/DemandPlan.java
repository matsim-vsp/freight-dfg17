package demand.demandObject;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.BasicPlan;

import demand.decoratedLSP.LSPDecorator;
import lsp.LogisticsSolution;

public interface DemandPlan extends BasicPlan{
	
	Double getScore();
	void setScore(Double arg0);
	ShipperShipment getShipment();
	LSPDecorator getLsp();
	Id<LogisticsSolution> getSolutionId();
	DemandObject getDemandObject();
	void setDemandObject(DemandObject demandObject);

}
