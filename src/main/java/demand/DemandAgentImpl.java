/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2022 by the members listed in the COPYING,        *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

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
		this.demandObjects = new ArrayList<>();
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
