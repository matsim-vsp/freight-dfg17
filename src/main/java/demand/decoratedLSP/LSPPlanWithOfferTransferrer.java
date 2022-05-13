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

import demand.offer.OfferTransferrer;
import lsp.LSP;
import lsp.LogisticsSolution;
import lsp.ShipmentAssigner;

public class LSPPlanWithOfferTransferrer implements LSPPlanDecorator{

	private LSPDecorator lsp;
	private double score;
	private final Collection<LogisticsSolutionDecorator> solutions;
	private final ShipmentAssigner assigner;
	private OfferTransferrer transferrer;
	
	public LSPPlanWithOfferTransferrer() {
		this.solutions = new ArrayList<>();
		this.assigner = new DefaultAssigner();
	}
	
	@Override
	public LSPPlanDecorator addSolution(LogisticsSolution solution) {
		try {
			LogisticsSolutionDecorator solutionDecorator = (LogisticsSolutionDecorator) solution;
			this.solutions.add(solutionDecorator);
			solution.setLSP(this.lsp);	
		}
		catch(ClassCastException e) {
			System.out.println("The class " + this + " expects an LogisticsSolutionDecorator and not any other implementation of LogisticsSolution");
			System.exit(1);
		}
		return this;
	}

	@Override
	public Collection<LogisticsSolution> getSolutions() {
		Collection<LogisticsSolution> solutionDecorators = new ArrayList<>();
		for(LogisticsSolution  solution : solutions) {
			LogisticsSolutionDecorator solutionDecorator = (LogisticsSolutionDecorator) solution;
			solutionDecorators.add(solutionDecorator);
		}
		return solutionDecorators;
	}

	@Override
	public ShipmentAssigner getAssigner() {
		return assigner;
	}

	@Override
	public LSPPlanDecorator setAssigner(ShipmentAssigner assigner) {
		//Has to be empty, DefaultAssigner is already in place.
		return this;
	}

	@Override
	public void setLSP(LSP lsp) {
		try {
			this.lsp = (LSPDecorator) lsp;
		}
		catch(ClassCastException e) {
			System.out.println("The class " + this + " expects an LSPDecorator and not any other implementation of LSP");
			System.exit(1);
		}
	}

	@Override
	public LSPDecorator getLsp() {
		return lsp;
	}

	@Override
	public void setScore(Double score) {
		this.score = score;
	}

	@Override
	public Double getScore() {
		return score;
	}

	public OfferTransferrer getOfferTransferrer() {
		return transferrer;
	}

	public void setOfferTransferrer(OfferTransferrer offerTransferrer) {
		this.transferrer = offerTransferrer;
		this.transferrer.setLSP(lsp);
	}

	@Override
	public Collection<LogisticsSolutionDecorator> getSolutionDecorators() {
		Collection<LogisticsSolutionDecorator> solutionDecorators = new ArrayList<>();
		for(LogisticsSolution  solution : solutions) {
			LogisticsSolutionDecorator solutionDecorator = (LogisticsSolutionDecorator) solution;
			solutionDecorators.add(solutionDecorator);
		}
		return solutionDecorators;
	}

	@Override
	public void addSolution(LogisticsSolutionDecorator solution) {
		this.solutions.add(solution);
	}

	@Override
	public void setLSP(LSPDecorator lsp) {
		this.lsp = lsp;
	}
	
}
