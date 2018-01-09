/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class PropertiesComparatorTest {

    private Properties a;
    private Properties b;

    @Before
    public void setup() {
        a = new Properties();
        b = new Properties();
    }

    @Test
    public void testGetAdditionalKeys() {
        a.setProperty("xxx", "X-Value");
        a.setProperty("yyy", "Y-Value");
        a.setProperty("zzz", "Z-Value");

        b.setProperty("aaa", "A-Value");
        b.setProperty("bbb", "B-Value");
        b.setProperty("xxx", "X-Value");
        b.setProperty("yyy", "Y-Value");
        b.setProperty("zzz", "Z-Value");

        final PropertiesComparator comp = new PropertiesComparator(a, b);
        assertEquals(set("aaa", "bbb"), comp.getAdditionalKeys());
    }

    @Test
    public void testMissingAdditionalKeys() {
        a.setProperty("xxx", "X-Value");
        a.setProperty("yyy", "Y-Value");
        a.setProperty("zzz", "Z-Value");

        b.setProperty("zzz", "Z-Value");

        final PropertiesComparator comp = new PropertiesComparator(a, b);
        assertEquals(set("xxx", "yyy"), comp.getMissingKeys());
    }

    @Test
    public void testFindVariables() {
        final Set<String> vars = PropertiesComparator
                .findVariables("Hello {1}, this is your {2}.");
        assertEquals(set("{1}", "{2}"), vars);
    }

    @Test
    public void testGetDifferentVariables() {
        a.setProperty("aaa", "A-Value");
        a.setProperty("xxx", "Hello {3}!");
        a.setProperty("yyy", "{0} or {1}");
        a.setProperty("zzz", "{3} is important, {4} too.");

        a.setProperty("bbb", "B-Value");
        b.setProperty("xxx", "Guten Tag {3}!");
        b.setProperty("yyy", "{1} or {0}");
        b.setProperty("zzz", "{3} ist wichtig.");

        final PropertiesComparator comp = new PropertiesComparator(a, b);
        assertEquals(set("zzz"), comp.getDifferentVariables());
    }

    private <T> Set<T> set(@SuppressWarnings("unchecked") T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
}
