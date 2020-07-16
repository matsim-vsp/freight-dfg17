package lsp.functions;

public interface LSPInfoFunctionValue<T> {

	public String getName();
	public T getValue();
	public void setValue(T value);

}
