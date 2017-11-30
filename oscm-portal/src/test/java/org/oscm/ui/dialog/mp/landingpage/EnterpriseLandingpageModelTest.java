/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * 
 */
package org.oscm.ui.dialog.mp.landingpage;

import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.landingpage.EnterpriseLandingpageData;
import org.oscm.internal.landingpage.POLandingpageEntry;
import org.oscm.internal.vo.VOCategory;

/**
 * 
 * Test cases for EnterpriseLandingpageData
 * 
 * @author cheld
 * 
 */
public class EnterpriseLandingpageModelTest {

    EnterpriseLandingpageModel model;

    @Before
    public void setup() {
        model = new EnterpriseLandingpageModel();
    }

    /**
     * Size returns the number of adds
     */
    @Test
    public void numberOfColumns_one() {

        // given one category added
        model.addLandingpageData(landingpageData(newServiceList("s1"),
                newCategories("cat1")));

        // when
        int size = model.getNumberOfColumns();

        // than
        assertEquals(1, size);
    }

    private List<VOCategory> newCategories(String... categoryIds) {
        List<VOCategory> categories = new ArrayList<VOCategory>();
        for (String categoryId : categoryIds) {
            categories.add(newCategory(categoryId));
        }
        return categories;
    }

    private EnterpriseLandingpageData landingpageData(
            List<POLandingpageEntry> entries, List<VOCategory> categories) {
        EnterpriseLandingpageData result = new EnterpriseLandingpageData();
        for (int i = 0; i < categories.size(); i++) {
            result.addEntriesForCategory(Arrays.asList(entries.get(i)),
                    categories.get(i));
        }
        return result;
    }

    @Test
    public void numberOfColumns_empty() {
        // given
        givenEmptyModel();

        // than
        assertEquals(0, model.getNumberOfColumns());
    }

    @Test
    public void numberOfColumns_two() {

        // given
        model.addLandingpageData(landingpageData(newServiceList("s1", "s2"),
                newCategories("cat1", "cat2")));

        // when
        int size = model.getNumberOfColumns();

        // than
        assertEquals(2, size);
    }

    /**
     * Maximum of three categories can be stored
     */
    @Test
    public void numberOfColumns_max() {

        // given
        model.addLandingpageData(landingpageData(
                newServiceList("s1", "s2", "s3"),
                newCategories("cat1", "cat2", "cat3")));

        // when
        int size = model.getNumberOfColumns();

        // than
        assertEquals(3, size);
    }

    /**
     * Return category previously stored
     */
    @Test
    public void getCategory() {

        // given on category
        model.addLandingpageData(landingpageData(newServiceList("s1"),
                newCategories("cat1")));

        // when
        VOCategory cat = model.getCategory(0);

        // than
        assertEquals(cat.getCategoryId(), "cat1");
    }

    /**
     * Store 3 categories. Access them randomly
     */
    @Test
    public void getCategory_randomIndex() {

        // given
        model.addLandingpageData(landingpageData(
                newServiceList("s1", "s2", "s3"),
                newCategories("cat1", "cat2", "cat3")));

        // when
        VOCategory cat1 = model.getCategory(1);
        VOCategory cat0 = model.getCategory(0);
        VOCategory cat2 = model.getCategory(2);

        // than
        assertEquals(cat0.getCategoryId(), "cat1");
        assertEquals(cat1.getCategoryId(), "cat2");
        assertEquals(cat2.getCategoryId(), "cat3");
    }

    @Test
    public void getServices() {

        // given on category
        givenModel(newCategories("cat1"), newServiceList("s1"));

        // when
        List<LandingpageEntryModel> services = model.getServicesFirstCategory();

        // than
        assertThat(services, hasItems());
    }

