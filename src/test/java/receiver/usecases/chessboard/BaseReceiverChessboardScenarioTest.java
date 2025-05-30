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
  
package receiver.usecases.chessboard;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;

public class BaseReceiverChessboardScenarioTest{

	@Test
	public void testCreateChessboardScenario() {
		
		Scenario sc = null;
		try {
			sc = BaseReceiverChessboardScenario.createChessboardScenario(1l, 1, 5, false );
		} catch (Exception e) {
			Assert.fail("Should create the scenario without exceptions.");
		}
		
		/* TODO Test the various elements. */
	}

}
