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

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;
import demand.demandObject.DemandPlan;
import demand.offer.Offer;
import org.matsim.core.replanning.ReplanningContext;

import java.util.Collection;

public class OfferReplanningStrategyModuleImpl extends OfferReplanningStrategyModule{

	
	public OfferReplanningStrategyModuleImpl(DemandObject demandObject) {
		super(demandObject);
	}
	
	public OfferReplanningStrategyModuleImpl() {
		super();
	}
	
	@Override
	public void handlePlan(DemandPlan demandPlan) {
		Collection<Offer> offers = recieveOffers(lsps);
		plan = createPlan(demandPlan, offers);
		demandObject.setSelectedPlan(plan);
	}
	
	protected Collection<Offer> recieveOffers(Collection<LSPDecorator> lsps){
		return demandObject.getOfferRequester().requestOffers(lsps);				
	}
	
	protected DemandPlan createPlan(DemandPlan demandPlan, Collection<Offer> offers) {
			return demandObject.getDemandPlanGenerator().createDemandPlan(offers);
	}

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {
			
	}
		
	
}
