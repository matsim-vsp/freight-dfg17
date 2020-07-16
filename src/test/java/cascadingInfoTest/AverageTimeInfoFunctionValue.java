package cascadingInfoTest;

import lsp.functions.LSPInfoFunctionValue;

public class AverageTimeInfoFunctionValue implements LSPInfoFunctionValue<Double> {

	private Double value;
	
	@Override
	public String getName() {
		return "averageTimeInSeconds";
	}
	
	@Override
	public void setValue(Double value) {
		this.value = value;
	}

	@Override
	public Double getValue() {
		return value;
	}
}
