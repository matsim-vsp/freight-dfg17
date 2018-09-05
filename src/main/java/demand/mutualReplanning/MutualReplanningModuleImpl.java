package demand.mutualReplanning;

import java.util.Collection;

import org.matsim.core.controler.events.ReplanningEvent;

import demand.decoratedLSP.LSPDecorator;
import demand.demandObject.DemandObject;

public class MutualReplanningModuleImpl extends MutualReplanningModule{
	
	public MutualReplanningModuleImpl(Collection<LSPDecorator> lsps, Collection<DemandObject> demandObjects) {
		super(lsps, demandObjects);
	}
	
	@Override
	void replanLSPs(ReplanningEvent event) {
		for(LSPDecorator lsp : lsps) {
			if(lsp.getReplanner()!= null) {
				lsp.getReplanner().replan(event);
			}
			if(lsp.getOfferUpdater()!= null) {
				lsp.getOfferUpdater().updateOffers();
			}
		}
	}

	@Override
	void replanDemandObjects(ReplanningEvent event, Collection<LSPDecorator> lsps) {
		for(DemandObject demandObject : demandObjects) {
			if(demandObject.getReplanner()!= null) {
				demandObject.getReplanner().replan(lsps, event);
			}
		}
	}

}
