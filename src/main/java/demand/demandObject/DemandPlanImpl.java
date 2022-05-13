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

import demand.decoratedLSP.LSPDecorator;
import lsp.LogisticsSolution;


public class DemandPlanImpl implements DemandPlan{

	private double score;
	private final ShipperShipment shipment;
	private final LSPDecorator lsp;
	private final Id<LogisticsSolution> solutionId;
	private DemandObject demandObject;
	
	public static class Builder{
		private ShipperShipment shipment;
		private LSPDecorator lsp;
		private Id<LogisticsSolution> solutionId;
		private DemandObject demandObject;
		
		public static Builder newInstance(){
			return new Builder();
		}
	
		private Builder(){
		}
		
		public void setShipperShipment(ShipperShipment shipment){
			this.shipment = shipment;
		}
		
		public void setLsp(LSPDecorator  lsp){
			this.lsp = lsp;
		}
		
		public void setDemandObject(DemandObject  demandObject){
			this.demandObject = demandObject;
		}
		
		public void setLogisticsSolutionId(Id<LogisticsSolution> solutionId){
			this.solutionId = solutionId;
		}
		
		public DemandPlanImpl build() {
			return new DemandPlanImpl(this);
		}
		
	}
	
	private DemandPlanImpl(Builder builder) {
		this.shipment = builder.shipment;
		this.lsp = builder.lsp;
		this.solutionId = builder.solutionId;
		this.demandObject = builder.demandObject;
	}
	
	@Override
	public Double getScore() {
		return score;
	}

	@Override
	public void setScore(Double arg0) {
		this.score = arg0;		
	}

	public ShipperShipment getShipment() {
		return shipment;
	}

	public LSPDecorator getLsp() {
		return lsp;
	}

	public Id<LogisticsSolution> getSolutionId() {
		return solutionId;
	}

	public DemandObject getDemandObject() {
		return demandObject;
	}

	public void setDemandObject(DemandObject demandObject) {
		this.demandObject = demandObject;
	}

}
