/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
  
/**
 * 
 */
package receiver;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carriers;

import receiver.collaboration.Coalition;
import receiver.collaboration.MutableCoalition;

/**
 *
 * @author jwjoubert, wlbean
 */
public final class MutableFreightScenario {
	final private Logger log = Logger.getLogger( MutableFreightScenario.class);
	private Scenario sc;
	
	public MutableFreightScenario( Scenario sc, Carriers carriers) {
		this.sc = sc;
		ReceiverUtils.setCarriers(  carriers, sc ) ;
		
		/* Set optional containers */
		ReceiverUtils.setCoalition( new MutableCoalition(), sc ) ;
		
		/* FIXME There are still local variables that are not initialised, with 
		 * no clear interpretable default values. */
	}

	
	public Scenario getScenario() {
		return this.sc;
	}
	
	
}
