/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                                                    
 *                                                                              
 *  Creation Date: 02.05.2010                                                      
 *                                                                              
 *  Completion Time:                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.internal.intf.TagService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTag;
import org.oscm.serviceprovisioningservice.local.TagServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.constants.Configuration;

import javax.ejb.EJBException;
import javax.persistence.Query;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * Tests for the tagging service bean.
 * 
 * @author Enes Sejfi
 * 
 */
public class TagServiceBeanIT extends EJBTestBase {

    private DataService dm;

    private TagService ts;
    private TagServiceLocal tsLocal;
    private ConfigurationServiceLocal csLocal;

    private Organization organization;

    // print debug output on console ?
    private boolean isDebugEnabled;

    private static final String FUJITSU_MID = "FUJITSU";
    private static final String GLOBAL_MID = "1234";
    private static final String[] MIDS = new String[] { FUJITSU_MID,
            GLOBAL_MID };

    private static final int TAGGING_MIN_SCORE = 2;
    private static final int TAGGING_MAX_TAGS = 20;

    private static final String LOCALE_DE = "de";
    private static final String LOCALE_JA = "ja";
    private static final String LOCALE_EN = "en";
    private static final String LOCALE_RS = "rs";
    private static final String LOCALE_IT = "it";
    private static final String LOCALE_CH = "ch";

    private static final int NUMBER_TAGS_DE = 6;
    private static final int NUMBER_TAGS_JA = TAGGING_MAX_TAGS + 1;
    private static final int NUMBER_TAGS_EN = 10;
    private static final int NUMBER_TAGS_CH = TAGGING_MAX_TAGS + 1;

    private static final int NUMBER_REFERENCES_DE = 12;
    private static final int NUMBER_REFERENCES_JA = 18;
    private static final int NUMBER_REFERENCES_EN = 7;
    private static final int NUMBER_REFERENCES_RS = 1;
    private static final int NUMBER_REFERENCES_CH = TAGGING_MIN_SCORE + 3;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        dm = container.get(DataService.class);

        container.addBean(new TagServiceBean());
        ts = container.get(TagService.class);
        tsLocal = container.get(TagServiceLocal.class);

