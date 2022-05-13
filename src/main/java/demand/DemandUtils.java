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

package demand;

import org.matsim.api.core.v01.Id;

import java.util.ArrayList;

public class DemandUtils {

	public static class DemandAgentImplBuilder {

		private Id<DemandAgent> id;
		private final ArrayList<UtilityFunction> utilityFunctions;

		public static DemandAgentImplBuilder newInstance() {
			return new DemandAgentImplBuilder();
		}

		private DemandAgentImplBuilder() {
			this.utilityFunctions = new ArrayList<>();
		}

		public void setId(Id<DemandAgent> id) {
			this.id = id;
		}

//		public Builder addUtilityFunction(UtilityFunction utilityFunction) {
//			this.utilityFunctions.add(utilityFunction);
//			return this;
//		}

		public DemandAgentImpl build() {
			return new DemandAgentImpl(this);
		}

		// --- Getters ---
		public Id<DemandAgent> getId() {
			return id;
		}

		public ArrayList<UtilityFunction> getUtilityFunctions() {
			return utilityFunctions;
		}
	}
}
