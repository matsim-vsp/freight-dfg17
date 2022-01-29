package lsp.functions;

import java.util.ArrayList;
import java.util.Collection;

class LSPInfoFunctionImpl implements LSPInfoFunction {

	private final Collection<LSPInfoFunctionValue<?>> values;
	
	LSPInfoFunctionImpl() {
		this.values = new ArrayList<>();
	}
	
	@Override
	public Collection<LSPInfoFunctionValue<?>> getValues() {
		return values;
	}

}
