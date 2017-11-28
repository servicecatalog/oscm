/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: groch                                                     
 *                                                                              
 *  Creation Date: Jan 27, 2011                                                      
 *                                                                              
 *  Completion Time: Feb 1, 2011                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.CategoryToCatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.CatalogEntryRemovedException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOService;

/**
 * Test class for CatalogEntryAssembler
 * 
 * @author groch
 * 
 */
@SuppressWarnings("boxing")
public class CatalogEntryAssemblerTest {

    private static final int PRODUCT_KEY = 5;
    private static final String MP_ID = "MP_GLOBAL";
    private CatalogEntry catalogEntry;
    private CatalogEntry catalogEntry2;
    private CatalogEntry catalogEntry3;
    private LocalizerFacade facade;
    private String localizedText;
    private VOCatalogEntry voCatalogEntry;
    private VOCatalogEntry voCatalogEntry2;
    private VOCatalogEntry voCatalogEntry3;
    private Marketplace mp;

    @Before
    public void setUp() {
        LocalizerServiceStub localizerServiceStub = new LocalizerServiceStub() {

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return localizedText;
            }

        };

        facade = new LocalizerFacade(localizerServiceStub,
                Locale.GERMAN.getLanguage());

        // server side entries
        Product product = new Product();
        product.setKey(PRODUCT_KEY);
        product.setTechnicalProduct(new TechnicalProduct());
        product.setAutoAssignUserEnabled(false);
        ProductFeedback feedback = new ProductFeedback();
        feedback.setAverageRating(new BigDecimal("2.5"));
        product.setProductFeedback(feedback);

        mp = new Marketplace();
        mp.setKey(1);
        mp.setMarketplaceId(MP_ID);
        mp.setOrganization(new Organization());

        catalogEntry = new CatalogEntry();
        catalogEntry.setKey(1);
        catalogEntry.setProduct(product);
        catalogEntry.setMarketplace(mp);
        catalogEntry.setAnonymousVisible(true);
        catalogEntry.setVisibleInCatalog(true);

        catalogEntry2 = new CatalogEntry();
        catalogEntry2.setKey(2);

        catalogEntry3 = new CatalogEntry();
        catalogEntry3.setKey(3);

        VOMarketplace voMp = new VOMarketplace();
        voMp.setMarketplaceId(MP_ID);

        // incoming entries
        voCatalogEntry = new VOCatalogEntry();
        voCatalogEntry.setKey(1);
        voCatalogEntry.setVersion(0);
        voCatalogEntry.setMarketplace(voMp);
        voCatalogEntry.setAnonymousVisible(true);
        voCatalogEntry.setVisibleInCatalog(true);

        voCatalogEntry2 = new VOCatalogEntry();
        voCatalogEntry2.setKey(2);

