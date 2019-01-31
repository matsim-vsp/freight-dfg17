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
package receiver.collaboration;

import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.utils.objectattributes.attributable.Attributable;

import receiver.Receivers;

/**
 *
 * @author jwjoubert, wlbean
 */
public interface ReceiverCarrierCostAllocation
//	  extends Attributable // not a data class.  not clear why it should implement "Attributable".  kai, jan'19
{
	
	/**
	 * This method is provided with a complete { MutableFreightScenario} with its
	 * {@link Carriers} and {@link Receivers}. The coalition costs are calculated
	 * and assigned to the <i>same</i> coalition members. The same container 
	 * is then returned with the (possibly adjusted) costs.
	 *  
	 */
	public void allocateCoalitionCosts();
	
//	public String getDescription();
	// I don't think that this is helpful at this point.  kai, jan'19
	
}
