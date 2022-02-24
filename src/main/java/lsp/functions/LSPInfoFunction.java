package lsp.functions;

import java.util.Collection;

/**
 * Contains one or several {@link LSPInfoFunctionValue}s
 */
public interface LSPInfoFunction {

	Collection<LSPInfoFunctionValue<?>> getValues();
	
}
