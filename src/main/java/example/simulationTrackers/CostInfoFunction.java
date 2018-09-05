package example.simulationTrackers;

import java.util.ArrayList;
import java.util.Collection;

import lsp.functions.InfoFunction;
import lsp.functions.InfoFunctionValue;



public class CostInfoFunction implements InfoFunction {

	private example.simulationTrackers.FixedCostFunctionValue fixedValue;
	private example.simulationTrackers.LinearCostFunctionValue linearValue;
	private Collection<InfoFunctionValue<?>> values;
	
	public CostInfoFunction() {
		values = new ArrayList<InfoFunctionValue<?>>();
		fixedValue = new example.simulationTrackers.FixedCostFunctionValue();
		linearValue = new example.simulationTrackers.LinearCostFunctionValue();
		values.add(fixedValue);
		values.add(linearValue);
		
	}
	
	@Override
	public Collection<InfoFunctionValue<?>> getValues() {
		return values;
	}

}
