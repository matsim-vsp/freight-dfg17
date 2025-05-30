/* *********************************************************************** *
 * project: org.matsim.*
 * SSReorderPolicyTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package receiver.reorderPolicy;


import org.junit.Assert;
import org.junit.Test;
import org.matsim.testcases.MatsimTestUtils;
import receiver.ReorderPolicy;
import receiver.SSReorderPolicy;

public class SSReorderPolicyTest {

	@Test
	public void testCalculateOrderQuantity() {
		ReorderPolicy policy = new SSReorderPolicy(5.0, 10.0);
		Assert.assertEquals("Wrong reorder quantity", 0.0, policy.calculateOrderQuantity(6.0), MatsimTestUtils.EPSILON);
		Assert.assertEquals("Wrong reorder quantity", 5.0, policy.calculateOrderQuantity(5.0), MatsimTestUtils.EPSILON);
		Assert.assertEquals("Wrong reorder quantity", 6.0, policy.calculateOrderQuantity(4.0), MatsimTestUtils.EPSILON);
	}

}
