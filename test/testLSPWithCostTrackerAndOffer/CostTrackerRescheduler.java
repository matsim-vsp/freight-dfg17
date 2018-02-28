package testLSPWithCostTrackerAndOffer;

import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;

import lsp.LSP;
import lsp.LSPs;
import lsp.LogisticsSolution;
import lsp.LogisticsSolutionElement;
import lsp.shipment.LSPShipment;

public class CostTrackerRescheduler implements BeforeMobsimListener{

private LSPs lsps;
	
	public CostTrackerRescheduler(LSPs lsps) {
		this.lsps = lsps;
	}
	
	public void notifyBeforeMobsim(BeforeMobsimEvent arg0) {
		//if(arg0.getIteration() !=  0) {
			for(LSP lsp : lsps.getLSPs().values()){
				lsp.getSelectedPlan().getSolutions().iterator().next().getShipments().clear();
				for(LSPShipment shipment : lsp.getShipments()) {
					lsp.getSelectedPlan().getSolutions().iterator().next().assignShipment(shipment);
				}
			for(LogisticsSolution solution : lsp.getSelectedPlan().getSolutions()) {
					System.out.println(lsp.getSelectedPlan().getSolutions().iterator().next().getShipments().size());
					
					if(arg0.getIteration() ==2) {
						System.exit(1);
					}
					for(LogisticsSolutionElement element : solution.getSolutionElements()) {
						element.getIncomingShipments().clear();
						element.getOutgoingShipments().clear();
					}
				}
				
				
				lsp.scheduleSoultions();
			}		
		//}		
	}

}
