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

package demand.demandObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;

public class DemandObjects {

	private final Map<Id<DemandObject>, DemandObject> demandObjects = new HashMap<>();
	
	public DemandObjects(Collection<DemandObject> demandObjects) {
		makeMap(demandObjects);
	}
	
//	public DemandObjects() {
//
//	}

	public Map<Id<DemandObject>, DemandObject> getDemandObjects(){
		return demandObjects;
	}

	private void makeMap(Collection<DemandObject> demandObjects) {
		for(DemandObject d : demandObjects) {
			this.demandObjects.put(d.getId(), d);
		}
	}

//	public void addDemandObject(DemandObject demandObject) {
//		if(!demandObjects.containsKey(demandObject.getId())) {
//			demandObjects.put(demandObject.getId(), demandObject);
//		}
//	}

}