    private void givenModel(List<VOCategory> categories,
            List<POLandingpageEntry> entries) {
        EnterpriseLandingpageModel result = new EnterpriseLandingpageModel();
        EnterpriseLandingpageData data = givenLandingpageData(categories,
                entries);

        result.addLandingpageData(data);

        addEntriesToModel(result, data);

        model = result;
    }

    private List<LandingpageEntryModel> newLandingpageModelEntries(
            List<POLandingpageEntry> entries) {
        ArrayList<LandingpageEntryModel> result = new ArrayList<LandingpageEntryModel>();
        for (POLandingpageEntry entry : entries) {
            LandingpageEntryModel entryModel = new LandingpageEntryModel(entry);
            entryModel.setAccessLink("http://access_url.com");
            result.add(entryModel);
        }
        return result;
    }

    private void addEntriesToModel(EnterpriseLandingpageModel result,
            EnterpriseLandingpageData data) {
        if (data.numberOfColumns() >= 1) {
            result.addEntries(0, newLandingpageModelEntries(data.getEntries(0)));
        }

        if (data.numberOfColumns() >= 2) {
            result.addEntries(1, newLandingpageModelEntries(data.getEntries(1)));
        }

        if (data.numberOfColumns() == 3) {
            result.addEntries(2, newLandingpageModelEntries(data.getEntries(2)));
        }
    }

    EnterpriseLandingpageData givenLandingpageData(List<VOCategory> categories,
            List<POLandingpageEntry> entries) {
        EnterpriseLandingpageData data = new EnterpriseLandingpageData();

        appendCategories(categories, data);

        appendEntries(entries, data);
        return data;
    }

    void appendEntries(List<POLandingpageEntry> entries,
            EnterpriseLandingpageData result) {
        if (result.category0 != null) {
            if (entries.isEmpty()) {
                result.entriesOfCateogry0 = Collections.emptyList();
            } else {
                result.entriesOfCateogry0 = Arrays.asList(entries.get(0));
            }
        }

        if (result.category1 != null) {
            if (entries.isEmpty() || entries.size() < 2) {
                result.entriesOfCateogry1 = Collections.emptyList();
            } else {
                result.entriesOfCateogry1 = Arrays.asList(entries.get(1));
            }
        }

        if (result.category2 != null) {
            if (entries.isEmpty() || entries.size() < 3) {
                result.entriesOfCateogry2 = Collections.emptyList();
            } else {
                result.entriesOfCateogry2 = Arrays.asList(entries.get(2));
            }
        }
    }

    void appendCategories(List<VOCategory> categories,
            EnterpriseLandingpageData result) {
        if (categories.size() > 0) {
            result.category0 = categories.get(0);
        }
        if (categories.size() > 1) {
            result.category1 = categories.get(1);
        }
        if (categories.size() > 2) {
            result.category2 = categories.get(2);
        }
    }

    @Test
    public void getServices_randomIndex() {
        // given
        givenModel(newCategories("cat1", "cat2", "cat3"),
                newServiceList("s1", "s2", "s3"));

        // when
        List<LandingpageEntryModel> services2 = model
                .getServicesSecondCategory();
        List<LandingpageEntryModel> services0 = model
                .getServicesFirstCategory();
        List<LandingpageEntryModel> services1 = model
                .getServicesThirdCategory();

        // then
        assertThat(services0, hasItems());
        assertThat(services1, hasItems());
        assertEquals(services2.get(0).getServiceId(), "s2");
    }

    private VOCategory newCategory(String categoryId) {
        VOCategory category = new VOCategory();
        category.setCategoryId(categoryId);
        category.setName(categoryId);
        return category;
    }

    private List<POLandingpageEntry> newServiceList(String... serviceIds) {
        List<POLandingpageEntry> entries = new ArrayList<POLandingpageEntry>();
        for (String serviceId : serviceIds) {
            POLandingpageEntry entry = newEntry(serviceId);
            entries.add(entry);
        }
        return entries;
    }

