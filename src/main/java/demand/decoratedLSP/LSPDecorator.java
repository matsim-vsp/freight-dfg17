package demand.decoratedLSP;

import org.matsim.api.core.v01.Id;

import demand.demandObject.DemandObject;
import demand.offer.Offer;
import demand.offer.OfferUpdater;
import lsp.LSP;
import lsp.LogisticsSolution;
import lsp.shipment.LSPShipment;

public interface LSPDecorator extends LSP {

	Offer getOffer(DemandObject object, String type, Id<LogisticsSolution> solutionId);
	void assignShipmentToSolution(LSPShipment shipment, Id<LogisticsSolution> id);
	OfferUpdater getOfferUpdater();
	void setOfferUpdater(OfferUpdater offerUpdater);
	LSPPlanDecorator getSelectedPlan();
}
