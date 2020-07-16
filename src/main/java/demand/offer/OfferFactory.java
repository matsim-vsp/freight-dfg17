package demand.offer;

import java.util.Collection;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.demandObject.DemandObject;

public interface OfferFactory {

	public Offer makeOffer(DemandObject object, String offerType);
	public Collection<Offer> getOffers();
	public LSPDecorator getLSP();
	public LogisticsSolutionDecorator getLogisticsSolution();
	public void setLogisticsSolution(LogisticsSolutionDecorator solution);
	public void setLSP(LSPDecorator lsp);
	public void addOffer(Offer offer);
}
