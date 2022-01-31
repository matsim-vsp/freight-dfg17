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
package receiver.product;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.api.core.v01.network.Link;
import org.matsim.utils.objectattributes.attributable.Attributable;

import receiver.Receiver;

/**
 * General interface for different product types.
 * 
 * @author wlbean, jwjoubert
 */
public interface ProductType extends Identifiable<ProductType>, Attributable{	
	
	void setDescription(String description);

	void setRequiredCapacity(double reqCapacity);

	String getDescription();

	double getRequiredCapacity();
	
	/**
	 * TODO The origin of a product is currently (Dec '18, JWJ) associated with
	 * the {@link ProductType}, assuming it comes from the same source for all
	 * {@link Receiver}s. In future this might change to rather be associated
	 * with the {@link ReceiverProduct}. 
	 * @return
	 */
	Id<Link> getOriginLinkId();
	
//	public void setOriginLinkId(Id<Link> originLinkId);
}
