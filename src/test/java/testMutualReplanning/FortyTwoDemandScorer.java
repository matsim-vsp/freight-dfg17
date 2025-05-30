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

package testMutualReplanning;

import demand.demandObject.DemandObject;
import demand.scoring.DemandScorer;

public class FortyTwoDemandScorer implements DemandScorer {

	@Override
	public double scoreCurrentPlan(DemandObject demandObject) {
		return 42;
	}

	@Override
	public void setDemandObject(DemandObject demandObject) {
		// TODO Auto-generated method stub
		
	}

}
