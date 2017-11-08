/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                               
 *                                                                              
 *  Creation Date: 28.02.2012                                                      
 *                                                                                                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.CategoryToCatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Product;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Utility class to create test data.
 * 
 */
public class Categories {

    /**
     * Create and persist a new category with the given id.
     */
    public static Category create(DataService mgr, String id,
            Marketplace marketplace) throws NonUniqueBusinessKeyException {
        Category category = new Category();
        category.setCategoryId(id);
        category.setMarketplace(marketplace);
        mgr.persist(category);
        return category;
    }

    /**
     * Publish the product to the marketplace and assign the category to the
     * product.
     */
    public static void assignToProduct(DataService mgr, Category category,
            Product product, Marketplace mp)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(product);
        ce.setMarketplace(mp);
        ce.setAnonymousVisible(true);
        mgr.persist(ce);
        assignToCatalogEntry(mgr, category, ce);
    }

    /**
     * Assign a category to an existing catalog entry.
     */
    public static void assignToCatalogEntry(DataService mgr, Category category,
            CatalogEntry ce) throws ObjectNotFoundException,
            NonUniqueBusinessKeyException {
        CategoryToCatalogEntry cc = new CategoryToCatalogEntry();
        cc.setCatalogEntry(ce);
        cc.setCategory(mgr.getReference(Category.class, category.getKey()));
        mgr.persist(cc);
    }

}
