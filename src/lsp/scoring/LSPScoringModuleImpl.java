package lsp.scoring;

import org.matsim.core.controler.events.ScoringEvent;

import lsp.LSP;
import lsp.LSPs;

public class LSPScoringModuleImpl implements LSPScoringModule{

	private LSPs lsps;
	
	public LSPScoringModuleImpl(LSPs lsps) {
		this.lsps = lsps;
	}
	
	@Override
	public void notifyScoring(ScoringEvent arg0) {
		scoreLSPs();
	}

	@Override
	public void scoreLSPs() {
		for(LSP lsp : lsps.getLSPs().values()) {
			lsp.scoreSelectedPlan();
		}	
	}
}
