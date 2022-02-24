package lsp.functions;

/**
 * Can have any shape that is specified by the generic hTi
 */
public interface LSPInfoFunctionValue<T> {

	String getName();
	T getValue();
	void setValue(T value);

}
