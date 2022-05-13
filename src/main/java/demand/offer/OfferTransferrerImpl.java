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

import org.matsim.api.core.v01.Id;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.demandObject.DemandObject;
import lsp.LogisticsSolution;

public class OfferTransferrerImpl implements OfferTransferrer{

	private LSPDecorator lsp;
	
	
	@Override
	public Offer transferOffer(DemandObject object, String type, Id<LogisticsSolution> solutionId) {
		for(LogisticsSolution planSolution : lsp.getSelectedPlan().getSolutions()) {
			if(planSolution instanceof LogisticsSolutionDecorator) {
				LogisticsSolutionDecorator offerSolution = (LogisticsSolutionDecorator) planSolution;
				if(offerSolution.getId() == solutionId) {
					if(!(offerSolution.getOffer(object, type) instanceof DefaultOfferImpl)) {
						return offerSolution.getOffer(object, type);
					}
				}			
			}
		}	
		return new DefaultOfferImpl(lsp, null);
	}

	public void setLSP(LSPDecorator lsp){
			this.lsp = lsp;
	}
	
	public LSPDecorator getLSP(){
		return lsp;
	}
}

