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

package demand.scoring;

import java.util.Collection;

import org.matsim.core.controler.events.ScoringEvent;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;


public class MutualScoringModuleImpl implements MutualScoringModule{

	private final Collection<DemandObject> demandObjects;
	private final Collection<LSPDecorator> lsps;
	
	public MutualScoringModuleImpl(Collection<DemandObject> demandObjects, Collection<LSPDecorator> lsps) {
		this.demandObjects = demandObjects;
		this.lsps = lsps;
	}
	
	@Override
	public void notifyScoring(ScoringEvent event) {
		scoreDemandObjects(event);	
		scoreLSPs(event);
	}

	@Override
	public void scoreDemandObjects(ScoringEvent event) {
		for(DemandObject demandObject : demandObjects) {
			if(demandObject.getScorer() != null) {
				demandObject.scoreSelectedPlan();	
			}	
		}
	}

	@Override
	public void scoreLSPs(ScoringEvent event) {
		for(LSPDecorator lsp : lsps) {
			lsp.scoreSelectedPlan();
		}
		
	}
}