        voCatalogEntry3 = new VOCatalogEntry();
        voCatalogEntry3.setKey(3);

    }

    @Test
    public void toVOCatalogEntry_NullInput() {
        VOCatalogEntry entry = CatalogEntryAssembler.toVOCatalogEntry(null,
                facade);
        assertNull(entry);
    }

    /**
     * domain object -> value object
     */
    @Test
    public void toVOCatalogEntry() {

        List<CategoryToCatalogEntry> categoryToCatalogEntry = new ArrayList<CategoryToCatalogEntry>();
        CategoryToCatalogEntry ctc = new CategoryToCatalogEntry();
        ctc.setCatalogEntry(catalogEntry);
        Category category = new Category();
        category.setCategoryId("categoryId");
        category.setMarketplaceKey(mp.getKey());
        category.setMarketplace(mp);
        category.setKey(3333);
        ctc.setCategory(category);
        ctc.setKey(2222);
        categoryToCatalogEntry.add(ctc);
        catalogEntry.setCategoryToCatalogEntry(categoryToCatalogEntry);
        catalogEntry.getProduct().setVendor(new Organization());

        VOCatalogEntry voEntry = CatalogEntryAssembler.toVOCatalogEntry(
                catalogEntry, facade);
        assertNotNull(voEntry);
        assertEquals(catalogEntry.getKey(), voEntry.getKey());
        assertEquals(catalogEntry.getProduct().getKey(), voEntry.getService()
                .getKey());
        assertEquals(catalogEntry.getVersion(), voEntry.getVersion());
        Assert.assertEquals(catalogEntry.isAnonymousVisible(),
                voEntry.isAnonymousVisible());
        Assert.assertEquals(catalogEntry.isVisibleInCatalog(),
                voEntry.isVisibleInCatalog());
        if (catalogEntry.getMarketplace() != null
                || voEntry.getMarketplace() != null) {
            assertNotNull(catalogEntry.getMarketplace());
            assertNotNull(voEntry.getMarketplace());
            assertEquals(catalogEntry.getMarketplace().getMarketplaceId(),
                    voEntry.getMarketplace().getMarketplaceId());
        }

        assertNotNull(voEntry.getCategories());
        assertEquals(1, voEntry.getCategories().size());
        assertEquals(category.getCategoryId(), voEntry.getCategories().get(0)
                .getCategoryId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateCatalogEntry_NullInputVO() throws Exception {
        CatalogEntryAssembler.updateCatalogEntry(catalogEntry, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateCatalogEntry_NullInputDO() throws Exception {
        CatalogEntryAssembler.updateCatalogEntry(null, voCatalogEntry);
    }

    @Test
    public void updateCatalogEntry() throws Exception {
        CatalogEntry entry = CatalogEntryAssembler.updateCatalogEntry(
                catalogEntry, voCatalogEntry);
        assertNotNull(entry);
        assertEquals(voCatalogEntry.getKey(), entry.getKey());
        assertEquals(PRODUCT_KEY, entry.getProduct().getKey());
        assertEquals(voCatalogEntry.getVersion(), entry.getVersion());
    }

    @Test
    public void toVOCatalogEntry_ForListing() throws Exception {
        // when
        VOCatalogEntry voCe = CatalogEntryAssembler.toVOCatalogEntry(
                catalogEntry, facade, PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);

        // then
        assertEquals(catalogEntry.getProduct().getKey(), voCe.getService()
                .getKey());
    }

    @Test(expected = SaaSSystemException.class)
    public void updateCatalogEntry_conflictingKeys() throws Exception {
        voCatalogEntry.setKey(2);
        CatalogEntryAssembler.updateCatalogEntry(catalogEntry, voCatalogEntry);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updateCatalogEntry_conflictingVersions() throws Exception {
        voCatalogEntry.setVersion(-1);
        CatalogEntryAssembler.updateCatalogEntry(catalogEntry, voCatalogEntry);
    }

    @Test
    public void updateCatalogEntry_updateAnoymousVisible() throws Exception {
        Assert.assertEquals(voCatalogEntry.isAnonymousVisible(),
                catalogEntry.isAnonymousVisible());
        voCatalogEntry.setAnonymousVisible(!catalogEntry.isAnonymousVisible());
        assertFalse(voCatalogEntry.isAnonymousVisible() == catalogEntry
                .isAnonymousVisible());
        CatalogEntry entry = CatalogEntryAssembler.updateCatalogEntry(
                catalogEntry, voCatalogEntry);
        Assert.assertEquals(voCatalogEntry.isAnonymousVisible(),
                entry.isAnonymousVisible());
    }

    @Test
    public void updateCatalogEntry_updateVisibleInCatalog() throws Exception {
        Assert.assertEquals(voCatalogEntry.isVisibleInCatalog(),
                catalogEntry.isVisibleInCatalog());
        voCatalogEntry.setVisibleInCatalog(!catalogEntry.isVisibleInCatalog());
        assertFalse(voCatalogEntry.isVisibleInCatalog() == catalogEntry
                .isVisibleInCatalog());
        CatalogEntry entry = CatalogEntryAssembler.updateCatalogEntry(
                catalogEntry, voCatalogEntry);
        Assert.assertEquals(voCatalogEntry.isVisibleInCatalog(),
                entry.isVisibleInCatalog());
    }

    @Test
    public void consolidateCatalogEntries_NullInput() throws Exception {
        List<CatalogEntry> consolidatedCatalogEntries = CatalogEntryAssembler
                .consolidateCatalogEntries(null, Arrays.asList(catalogEntry));
        assertNotNull(consolidatedCatalogEntries);
        assertEquals(1, consolidatedCatalogEntries.size());
        assertEquals(catalogEntry, consolidatedCatalogEntries.get(0));
    }

    @Test
    public void consolidateCatalogEntries_EmptyInput() throws Exception {
        List<CatalogEntry> consolidatedCatalogEntries = CatalogEntryAssembler
                .consolidateCatalogEntries(new HashSet<VOCatalogEntry>(),
                        Arrays.asList(catalogEntry));
        assertNotNull(consolidatedCatalogEntries);
        assertEquals(1, consolidatedCatalogEntries.size());
        assertEquals(catalogEntry, consolidatedCatalogEntries.get(0));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void consolidateCatalogEntries_WrongVersion() throws Exception {
        voCatalogEntry.setVersion(-1);
        CatalogEntryAssembler.consolidateCatalogEntries(
                Collections.singleton(voCatalogEntry),
                Arrays.asList(catalogEntry));
    }

    @Test
    public void consolidateCatalogEntries_OneEntryNoModification()
            throws Exception {
        List<CatalogEntry> consolidatedCatalogEntries = CatalogEntryAssembler
                .consolidateCatalogEntries(
                        Collections.singleton(voCatalogEntry),
                        Arrays.asList(catalogEntry));
        assertNotNull(consolidatedCatalogEntries);
        assertEquals(1, consolidatedCatalogEntries.size());
        assertEquals(catalogEntry, consolidatedCatalogEntries.get(0));
    }

    @Test
    public void consolidateCatalogEntries_OneEntryModificationNoClash()
            throws Exception {
        List<CatalogEntry> consolidatedCatalogEntries = CatalogEntryAssembler
                .consolidateCatalogEntries(
                        Collections.singleton(voCatalogEntry),
                        Arrays.asList(catalogEntry));
        assertNotNull(consolidatedCatalogEntries);
        assertEquals(1, consolidatedCatalogEntries.size());
        assertEquals(catalogEntry, consolidatedCatalogEntries.get(0));
    }

    @Test
    public void consolidateCatalogEntries_TwoIncomingThreeExisting()
            throws Exception {
        List<CatalogEntry> consolidatedCatalogEntries = CatalogEntryAssembler
                .consolidateCatalogEntries(
                        new HashSet<VOCatalogEntry>(Arrays.asList(
                                voCatalogEntry, voCatalogEntry2)), Arrays
                                .asList(catalogEntry, catalogEntry2,
                                        catalogEntry3));
        assertNotNull(consolidatedCatalogEntries);
        assertEquals(3, consolidatedCatalogEntries.size());
        assertEquals(catalogEntry, consolidatedCatalogEntries.get(0));
        assertEquals(catalogEntry2, consolidatedCatalogEntries.get(1));
        assertEquals(catalogEntry3, consolidatedCatalogEntries.get(2));
    }

    @Test(expected = CatalogEntryRemovedException.class)
    public void consolidateCatalogEntries_ThreeIncomingTwoExisting()
            throws Exception {
        CatalogEntryAssembler.consolidateCatalogEntries(
                new HashSet<VOCatalogEntry>(Arrays.asList(voCatalogEntry,
                        voCatalogEntry2, voCatalogEntry3)), Arrays.asList(
                        catalogEntry, catalogEntry2));
    }

    @Test(expected = CatalogEntryRemovedException.class)
    public void consolidateCatalogEntries_OneIncomingNoneExisting()
            throws Exception {
        CatalogEntryAssembler.consolidateCatalogEntries(
                Collections.singleton(voCatalogEntry),
                new ArrayList<CatalogEntry>());
    }

    @Test(expected = CatalogEntryRemovedException.class)
    public void consolidateCatalogEntries_TwoEntriesWithSameKey()
            throws Exception {
        voCatalogEntry2.setKey(voCatalogEntry.getKey());
        CatalogEntryAssembler.consolidateCatalogEntries(
                new HashSet<VOCatalogEntry>(Arrays.asList(voCatalogEntry,
                        voCatalogEntry2)), Arrays.asList(catalogEntry,
                        catalogEntry2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toCatalogEntry_NullArgument() throws ValidationException {
        CatalogEntryAssembler.toCatalogEntry(null);
    }

    @Test
    public void toCatalogEntry() throws ValidationException {
        // given
        VOService service = new VOService();
        service.setServiceId("sId");
        voCatalogEntry.setService(service);

        // when
        CatalogEntry ce = CatalogEntryAssembler.toCatalogEntry(voCatalogEntry);

        // then
        assertEquals(voCatalogEntry.getKey(), ce.getKey());
        assertEquals(voCatalogEntry.isAnonymousVisible(),
                ce.isAnonymousVisible());
        assertEquals(voCatalogEntry.isVisibleInCatalog(),
                ce.isVisibleInCatalog());
        assertNotNull(ce.getMarketplace());
        assertEquals(voCatalogEntry.getMarketplace().getMarketplaceId(), ce
                .getMarketplace().getMarketplaceId());
    }

}
