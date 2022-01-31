package demand.offer;

import java.util.ArrayList;
import java.util.Collection;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.demandObject.DemandObject;

public class OfferFactoryImpl implements OfferFactory{

	
	private final ArrayList<Offer> offerList;
	private LogisticsSolutionDecorator solution;
	private LSPDecorator lsp;
	
	public OfferFactoryImpl(LogisticsSolutionDecorator solution) {
		this.solution = solution;
		this.lsp = solution.getLSP();
		offerList = new ArrayList<>();
	}	
	
	@Override
	public Offer makeOffer(DemandObject object, String offerType) {
		for(Offer offer : offerList) {
			if(offer.getType().equals(offerType)) {
				offer.setLSP(lsp);
				return offer;
			}
		}
		return new DefaultOfferImpl(this.lsp, this.solution);
	}

	@Override
	public Collection<Offer> getOffers() {
		return offerList;
	}

	@Override
	public LSPDecorator getLSP() {
		return	 lsp;
	}

	@Override
	public LogisticsSolutionDecorator getLogisticsSolution() {
		return solution;
	}

	@Override
	public void setLogisticsSolution(LogisticsSolutionDecorator solution) {
		this.solution = solution;
	}

	@Override
	public void setLSP(LSPDecorator lsp) {
		this.lsp = lsp;
	}

	@Override
	public void addOffer(Offer offer) {
		offer.setLSP(lsp);
		offer.setSolution(solution);
		offerList.add(offer);
	}

	
}
