package testLSPWithCostTracker;

import lsp.functions.LSPInfoFunctionValue;

public class FixedCostFunctionValue implements LSPInfoFunctionValue<Double> {

	private double value;

	@Override
	public String getName() {
		return "fixed";
	}

	@Override
	public Double getValue() {
		return value;
	}

	@Override
	public void setValue(Double value) {
		this.value = value;	
	}
		
}
