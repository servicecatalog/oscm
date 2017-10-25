/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.landingpage;

import java.util.List;

import org.oscm.validation.Invariants;
import org.oscm.internal.base.BasePO;
import org.oscm.internal.vo.VOCategory;

/**
 * @author zankov
 * 
 */
public class EnterpriseLandingpageData extends BasePO {

    private static final long serialVersionUID = 1033158173242178237L;

    public VOCategory category0;
    public List<POLandingpageEntry> entriesOfCateogry0;

    public VOCategory category1;
    public List<POLandingpageEntry> entriesOfCateogry1;

    public VOCategory category2;
    public List<POLandingpageEntry> entriesOfCateogry2;

    /**
     * Add the given services for the given category. The data is to be shown in
     * one column of the enterprise landing page.
     */
    public void addEntriesForCategory(List<POLandingpageEntry> entries,
            VOCategory category) {
        if (category0 == null) {
            category0 = category;
            entriesOfCateogry0 = entries;
        } else if (category1 == null) {
            category1 = category;
            entriesOfCateogry1 = entries;
        } else if (category2 == null) {
            category2 = category;
            entriesOfCateogry2 = entries;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Number of categories stored. The number corresponds to the number of
     * column in the landing page.
     */
    public int numberOfColumns() {
        if (category0 == null) {
            return 0;
        } else if (category1 == null) {
            return 1;
        } else if (category2 == null) {
            return 2;
        }
        return 3;
    }

    /**
     * Returns the category for the given index. The index corresponds to the
     * column of the landing page.
     */
    public VOCategory getCategory(int indexOfColumn) {
        Invariants.assertBetween(indexOfColumn, 0, 2);
        switch (indexOfColumn) {
        case 0:
            assertNotNull(category0);
            return category0;
        case 1:
            assertNotNull(category1);
            return category1;
        default:
            assertNotNull(category2);
            return category2;
        }
    }

    /**
     * Returns the services for the given index. The index corresponds to the
     * column of the landing page.
     */
    public List<POLandingpageEntry> getEntries(int indexOfColumn) {
        Invariants.assertBetween(indexOfColumn, 0, 2);
        switch (indexOfColumn) {
        case 0:
            assertNotNull(entriesOfCateogry0);
            return entriesOfCateogry0;
        case 1:
            assertNotNull(entriesOfCateogry1);
            return entriesOfCateogry1;
        default:
            assertNotNull(entriesOfCateogry2);
            return entriesOfCateogry2;
        }
    }

    private void assertNotNull(Object object) {
        if (object == null) {
            throw new IndexOutOfBoundsException();
        }
    }

}
