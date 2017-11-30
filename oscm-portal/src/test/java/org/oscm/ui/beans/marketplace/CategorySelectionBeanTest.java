/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                       
 *                                                                              
 *  Creation Date: 07.03.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.marketplace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.vo.VOCategory;

/**
 * @author Enes Sejfi
 */
public class CategorySelectionBeanTest {

    private static final String MID = "MID";

    private CategorySelectionBean categorySelectionBean;

    private ServicePagingBean spb;
    private CategorizationService cs;

    private List<VOCategory> returnedCategories;

    @Before
    public void setup() {
        categorySelectionBean = spy(new CategorySelectionBean());
        doReturn("en").when(categorySelectionBean).getUserLanguage();

        spb = mock(ServicePagingBean.class);
        cs = mock(CategorizationService.class);

        returnedCategories = new ArrayList<VOCategory>();
        addCategoryToList(returnedCategories, "cat1");
        addCategoryToList(returnedCategories, "cat2");
        when(cs.getCategories(anyString(), anyString())).thenReturn(
                returnedCategories);

        categorySelectionBean.ui = mock(UiDelegate.class);

        when(categorySelectionBean.ui.findServicePagingBean()).thenReturn(spb);
        when(categorySelectionBean.ui.getMarketplaceId()).thenReturn(MID);
        when(
                categorySelectionBean.ui
                        .findService(eq(CategorizationService.class)))
                .thenReturn(cs);

    }

    @Test
    public void resetCategoriesForMarketplace() {
        // given
        categorySelectionBean.categoriesForMarketplace = new LinkedList<VOCategory>();
        categorySelectionBean.categoriesForMarketplace.add(new VOCategory());

        // when
        categorySelectionBean.resetCategoriesForMarketplace();

        // then
        assertNull(categorySelectionBean.categoriesForMarketplace);
    }

    @Test
    public void sortCategories() {
        // given
        givenUnsortedCategories();

        // when
        categorySelectionBean.sortCategories();

        // then
        List<VOCategory> categories = categorySelectionBean.categoriesForMarketplace;
        assertEquals("%", categories.get(0).getName());
        assertEquals("&", categories.get(1).getName());
        assertEquals("A", categories.get(2).getName());
        assertEquals("a", categories.get(3).getName());
        assertEquals("Aa", categories.get(4).getName());
        assertEquals("aA", categories.get(5).getName());
        assertEquals("B", categories.get(6).getName());
        assertEquals("b", categories.get(7).getName());
    }

    @Test
    public void selectByCategory() {
        String outcome = categorySelectionBean.selectByCategory();

        assertEquals(BaseBean.OUTCOME_SHOW_SERVICE_LIST, outcome);
        verify(spb).setFilterTag(eq(""));
        verify(spb).setSelectedPage(eq(1));
    }

    /**
     * Bug 9976 reset list when mp has changed
     */
    @Test
    public void loadCategories_ResetMarketplace() {
        categorySelectionBean.categoriesForMarketplace = new ArrayList<VOCategory>();
        categorySelectionBean.lastUsedMarketplaceId = "othermid";
        categorySelectionBean.lastUsedLanguage = "en";

        categorySelectionBean.loadCategories();

        assertSame(returnedCategories,
                categorySelectionBean.categoriesForMarketplace);
        verify(cs).getCategories(eq(MID), eq("en"));
    }

    @Test
    public void hasMarketplaceChanged_Initial() {
        categorySelectionBean.lastUsedMarketplaceId = null;

        assertTrue(categorySelectionBean.hasMarketplaceChanged());
    }

    @Test
    public void hasMarketplaceChanged() {
        categorySelectionBean.lastUsedMarketplaceId = "othermid";

        assertTrue(categorySelectionBean.hasMarketplaceChanged());
    }

    @Test
    public void hasMarketplaceChanged_Negative() {
        categorySelectionBean.lastUsedMarketplaceId = MID;

        assertFalse(categorySelectionBean.hasMarketplaceChanged());
    }

    @Test
    public void getHasCategories_ResetMarketplace() {
        categorySelectionBean.categoriesForMarketplace = new ArrayList<VOCategory>();
        categorySelectionBean.lastUsedMarketplaceId = "othermid";
        categorySelectionBean.lastUsedLanguage = "en";

        boolean hasCategories = categorySelectionBean.getHasCategories();

        assertTrue(hasCategories);
        verify(cs).getCategories(eq(MID), eq("en"));
    }

    @Test
    public void getHasCategories() {
        categorySelectionBean.categoriesForMarketplace = new ArrayList<VOCategory>();
        categorySelectionBean.lastUsedMarketplaceId = MID;
        categorySelectionBean.lastUsedLanguage = "en";

        boolean hasCategories = categorySelectionBean.getHasCategories();

        assertFalse(hasCategories);
        verifyZeroInteractions(cs);
    }

    private void givenUnsortedCategories() {
        categorySelectionBean = new CategorySelectionBean();
        List<VOCategory> categories = new LinkedList<VOCategory>();
        categorySelectionBean.categoriesForMarketplace = categories;

        addCategoryToList(categories, "B");
        addCategoryToList(categories, "a");
        addCategoryToList(categories, "A");
        addCategoryToList(categories, "%");
        addCategoryToList(categories, "aA");
        addCategoryToList(categories, "b");
        addCategoryToList(categories, "Aa");
        addCategoryToList(categories, "&");
    }

    private void addCategoryToList(List<VOCategory> categories,
            String categoryName) {
        VOCategory category = new VOCategory();
        category.setName(categoryName);
        categories.add(category);
    }
}