        csLocal = container.get(ConfigurationServiceLocal.class);
        setUpDirServerStub(csLocal);
        csLocal.setConfigurationSetting(
                new ConfigurationSetting(ConfigurationKey.TAGGING_MIN_SCORE,
                        Configuration.GLOBAL_CONTEXT, TAGGING_MIN_SCORE + ""));
    }

    private void setupTagData(final String... locales) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Collection<String> localeSet = Arrays.asList(locales);
                organization = Organizations.createOrganization(dm,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                if (localeSet.contains(LOCALE_DE)) {
                    for (int key = 0; key < NUMBER_REFERENCES_DE; key++) {
                        for (int i = 0; i < NUMBER_TAGS_DE; i++) {
                            if (showTagForTP(i, key)) {
                                createTag(LOCALE_DE, createValue(LOCALE_DE, i),
                                        key);
                            }
                        }
                    }
                }
                if (localeSet.contains(LOCALE_JA)) {
                    for (int key = 0; key < NUMBER_REFERENCES_JA; key++) {
                        for (int i = 0; i < NUMBER_TAGS_JA; i++) {
                            if (showTagForTP(i, key)) {
                                createTag(LOCALE_JA, createValue(LOCALE_JA, i),
                                        key);
                            }
                        }
                    }
                }
                if (localeSet.contains(LOCALE_EN)) {
                    for (int key = 0; key < NUMBER_REFERENCES_EN; key++) {
                        for (int i = 0; i < NUMBER_TAGS_EN; i++) {
                            if (showTagForTP(i, key)) {
                                createTag(LOCALE_EN, createValue(LOCALE_EN, i),
                                        key);
                            }
                        }
                    }
                }
                if (localeSet.contains(LOCALE_RS)) {
                    for (int key = 0; key < NUMBER_REFERENCES_RS; key++) {
                        for (int i = 0; i < TAGGING_MIN_SCORE - 1; i++) {
                            if (showTagForTP(i, key)) {
                                createTag(LOCALE_RS, createValue(LOCALE_RS, i),
                                        key);
                            }
                        }
                    }
                }

                if (localeSet.contains(LOCALE_CH)) {
                    for (int key = 0; key < NUMBER_REFERENCES_CH; key++) {
                        for (int i = 0; i < NUMBER_TAGS_CH; i++) {

                            // product with odd number belongs FUJITSU_MID
                            // details refer to
                            // isProductPublishedOnMarketplace(...)

                            // most of the tag only be referenced in FUJITSU_MID
                            int k = key * 2;
                            // make ch_4 to be the lease weighted tag
                            if (i == 4 && key > TAGGING_MIN_SCORE + 1) {
                                continue;
                            }
                            // make ch_5 to be the lease weighted tag of
                            // FUJITSU_MID
                            if (i == 5 && key > TAGGING_MIN_SCORE) {
                                k = key * 2 + 1;
                            }
                            createTag(LOCALE_CH, createValue(LOCALE_CH, i), k);
                        }
                    }
                }

                dm.flush();
                Query query = dm.createQuery(
                        "UPDATE Product p SET p.dataContainer.status = :status");
                query.setParameter("status", ServiceStatus.ACTIVE);
                query
                        .executeUpdate();

                return null;
            }
        });
    }

    /**
     * Test if given list is sorted ascending
     * 
     * @param voTags
     */
    private void testSortedAscending(List<VOTag> voTags) {
        for (int i = 0; i < voTags.size(); i++) {
            VOTag voTag = voTags.get(i);
            if (i >= 1) {
                VOTag before = voTags.get(i - 1);
                Assert.assertTrue(
                        voTag.getValue().compareTo(before.getValue()) > 0);
            }
        }
    }

    /**
     * Test case for bug #9160
     * 
     * The tags with heaviest weights should be put in to tag cloud
     */
    @Test
    public void testTagsByLocale_LowerWeightTagsNotIncluded_B9160()
            throws NonUniqueBusinessKeyException, Exception {

        // Prepare 21 tags, ch_4 is the least weighted tag
        setupTagData(LOCALE_CH);

        // Test the getTags for certain locale
        List<VOTag> voTags1 = ts.getTagsByLocale(LOCALE_CH);
        assertNotNull(voTags1);
        assertEquals(TAGGING_MAX_TAGS, voTags1.size());
        // The tag with lowest weight should not appear
        for (VOTag t : voTags1) {
            assertFalse(t.getValue().equals(createValue(LOCALE_CH, 4)));
        }

    }

    /**
     * Test case for bug #9160
     * 
     * The tags with heaviest weights should be put in to tag cloud for certain
     * marketplace
     */
    @Test
    public void testTagsForMarketplace_LowerWeightTagsNotIncluded_B9160()
            throws NonUniqueBusinessKeyException, Exception {
        // Prepare 21 tags, ch_5 is the least weighted tag for FUJITSU_MID
        setupTagData(LOCALE_CH);
        // test the getTags for FUJITSU_MID
        List<VOTag> voTags2 = ts.getTagsForMarketplace(LOCALE_CH, FUJITSU_MID);
        assertNotNull(voTags2);
        assertEquals(TAGGING_MAX_TAGS, voTags2.size());
        // the tag with lowest weight should not appear
        for (VOTag t : voTags2) {
            assertFalse(t.getValue().equals(createValue(LOCALE_CH, 5)));
        }
    }

    @Test
    public void testGetTags_German()
            throws NonUniqueBusinessKeyException, Exception {
        setupTagData(LOCALE_DE, LOCALE_EN);

        List<VOTag> voTags = ts.getTagsForMarketplace(LOCALE_DE, GLOBAL_MID);
        assertNotNull(voTags);
        assertEquals(NUMBER_TAGS_DE, voTags.size());
        testSortedAscending(voTags);

        for (int i = 0; i < voTags.size(); i++) {
            VOTag voTag = voTags.get(i);

            assertEquals(LOCALE_DE, voTag.getLocale());
            debug("Tag: " + voTag.getValue() + " with "
                    + voTag.getNumberReferences());

            int tagIndex = getIndexFromValue(voTag.getValue());
            int expectedOccurrences = calculateTagReferences(GLOBAL_MID,
                    NUMBER_REFERENCES_DE, tagIndex);
            assertEquals(expectedOccurrences, voTag.getNumberReferences());

            assertEquals(createValue(LOCALE_DE, i), voTag.getValue());
        }
    }

    @Test
    public void testGetTags_English()
            throws NonUniqueBusinessKeyException, Exception {
        setupTagData(LOCALE_EN);
        List<VOTag> voTags = ts.getTagsForMarketplace(LOCALE_EN, FUJITSU_MID);
        assertNotNull(voTags);
        assertEquals(NUMBER_TAGS_EN, voTags.size());
        testSortedAscending(voTags);

        for (int i = 0; i < voTags.size(); i++) {
            VOTag voTag = voTags.get(i);

            assertEquals(LOCALE_EN, voTag.getLocale());
            int tagIndex = getIndexFromValue(voTag.getValue());
            int expectedOccurrences = calculateTagReferences(FUJITSU_MID,
                    NUMBER_REFERENCES_EN, tagIndex);
            assertEquals(expectedOccurrences, voTag.getNumberReferences());
        }
    }

    @Test
    public void testGetTags_NonExistingLocale()
            throws NonUniqueBusinessKeyException, Exception {
        setupTagData(LOCALE_EN);

        List<VOTag> voTags = ts.getTagsForMarketplace(LOCALE_IT, FUJITSU_MID);
        assertNotNull(voTags);

        // no tags if nonExistingLocal
        assertEquals(0, voTags.size());
    }

    private void createTag(String locale, String value, long tagObjectKey)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {

        Tag tag = new Tag(locale, value);

        // Read or create tag
        Tag foundTag = (Tag) dm.find(tag);
        if (foundTag == null) {
            dm.persist(tag);
            foundTag = tag;
        }

        // Read or create technical product
        TechnicalProduct tp = new TechnicalProduct();
        tp.setTechnicalProductId(Long.toString(tagObjectKey));
        tp.setOrganizationKey(organization.getKey());

        TechnicalProduct foundTP = (TechnicalProduct) dm.find(tp);
        if (foundTP == null) {
            foundTP = TechnicalProducts.createTechnicalProduct(dm, organization,
                    Long.toString(tagObjectKey), false,
                    ServiceAccessType.DIRECT);
            debug("  = Created TP " + foundTP.getTechnicalProductId());
            dm.flush();
        }
        for (int h = 0; h < MIDS.length; h++) {
            String marketplaceId = MIDS[h];
            if (isProductPublishedOnMarketplace(tagObjectKey, h)) {
                String productId = foundTP.getTechnicalProductId() + "_"
                        + marketplaceId;
                Product foundProduct = Products.findProduct(dm, organization,
                        productId);
                if (foundProduct == null) {
                    foundProduct = Products.createProduct(
                            organization.getOrganizationId(), productId,
                            foundTP.getTechnicalProductId(), dm);
                    debug("  = Created P " + foundProduct.getProductId());

                    Marketplace globalMp = Marketplaces.findMarketplace(dm,
                            marketplaceId);
                    if (globalMp == null) {
                        globalMp = Marketplaces.createGlobalMarketplace(
                                organization, marketplaceId, dm);
                        dm.flush();
                    }
                    CatalogEntry ce = new CatalogEntry();
                    ce.setMarketplace(globalMp);
                    ce.setProduct(foundProduct);
                    ce.setVisibleInCatalog(true);
                    dm.persist(ce);
                }
            }
        }
        debug("createTag: " + locale + " _ " + value + " => "
                + foundTP.getTechnicalProductId());

        TechnicalProductTag tpt = new TechnicalProductTag();
        tpt.setTag(foundTag);
        tpt.setTechnicalProduct(foundTP);
        dm.persist(tpt);
    }

    private int calculateTagReferences(String mId, int tp_count, int tagIndex) {
        int result = 0;
        for (int mpIndex = 0; mpIndex < MIDS.length; mpIndex++) {
            if (MIDS[mpIndex].equals(mId)) {
                debug("Visibility on MP " + mId);
                for (int tpIndex = 0; tpIndex < tp_count; tpIndex++) {
                    if (isProductPublishedOnMarketplace(tpIndex, mpIndex)) {
                        debug("  tp " + tpIndex + " is published");
                        if (showTagForTP(tagIndex, tpIndex)) {
                            debug("  and tag with index " + tagIndex
                                    + " exists");
                            result++;
                        }
                    }
                }
                break;
            }
        }
        return result;
    }

    private int calculateTagReferences(int tp_count, int tagIndex) {
        int result = 0;
        for (int tpIndex = 0; tpIndex < tp_count; tpIndex++) {
            if (showTagForTP(tagIndex, tpIndex)) {
                result++;
            }
        }
        return result;
    }

    /**
     * Determines whether the technical product with the given index should be
     * published on the given marketplace. A mathematical approach is used to
     * allow better calculation of the assertion numbers.
     * 
     * @param tpIndex
     *            index of technical product
     * @param mpIndex
     *            index of the marketplace
     */
    private boolean isProductPublishedOnMarketplace(long tpIndex, int mpIndex) {
        return tpIndex % Math.pow(2, (mpIndex + 1)) < (mpIndex + 1);
    }

    /**
     * Determines whether the technical product with the given index should be
     * tagged with the tag at the given index. A mathematical approach is used
     * to allow better calculation of the assertion numbers.
     * 
     * @param tagIndex
     *            index of tag
     * @param tpIndex
     *            index of technical product
     * 
     */
    private boolean showTagForTP(int tagIndex, int tpIndex) {
        return Math.ceil(tpIndex / 2) != tagIndex + 1;
    }

    private String createValue(String locale, int value) {
        return locale + "_" + value;
    }

    private int getIndexFromValue(String tagValue) {
        int result = -1;
        if (tagValue != null) {
            String[] split = tagValue.split("_");
            if (split.length == 2) {
                try {
                    result = Integer.valueOf(split[1]).intValue();
                } catch (NumberFormatException e) {
                    // don't care
                }
            }
        }
        return result;
    }

    @Test
    public void testGetTagsByPattern() throws Exception {

        setupTagData(LOCALE_JA, LOCALE_EN);

        List<String> rc;

        rc = ts.getTagsByPattern("ja", "ja_1%", 1);
        assertEquals(1, rc.size());
        Assert.assertTrue(rc.contains("ja_1"));

        rc = ts.getTagsByPattern("ja", "ja_1%", 2);
        assertEquals(2, rc.size());
        Assert.assertTrue(rc.contains("ja_1"));
        Assert.assertTrue(rc.contains("ja_10"));

        rc = ts.getTagsByPattern("ja", "JA_1%", 2);
        assertEquals(2, rc.size());
        Assert.assertTrue(rc.contains("ja_1"));
        Assert.assertTrue(rc.contains("ja_10"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTagsByPattern_IllegalLimit() throws Exception {
        try {
            ts.getTagsByPattern(LOCALE_DE, "abc", 0);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testGetTags_DuplicateValues() throws Exception {

        setupTagData(LOCALE_EN, LOCALE_DE);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Create an english tag with the same value than an already
                // existing german tag:
                for (int i = 0; i < TAGGING_MIN_SCORE; i++) {
                    createTag(LOCALE_EN, "de_0", i);
                }
                return null;
            }
        });

        // Now check whether the value "de_0" is not contained two times!
        List<VOTag> voTags = ts.getTagsForMarketplace(LOCALE_DE, FUJITSU_MID);
        assertNotNull(voTags);
        int size = voTags.size();
        // also cover delete function here
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tsLocal.deleteOrphanedTags();
                return null;
            }
        });

        voTags = ts.getTagsForMarketplace(LOCALE_DE, FUJITSU_MID);
        assertNotNull(voTags);
        assertEquals(size, voTags.size());

        int counter = 0;
        for (int i = 0; i < voTags.size(); i++) {
            VOTag voTag = voTags.get(i);
            if (voTag.getValue().equals("de_0"))
                counter++;
        }
        assertEquals(1, counter);
        assertEquals(NUMBER_TAGS_DE, voTags.size());
    }

    @Test
    public void testGetTag() throws Exception {
        setupTagData(LOCALE_DE);
        final String tagValue = createValue(LOCALE_DE, 0);
        Tag tag = runTX(new Callable<Tag>() {

            @Override
            public Tag call() throws Exception {
                return tsLocal.getTag(tagValue, LOCALE_DE);
            }
        });
        Assert.assertNotNull(tag);
        Assert.assertEquals(LOCALE_DE, tag.getLocale());
        Assert.assertEquals(tagValue, tag.getValue());
        Assert.assertTrue(tag.getKey() > 0);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetTag_NotFoundByValue() throws Exception {
        setupTagData();
        try {
            runTX(new Callable<Tag>() {

                @Override
                public Tag call() throws Exception {
                    return tsLocal.getTag("notExisting", LOCALE_DE);
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetTag_NotFoundByLocale() throws Exception {
        setupTagData();
        try {
            runTX(new Callable<Tag>() {

                @Override
                public Tag call() throws Exception {
                    return tsLocal.getTag(createValue(LOCALE_DE, 0), LOCALE_EN);
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testGetTagsByLocale_DummyLocale() throws Exception {
        setupTagData();
        List<VOTag> voTags = ts.getTagsByLocale("Dummy");
        assertNotNull(voTags);
        assertEquals(0, voTags.size());
    }

    @Test
    public void testGetTagsByLocale() throws Exception {
        setupTagData(LOCALE_DE);
        List<VOTag> voTags = ts.getTagsByLocale(LOCALE_DE);
        assertNotNull(voTags);
        assertEquals(NUMBER_TAGS_DE, voTags.size());
        testSortedAscending(voTags);

        for (int i = 0; i < voTags.size(); i++) {
            VOTag voTag = voTags.get(i);
            debug("Tag: " + voTag.getValue() + " with "
                    + voTag.getNumberReferences());
            assertEquals(LOCALE_DE, voTag.getLocale());
            int tagIndex = getIndexFromValue(voTag.getValue());

            assertEquals(calculateTagReferences(NUMBER_REFERENCES_DE, tagIndex),
                    voTag.getNumberReferences());
            assertEquals(createValue(LOCALE_DE, i), voTag.getValue());
        }
    }

    @Test
    public void testGetTagsByLocale_Limit() throws Exception {
        setupTagData(LOCALE_JA, LOCALE_EN);
        List<VOTag> voTags = ts.getTagsByLocale(LOCALE_JA);
        assertNotNull(voTags);
        assertEquals(TAGGING_MAX_TAGS, voTags.size());
        testSortedAscending(voTags);

        for (int i = 0; i < voTags.size(); i++) {
            VOTag voTag = voTags.get(i);
            debug("Tag: " + voTag.getValue() + " with "
                    + voTag.getNumberReferences());
            assertEquals(LOCALE_JA, voTag.getLocale());
            int tagIndex = getIndexFromValue(voTag.getValue());

            assertEquals(calculateTagReferences(NUMBER_REFERENCES_JA, tagIndex),
                    voTag.getNumberReferences());
        }
    }

    /**
     * Testcase for bugfix 7580.
     */
    @Test
    public void testTooManyTagsForLocale() throws Exception {
        setupTagData();
        final long tagObjectKey = 864238476;
        final String[] newTagValues = { "q", "w", "e", "r", "t" };

        try {
            // create new tags for object 864238476
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (String tagValue : newTagValues) {
                        createTag(LOCALE_EN, tagValue, tagObjectKey);
                    }
                    return null;
                }
            });

            // update tags
            final TechnicalProduct tp = runTX(new Callable<TechnicalProduct>() {
                @Override
                public TechnicalProduct call() throws Exception {

                    final TechnicalProduct tpDummy = new TechnicalProduct();
                    tpDummy.setTechnicalProductId(Long.toString(tagObjectKey));
                    tpDummy.setOrganizationKey(organization.getKey());

                    TechnicalProduct tp = (TechnicalProduct) dm.find(tpDummy);

                    for (TechnicalProductTag tpt : tp.getTags()) {
                        tpt.getTag().getKey();
                    }
                    return tp;
                }
            });

            String[] newValueArray = new String[] { "q", "w", "e", "1", "2",
                    "3", "4", "5" };
            final List<Tag> tags = createTransientTags(LOCALE_EN,
                    newValueArray);

            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    // new transaction, so we must reload all domain objects
                    TechnicalProduct tpReloaded = (TechnicalProduct) dm
                            .find(tp);
                    tsLocal.updateTags(tpReloaded, LOCALE_EN, tags);
                    return null;
                }
            });

            Assert.fail(
                    "Method UpdateTags doesn't throw a ValidationException.");

        } catch (ValidationException e) {
            // verify tags
            final String[] tagValues = runTX(new Callable<String[]>() {
                @Override
                public String[] call() throws Exception {
                    final TechnicalProduct tpDummy = new TechnicalProduct();
                    tpDummy.setTechnicalProductId(Long.toString(tagObjectKey));
                    tpDummy.setOrganizationKey(organization.getKey());

                    TechnicalProduct tp = (TechnicalProduct) dm.find(tpDummy);

                    String[] tagValues = new String[tp.getTags().size()];
                    int i = 0;
                    for (TechnicalProductTag tpt : tp.getTags()) {
                        tagValues[i++] = tpt.getTag().getValue();
                    }
                    return tagValues;
                }
            });

            for (int i = 0; i < tagValues.length; i++) {
                assertEquals(newTagValues[i], tagValues[i]);
            }
        }
    }

    @Test
    public void testUpdateTags() throws Exception {
        setupTagData(LOCALE_RS);
        final List<Tag> tagsDe = new ArrayList<>();
        final List<Tag> tagsEn = new ArrayList<>();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // using two TPs due to minimum weight restriction
                TechnicalProduct tp0 = new TechnicalProduct();
                tp0.setTechnicalProductId(Long.toString(0));
                tp0.setOrganizationKey(organization.getKey());
                tp0 = (TechnicalProduct) dm.find(tp0);
                assertNotNull(tp0);
                TechnicalProduct tp1 = new TechnicalProduct();
                tp1.setTechnicalProductId(Long.toString(1));
                tp1.setOrganizationKey(organization.getKey());
                tp1 = (TechnicalProduct) dm.find(tp1);
                if (tp1 == null) {
                    tp1 = TechnicalProducts.createTechnicalProduct(dm,
                            organization, Long.toString(1), false,
                            ServiceAccessType.DIRECT);
                    dm.flush();
                }
                tagsDe.add(new Tag(LOCALE_DE, "tag1"));
                tsLocal.updateTags(tp0, LOCALE_DE, new ArrayList<>(tagsDe));
                tsLocal.updateTags(tp1, LOCALE_DE, new ArrayList<>(tagsDe));
                return null;
            }
        });

        List<VOTag> tags = ts.getTagsByLocale(LOCALE_DE);
        assertNotNull(tags);
        assertEquals(1, tags.size());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct tp0 = new TechnicalProduct();
                tp0.setTechnicalProductId(Long.toString(0));
                tp0.setOrganizationKey(organization.getKey());
                tp0 = (TechnicalProduct) dm.find(tp0);
                assertNotNull(tp0);
                TechnicalProduct tp1 = new TechnicalProduct();
                tp1.setTechnicalProductId(Long.toString(1));
                tp1.setOrganizationKey(organization.getKey());
                tp1 = (TechnicalProduct) dm.find(tp1);
                assertNotNull(tp1);
                tagsDe.add(new Tag(LOCALE_DE, "tag2"));
                tagsDe.add(new Tag(LOCALE_DE, "tag3"));
                tsLocal.updateTags(tp0, LOCALE_DE, new ArrayList<>(tagsDe));
                tsLocal.updateTags(tp1, LOCALE_DE, new ArrayList<>(tagsDe));
                tagsEn.add(new Tag(LOCALE_EN, "tag2"));
                tagsEn.add(new Tag(LOCALE_EN, "tag3"));
                tsLocal.updateTags(tp0, LOCALE_EN, new ArrayList<>(tagsEn));
                tsLocal.updateTags(tp1, LOCALE_EN, new ArrayList<>(tagsEn));
                return null;
            }
        });

        tags = ts.getTagsByLocale(LOCALE_DE);
        assertNotNull(tags);
        assertEquals(3, tags.size());
        tags = ts.getTagsByLocale(LOCALE_EN);
        assertNotNull(tags);
        assertEquals(2, tags.size());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct tp0 = new TechnicalProduct();
                tp0.setTechnicalProductId(Long.toString(0));
                tp0.setOrganizationKey(organization.getKey());
                tp0 = (TechnicalProduct) dm.find(tp0);
                assertNotNull(tp0);
                TechnicalProduct tp1 = new TechnicalProduct();
                tp1.setTechnicalProductId(Long.toString(1));
                tp1.setOrganizationKey(organization.getKey());
                tp1 = (TechnicalProduct) dm.find(tp1);
                assertNotNull(tp1);
                tagsDe.clear();
                tagsDe.add(new Tag(LOCALE_DE, "tag1"));
                tsLocal.updateTags(tp0, LOCALE_DE, new ArrayList<>(tagsDe));
                tsLocal.updateTags(tp1, LOCALE_DE, new ArrayList<>(tagsDe));
                tagsEn.clear();
                tagsEn.add(new Tag(LOCALE_EN, "tag3"));
                tsLocal.updateTags(tp0, LOCALE_EN, new ArrayList<>(tagsEn));
                tsLocal.updateTags(tp1, LOCALE_EN, new ArrayList<>(tagsEn));
                return null;
            }
        });

        tags = ts.getTagsByLocale(LOCALE_DE);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        tags = ts.getTagsByLocale(LOCALE_EN);
        assertNotNull(tags);
        assertEquals(1, tags.size());

    }

    private List<Tag> createTransientTags(String locale, String[] tagValues) {
        List<Tag> result = new LinkedList<>();

        for (String tagValue : tagValues) {
            result.add(new Tag(locale, tagValue));
        }
        return result;
    }

    private void debug(String message) {
        if (isDebugEnabled) {
            System.out.println(message);
        }
    }
}
