package testDemandObjectsWithLotsizes;

import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import demand.decoratedLSP.LSPWithOffers;
import demand.decoratedLSP.LogisticsSolutionWithOffers;
import demand.demandAgent.DemandAgent;
import demand.demandObject.DemandPlan;
import demand.demandObject.ShipperShipment;

public class DemandObjectCreator {

	private Collection<DemandAgent> demandAgents;
	private Id<Link> destinationLinkId;
	private LSPWithOffers LSP;
	private LogisticsSolutionWithOffers solution;
	
	
	
	
	
	private DemandPlan createInitialPlan(ShipperShipment shipment) {
		DemandPlan.Builder builder = DemandPlan.Builder.getInstance();
		builder.setShipperShipment(shipment);
		builder.setLsp(LSP);
		builder.setLogisticsSolutionId(solution.getId());
		return builder.build();
	}
	
	
	
	
	
	private LotSizeShipment createInitialShipment() {
		return null;
		
	}
	
}
