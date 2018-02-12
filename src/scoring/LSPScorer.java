package scoring;

import lsp.LSP;

public interface LSPScorer extends Scorer{

	public double scoreCurrentPlan(LSP lsp);
	
}
