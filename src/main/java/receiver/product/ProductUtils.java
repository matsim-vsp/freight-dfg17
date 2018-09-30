package receiver.product;

import org.matsim.api.core.v01.Id;

public class ProductUtils {
	private ProductUtils(){} // should not be instantiated
	
	public static ProductType createProductType( final Id<ProductType> typeId ) {
		return new ProductTypeImpl( typeId );
	}
}
