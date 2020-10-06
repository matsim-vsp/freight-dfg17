package demand;

import java.util.Collection;

import org.matsim.api.core.v01.Id;

import demand.demandObject.DemandObject;

public interface DemandAgent {
 
	public Id<DemandAgent> getId();
	public Collection<DemandObject> getDemandObjects();
//	public Collection<UtilityFunction> getUtilityFunctions();
}