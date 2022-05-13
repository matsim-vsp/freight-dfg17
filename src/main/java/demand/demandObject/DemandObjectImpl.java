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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import demand.DemandAgent;
import demand.mutualReplanning.DemandReplanner;
import demand.scoring.DemandScorer;
import demand.UtilityFunction;
import lsp.LSPInfo;
import lsp.shipment.Requirement;
import org.matsim.utils.objectattributes.attributable.Attributes;

public class DemandObjectImpl implements DemandObject{

	private final Id<DemandObject> id;
	private final ArrayList<DemandPlan> plans;
	private final double strengthOfFlow;
	private final Id<Link> fromLinkId;
	private final Id<Link> toLinkId;
	//	private ArrayList <UtilityFunction> utilityFunctions;
	private DemandPlan selectedPlan;
	private DemandScorer scorer;
	private final DemandReplanner replanner;
	private final Collection<Requirement> requirements;
	private final OfferRequester offerRequester;
	private final DemandPlanGenerator generator;
	private final Collection<LSPInfo> infos;
	private final Attributes attributes = new Attributes();

	public static class Builder{
		private DemandAgent shipper;
		private DemandAgent recipient;
		private final Id<DemandObject> id;
		private double strengthOfFlow;
		private Id<Link> fromLinkId;
		private Id<Link> toLinkId;
		private final ArrayList <UtilityFunction> utilityFunctions;
		private DemandPlan initialPlan;
		private DemandScorer scorer;
		private DemandReplanner replanner;
		private final Collection<Requirement> requirements;
		private OfferRequester offerRequester;
		private DemandPlanGenerator generator;
		private final Collection<LSPInfo> infos;

		public static Builder newInstance(Id<DemandObject> id) {
			return new Builder(id);
		}

		private Builder(Id<DemandObject> id) {
			this.id = id;
			this.requirements = new ArrayList<>();
			this.utilityFunctions = new ArrayList<>();
			this.infos = new ArrayList<>();
		}

		public void setShipper(DemandAgent shipper) {
			this.shipper = shipper;
		}

		public void setRecipient(DemandAgent recipient) {
			this.recipient = recipient;
		}


		public void setInitialPlan(DemandPlan plan){
			this.initialPlan = plan;
		}

		public void setStrengthOfFlow(double strength){
			this.strengthOfFlow = strength;
		}

		public void setFromLinkId(Id<Link> fromLinkId){
			this.fromLinkId = fromLinkId;
		}

		public void setToLinkId(Id<Link> toLinkId){
			this.toLinkId = toLinkId;
		}

		public void setOfferRequester(OfferRequester offerRequester){
			this.offerRequester = offerRequester;
		}

		public void setDemandPlanGenerator(DemandPlanGenerator generator){
			this.generator = generator;
		}

		public Builder addUtilityFunction(UtilityFunction utilityFunction) {
			this.utilityFunctions.add(utilityFunction);
			return this;
		}

		public void addRequirement(Requirement requirement) {
			this.requirements.add(requirement);
		}

		public Builder addInfo(LSPInfo info) {
			this.infos.add(info);
			return this;
		}

		public void setScorer(DemandScorer scorer) {
			this.scorer = scorer;
		}

		public void setReplanner(DemandReplanner replanner) {
			this.replanner = replanner;
		}

		public DemandObject build() {
			return new DemandObjectImpl(this);
		}

	}

