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
public final class MutableFreightScenario implements FreightScenario {
	final private Logger log = Logger.getLogger(MutableFreightScenario.class);
	private Scenario sc;
	private Carriers carriers;
	private Receivers receivers;
	private int replanInterval;
	private MutableCoalition coalition;
	
	public MutableFreightScenario(Scenario sc, Carriers carriers) {
		this.sc = sc;
		this.carriers = carriers;
		
		/* Set optional containers */
		this.coalition = new MutableCoalition();
		
		/* FIXME There are still local variables that are not initialised, with 
		 * no clear interpretable default values. */
	}

	
	@Override
	public Scenario getScenario() {
		return this.sc;
	}

	
	@Override
	public Carriers getCarriers() {
		return ReceiverUtils.getCarriers( sc );
	}

	public void setReceivers(Receivers receivers) {
		ReceiverUtils.setReceivers( receivers, sc );
	}
	
	public void setReplanInterval(int interval){
		ReceiverUtils.setReplanInterval( interval, sc );
	}
	
	@Override
	public Receivers getReceivers() {
		return ReceiverUtils.getReceivers( sc ) ;
	}


	@Override
	public int getReplanInterval() {
		return ReceiverUtils.getReplanInterval( sc ) ;
	}


	public void setCarriers(Carriers carriers) {
		ReceiverUtils.setCarriers( carriers, sc );
	}


	public void setCoalition(MutableCoalition coalition) {
		ReceiverUtils.setCoalition( coalition, sc );
	}
	
	public Coalition getCoalition(){
		return ReceiverUtils.getCoalition( sc ) ;
	}
	

}
