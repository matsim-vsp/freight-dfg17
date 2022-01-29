package testLSPWithCostTracker;

import lsp.functions.LSPInfo;
import lsp.functions.LSPInfoFunction;

public class CostInfo extends LSPInfo {

	private final CostInfoFunction costFunction;
	
	public CostInfo() {
		this.costFunction = new CostInfoFunction();
	}
	
	
	@Override
	public String getName() {
		return "cost_function";
	}

	@Override
	public LSPInfoFunction getFunction() {
		return costFunction;
	}

	@Override
	public double getFromTime() {
		return 0;
	}

	@Override
	public double getToTime() {
		return Double.MAX_VALUE;
	}


	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

}
