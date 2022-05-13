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

import org.matsim.core.controler.events.ReplanningEvent;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;

public class MutualReplanningModuleImpl extends MutualReplanningModule{
	
	public MutualReplanningModuleImpl(Collection<LSPDecorator> lsps, Collection<DemandObject> demandObjects) {
		super(lsps, demandObjects);
	}
	
	@Override
	void replanLSPs(ReplanningEvent event) {
		for(LSPDecorator lsp : lsps) {
			lsp.replan(event);
			if(lsp.getOfferUpdater()!= null) {
				lsp.getOfferUpdater().updateOffers();
			}
		}
	}

	@Override
	void replanDemandObjects(ReplanningEvent event, Collection<LSPDecorator> lsps) {
		for(DemandObject demandObject : demandObjects) {
			if(demandObject.getReplanner()!= null) {
				demandObject.getReplanner().replan(lsps, event);
			}
		}
	}

}
