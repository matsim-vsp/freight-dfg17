package lsp.functions;

class LSPInfoFunctionValueImpl<T> implements LSPInfoFunctionValue<T> {

	private final String name;
	private T value;
	
	LSPInfoFunctionValueImpl(String name ) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void setValue(T value) {
		this.value = value;
	}
	
}
