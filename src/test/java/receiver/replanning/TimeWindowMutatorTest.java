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
  
package receiver.replanning;


import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.misc.Time;
import org.matsim.testcases.MatsimTestUtils;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.ReceiverUtils;

public class TimeWindowMutatorTest {
	final private static Logger LOG = Logger.getLogger(TimeWindowMutatorTest.class);
	@Rule public MatsimTestUtils utils = new MatsimTestUtils();
	
	@Test
	public void testPickRandomTimeWindow() {
		ReceiverPlan plan = getPlanWithTimeWindow();
		TimeWindowMutator twm = new TimeWindowMutator(3600.0);
		TimeWindow tw = twm.pickRandomTimeWindow(plan);
	
		/*TODO There is currently only one time window for customer one */ 
		Assert.assertEquals("Wrong time window start", Time.parseTime("08:00:00"), tw.getStart(), MatsimTestUtils.EPSILON);
		Assert.assertEquals("Wrong time window end", Time.parseTime("16:00:00"), tw.getEnd(), MatsimTestUtils.EPSILON);
	}

	@Test
	public void testExtendTimeWindowDownwards() {
		TimeWindowMutator twm = new TimeWindowMutator(Time.parseTime("01:00:00"));
		TimeWindow tw = twm.extendTimeWindowDownwards(getTimeWindow());
		LOG.info("Test extend downwards: " + tw.toString());
		
		Assert.assertEquals("Wrong time window end.", getTimeWindow().getEnd(), tw.getEnd(), MatsimTestUtils.EPSILON);
		Assert.assertTrue("Time window should be extended.", tw.getStart() <= getTimeWindow().getStart());
		Assert.assertTrue("Time window starts too early.", tw.getStart() >= getTimeWindow().getStart() - Time.parseTime("01:00:00"));
	}
	
	@Test
	public void testExtendTimeWindowUpwards() {
		TimeWindowMutator twm = new TimeWindowMutator(Time.parseTime("01:00:00"));
		TimeWindow tw = twm.extendTimeWindowUpwards(getTimeWindow());
		LOG.info("Test extend upwards: " + tw.toString());
		
		Assert.assertEquals("Wrong time window start.", getTimeWindow().getStart(), tw.getStart(), MatsimTestUtils.EPSILON);
		Assert.assertTrue("Time window should be extended.", tw.getEnd() >= getTimeWindow().getEnd());
		Assert.assertTrue("Time window ends too late.", tw.getEnd() <= getTimeWindow().getEnd() + Time.parseTime("01:00:00"));
	}
	
	@Test
	public void testContractTimeWindowBottom() {
		TimeWindowMutator twm = new TimeWindowMutator(Time.parseTime("01:00:00"));
		TimeWindow tw = twm.contractTimeWindowBottom(getTimeWindow());
		LOG.info("Test contract bottom: " + tw.toString());
		
		Assert.assertEquals("Wrong time window end.", getTimeWindow().getEnd(), tw.getEnd(), MatsimTestUtils.EPSILON);
		Assert.assertTrue("Time window should be contracted.", tw.getStart() >= getTimeWindow().getStart());
		Assert.assertTrue("Time window starts too early.", tw.getStart() <= getTimeWindow().getStart() + Time.parseTime("01:00:00"));
	}
	
	@Test
	public void testContractTimeWindowTop() {
		TimeWindowMutator twm = new TimeWindowMutator(Time.parseTime("01:00:00"));
		TimeWindow tw = twm.contractTimeWindowTop(getTimeWindow());
		LOG.info("Test contract top: " + tw.toString());
		
		Assert.assertEquals("Wrong time window start.", getTimeWindow().getStart(), tw.getStart(), MatsimTestUtils.EPSILON);
		Assert.assertTrue("Time window should be contracted.", tw.getEnd() <= getTimeWindow().getEnd());
		Assert.assertTrue("Time window starts too early.", tw.getEnd() >= getTimeWindow().getEnd() - Time.parseTime("01:00:00"));
	}
	
	@Test
	public void testWiggleTimeWindow() {
		MatsimRandom.reset(12345l);
		TimeWindowMutator twm = new TimeWindowMutator(Time.parseTime("01:00:00"));
		TimeWindow tw = twm.wiggleTimeWindow(getTimeWindow());
		LOG.info("Test wiggle: " + tw.toString());
		
		Assert.assertEquals("Wrong time window start.", getTimeWindow().getStart(), tw.getStart(), 1.0);
		Assert.assertEquals("Wrong time window end.", Time.parseTime("15:10:48"), tw.getEnd(), 1.0);
	}
	
	@Test
	public void testHandlePlan() {
		ReceiverPlan plan = getPlanWithTimeWindow();
		TimeWindowMutator twm = new TimeWindowMutator(Time.parseTime("01:00:00"));
		twm.handlePlan(plan);
		
		TimeWindow tw = plan.getTimeWindows().get(0);
		LOG.info("Test adapted plan: " + tw.toString());
		Assert.assertEquals("Wrong time window start.", Time.parseTime("08:00:00"), tw.getStart(), 1.0);
		Assert.assertEquals("Wrong time window end.", Time.parseTime("15:15:48"), tw.getEnd(), 1.0);
	}
	
	
	private TimeWindow getTimeWindow() {
		return TimeWindow.newInstance(Time.parseTime("08:00:00"), Time.parseTime("16:00:00"));
	}
	
	private ReceiverPlan getPlanWithTimeWindow() {
		Receiver receiver = ReceiverUtils.newInstance(Id.create("test", Receiver.class));
		return ReceiverPlan.Builder.newInstance(receiver).addTimeWindow(getTimeWindow()).build();
	}
	
}
