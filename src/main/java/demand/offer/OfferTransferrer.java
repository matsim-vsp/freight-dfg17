package demand.offer;

import org.matsim.api.core.v01.Id;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;
import lsp.LogisticsSolution;

public interface OfferTransferrer {

	Offer transferOffer(DemandObject object, String type, Id<LogisticsSolution> solutionId);
	void setLSP(LSPDecorator lsp);
	LSPDecorator getLSP();
}
