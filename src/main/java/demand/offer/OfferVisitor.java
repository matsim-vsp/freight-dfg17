package demand.offer;

import lsp.LogisticsSolution;

public interface OfferVisitor {

	void visit(Offer offer);
	Class<? extends Offer> getOfferClass();
	LogisticsSolution getLogisticsSolution();
}
