/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.setup;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DatabaseVersionInfoTest {

    @Test
    public void testGetters() {
        final DatabaseVersionInfo info = new DatabaseVersionInfo(12, 34, 56);
        assertEquals(12, info.getProductMajorVersion());
        assertEquals(34, info.getProductMinorVersion());
        assertEquals(56, info.getSchemaVersion());
    }

    @Test
    public void testCompareMajor() {
        final DatabaseVersionInfo info1 = new DatabaseVersionInfo(20, 99, 99);
        final DatabaseVersionInfo info2 = new DatabaseVersionInfo(10, 100, 100);
        assertEquals(1, info1.compareTo(info2));
        assertEquals(-1, info2.compareTo(info1));
    }

    @Test
    public void testCompareMinor() {
        final DatabaseVersionInfo info1 = new DatabaseVersionInfo(3, 20, 99);
        final DatabaseVersionInfo info2 = new DatabaseVersionInfo(3, 10, 100);
        assertEquals(1, info1.compareTo(info2));
        assertEquals(-1, info2.compareTo(info1));
    }

    @Test
    public void testCompareSchema() {
        final DatabaseVersionInfo info1 = new DatabaseVersionInfo(3, 99, 15);
        final DatabaseVersionInfo info2 = new DatabaseVersionInfo(3, 99, 12);
        assertEquals(1, info1.compareTo(info2));
        assertEquals(-1, info2.compareTo(info1));
    }

}
