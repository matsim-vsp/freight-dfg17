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
import org.matsim.core.controler.listener.ReplanningListener;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;

public abstract class MutualReplanningModule implements ReplanningListener{

	protected final Collection<LSPDecorator> lsps;
	protected final Collection<DemandObject> demandObjects;
	
	public MutualReplanningModule(Collection<LSPDecorator> lsps, Collection<DemandObject> demandObjects) {
		this.lsps = lsps;
		this.demandObjects = demandObjects;
	}
	
	public void notifyReplanning(ReplanningEvent event) {
		replan(event);
	}
	
	public void replan(ReplanningEvent arg0) {
		replanLSPs(arg0);
		replanDemandObjects(arg0, lsps);
	}
	
	abstract void replanLSPs(ReplanningEvent event);
	
	abstract void replanDemandObjects(ReplanningEvent event, Collection<LSPDecorator> lsps);
	
}
	

