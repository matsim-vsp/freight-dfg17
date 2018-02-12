package usecase;

import java.util.ArrayList;

import lsp.LSP;
import lsp.SolutionScheduler;
import lsp.resources.Resource;

public class SimpleSolutionScheduler implements SolutionScheduler {

	private LSP lsp;
	private ArrayList<Resource> resources;
	
	public SimpleSolutionScheduler(ArrayList<Resource> resources) {
		this.resources = resources;
	}
	
	@Override
	public void scheduleSolutions() {
		for(Resource resource : resources) {
			for(Resource lspResource : lsp.getResources()) {
				if(lspResource == resource) {
					lspResource.schedule();
				}
			}
		}
	}

	@Override
	public void setLSP(LSP lsp) {
		this.lsp = lsp;
	}

}
