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

package testMutualreplanningWithOfferUpdate;

import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.offer.Offer;
import demand.offer.OfferVisitor;
import example.lsp.simulationTrackers.LinearOffer;
import lsp.LogisticsSolution;

public class LinearOfferVisitor implements OfferVisitor {

	private final LogisticsSolutionDecorator solution;
	
	public LinearOfferVisitor(LogisticsSolutionDecorator solution) {
		this.solution = solution;
	}
	
	
	@Override
	public void visit(Offer offer) {
		if(offer instanceof LinearOffer ) {
			LinearOffer linearOffer = (LinearOffer) offer;
			linearOffer.update();
		}		
	}

	@Override
	public Class<? extends Offer> getOfferClass(){
		return LinearOffer.class;
	}

	@Override
	public LogisticsSolution getLogisticsSolution() {
		return solution;
	}

}
