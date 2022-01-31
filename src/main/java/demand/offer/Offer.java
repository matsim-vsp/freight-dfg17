package demand.offer;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LogisticsSolutionDecorator;

public interface Offer {

	LSPDecorator getLsp();
	LogisticsSolutionDecorator getSolution();
	String getType();
	void accept(OfferVisitor visitor);
	void update();
	void setLSP(LSPDecorator lsp);
	void setSolution(LogisticsSolutionDecorator solution);
}
