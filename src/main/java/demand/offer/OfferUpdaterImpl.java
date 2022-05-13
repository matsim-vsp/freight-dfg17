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

package demand.offer;

import java.util.ArrayList;
import java.util.Collection;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import lsp.LogisticsSolution;

public class OfferUpdaterImpl implements OfferUpdater{

	private LSPDecorator  lsp;
	private final Collection <OfferVisitor> visitors;
	
	public OfferUpdaterImpl() {
		this.visitors = new ArrayList<>();
	}
	
	
	@Override
	public void updateOffers() {
		for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
			if(solution instanceof LogisticsSolutionDecorator) {
				LogisticsSolutionDecorator offerSolution = (LogisticsSolutionDecorator) solution;
				for(OfferVisitor visitor : visitors) {
					if(visitor.getLogisticsSolution() == solution) {
						for(Offer offer : offerSolution.getOfferFactory().getOffers()) {
							if(offer.getClass() == visitor.getOfferClass()) {
								visitor.visit(offer);
							}
						}
					}
				}
			}
		}
		
	}

	@Override
	public Collection<OfferVisitor> getOfferVisitors() {
		return visitors;
	}

	public void setLSP(LSPDecorator  lsp) {
		this.lsp = lsp;
	}
}
