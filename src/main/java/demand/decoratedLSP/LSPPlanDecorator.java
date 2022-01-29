package demand.decoratedLSP;

import java.util.Collection;

import demand.offer.OfferTransferrer;
import lsp.LSPPlan;

public interface LSPPlanDecorator extends LSPPlan{

	void setOfferTransferrer(OfferTransferrer transferrer);
	OfferTransferrer getOfferTransferrer();
	Collection<LogisticsSolutionDecorator> getSolutionDecorators();
	void addSolution(LogisticsSolutionDecorator solution);
	void setLSP(LSPDecorator lsp);
	@Override
	LSPDecorator getLsp();
}