	private DemandObjectImpl(Builder builder) {
		this.plans = new ArrayList<>();
//		this.utilityFunctions = new ArrayList<UtilityFunction>();
		DemandAgent shipper = builder.shipper;
		if(shipper != null) {
			shipper.getDemandObjects().add(this);
		}
		DemandAgent recipient = builder.recipient;
		if(recipient != null) {
			recipient.getDemandObjects().add(this);
		}
		this.id = builder.id;
		this.strengthOfFlow = builder.strengthOfFlow;
		this.fromLinkId = builder.fromLinkId;
		this.toLinkId = builder.toLinkId;
		this.selectedPlan=builder.initialPlan;
		if(this.selectedPlan != null) {
			this.selectedPlan.setDemandObject(this);
			this.selectedPlan.getShipment().setDemandObject(this);
		}
		this.plans.add(builder.initialPlan);
//		this.utilityFunctions = builder.utilityFunctions;
		this.scorer = builder.scorer;
		if(this.scorer != null) {
			this.scorer.setDemandObject(this);
		}
		this.replanner = builder.replanner;
		if(this.replanner != null) {
			this.replanner.setDemandObject(this);
		}
		this.requirements = builder.requirements;
		this.offerRequester = builder.offerRequester;
		if(this.offerRequester != null) {
			this.offerRequester.setDemandObject(this);
		}
		this.infos = builder.infos;
		this.generator = builder.generator;
		if(this.generator != null) {
			generator.setDemandObject(this);
		}
	}


	@Override
	public boolean addPlan(DemandPlan plan) {
		return plans.add(plan);
	}

	@Override
	public boolean removePlan(DemandPlan plan) {
		if(plans.contains(plan)) {
			plans.remove(plan);
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public DemandPlan getSelectedPlan() {
		return selectedPlan;
	}

	@Override
	public void setSelectedPlan(DemandPlan selectedPlan) {
		if(!plans.contains(selectedPlan)) plans.add(selectedPlan);
		this.selectedPlan = selectedPlan;
	}

	@Override
	public DemandPlan createCopyOfSelectedPlanAndMakeSelected() {
		DemandPlan newPlan = DemandObjectImpl.copyPlan(this.selectedPlan) ;
		this.setSelectedPlan( newPlan ) ;
		return newPlan;
	}

//	@Override
//	public DemandAgent getShipper() {
//		return shipper;
//	}
//
//	@Override
//	public DemandAgent getRecipient() {
//		return recipient;
//	}

	@Override
	public Id<DemandObject> getId() {
		return id;
	}

	@Override
	public List<? extends DemandPlan> getPlans() {
		return plans;
	}

	@Override
	public double getStrengthOfFlow() {
		return strengthOfFlow;
	}

	@Override
	public Id<Link> getFromLinkId() {
		return fromLinkId;
	}

	@Override
	public Id<Link> getToLinkId() {
		return toLinkId;
	}

//	@Override
//	public Collection<UtilityFunction> getUtilityFunctions() {
//		return utilityFunctions;
//	}

	private static DemandPlan copyPlan(DemandPlan plan2copy) {
		DemandPlanImpl.Builder builder = DemandPlanImpl.Builder.newInstance();
		builder.setLogisticsSolutionId(plan2copy.getSolutionId());
		builder.setLsp(plan2copy.getLsp());
		builder.setShipperShipment(plan2copy.getShipment());
		DemandPlan copiedPlan = builder.build();
		copiedPlan.setScore(plan2copy.getScore());
		return copiedPlan;
	}

	@Override
	public void scoreSelectedPlan() {
		double score = scorer.scoreCurrentPlan(this);
		this.selectedPlan.setScore(score);
	}

	@Override
	public void setScorer(DemandScorer scorer) {
		this.scorer = scorer;
	}

	@Override
	public DemandScorer getScorer() {
		return scorer;
	}

	@Override
	public DemandReplanner getReplanner() {
		return replanner;
	}

//	@Override
//	public void setReplanner(DemandReplanner replanner) {
//		this.replanner = replanner;
//	}
//
//	@Override
//	public void setOfferRequester(OfferRequester requester) {
//		this.offerRequester = requester;
//	}

	@Override
	public OfferRequester getOfferRequester() {
		return offerRequester;
	}

//	@Override
//	public void setDemandPlanGenerator(DemandPlanGenerator generator) {
//		this.generator = generator;
//	}

	@Override
	public DemandPlanGenerator getDemandPlanGenerator() {
		return generator;
	}

	@Override
	public Collection<Requirement> getRequirements() {
		return requirements;
	}

	@Override
	public Collection<LSPInfo> getInfos() {
		return infos;
	}

	@Override
	public Attributes getAttributes() {
		return attributes;
	}
}
