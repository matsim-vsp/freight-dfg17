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

package lsp.usecase;

import lsp.shipment.*;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.Tour;
import org.matsim.contrib.freight.carrier.Tour.ServiceActivity;
import org.matsim.contrib.freight.carrier.Tour.TourElement;

import org.matsim.contrib.freight.events.LSPTourStartEvent;
import org.matsim.contrib.freight.events.eventhandler.LSPTourStartEventHandler;
import lsp.LogisticsSolutionElement;
import lsp.LSPCarrierResource;

/*package-private*/ class MainRunTourStartEventHandler implements LSPTourStartEventHandler {

	private final LSPShipment lspShipment;
	private final CarrierService carrierService;
	private final LogisticsSolutionElement solutionElement;
	private final LSPCarrierResource resource;
	
	
	public MainRunTourStartEventHandler(LSPShipment lspShipment, CarrierService carrierService, LogisticsSolutionElement solutionElement, LSPCarrierResource resource){
		this.lspShipment=lspShipment;
		this.carrierService=carrierService;
		this.solutionElement=solutionElement;
		this.resource=resource;
	}
	
	
	@Override
	public void reset(int iteration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(LSPTourStartEvent event) {
		for(TourElement tourElement : event.getTour().getTourElements()){
			if(tourElement instanceof ServiceActivity){
				ServiceActivity serviceActivity = (ServiceActivity) tourElement;
				if(serviceActivity.getService().getId() == carrierService.getId() && event.getCarrierId() == resource.getCarrier().getId()){
					logLoad(event);
					logTransport(event);
				}
			}
		}

	}

	private void logLoad(LSPTourStartEvent event){
		ShipmentUtils.LoggedShipmentLoadBuilder builder = ShipmentUtils.LoggedShipmentLoadBuilder.newInstance();
		builder.setCarrierId(event.getCarrierId());
		builder.setLinkId(event.getTour().getStartLinkId());
		double startTime = event.getTime() - getCumulatedLoadingTime(event.getTour());
		builder.setStartTime(startTime);
		builder.setEndTime(event.getTime());
		builder.setLogisticsSolutionElement(solutionElement);
		builder.setResourceId(resource.getId());
		ShipmentPlanElement loggedShipmentLoad = builder.build();
		String idString = loggedShipmentLoad.getResourceId() + "" + loggedShipmentLoad.getSolutionElement().getId() + "" + loggedShipmentLoad.getElementType();
		Id<ShipmentPlanElement> loadId = Id.create(idString, ShipmentPlanElement.class);
		lspShipment.getLog().addPlanElement(loadId, loggedShipmentLoad);
	}

	private double getCumulatedLoadingTime(Tour tour){
		double cumulatedLoadingTime = 0;
		for(TourElement tourElement : tour.getTourElements()){
			if(tourElement instanceof ServiceActivity){
				ServiceActivity serviceActivity = (ServiceActivity) tourElement;
				cumulatedLoadingTime = cumulatedLoadingTime + serviceActivity.getDuration();
			}
		}
		return cumulatedLoadingTime;
	}

	private void logTransport(LSPTourStartEvent event){
		ShipmentUtils.LoggedShipmentTransportBuilder builder = ShipmentUtils.LoggedShipmentTransportBuilder.newInstance();
		builder.setCarrierId(event.getCarrierId());
		builder.setFromLinkId(event.getTour().getStartLinkId());
		builder.setToLinkId(event.getTour().getEndLinkId());
		builder.setStartTime(event.getTime());
		builder.setLogisticsSolutionElement(solutionElement);
		builder.setResourceId(resource.getId());
		LoggedShipmentTransport transport = builder.build();
		String idString = transport.getResourceId() + "" + transport.getSolutionElement().getId() + "" + transport.getElementType();
		Id<ShipmentPlanElement> transportId = Id.create(idString, ShipmentPlanElement.class);
		lspShipment.getLog().addPlanElement(transportId, transport);
	}


	public LSPShipment getLspShipment() {
		return lspShipment;
	}


	public CarrierService getCarrierService() {
		return carrierService;
	}


	public LogisticsSolutionElement getSolutionElement() {
		return solutionElement;
	}


	public LSPCarrierResource getResource() {
		return resource;
	}

	
	
}
