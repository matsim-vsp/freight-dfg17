package demand;

import java.util.Collection;

import org.matsim.api.core.v01.Id;

import demand.demandObject.DemandObject;

public interface DemandAgent {
 
	Id<DemandAgent> getId();
	Collection<DemandObject> getDemandObjects();
//	public Collection<UtilityFunction> getUtilityFunctions();
}
