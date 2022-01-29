package demand;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;

import demand.demandObject.DemandObject;

class DemandAgentImpl implements DemandAgent {

	private final Id<DemandAgent> id;
	private final ArrayList<DemandObject> demandObjects;
//	private ArrayList <UtilityFunction> utilityFunctions;

	DemandAgentImpl(DemandUtils.DemandAgentImplBuilder builder) {
		this.demandObjects = new ArrayList<DemandObject>();
//		this.utilityFunctions = new ArrayList<UtilityFunction>();
//		this.utilityFunctions = builder.utilityFunctions;
		this.id = builder.getId();
	}
	
	
	@Override
	public Id<DemandAgent> getId() {
		return id;
	}

	@Override
	public Collection<DemandObject> getDemandObjects() {
		return demandObjects;
	}

//	@Override
//	public Collection<UtilityFunction> getUtilityFunctions() {
//		return utilityFunctions;
//	}

}
