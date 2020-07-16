package demand.controler;

import demand.decoratedLSP.LSPDecorator;

import java.util.Collection;

public class DemandControlerUtils {
	public static LSPDecorators createLSPDecorators(Collection<LSPDecorator> lsps) {
		return new LSPDecorators(lsps);
	}
}
