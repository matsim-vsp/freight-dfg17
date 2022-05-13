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

import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import lsp.LSP;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.shipment.LSPShipment;

/*package-private*/ class SupplyClearer implements IterationEndsListener{
	
	private final LSPDecorators lsps;
	
	SupplyClearer(LSPDecorators lsps) {
		this.lsps = lsps;
	}
					
	@Override
	public void notifyIterationEnds(IterationEndsEvent arg0) {
			for(LSP lsp : lsps.getLSPs().values()){
				lsp.getShipments().clear();
				for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
					solution.getShipments().clear();
					for(LogisticsSolutionElement element : solution.getSolutionElements()) {
						element.getIncomingShipments().clear();
						element.getOutgoingShipments().clear();
					}
				}	
			
				for(LSPShipment shipment : lsp.getShipments()) {
					shipment.getShipmentPlan().clear();
					shipment.getLog().clear();
					lsp.getSelectedPlan().getAssigner().assignToSolution(shipment); //Can also be left out, as the DefaultAssigner does nothing.
				}
			
			}		
	}
						
}
