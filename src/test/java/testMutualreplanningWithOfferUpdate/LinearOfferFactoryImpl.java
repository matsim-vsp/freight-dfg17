package testMutualreplanningWithOfferUpdate;

import java.util.ArrayList;
import java.util.Collection;

import demand.decoratedLSP.LSPDecorator;
import demand.decoratedLSP.LogisticsSolutionDecorator;
import demand.demandObject.DemandObject;
import demand.offer.DefaultOfferImpl;
import demand.offer.Offer;
import demand.offer.OfferFactory;
import example.lsp.simulationTrackers.LinearOffer;

public class LinearOfferFactoryImpl implements OfferFactory{

	
	private final ArrayList<Offer> offerList;
	private LogisticsSolutionDecorator solution;
	private LSPDecorator lsp;
	
	public LinearOfferFactoryImpl(LogisticsSolutionDecorator solution) {
		this.solution = solution;
		this.lsp = solution.getLSP();
		offerList = new ArrayList<>();
		offerList.add(new LinearOffer(solution) );
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
		// TODO Auto-generated method stub
		
	}

		
}
