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

package demand.demandObject;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.TimeWindow;

import lsp.shipment.LSPShipment;



public interface ShipperShipment {

	Id<ShipperShipment> getId();
	double getShipmentSize();
	TimeWindow getStartTimeWindow();
	void setStartTimeWindow(TimeWindow timeWindow);
	TimeWindow getEndTimeWindow();
	void setEndTimeWindow(TimeWindow timeWindow);
	double getServiceTime();
	void setLSPShipment(LSPShipment lspShipment);
	LSPShipment getLSPShipment();
	void setDemandObject(DemandObject demandObject);
	DemandObject getDemandObject();
	
}
