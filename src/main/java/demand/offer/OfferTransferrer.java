package demand.offer;

import org.matsim.api.core.v01.Id;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;
import lsp.LogisticsSolution;

public interface OfferTransferrer {

	public Offer transferOffer(DemandObject object, String type, Id<LogisticsSolution> solutionId);
	public void setLSP(LSPDecorator lsp);
	public LSPDecorator getLSP();
}
