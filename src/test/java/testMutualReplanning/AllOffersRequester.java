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

import java.util.ArrayList;
import java.util.Collection;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.demandObject.DemandObject;
import demand.demandObject.OfferRequester;
import demand.offer.Offer;

public class AllOffersRequester implements OfferRequester{

	private DemandObject demandObject;
	
	public AllOffersRequester() {
		
	}
	
	@Override
	public Collection<Offer> requestOffers(Collection<LSPDecorator> lsps) {
		ArrayList<Offer> offers = new ArrayList<>();
		for(LSPDecorator lsp : lsps) {
			for(LogisticsSolutionDecorator solution : lsp.getSelectedPlan().getSolutionDecorators()) {
				offers.add(lsp.getOffer(demandObject, "linear", solution.getId()));
			}
		}
		return offers;
	}

	@Override
	public void setDemandObject(DemandObject demandObject) {
		this.demandObject = demandObject;
	}

}
