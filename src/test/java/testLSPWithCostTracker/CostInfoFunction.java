package testLSPWithCostTracker;

import java.util.ArrayList;
import java.util.Collection;

import lsp.functions.LSPInfoFunction;
import lsp.functions.LSPInfoFunctionValue;



public class CostInfoFunction implements LSPInfoFunction {

	private final Collection<LSPInfoFunctionValue<?>> values;
	
	public CostInfoFunction() {
		values = new ArrayList<LSPInfoFunctionValue<?>>();
		FixedCostFunctionValue fixedValue = new FixedCostFunctionValue();
		LinearCostFunctionValue linearValue = new LinearCostFunctionValue();
		values.add(fixedValue);
		values.add(linearValue);
		
	}
	
	@Override
	public Collection<LSPInfoFunctionValue<?>> getValues() {
		return values;
	}

}
