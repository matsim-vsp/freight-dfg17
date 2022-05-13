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

import demand.demandObject.DemandObject;
import demand.offer.Offer;
import demand.offer.OfferFactory;
import lsp.LogisticsSolution;

public interface LogisticsSolutionDecorator  extends LogisticsSolution {

	Offer getOffer(DemandObject object, String type);
	void setOfferFactory(OfferFactory factory);
	OfferFactory getOfferFactory();
	LSPDecorator getLSP();
	
}
