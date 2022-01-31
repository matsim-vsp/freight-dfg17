package demand.demandObject;

import java.util.Collection;

import demand.decoratedLSP.LSPDecorator;
import demand.offer.Offer;

public interface OfferRequester {

	Collection<Offer> requestOffers(Collection<LSPDecorator> lsps);
	void setDemandObject(DemandObject demandObject);
}
