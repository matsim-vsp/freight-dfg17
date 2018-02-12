package lsp.functions;

public interface InfoFunctionValue {

	public String getName();
	public Class<?> getDataType();
	public String getValue();
	public void setValue(String value);
}
