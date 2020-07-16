package demand.controler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import demand.decoratedLSP.LSPDecorator;
import org.matsim.api.core.v01.Id;

import lsp.LSP;

/*package-private*/ class LSPDecorators {

	private Map<Id<LSP>, LSPDecorator> lsps = new HashMap<>();

	LSPDecorators(Collection<LSPDecorator> lsps) {
		makeMap(lsps);
	}

	private void makeMap(Collection<LSPDecorator> lsps) {
		for (LSPDecorator c : lsps) {
			this.lsps.put(c.getId(), c);
		}
	}

	public LSPDecorators() {

	}

	public Map<Id<LSP>, LSPDecorator> getLSPs() {
		return lsps;
	}

	public void addLSP(LSPDecorator lsp) {
		if(!lsps.containsKey(lsp.getId())){
			lsps.put(lsp.getId(), lsp);
		}
	}
}
