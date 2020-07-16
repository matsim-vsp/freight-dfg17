package lsp.functions;

public class LSPInfoFunctionUtils {
	public static LSPInfoFunctionImpl createDefaultInfoFunction(){
		return new LSPInfoFunctionImpl();
	}
	public static <T> LSPInfoFunctionValueImpl<T> createInfoFunctionValue(String name ){
		return new LSPInfoFunctionValueImpl<T>( name );
	}
}
