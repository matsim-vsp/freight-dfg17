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
  
/**
 * This class aims to keep track of all the different attributes names that are
 * used throughout the contrib.
 * 
 * @author jwjoubert, wlbean
 */
public enum ReceiverAttributes {
	// yy these enum types are used as names for {@link Attributable}, including file i/o.  --> DO NOT CHANGE.  Consider replacing
	// by normal string constants.  kai, jan'19
	collaborationStatus,
	grandCoalitionMember
	
}
