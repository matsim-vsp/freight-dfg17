package lsp.functions;

import java.util.ArrayList;
import java.util.Collection;

class InfoFunctionImpl implements InfoFunction{

	private Collection<InfoFunctionValue<?>> values;
	
	InfoFunctionImpl() {
		this.values = new ArrayList<InfoFunctionValue<?>>();
	}
	
	@Override
	public Collection<InfoFunctionValue<?>> getValues() {
		return values;
	}

}
