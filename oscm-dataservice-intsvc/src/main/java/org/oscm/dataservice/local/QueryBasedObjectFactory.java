/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: 28.01.2011                                                      
 *                                                                              
 *  Completion Time: 28.01.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.local;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Product;

/**
 * Factory for creating objects that have certain properties set with respect to
 * query results.
 * 
 * @author Dirk Bernsau
 * 
 */
public class QueryBasedObjectFactory {

    /**
     * Creates a <code>CatalogEntry</code> for given supplier and product on the
     * given marketplace and sets the position of the entry to the end of
     * existing products of this organization. The created catalog entry is not
     * yet persisted. If no marketplace is given, the local marketplace of the
     * supplier is assumed.
     * 
     * @param product
     *            the product
     * @param marketplace
     *            the marketplace
     * @return the transient catalog entry
     */
    public static CatalogEntry createCatalogEntry(Product product,
            Marketplace marketplace) {
        // now generate a catalog entry and determine the next usable
        // position for the entry
        CatalogEntry catalogEntry = new CatalogEntry();
        catalogEntry.setProduct(product);
        catalogEntry.setMarketplace(marketplace);

        return catalogEntry;
    }

}
