package testLSPWithCostTracker;

import lsp.functions.LSPInfoFunctionValue;

public class LinearCostFunctionValue implements LSPInfoFunctionValue<Double> {

	private Double value;

	@Override
	public String getName() {
		return "linear";
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
