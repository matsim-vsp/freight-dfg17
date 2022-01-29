package demand.decoratedLSP;

import java.util.Collection;

import demand.offer.OfferTransferrer;
import lsp.LSPPlan;

public interface LSPPlanDecorator extends LSPPlan{

	public void setOfferTransferrer(OfferTransferrer transferrer);
	public OfferTransferrer getOfferTransferrer();
	public Collection<LogisticsSolutionDecorator> getSolutionDecorators();
	public void addSolution (LogisticsSolutionDecorator solution);
	public void setLSP(LSPDecorator lsp);
	@Override public LSPDecorator getLsp();
}
