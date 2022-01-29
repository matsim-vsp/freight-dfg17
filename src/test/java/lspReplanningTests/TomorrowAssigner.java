package lspReplanningTests;

import java.util.Random;

import lsp.LSP;
import lsp.ShipmentAssigner;
import lsp.shipment.LSPShipment;

public class TomorrowAssigner implements ShipmentAssigner{

	private LSP lsp;
	private final Random random;
	
	public TomorrowAssigner() {
		this.random = new Random(1);
	}
	
	@Override
	public void assignShipment(LSPShipment shipment) {
		boolean assignToday = random.nextBoolean();
		if(assignToday) {
			lsp.getSelectedPlan().getSolutions().iterator().next().assignShipment(shipment);
		}	
	}

	@Override
	public void setLSP(LSP lsp) {
		this.lsp = lsp;
		
	}

//	@Override
//	public LSP getLSP() {
//		return lsp;
//	}

}
