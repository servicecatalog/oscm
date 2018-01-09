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

public class PropertiesSyntaxCheckerTest {

    private Properties a;

    @Before
    public void setup() {
        a = new Properties();
    }

    @Test
    public void testGetSyntaxErrors_NoQuotes_OnlyParam() {
        a.setProperty("prop", "{0}");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_NoQuotes_Begin() {
        a.setProperty("prop", "{0} is wrong value.");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_NoQuotes_Middle() {
        a.setProperty("prop", "Value {0} is expected.");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_NoQuotes_End() {
        a.setProperty("prop", "Expected value is {0}");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_DoubleQuotes_OnlyParam() {
        a.setProperty("prop", "''{0}''");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_DoubleQuotes_Begin() {
        a.setProperty("prop", "''{0}'' is wrong value.");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_DoubleQuotes_Middle() {
        a.setProperty("prop", "Value ''{0}'' is expected.");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_DoubleQuotes_End() {
        a.setProperty("prop", "Expected value is ''{0}''");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_SingleQuotes_OnlyParam() {
        a.setProperty("prop", "'{0}'");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(set("prop"), checker.getSyntaxSingleQuotesErrorKeys());
    }

    @Test
    public void testGetSyntaxErrors_SingleQuotes_Begin() {
        a.setProperty("prop", "'{0}' is wrong value.");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(set("prop"), checker.getSyntaxSingleQuotesErrorKeys());
    }

    @Test
    public void testGetSyntaxErrors_SingleQuotes_Middle() {
        a.setProperty("prop", "Value '{0}' is expected.");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(set("prop"), checker.getSyntaxSingleQuotesErrorKeys());
    }

    @Test
    public void testGetSyntaxErrors_SingleQuotes_End() {
        a.setProperty("prop", "Expected value is '{0}'");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(set("prop"), checker.getSyntaxSingleQuotesErrorKeys());
    }

    @Test
    public void testGetSyntaxErrors_IrregularQuotes_OnlyParam() {
        a.setProperty("prop", "'{0}");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_IrregularQuotes_Begin() {
        a.setProperty("prop", "'{0} is wrong value.");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_IrregularQuotes_Middle() {
        a.setProperty("prop", "Value '''{0}' is expected.");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_IrregularQuotes_End() {
        a.setProperty("prop", "Expected value is ''{0}'");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(0, checker.getSyntaxSingleQuotesErrorKeys().size());
    }

    @Test
    public void testGetSyntaxErrors_MoreData() {
        a.setProperty("prop1", "Expected value is {0}");
        a.setProperty("prop2", "Expected value is '{0}'");
        final PropertiesSyntaxChecker checker = new PropertiesSyntaxChecker(a);
        assertEquals(set("prop2"), checker.getSyntaxSingleQuotesErrorKeys());
    }

    private <T> Set<T> set(@SuppressWarnings("unchecked") T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
}
