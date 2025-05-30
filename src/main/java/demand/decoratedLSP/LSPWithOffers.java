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

package demand.decoratedLSP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Id;

import demand.demandObject.DemandObject;
import demand.offer.Offer;
import demand.offer.OfferUpdater;
import lsp.LSP;
import lsp.LSPPlan;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.SolutionScheduler;
import lsp.replanning.LSPReplanner;
import lsp.LSPResource;
import lsp.scoring.LSPScorer;
import lsp.shipment.LSPShipment;
import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.utils.objectattributes.attributable.Attributes;

public class LSPWithOffers implements LSPDecorator {

	private final Id<LSP> id;
	private final Collection<LSPShipment> shipments;
	private final ArrayList<LSPPlan> plans;
	private final SolutionScheduler solutionScheduler;
	private LSPPlanDecorator selectedPlan;
	private final Collection<LSPResource> resources;
	private LSPScorer scorer;
	private LSPReplanner replanner;
	private OfferUpdater offerUpdater;
	private final Attributes attributes = new Attributes();

	@Override
	public Attributes getAttributes() {
		return attributes;
	}

	public static class Builder{
		private Id<LSP> id;
		private SolutionScheduler solutionScheduler;
		private LSPPlanDecorator initialPlan;
		private final Collection<LSPResource> resources;
		private LSPScorer scorer;
		private LSPReplanner replanner;
		private OfferUpdater offerUpdater;


		public static Builder newInstance(){
			return new Builder();
		}

		private Builder(){
			this.resources = new ArrayList<>();

		}

		public void setSolutionScheduler(SolutionScheduler solutionScheduler){
			this.solutionScheduler = solutionScheduler;
		}

		public Builder setSolutionScorer(LSPScorer scorer){
			this.scorer = scorer;
			return this;
		}

		public Builder setReplanner(LSPReplanner replanner){
			this.replanner= replanner;
			return this;
		}

		public Builder setOfferUpdater(OfferUpdater offerUpdater){
			this.offerUpdater= offerUpdater;
			return this;
		}

		public void setInitialPlan(LSPPlanDecorator plan){
			this.initialPlan = plan;
			for(LogisticsSolution solution : plan.getSolutions()) {
				for(LogisticsSolutionElement element : solution.getSolutionElements()) {
					if(!resources.contains(element.getResource())) {
						resources.add(element.getResource());
					}
				}
			}
		}

		public void setId(Id<LSP> id){
			this.id = id;
		}

		public LSPWithOffers build(){
			return new LSPWithOffers(this);
		}

	}

	private LSPWithOffers(LSPWithOffers.Builder builder){
		this.shipments = new ArrayList<>();
		this.plans= new ArrayList<>();
		this.id = builder.id;
		this.solutionScheduler = builder.solutionScheduler;
		this.solutionScheduler.setLSP(this);
		builder.initialPlan.setLSP(this);
		this.selectedPlan=builder.initialPlan;
		this.plans.add(builder.initialPlan);
		this.selectedPlan.setLSP(this);
		this.selectedPlan.getOfferTransferrer().setLSP(this);
		this.resources = builder.resources;
		this.scorer = builder.scorer;
		if(this.scorer != null) {
			this.scorer.setLSP(this);
		}
		this.replanner = builder.replanner;
		if(this.replanner  != null) {
			this.replanner .setLSP(this);
		}
		this.offerUpdater = builder.offerUpdater;
		if(offerUpdater != null) {
			offerUpdater.setLSP(this);
		}
	}

	@Override
	public Id<LSP> getId() {
		return id;
	}

	@Override
	public Collection<LSPShipment> getShipments() {
		return shipments;
	}

	@Override
	public void scheduleSolutions() {
		solutionScheduler.scheduleSolutions();
	}

	@Override
	public ArrayList<LSPPlan> getPlans() {
		for(LSPPlan plan : plans) {
			plan.setLSP(this);
			for(LogisticsSolution solution : plan.getSolutions()) {
				if(solution instanceof LogisticsSolutionDecoratorImpl_wOffers) {
					LogisticsSolutionDecoratorImpl_wOffers solutionWithOffers = (LogisticsSolutionDecoratorImpl_wOffers) solution;
					solutionWithOffers.setLSP(this);
					solutionWithOffers.getOfferFactory().setLSP(this);
					for(Offer offer : solutionWithOffers.getOfferFactory().getOffers()) {
						offer.setLSP(this);
					}
				}
			}
		}
		return plans;
	}

	@Override
	public Collection<LSPResource> getResources() {
		return resources;
	}

	@Override
	public LSPPlanDecorator getSelectedPlan() {
		selectedPlan.setLSP(this);
		for(LogisticsSolution solution : selectedPlan.getSolutions()) {
			if(solution instanceof LogisticsSolutionDecoratorImpl_wOffers) {
				LogisticsSolutionDecoratorImpl_wOffers solutionWithOffers = (LogisticsSolutionDecoratorImpl_wOffers) solution;
				solutionWithOffers.setLSP(this);
				solutionWithOffers.getOfferFactory().setLSP(this);
			}
		}
		return selectedPlan;
	}

