package lsp.functions;

import java.util.Collection;

public interface LSPInfoFunction {

	public Collection<LSPInfoFunctionValue<?>> getValues();
	
}
