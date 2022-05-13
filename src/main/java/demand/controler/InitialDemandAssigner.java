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

package demand.controler;

import java.util.Collection;

import lsp.shipment.ShipmentUtils;
import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;
import demand.demandObject.DemandObjects;
import demand.demandObject.DemandPlan;
import demand.offer.Offer;
import lsp.LSPInfo;
import lsp.shipment.LSPShipment;

/*package-private*/ class InitialDemandAssigner implements StartupListener{

	private final DemandObjects demandObjects;
	private final LSPDecorators lsps;
	
	InitialDemandAssigner(DemandObjects demandObjects, LSPDecorators lsps) {
		this.demandObjects = demandObjects;
		this.lsps = lsps;
	}

	@Override
	public void notifyStartup(StartupEvent event) {
		for(DemandObject demandObject : demandObjects.getDemandObjects().values()) {
			if(demandObject.getSelectedPlan() == null) {
				createInitialPlan(demandObject);
			}
				assignShipmentToLSP(demandObject);
		}

		for(LSPDecorator lsp : lsps.getLSPs().values()) {
			lsp.scheduleSolutions();
		}
	}

	private void createInitialPlan(DemandObject demandObject) {
		Collection<Offer> offers = demandObject.getOfferRequester().requestOffers(lsps.getLSPs().values());
		DemandPlan initialPlan = demandObject.getDemandPlanGenerator().createDemandPlan(offers);
		demandObject.setSelectedPlan(initialPlan);	
	}
	
	private void assignShipmentToLSP(DemandObject demandObject) {
		Id<LSPShipment> id = Id.create(demandObject.getSelectedPlan().getShipment().getId(), LSPShipment.class);
		ShipmentUtils.LSPShipmentBuilder builder = ShipmentUtils.LSPShipmentBuilder.newInstance(id );
		builder.setFromLinkId(demandObject.getFromLinkId());
		builder.setToLinkId(demandObject.getToLinkId());
		builder.setCapacityDemand((int)demandObject.getSelectedPlan().getShipment().getShipmentSize());
		builder.setDeliveryServiceTime(demandObject.getSelectedPlan().getShipment().getServiceTime() );
		builder.setStartTimeWindow(demandObject.getSelectedPlan().getShipment().getStartTimeWindow());
		builder.setEndTimeWindow(demandObject.getSelectedPlan().getShipment().getEndTimeWindow());
		for(LSPInfo info : demandObject.getInfos()) {
			builder.addInfo(info);
		}
		LSPShipment lspShipment = builder.build();
		demandObject.getSelectedPlan().getLsp().assignShipmentToSolution(lspShipment, demandObject.getSelectedPlan().getSolutionId());
		demandObject.getSelectedPlan().getShipment().setLSPShipment(lspShipment);
	}
	
	
}
