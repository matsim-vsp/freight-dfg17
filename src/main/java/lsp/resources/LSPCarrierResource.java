package lsp.resources;

import org.matsim.contrib.freight.carrier.Carrier;

/* Das ist nicht so, wie man es machen sollte, weil hier inheritance vor Composition geht
 * Spaeter noch verbessern!
 */
public interface LSPCarrierResource extends LSPResource {

	public Carrier getCarrier();
	
}
