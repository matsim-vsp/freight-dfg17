package cascadingInfoTest;

import java.util.ArrayList;
import java.util.Collection;

import lsp.functions.LSPInfoFunction;
import lsp.functions.LSPInfoFunctionValue;



public class AverageTimeInfoFunction implements LSPInfoFunction {

	private Collection<LSPInfoFunctionValue<?>> values;
	
	public AverageTimeInfoFunction() {
		values = new ArrayList<LSPInfoFunctionValue<?>>();
		values.add(new AverageTimeInfoFunctionValue());
	}
		
	@Override
	public Collection<LSPInfoFunctionValue<?>> getValues() {
		return values;
	}

}
