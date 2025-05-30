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

package testSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import adapterTests.CollectionAdapterTest;
import adapterTests.DistributionAdapterTest;
import lsp.usecase.FirstReloadAdapterTest;
import adapterTests.MainRunAdapterTest;
import lsp.usecase.SecondReloadAdapterTest;
import demandObjectTests.DemandObjectBuilderTest;
import lspCreationTests.CollectionLSPCreationTest;
import lspCreationTests.CompleteLSPCreationTest;
import lspMobsimTests.CollectionLSPMobsimTest;
import lspMobsimTests.CompleteLSPMobsimTest;
import lspMobsimTests.FirstReloadLSPMobsimTest;
import lspMobsimTests.MainRunLSPMobsimTest;
import lspMobsimTests.MainRunOnlyLSPMobsimTest;
import lspMobsimTests.MultipleIterationsCollectionLSPMobsimTest;
import lspMobsimTests.MultipleIterationsFirstReloadLSPMobsimTest;
import lspMobsimTests.MultipleIterationsMainRunLSPMobsimTest;
import lspMobsimTests.MultipleIterationsSecondReloadLSPMobsimTest;
import lspMobsimTests.MultipleItreationsCompleteLSPMobsimTest;
import lspMobsimTests.MultipleShipmentsCollectionLSPMobsimTest;
import lspMobsimTests.MultipleShipmentsCompleteLSPMobsimTest;
import lspMobsimTests.MultipleShipmentsFirstReloadLSPMobsimTest;
import lspMobsimTests.MultipleShipmentsMainRunLSPMobsimTest;
import lspMobsimTests.MultipleShipmentsSecondReloadLSPMobsimTest;
import lspMobsimTests.RepeatedMultipleShipmentsCompleteLSPMobsimTest;
import lspMobsimTests.SecondReloadLSPMobsimTest;
import lspPlanTests.CollectionLSPPlanTest;
import lspPlanTests.CompleteLSPPlanTest;
import lspReplanningTests.CollectionLSPReplanningTest;
import lsp.usecase.CollectionLSPSchedulingTest;
import lsp.usecase.CompleteLSPSchedulingTest;
import lsp.usecase.FirstReloadLSPSchedulingTest;
import lsp.usecase.MainRunLSPSchedulingTest;
import lsp.usecase.MultipleShipmentsCollectionLSPSchedulingTest;
import lsp.usecase.MultipleShipmentsCompleteLSPSchedulingTest;
import lsp.usecase.MultipleShipmentsFirstReloadLSPSchedulingTest;
import lsp.usecase.MultipleShipmentsMainRunLSPSchedulingTest;
import lsp.usecase.MultipleShipmentsSecondReloadLSPSchedulingTest;
import lsp.usecase.SecondReloadLSPSchedulingTest;
import example.lsp.lspScoring.CollectionLSPScoringTest;
import example.lsp.lspScoring.MultipleIterationsCollectionLSPScoringTest;
import lspShipmentAssignmentTests.CollectionLSPShipmentAssigmentTest;
import lspShipmentAssignmentTests.CompleteLSPShipmentAssignerTest;
import lspShipmentTest.CollectionShipmentBuilderTest;
import lspShipmentTest.CompleteShipmentBuilderTest;
import lspShipmentTest.DistributionShipmentBuilderTest;
import example.lspAndDemand.requirementsChecking.AssignerRequirementsTest;
import example.lspAndDemand.requirementsChecking.TransferrerRequirementsTest;
import solutionElementTests.CollectionElementTest;
import solutionElementTests.DistributionElementTest;
import lsp.usecase.FirstReloadElementTest;
import solutionElementTests.MainRunElementTest;
import solutionElementTests.SecondReloadElementTest;
import solutionTests.CollectionSolutionTest;
import solutionTests.CompleteSolutionTest;
import example.lsp.simulationTrackers.CollectionTrackerTest;
import example.lsp.simulationTrackers.MutualReplanningTest;
import example.lsp.simulationTrackers.MutualReplanningAndOfferUpdateTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CollectionAdapterTest.class,
	DistributionAdapterTest.class,
	FirstReloadAdapterTest.class,
	MainRunAdapterTest.class,
	SecondReloadAdapterTest.class,
//	CascadingInfoTest.class,
	DemandObjectBuilderTest.class,
	CompleteLSPCreationTest.class,
	CollectionLSPCreationTest.class,
	CollectionLSPMobsimTest.class,
	CompleteLSPMobsimTest.class,
	FirstReloadLSPMobsimTest.class,
	MainRunLSPMobsimTest.class,
	MainRunOnlyLSPMobsimTest.class,
	SecondReloadLSPMobsimTest.class,
	CompleteLSPPlanTest.class,
	CollectionLSPPlanTest.class,
	CollectionLSPReplanningTest.class,
	CollectionLSPSchedulingTest.class,
	CompleteLSPSchedulingTest.class,
	FirstReloadLSPSchedulingTest.class,
	MainRunLSPSchedulingTest.class,
	SecondReloadLSPSchedulingTest.class,
	CollectionLSPScoringTest.class,
	CollectionShipmentBuilderTest.class,
	CompleteShipmentBuilderTest.class,
	DistributionShipmentBuilderTest.class,
	AssignerRequirementsTest.class,
	TransferrerRequirementsTest.class,
	CollectionElementTest.class,
	DistributionElementTest.class,
	FirstReloadElementTest.class,
	MainRunElementTest.class,
	SecondReloadElementTest.class,
	CompleteSolutionTest.class,
	CollectionSolutionTest.class,
	CollectionTrackerTest.class,
	MutualReplanningTest.class,
	MutualReplanningAndOfferUpdateTest.class,
	CollectionLSPShipmentAssigmentTest.class,
	CompleteLSPShipmentAssignerTest.class,
	MultipleShipmentsCollectionLSPSchedulingTest.class,
	MultipleShipmentsFirstReloadLSPSchedulingTest.class,
	MultipleShipmentsMainRunLSPSchedulingTest.class,
	MultipleShipmentsSecondReloadLSPSchedulingTest.class,
	MultipleShipmentsCompleteLSPSchedulingTest.class,
	MultipleIterationsCollectionLSPScoringTest.class,
	MultipleIterationsCollectionLSPMobsimTest.class,
	MultipleIterationsFirstReloadLSPMobsimTest.class,
	MultipleIterationsMainRunLSPMobsimTest.class,
	MultipleIterationsSecondReloadLSPMobsimTest.class,
	MultipleItreationsCompleteLSPMobsimTest.class,
	MultipleShipmentsCollectionLSPMobsimTest.class,
	MultipleShipmentsCompleteLSPMobsimTest.class,
	MultipleShipmentsFirstReloadLSPMobsimTest.class,
	MultipleShipmentsMainRunLSPMobsimTest.class,
	MultipleShipmentsSecondReloadLSPMobsimTest.class,
	RepeatedMultipleShipmentsCompleteLSPMobsimTest.class,
})

public class FTLabTestSuite {
	
}
