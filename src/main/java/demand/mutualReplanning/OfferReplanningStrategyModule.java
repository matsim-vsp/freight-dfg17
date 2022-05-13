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

import java.util.Collection;

import lsp.shipment.ShipmentUtils;
import org.matsim.api.core.v01.Id;
import org.matsim.core.replanning.modules.GenericPlanStrategyModule;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;
import demand.demandObject.DemandPlan;
import demand.offer.Offer;
import lsp.LSPInfo;
import lsp.shipment.LSPShipment;

public abstract class OfferReplanningStrategyModule implements GenericPlanStrategyModule<DemandPlan>{

	protected DemandObject demandObject;
	protected Collection<LSPDecorator> lsps;
	protected DemandPlan plan; 
	
	
	public OfferReplanningStrategyModule() {
		
	}
	
	public OfferReplanningStrategyModule(DemandObject demandObject) {
		this.demandObject = demandObject;
	}
		
	@Override
	public void handlePlan(DemandPlan demandPlan) {
		Collection<Offer> offers = recieveOffers(lsps);
		plan = createPlan(demandPlan, offers);
		demandObject.setSelectedPlan(plan);
	}
	
	protected abstract Collection<Offer> recieveOffers(Collection<LSPDecorator> lsps);
	protected abstract DemandPlan createPlan(DemandPlan demandPlan, Collection<Offer> offers);
			
	@Override
	public void finishReplanning() {
		Id<LSPShipment> id = Id.create(plan.getShipment().getId(), LSPShipment.class);
		ShipmentUtils.LSPShipmentBuilder builder = ShipmentUtils.LSPShipmentBuilder.newInstance(id );
		builder.setFromLinkId(demandObject.getFromLinkId());
		builder.setToLinkId(demandObject.getToLinkId());
		builder.setCapacityDemand((int)plan.getShipment().getShipmentSize());
		builder.setDeliveryServiceTime(plan.getShipment().getServiceTime() );
		builder.setStartTimeWindow(plan.getShipment().getStartTimeWindow());
		builder.setEndTimeWindow(plan.getShipment().getEndTimeWindow());
		for(LSPInfo info : demandObject.getInfos()) {
			builder.addInfo(info);
		}
		LSPShipment lspShipment = builder.build();
		
		if(plan.getLsp() != null) {
			plan.getLsp().assignShipmentToSolution(lspShipment, plan.getSolutionId());
			plan.getShipment().setLSPShipment(lspShipment);
		}	
	}

	public void setLSPS(Collection<LSPDecorator> lsps) {
		this.lsps = lsps;
	}
	
	public void setDemandObject (DemandObject demandObject) {
		this.demandObject = demandObject;
	}
	
	public DemandObject getDemandObject () {
		return demandObject;
	}
}
