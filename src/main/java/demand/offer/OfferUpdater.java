package demand.offer;

import java.util.Collection;

import demand.decoratedLSP.LSPDecorator;

public interface OfferUpdater  {

	void updateOffers();
	Collection<OfferVisitor> getOfferVisitors();
	void setLSP(LSPDecorator lsp);
}
