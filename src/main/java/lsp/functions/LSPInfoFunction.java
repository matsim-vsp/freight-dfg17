package lsp.functions;

import java.util.Collection;

public interface LSPInfoFunction {

	Collection<LSPInfoFunctionValue<?>> getValues();
	
}
