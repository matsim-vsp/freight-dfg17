package demand.decoratedLSP;

import demand.demandObject.DemandObject;
import demand.offer.Offer;
import demand.offer.OfferFactory;
import lsp.LogisticsSolution;

public interface LogisticsSolutionDecorator  extends LogisticsSolution {

	Offer getOffer(DemandObject object, String type);
	void setOfferFactory(OfferFactory factory);
	OfferFactory getOfferFactory();
	LSPDecorator getLSP();
	
}
