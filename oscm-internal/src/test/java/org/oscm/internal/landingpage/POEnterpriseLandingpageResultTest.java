/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.landingpage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.vo.VOCategory;

/**
 * @author zankov
 * 
 */
public class POEnterpriseLandingpageResultTest {

    private EnterpriseLandingpageData landingpageResult;

    @Before
    public void setUp() {
        landingpageResult = new EnterpriseLandingpageData();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getCategory_outOfBounds() {

        // given one category
        landingpageResult.addEntriesForCategory(newEntries("s1", "s2"),
                newCategory("cat1"));

        // when accessing category 2, than exception must be thrown
        landingpageResult.getCategory(2);
    }

    /**
     * Add category. Category and services must be set
     */
    @Test
    public void addServicesForCategory() {

        // given category and corresponding services
        VOCategory category = newCategory("cat1");
        List<POLandingpageEntry> services = newEntries("s1", "s2");

        // when
        landingpageResult.addEntriesForCategory(services, category);

        // then data is set
        assertNotNull(landingpageResult.category0);
        assertNotNull(landingpageResult.entriesOfCateogry0);
        assertNull(landingpageResult.category1);
        assertNull(landingpageResult.category2);
    }

    @Test
    public void addServicesForCategory_twice() {

        // given
        VOCategory category1 = newCategory("cat1");
        List<POLandingpageEntry> services1 = newEntries("s1", "s2");
        VOCategory category2 = newCategory("cat2");
        List<POLandingpageEntry> services2 = newEntries("s3", "s4");

        // when
        landingpageResult.addEntriesForCategory(services1, category1);
        landingpageResult.addEntriesForCategory(services2, category2);

        // then
        assertNotNull(landingpageResult.category0);
        assertNotNull(landingpageResult.category1);
        assertNull(landingpageResult.category2);
    }

    /**
     * Maximum of three categories can be stored
     */
    @Test(expected = IllegalStateException.class)
    public void addServicesForCategory_tooOften() {
        landingpageResult.addEntriesForCategory(newEntries("s1", "s2"),
                newCategory("cat1"));
        landingpageResult.addEntriesForCategory(newEntries("s2", "s3"),
                newCategory("cat2"));
        landingpageResult.addEntriesForCategory(newEntries("s4", "s5"),
                newCategory("cat3"));
        landingpageResult.addEntriesForCategory(newEntries("s6", "s7"),
                newCategory("cat4"));
    }

    private VOCategory newCategory(String categoryId) {
        VOCategory category = new VOCategory();
        category.setCategoryId(categoryId);
        category.setName(categoryId);
        return category;
    }

    private List<POLandingpageEntry> newEntries(String... serviceIds) {
        List<POLandingpageEntry> entries = new ArrayList<POLandingpageEntry>();
        for (String serviceId : serviceIds) {
            POLandingpageEntry entry = new POLandingpageEntry();
            entry.setServiceId(serviceId);
            entry.setName(serviceId);
            entries.add(entry);
        }
        return entries;
    }
}
