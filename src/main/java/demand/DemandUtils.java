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
			this.utilityFunctions = new ArrayList<UtilityFunction>();
		}

		public DemandAgentImplBuilder setId(Id<DemandAgent> id) {
			this.id = id;
			return this;
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
