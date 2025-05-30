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

package demand.mutualReplanning;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.replanning.GenericPlanStrategy;
import org.matsim.core.replanning.GenericStrategyManager;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;
import demand.demandObject.DemandPlan;

public class DemandReplannerImpl implements DemandReplanner{

	private DemandObject demandObject;
	private final GenericStrategyManager<DemandPlan, DemandObject> strategyManager;
	
	
	public DemandReplannerImpl(DemandObject demandObject) {
		this.demandObject = demandObject;
		this.strategyManager = new GenericStrategyManager<>();
	}
	
	public DemandReplannerImpl() {
		this.strategyManager = new GenericStrategyManager<>();
	}
		
	
	@Override
	public void setDemandObject(DemandObject demandObject) {
		this.demandObject = demandObject;
	}

	@Override
	public void replan(Collection<LSPDecorator> lsps, ReplanningEvent event) {
		for(GenericPlanStrategy<DemandPlan, DemandObject> strategy : strategyManager.getStrategies(null)) {
			DemandPlanStrategyImpl demandPlanStrategy = (DemandPlanStrategyImpl) strategy;
			demandPlanStrategy.setDemandObject(demandObject);
			demandPlanStrategy.getModule().setDemandObject(demandObject);
			demandPlanStrategy.getModule().setLSPS(lsps);
		}
		
		if(strategyManager != null) {
			ArrayList<DemandObject> demandObjectList = new ArrayList <>();
			demandObjectList.add(demandObject);
			strategyManager.run(demandObjectList, null, event.getIteration(), event.getReplanningContext());
		}
		
	}

	
	@Override
	public void addStrategy(DemandPlanStrategyImpl strategy) {
		strategy.setDemandObject(demandObject);
		strategyManager.addStrategy(strategy, null, 1);
	}

	@Override
	public DemandObject getDemandObject() {
		return demandObject;
	}

}
