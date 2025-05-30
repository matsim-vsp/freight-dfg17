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


import lsp.shipment.LSPShipment;

public class ShipmentWithTime{
	private final LSPShipment shipment;
	private final double time;

	public ShipmentWithTime( double time , LSPShipment shipment ) {
		this.shipment= shipment;
		this.time = time;
	}

	public LSPShipment getShipment() {
		return shipment;
	}

	public double getTime() {
		return time;
	}

}
