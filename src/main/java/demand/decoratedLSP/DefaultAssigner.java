package demand.decoratedLSP;

import lsp.LSP;
import lsp.ShipmentAssigner;
import lsp.shipment.LSPShipment;

/*package-private*/ class DefaultAssigner implements ShipmentAssigner{

	private LSP lsp;

	DefaultAssigner (LSP lsp) {
		this.lsp = lsp;
	}
	
	@Override
	public void assignShipment(LSPShipment shipment) {
		//Has to be empty, as an LSPWithOffers does not assign with the assigner. 
		//This job is done by the OfferTransferrer who gives the right solution in the offer
	}

	@Override
	public void setLSP(LSP lsp) {
		this.lsp = lsp;
		
	}

	@Override
	public LSP getLSP() {
		return lsp;
	}

}
