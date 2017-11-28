/*******************************************************************************
 *                                                                              
7*  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: groch                                                 
 *                                                                              
 *  Creation Date: 07.03.2011                                                      
 *                                                                              
 *  Completion Time: 10.03.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.CategoryToCatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOCategory;

/**
 * Unit tests for categorization management.
 * 
 * @author min Chen
 * 
 */
public class CategorizationServiceBean2Test {

    private CategorizationServiceBean categorizationService;

    @Before
    public void setup() throws Exception {
        categorizationService = spy(new CategorizationServiceBean());
        categorizationService.dm = mock(DataService.class);
    }

    @Test
    public void updateAssignedCategories_noChanges()
            throws ObjectNotFoundException {
        boolean isChanged = false;
        CatalogEntry catalogEntry = givenCatalogEntry(new int[] { 1, 2 });
        isChanged = categorizationService.updateAssignedCategories(
                catalogEntry, givenVOCategories(new int[] { 1, 2 }));
        assertEquals(Boolean.FALSE, Boolean.valueOf(isChanged));

    }

    @Test
    public void updateAssignedCategories_addNew()
            throws ObjectNotFoundException {
        boolean isChanged = false;

        CatalogEntry catalogEntry = givenCatalogEntry(new int[] { 1 });
        isChanged = categorizationService.updateAssignedCategories(
                catalogEntry, givenVOCategories(new int[] { 1, 2 }));
        assertEquals(Boolean.TRUE, Boolean.valueOf(isChanged));

    }

    @Test
    public void updateAssignedCategories_deleteSome()
            throws ObjectNotFoundException {
        boolean isChanged = false;
        CatalogEntry catalogEntry = givenCatalogEntry(new int[] { 1, 2 });
        isChanged = categorizationService.updateAssignedCategories(
                catalogEntry, givenVOCategories(new int[] {}));
        assertEquals(Boolean.TRUE, Boolean.valueOf(isChanged));

    }

    @Test
    public void updateAssignedCategories_changed()
            throws ObjectNotFoundException {
        boolean isChanged = false;
        CatalogEntry catalogEntry = givenCatalogEntry(new int[] { 1, 2, 4 });
        isChanged = categorizationService.updateAssignedCategories(
                catalogEntry, givenVOCategories(new int[] { 1, 3 }));
        assertEquals(Boolean.TRUE, Boolean.valueOf(isChanged));

    }

    /**
     * @param mp
     * @param isAnonymousVisible
     * @return
     */
    private CatalogEntry givenCatalogEntry(int[] categoryIds) {
        CatalogEntry ce = new CatalogEntry();
        ce.setMarketplace(givenMarketplace(10000));
        ce.setCategoryToCatalogEntry(new ArrayList<CategoryToCatalogEntry>());
        for (int categoryId : categoryIds) {
            ce.getCategoryToCatalogEntry().add(
                    givenCategoryToCatalogEntry(categoryId));
        }
        return ce;
    }

    private CategoryToCatalogEntry givenCategoryToCatalogEntry(int categoryId) {
        CategoryToCatalogEntry ctce = new CategoryToCatalogEntry();
        ctce.setCategory(givenCategory(categoryId));
        return ctce;
    }

    private List<VOCategory> givenVOCategories(int[] categoryIds) {
        List<VOCategory> categoris = new ArrayList<VOCategory>();
        for (int categoryId : categoryIds) {
            VOCategory voCategory = new VOCategory();
            voCategory.setCategoryId(String.valueOf(categoryId));
            voCategory.setKey(categoryId);
            voCategory.setMarketplaceId("10000");
            categoris.add(voCategory);
        }
        return categoris;
    }

    /**
     * @param mp
     * @param isAnonymousVisible
     * @return
     */
    private Category givenCategory(int categoryId) {
        Category category = new Category();
        category.setCategoryId(String.valueOf(categoryId));
        category.setKey(categoryId);
        return category;
    }

    /**
     * @param marketplaceId
     * @param org
     *            used for marketplaceToOrganizations
     * @return
     */
    private Marketplace givenMarketplace(int marketplaceId) {
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId(String.valueOf(marketplaceId));
        mp.setKey(marketplaceId);
        return mp;
    }
}
