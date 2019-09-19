package demand.mutualReplanning;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;
import demand.demandObject.DemandPlan;
import demand.offer.Offer;
import org.matsim.core.replanning.ReplanningContext;

import java.util.Collection;

public class OfferReplanningStrategyModuleImpl extends OfferReplanningStrategyModule{

	
	public OfferReplanningStrategyModuleImpl(DemandObject demandObject) {
		super(demandObject);
	}
	
	public OfferReplanningStrategyModuleImpl() {
		super();
	}
	
	@Override
	public void handlePlan(DemandPlan demandPlan) {
		Collection<Offer> offers = recieveOffers(lsps);
		plan = createPlan(demandPlan, offers);
		demandObject.setSelectedPlan(plan);
	}
	
	protected Collection<Offer> recieveOffers(Collection<LSPDecorator> lsps){
		return demandObject.getOfferRequester().requestOffers(lsps);				
	}
	
	protected DemandPlan createPlan(DemandPlan demandPlan, Collection<Offer> offers) {
			return demandObject.getDemandPlanGenerator().createDemandPlan(offers);
	}

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {
			
	}
		
	
}
