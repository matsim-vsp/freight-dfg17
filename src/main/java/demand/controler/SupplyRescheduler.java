package demand.controler;

import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;


import lsp.LSP;


class SupplyRescheduler implements BeforeMobsimListener{

	private final LSPDecorators lsps;
	
	SupplyRescheduler(LSPDecorators  lsps) {
		this.lsps = lsps;
	}
	
	
	public void notifyBeforeMobsim(BeforeMobsimEvent arg0) {
		if(arg0.getIteration() !=  0) {
			for(LSP lsp : lsps.getLSPs().values()){
				lsp.scheduleSolutions();
			}		
		}	
	}
}
