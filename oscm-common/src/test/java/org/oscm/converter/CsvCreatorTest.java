/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: Aug 5, 2011                                                      
 *                                                                              
 *  Completion Time: Aug 5, 2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * @author tokoda
 * 
 */
public class CsvCreatorTest {

    private static final String[] TESTCASE_NORMAL = new String[] { "aa", "11",
            "@#" };
    private static final String[] TESTCASE_ESCAPE = new String[] { "a,a", "\"",
            "\"\"", "a\na", "a\r\na" };
    private static final String[] TESTCASE_SPACE = new String[] { " ", "\t",
            "　" };
    private static final String[] TESTCASE_ZERO_LENGTH = new String[] { "a", "" };
    private static final String[] TESTCASE_NULL = null;
    private static final String[] TESTCASE_EMPTY = new String[] {};

    @Test
    public void ctors() {
        new CsvCreator();
    }

    @Test
    public void testNormal() {
        String csv = CsvCreator.createCsvLine(TESTCASE_NORMAL);
        assertEquals("aa,11,@#", csv);
    }

    @Test
    public void testEscape() {
        String csv = CsvCreator.createCsvLine(TESTCASE_ESCAPE);
        assertEquals("\"a,a\",\"\"\"\",\"\"\"\"\"\",\"a\na\",\"a\r\na\"", csv);
    }

    @Test
    public void testSpace() {
        String csv = CsvCreator.createCsvLine(TESTCASE_SPACE);
        assertEquals(" ,\t,　", csv);
    }

    @Test
    public void testZeroLength() {
        String csv = CsvCreator.createCsvLine(TESTCASE_ZERO_LENGTH);
        assertEquals("a,", csv);
    }

    @Test
    public void testNull() {
        String csv = CsvCreator.createCsvLine(TESTCASE_NULL);
        assertNull(csv);
    }

    @Test
    public void testEmpty() {
        String csv = CsvCreator.createCsvLine(TESTCASE_EMPTY);
        assertEquals("", csv);
    }

}
