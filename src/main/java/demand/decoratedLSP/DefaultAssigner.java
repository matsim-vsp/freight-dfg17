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

package demand.decoratedLSP;

import lsp.LSP;
import lsp.ShipmentAssigner;
import lsp.shipment.LSPShipment;

/*package-private*/ class DefaultAssigner implements ShipmentAssigner{

//	private LSP lsp;

//	DefaultAssigner() {
//		this.lsp = lsp;
//	}
	
	@Override
	public void assignToSolution(LSPShipment shipment) {
		//Has to be empty, as an LSPWithOffers does not assign with the assigner. 
		//This job is done by the OfferTransferrer who gives the right solution in the offer
	}

	@Override
	public void setLSP(LSP lsp) {
//		this.lsp = lsp;
		
	}

//	@Override
//	public LSP getLSP() {
//		return lsp;
//	}

}
