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

package lsp;

import java.util.Collection;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.HasPlansAndId;

import lsp.replanning.LSPReplanner;
import lsp.scoring.LSPScorer;
import lsp.shipment.LSPShipment;
import org.matsim.core.controler.events.ReplanningEvent;

/**
 *  In the class library, the interface LSP has the following tasks:
 * 1. Maintain one or several transport chains through which {@link LSPShipment}s are routed.
 * 2. Assign {@link LSPShipment}s to the suitable transport chain. --> {@link ShipmentAssigner}.
 * 3. Interact with the agents that embody the demand side of the freight transport market, if they are specified in the setting.
 * 4. Coordinate carriers that are in charge of the physical transport.
 */
public interface LSP extends HasPlansAndId<LSPPlan,LSP>{
	
	/**
	 * @return
	 *
	 * ok
	 */
	Id<LSP> getId();
	
	/**
	 * @return
	 *
	 * yyyy does this have to be exposed?
	 */
	Collection<LSPShipment> getShipments();
	
	/**
	 * ok (behavioral method)
	 */
	void scheduleSolutions();
	

	/**
	 * @return
	 *
	 * yyyy does this have to be exposed?
	 */
	Collection<LSPResource> getResources();


	/**
	 * ok (behavioral method)
	 */
	void scoreSelectedPlan();
	

	/**
	 * @param shipment
	 *
	 * ok (LSP needs to be told that it is responsible for shipment)
	 */
	void assignShipmentToLSP( LSPShipment shipment );
	
	void replan( ReplanningEvent arg0 );

	
	/**
	 * @param scorer
	 *
	 * yyyy does it make sense to expose this (implies that scorer can be changed during iterations)?
	 */
	void setScorer( LSPScorer scorer );
	
	/**
	 * @param replanner
	 *
	 * yyyy does it make sense to expose this (implies that replanner can be changed during iterations)?
	 */
	void setReplanner( LSPReplanner replanner );
	

}    
