/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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
  
package receiver.usecases;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.CarrierVehicleType;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.scoring.CarrierScoringFunctionFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

public class UsecasesUtils {
	
	public static CarrierScoringFunctionFactory getCarrierScoringFunctionFactory(Network network) {
		return new UsecasesCarrierScoringFunctionFactoryImpl(network);
	}
	
	
	/**
	 * This is a copy from the (now package-protected) 
	 * 
	 * org.matsim.contrib.freight.usecases.chessboard.TravelDisutilities
	 * 
	 * @param vehicleTypes
	 * @param travelTime
	 * @return
	 */
	public static TravelDisutility createBaseDisutility(final CarrierVehicleTypes vehicleTypes, final TravelTime travelTime){
		
		return new TravelDisutility() {

			@Override
			public double getLinkTravelDisutility(Link link, double time, Person person, org.matsim.vehicles.Vehicle vehicle) {
				CarrierVehicleType type = vehicleTypes.getVehicleTypes().get(vehicle.getType().getId());
				if(type == null) throw new IllegalStateException("vehicle "+vehicle.getId()+" has no type");
				double tt = travelTime.getLinkTravelTime(link, time, person, vehicle);
				return type.getVehicleCostInformation().perDistanceUnit*link.getLength() + type.getVehicleCostInformation().perTimeUnit*tt;
			}

			@Override
			public double getLinkMinimumTravelDisutility(Link link) {
				double minDisutility = Double.MAX_VALUE;
				double free_tt = link.getLength()/link.getFreespeed();
				for(CarrierVehicleType type : vehicleTypes.getVehicleTypes().values()){
					double disu = type.getVehicleCostInformation().perDistanceUnit*link.getLength() + type.getVehicleCostInformation().perTimeUnit*free_tt;
					if(disu < minDisutility) minDisutility=disu;
				}
				return minDisutility;
			}
		};
	}

}
