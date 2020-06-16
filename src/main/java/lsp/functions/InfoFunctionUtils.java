package lsp.functions;

public class InfoFunctionUtils{
	public static InfoFunctionImpl createDefaultInfoFunction(){
		return new InfoFunctionImpl();
	}
	public static <T> InfoFunctionValueImpl<T> createInfoFunctionValue( String name ){
		return new InfoFunctionValueImpl<T>( name );
	}
}
