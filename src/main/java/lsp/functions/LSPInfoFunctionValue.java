package lsp.functions;

public interface LSPInfoFunctionValue<T> {

	String getName();
	T getValue();
	void setValue(T value);

}
