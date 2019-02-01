package receiver.product;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public class ProductUtils {
	private ProductUtils(){} // should not be instantiated
	
	/**
	 * Since using shipments we need to know where products are picked up.
	 * 
	 * @param typeId
	 * @return
	 */
	@Deprecated
	public static ProductType createProductType( final Id<ProductType> typeId ) {
		return new ProductTypeImpl( typeId );
	}

	public static ProductType createProductType( final Id<ProductType> typeId,final Id<Link> originLinkId ) {
		return new ProductTypeImpl( typeId, originLinkId );
	}
}
