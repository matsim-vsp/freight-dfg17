package lsp.functions;

class InfoFunctionValueImpl<T> implements InfoFunctionValue<T> {

	private String name;
	private T value;
	
	InfoFunctionValueImpl( String name ) {
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