    private POLandingpageEntry newEntry(String serviceId) {
        POLandingpageEntry entry = new POLandingpageEntry();
        entry.setServiceId(serviceId);
        return entry;
    }

    @Test
    public void getIdFirstCategory() {
        // given
        givenModelWithCategories(Arrays.asList("cat1", "cat2", "cat3"));

        // when
        String result = model.getIdFirstCategory();

        // then
        assertEquals("cat1", result);

    }

    @Test
    public void getIdSecondCategory() {
        // given
        givenModelWithCategories(Arrays.asList("cat1", "cat2", "cat3"));

        // when
        String result = model.getIdSecondCategory();

        // then
        assertEquals("cat2", result);

    }

    @Test
    public void getIdThirdCategory() {

        // given
        givenModelWithCategories(Arrays.asList("cat1", "cat2", "cat3"));

        // when
        String result = model.getIdThirdCategory();

        // then
        assertEquals("cat3", result);

    }

    @Test
    public void getNameFirstCategory() {
        // given on category
        givenModelWithCategories(Arrays.asList("cat1", "cat2", "cat3"));

        // when
        String result = model.getNameFirstCategory();

        // then
        assertEquals("cat1", result);

    }

    @Test
    public void getNameSecondCategory() {
        // given on category
        givenModelWithCategories(Arrays.asList("cat1", "cat2", "cat3"));

        // when
        String result = model.getNameSecondCategory();

        // then
        assertEquals("cat2", result);

    }

    @Test
    public void getNameThirdCategory() {

        // given on model
        givenModelWithCategories(Arrays.asList("cat1", "cat2", "cat3"));

        // when
        String result = model.getNameThirdCategory();

        // then
        assertEquals("cat3", result);

    }

    /**
     * @param categories
     */
    private void givenModelWithCategories(List<String> categories) {
        EnterpriseLandingpageData result = new EnterpriseLandingpageData();
        for (String categoryId : categories) {
            VOCategory category = new VOCategory();
            category.setName(categoryId);
            category.setCategoryId(categoryId);

            if (result.category0 == null) {
                result.category0 = category;
                result.entriesOfCateogry0 = Collections.emptyList();
            } else if (result.category1 == null) {
                result.category1 = category;
                result.entriesOfCateogry1 = Collections.emptyList();
            } else if (result.category2 == null) {
                result.category2 = category;
                result.entriesOfCateogry2 = Collections.emptyList();
            }
        }

        model.addLandingpageData(result);
    }

    /**
     * Load services for given category
     */
    @Test
    public void getServicesFirstCategory() {
        // given services of one category
        givenModel(newCategories("cat1"), newServiceList("s1"));

        // when
        List<LandingpageEntryModel> result = model.getServicesFirstCategory();

        // then
        assertEquals(1, result.size());
    }

    @Test
    public void getServicesFirstCategory_NoData() {
        // given
        givenEmptyModel();

        // when
        assertNull(model.getServicesFirstCategory());
    }

    private EnterpriseLandingpageModel givenEmptyModel() {
        EnterpriseLandingpageModel data = new EnterpriseLandingpageModel();
        EnterpriseLandingpageData entries = new EnterpriseLandingpageData();

        model.addLandingpageData(entries);
        return data;
    }

    /**
     * Load services for given category
     */
    @Test
    public void getServicesSecondCategory() {
        // given services of one category
        givenModel(newCategories("cat1", "cat2"), newServiceList("s1", "s2"));

        // when
        List<LandingpageEntryModel> result = model.getServicesSecondCategory();

        // then
        assertEquals(1, result.size());
    }

    /**
     * Load services for given category
     */
    @Test
    public void getServicesThirdCategory() {
        // given services of one category
        givenModel(newCategories("cat1", "cat2", "cat3"),
                newServiceList("s1", "s2", "s3"));

        // when
        List<LandingpageEntryModel> result = model.getServicesThirdCategory();

        // then
        assertEquals(1, result.size());
    }
}
