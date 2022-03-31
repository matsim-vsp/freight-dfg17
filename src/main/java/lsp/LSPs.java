package lsp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;

public class LSPs {
	
	private final Map<Id<LSP>, LSP> lsps = new HashMap<>();

	public LSPs(Collection<LSP> lsps) {
		makeMap(lsps);
	}

	private void makeMap(Collection<LSP> lsps) {
		for (LSP c : lsps) {
			this.lsps.put(c.getId(), c);
		}
	}


	public Map<Id<LSP>, LSP> getLSPs() {
		return lsps;
	}

}
