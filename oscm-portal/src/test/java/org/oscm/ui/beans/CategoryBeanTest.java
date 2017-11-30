/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.marketplace.CategorySelectionBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.model.CategoryRow;
import org.oscm.ui.stubs.ApplicationStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.ResourceBundleStub;
import org.oscm.ui.stubs.UIViewRootStub;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOService;

@SuppressWarnings("boxing")
public class CategoryBeanTest {

    private CategoryBean categoryBean;

    private CategorizationService categorizationServiceMock;
    private MarketplaceService marketplaceServiceMock;

    private final List<VOCategory> englishCategories = new ArrayList<VOCategory>();
    private final List<VOService> servicesOfCategory1 = new ArrayList<VOService>();
    private final List<VOService> servicesOfCategory2 = new ArrayList<VOService>();
    private VOCategory voCategory1;
    private VOCategory voCategory2;
    private CategorySelectionBean categorySelectionBean;
    private MarketplaceBean marketplaceBean;

    private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();
    public static final String OUTCOME_REFRESH = "refresh";

    @Before
    public void setUp() {

        FacesContextStub contextStub = new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }
        };
        UIViewRootStub vrStub = mock(UIViewRootStub.class);
        when(vrStub.getLocale()).thenReturn(Locale.ENGLISH);

        contextStub.setViewRoot(vrStub);

        // Create a category and add it to the list of categories.
        voCategory1 = new VOCategory();
        voCategory1.setKey(1);
        voCategory1.setCategoryId("Category1");
        voCategory1.setName("English text1");
        voCategory1.setMarketplaceId("FUJITSU");

        voCategory2 = new VOCategory();
        voCategory2.setKey(2);
        voCategory2.setCategoryId("Category2");
        voCategory2.setName("English text2");
        voCategory2.setMarketplaceId("FUJITSU");
        englishCategories.add(voCategory1);
        englishCategories.add(voCategory2);

        // Create some services.
        VOService service1 = new VOService();
        VOService service2 = new VOService();
        VOService service3 = new VOService();

        servicesOfCategory1.add(service1);
        servicesOfCategory1.add(service2);
        servicesOfCategory1.add(service3);

        servicesOfCategory2.add(service1);
        servicesOfCategory2.add(service2);

        categorizationServiceMock = mock(CategorizationService.class);
        marketplaceServiceMock = mock(MarketplaceService.class);

        when(categorizationServiceMock.getCategories("FUJITSU", "en"))
        .thenReturn(englishCategories);

        when(
                categorizationServiceMock.getServicesForCategory(voCategory1
                        .getKey())).thenReturn(servicesOfCategory1);

        when(
                categorizationServiceMock.getServicesForCategory(voCategory2
                        .getKey())).thenReturn(servicesOfCategory2);

        categoryBean = spy(new CategoryBean() {

            private static final long serialVersionUID = -1587418167971768807L;

            @Override
            protected CategorizationService getCategorizationService() {
                return categorizationServiceMock;
            };
        });

        categoryBean.setMarketplaceId("FUJITSU");

        categorySelectionBean = mock(CategorySelectionBean.class);
        UiDelegate ui = mock(UiDelegate.class);
        when(ui.findCategorySelectionBean()).thenReturn(categorySelectionBean);

        marketplaceBean = mock(MarketplaceBean.class);
        when(ui.findMarketplaceBean()).thenReturn(marketplaceBean);
        categoryBean.ui = ui;
        ResourceBundleStub resourceBundleStub = new ResourceBundleStub();
        ((ApplicationStub) contextStub.getApplication())
        .setResourceBundleStub(resourceBundleStub);
        resourceBundleStub.addResource("category.displayLocale",
                "Display as {0}");
        categoryBean.resetToken();
        categoryBean.setToken(categoryBean.getToken());

        categoryBean.setMarketplaceBean(marketplaceBean);
    }

    @Test
    public void getSelectedMarketplaceId() {
        assertEquals(categoryBean.getMarketplaceId(), "FUJITSU");
    }

    @Test
    public void getLocale() {
        assertEquals(categoryBean.getLocale(), "en");
    }

    @Test
    public void changeLocale() {
        categoryBean.setLocale("de");
        assertEquals(categoryBean.getLocale(), "de");
    }

    @Test
    public void getDisplayAsColumnHeader() {
        categoryBean.setLocale("de");
        assertEquals("Display as German",
                categoryBean.getDisplayAsColumnHeader());

    }

    @Test
    public void getCategoriesRows() {
        List<CategoryRow> result = categoryBean.getCategoriesRows();
        assertNotNull(result);
        assertEquals(result.size(), 2);
        CategoryRow resultCatRow = result.get(0);
        assertEquals(resultCatRow.isNewCategory(), false);
        assertEquals(resultCatRow.getCategory().getCategoryId(), "Category1");
        assertEquals(resultCatRow.getCategory().getName(), "English text1");
        assertEquals(resultCatRow.getCategory().getMarketplaceId(), "FUJITSU");
    }

    @Test
    public void addCategory() {
        List<CategoryRow> result = categoryBean.getCategoriesRows();
        categoryBean.addCategory();
        assertNotNull(result);
        assertEquals(result.size(), 3);
        CategoryRow resultCatRow = result.get(2);
        assertEquals(resultCatRow.isNewCategory(), true);
        assertEquals(resultCatRow.getCategory().getCategoryId(), null);
        assertEquals(resultCatRow.getCategory().getName(), null);
        assertEquals(resultCatRow.getCategory().getMarketplaceId(), "FUJITSU");
    }

    @Test
    public void saveCategories() throws Exception {
        // given
        facesMessages = new ArrayList<FacesMessage>();
        // First fetch the categories.
        categoryBean.getCategoriesRows();

        // when
        categoryBean.saveCategories();

        // then
        assertEquals(1, facesMessages.size());

        List<VOCategory> toSave = new ArrayList<VOCategory>();
        toSave = englishCategories;
        List<VOCategory> toDelete = new ArrayList<VOCategory>();

        // Verify that the service was called with the correct parameters toSave
        // and toDelete.
        verify(categorizationServiceMock, times(1)).saveCategories(eq(toSave),
                eq(toDelete), eq("en"));
        verify(categorySelectionBean, times(1)).resetCategoriesForMarketplace();
    }

    @Test
    public void saveCategories_TokenInValid() throws Exception {
        // given
        facesMessages = new ArrayList<FacesMessage>();
        // First fetch the categories.
        categoryBean.getCategoriesRows();
        categoryBean.resetToken();
        categoryBean.setToken("");

        // when
        String result = categoryBean.saveCategories();

        // then
        assertEquals(0, facesMessages.size());
        assertEquals(result, OUTCOME_REFRESH);

        List<VOCategory> toSave = new ArrayList<VOCategory>();
        toSave = englishCategories;
        List<VOCategory> toDelete = new ArrayList<VOCategory>();

        // Verify that the service was called with the correct parameters toSave
        // and toDelete.
        verify(categorizationServiceMock, times(0)).saveCategories(eq(toSave),
                eq(toDelete), eq("en"));
        verify(categorySelectionBean, times(0)).resetCategoriesForMarketplace();
    }

    @Test
    public void saveCategories_AllCategoriesToDelete() throws Exception {
        facesMessages = new ArrayList<FacesMessage>();

        categoryBean.getCategoriesRows();

        // Add a new category.
        categoryBean.addCategory();

        // Fetch the categories.
        List<CategoryRow> categoriesRows = categoryBean.getCategoriesRows();

        // Set the checkbox "delete" to true to the two old and the one new
        // category.
        categoriesRows.get(0).setSelected(true);
        categoriesRows.get(1).setSelected(true);
        categoriesRows.get(2).setSelected(true);

        // then save.
        categoryBean.saveCategories();
        assertEquals(1, facesMessages.size());

        List<VOCategory> toSave = new ArrayList<VOCategory>();
        List<VOCategory> toDelete = new ArrayList<VOCategory>();
        toDelete = englishCategories;

        // Verify that the service was called with the correct parameters toSave
        // and toDelete.
        verify(categorizationServiceMock, times(1)).saveCategories(eq(toSave),
                eq(toDelete), eq("en"));
    }

    @Test
    public void saveCategories_OneOld_OneNew_ToDelete() throws Exception {
        categoryBean.getCategoriesRows();

        // Add a new category.
        categoryBean.addCategory();

        // Fetch the categories.
        List<CategoryRow> categoriesRows = categoryBean.getCategoriesRows();

        // Set the checkbox "delete" to true to the second old and the one new
        // category.
        categoriesRows.get(1).setSelected(true);
        categoriesRows.get(2).setSelected(true);

        // then save.
        categoryBean.saveCategories();
        assertEquals(1, facesMessages.size());

        List<VOCategory> toSave = new ArrayList<VOCategory>();
        toSave.add(voCategory1);

        List<VOCategory> toDelete = new ArrayList<VOCategory>();
        toDelete.add(voCategory2);

        // Verify that the service was called with the correct toSave and
        // toDelete parameters.
        verify(categorizationServiceMock, times(1)).saveCategories(eq(toSave),
                eq(toDelete), eq("en"));
    }

    @Test
    public void getNumOfServicesToDisplay_InvalidCategoryKey() {
        VOCategory voCategory = new VOCategory();
        String servicesAssignedToCategoryMsg = categoryBean
                .getNumOfServicesToDisplay(voCategory);
        assertEquals("", servicesAssignedToCategoryMsg);
    }

    @Test
    public void getNumOfServicesToDisplay_NullCategory() {
        String servicesAssignedToCategoryMsg = categoryBean
                .getNumOfServicesToDisplay(null);
        assertEquals("", servicesAssignedToCategoryMsg);
    }

    @Test
    public void getNumOfServicesToDisplay() {
        String servicesAssignToCatMsg = categoryBean
                .getNumOfServicesToDisplay(voCategory1);

        assertTrue(servicesAssignToCatMsg.length() > 0);
    }

    @Test
    public void getConfirmMsgToDisplay_NoCategoriesToDelete() throws Exception {
        facesMessages = new ArrayList<FacesMessage>();

        categoryBean.getCategoriesRows();

        categoryBean.saveCategories();
        assertEquals(1, facesMessages.size());

        // Check that the confirmation message is an empty string.
        String confirmMsgToDisplay = categoryBean.getConfirmMessage();
        assertEquals("", confirmMsgToDisplay);
    }

    @Test
    public void getConfirmMsgToDisplay() throws Exception {
        facesMessages = new ArrayList<FacesMessage>();

        List<CategoryRow> categoriesRows = categoryBean.getCategoriesRows();

        // Set the checkbox "delete" to true to the two categories.
        categoriesRows.get(0).setSelected(true);
        categoriesRows.get(1).setSelected(true);

        // save the categories.
        categoryBean.saveCategories();
        assertEquals(1, facesMessages.size());

        // check that the confirmation message is set.
        String confirmMsgToDisplay = categoryBean.getConfirmMessage();
        assertTrue(confirmMsgToDisplay.length() > 0);

        // verify that the getServicesForCategory has been called twice with the
        // keys of the categories
        // that were selected for deletion.
        verify(categorizationServiceMock, times(1)).getServicesForCategory(
                eq(voCategory1.getKey()));
        verify(categorizationServiceMock, times(1)).getServicesForCategory(
                eq(voCategory2.getKey()));
    }

    @Test
    public void processValueChange_ObjectNotFoundException() throws Exception {
        ValueChangeEvent eventMock = mock(ValueChangeEvent.class);
        doReturn("newMarketplaceId").when(eventMock).getNewValue();

        doThrow(new ObjectNotFoundException()).when(marketplaceServiceMock)
        .getBrandingUrl("newMarketplaceId");

        doReturn(marketplaceServiceMock).when(categoryBean)
        .getMarketplaceService();
        doThrow(new ObjectNotFoundException()).when(marketplaceServiceMock)
        .getMarketplaceById("newMarketplaceId");

        categoryBean.processValueChange(eventMock);
        assertEquals(0, facesMessages.size());
    }

}