	@Override
	public void setSelectedPlan(LSPPlan plan) {
		try {
			plan.setLSP(this);
			this.selectedPlan = (LSPPlanDecorator) plan;
			for(LogisticsSolution solution : selectedPlan.getSolutions()) {
				if(solution instanceof LogisticsSolutionDecoratorImpl_wOffers) {
					LogisticsSolutionDecoratorImpl_wOffers solutionWithOffers = (LogisticsSolutionDecoratorImpl_wOffers) solution;
					solutionWithOffers.setLSP(this);
					solutionWithOffers.getOfferFactory().setLSP(this);
				}
			}
			if(!plans.contains(selectedPlan)) {
				plans.add(selectedPlan);
			}
		}
		catch(ClassCastException e) {
			System.out.println("The class " + this + " expects an LSPPlanDecorator and not any other implementation of LSPPlan");
			System.exit(1);
		}
	}

	@Override
	public void scoreSelectedPlan() {
		if(this.scorer != null) {
			double score = scorer.scoreCurrentPlan(this);
			this.selectedPlan.setScore(score);
		}
	}

	@Override
	public void assignShipmentToLSP(LSPShipment shipment) {
		this.shipments.add(shipment);
	}

	public void replan( final ReplanningEvent arg0 ) {
		if ( this.replanner!=null ) {
			this.replanner.replan( arg0 );
		}
	}

	//	@Override
//	public LSPScorer getScorer() {
//		return scorer;
//	}
//
	@Override
	public void setScorer(LSPScorer scorer) {
		this.scorer = scorer;
	}

	@Override
	public boolean addPlan(LSPPlan p) {
		try {
			LSPPlanDecorator plan = (LSPPlanDecorator) p;
			for(LogisticsSolution solution : plan.getSolutions()) {
				for(LogisticsSolutionElement element : solution.getSolutionElements()) {
					if(!resources.contains(element.getResource())) {
						resources.add(element.getResource());
					}
				}
			}
			return plans.add(plan);
		}
		catch(ClassCastException e) {
			System.out.println("The class " + this + " expects an LSPPlanDecorator and not any other implementation of LSPPlan");
			System.exit(1);
		}
		return false;
	}

	@Override
	public boolean removePlan(LSPPlan plan) {
		if(plans.contains(plan)) {
			plans.remove(plan);
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public LSPPlan createCopyOfSelectedPlanAndMakeSelected() {
		LSPPlanDecorator newPlan = LSPWithOffers.copyPlan(this.selectedPlan) ;
		this.setSelectedPlan( newPlan ) ;
		return newPlan ;
	}

	@Override
	public Offer getOffer(DemandObject object, String type, Id<LogisticsSolution> solutionId) {
		return selectedPlan.getOfferTransferrer().transferOffer(object, type, solutionId);
	}

	@Override
	public void assignShipmentToSolution(LSPShipment shipment, Id<LogisticsSolution> id) {
		assignShipmentToLSP(shipment);
		for(LogisticsSolution solution : selectedPlan.getSolutions()) {
			if(solution.getId() == id) {
				solution.assignShipment(shipment);
			}
		}
	}

//	@Override
//	public LSPReplanner getReplanner() {
//		return replanner;
//	}

	@Override
	public void setReplanner(LSPReplanner replanner) {
		this.replanner = replanner;
	}

	@Override
	public OfferUpdater getOfferUpdater() {
		return offerUpdater;
	}

	@Override
	public void setOfferUpdater(OfferUpdater offerUpdater) {
		this.offerUpdater = offerUpdater;
	}

	public static LSPPlanDecorator copyPlan(LSPPlanDecorator plan2copy) {
		List<LogisticsSolutionDecorator> copiedSolutions = new ArrayList<>();
		for (LogisticsSolution solution : plan2copy.getSolutions()) {
			LogisticsSolutionDecorator solutionDecorator = (LogisticsSolutionDecorator) solution;
			LogisticsSolutionDecorator copiedSolution = DecoratedLSPUtils.LogisticsSolutionDecoratorImpl_wOffersBuilder.newInstance(solutionDecorator.getId()).build();
			copiedSolution.getSolutionElements().addAll(solutionDecorator.getSolutionElements());
			copiedSolution.setOfferFactory(solutionDecorator.getOfferFactory());
			copiedSolutions.add(copiedSolution);
		}
		LSPPlanDecorator copiedPlan = new LSPPlanWithOfferTransferrer();
		copiedPlan.setOfferTransferrer(plan2copy.getOfferTransferrer());
		copiedPlan.setLSP(plan2copy.getLsp());
		double initialScoreOfCopiedPlan = plan2copy.getScore();
		copiedPlan.setScore(initialScoreOfCopiedPlan);
		copiedPlan.getSolutions().addAll(copiedSolutions);
		return copiedPlan;
	}

//	@Override
//	public SolutionScheduler getScheduler() {
//		return solutionScheduler;
//	}

}
