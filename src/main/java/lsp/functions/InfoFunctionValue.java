package lsp.functions;

public interface InfoFunctionValue <T> {

	public String getName();
	public T getValue();
	public void setValue(T value);

}
