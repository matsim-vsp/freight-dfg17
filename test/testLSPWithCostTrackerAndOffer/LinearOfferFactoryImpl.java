package testLSPWithCostTrackerAndOffer;

import java.util.ArrayList;
import java.util.Collection;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LSPWithOffers;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.decoratedLSP.LogisticsSolutionWithOffers;
import demand.demandObject.DemandObject;
import demand.offer.DefaultOfferImpl;
import demand.offer.Offer;
import demand.offer.OfferFactory;
import lsp.LSP;
import lsp.LogisticsSolution;

public class LinearOfferFactoryImpl implements OfferFactory{

	
	private ArrayList<Offer> offerList;
	private LogisticsSolutionDecorator solution;
	private LSP lsp;
	
	public LinearOfferFactoryImpl(LogisticsSolutionDecorator solution) {
		this.solution = solution;
		this.lsp = solution.getLSP();
		offerList = new ArrayList<Offer>();
		offerList.add(new LinearOffer(solution));
	}	
	
	@Override
	public Offer makeOffer(DemandObject object, String offerType) {
		for(Offer offer : offerList) {
			if(offer.getType() == offerType) {
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
	public LSP getLSP() {
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
	public void setLSP(LSP lsp) {
		this.lsp = lsp;
	}

	@Override
	public void addOffer(Offer offer) {
		// TODO Auto-generated method stub
		
	}

		
}
