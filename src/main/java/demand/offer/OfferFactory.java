package demand.offer;

import java.util.Collection;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.demandObject.DemandObject;

public interface OfferFactory {

	Offer makeOffer(DemandObject object, String offerType);
	Collection<Offer> getOffers();
	LSPDecorator getLSP();
	LogisticsSolutionDecorator getLogisticsSolution();
	void setLogisticsSolution(LogisticsSolutionDecorator solution);
	void setLSP(LSPDecorator lsp);
	void addOffer(Offer offer);
}
