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

import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.replanning.GenericStrategyManager;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LSPWithOffers;
import demand.offer.OfferUpdater;
import lsp.LSP;
import lsp.LSPPlan;
import lsp.replanning.LSPReplanner;

public class LSPWithOffersReplanner implements LSPReplanner{

	private LSPDecorator lsp;
	private GenericStrategyManager<LSPPlan, LSP> strategyManager;
	private OfferUpdater offerUpdater;
	
	public LSPWithOffersReplanner(LSPDecorator  lsp) {
		this.lsp = lsp;
	}
	
	public LSPWithOffersReplanner() {
		
	}
			
	@Override
	public void replan(ReplanningEvent event) {
		if(strategyManager != null) {
			ArrayList<LSP> lspList = new ArrayList<>();
			lspList.add(lsp);
			strategyManager.run(lspList, null, event.getIteration(), event.getReplanningContext());
		}
		if(offerUpdater != null) {
			offerUpdater.updateOffers();
		}
	}

	@Override
	public GenericStrategyManager<LSPPlan, LSP> getStrategyManager() {
		return strategyManager;
	}

	@Override
	public void setStrategyManager(GenericStrategyManager<LSPPlan, LSP> strategyManager) {
		this.strategyManager = strategyManager;
	}

	public void setOfferUpdater(OfferUpdater offerUpdater) {
		this.offerUpdater = offerUpdater;
		offerUpdater.setLSP(lsp);
	}

	public OfferUpdater getOfferUpdater() {
		return offerUpdater;
	}

	@Override
	public void setLSP(LSP lsp) {
		try {
			this.lsp = (LSPWithOffers) lsp;
			if(this.lsp.getOfferUpdater() != null) {
				this.offerUpdater = this.lsp.getOfferUpdater();
			}
		}
		catch(ClassCastException e) {
			System.out.println("The class " + this + " expects an LSPWithOffers and not any other implementation of LSP");
			System.exit(1);
		}
	}
	
	public void setLSP(LSPDecorator lsp) {
		this.lsp = lsp;
		if(this.lsp.getOfferUpdater() != null) {
			this.offerUpdater = this.lsp.getOfferUpdater();
		}
	}
}
