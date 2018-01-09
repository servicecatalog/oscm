/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Aug 4, 2011                                                      
 *                                                                              
 *  Completion Time: Aug 4, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.build.ant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the XPath evaluation custom ANT condition.
 * 
 * @author Dirk Bernsau
 * 
 */
public class XPathConditionTest {

    private static final File TESTDIR = new File(
            "resources/result/reports/test");

    private static final String JUNIT_XPATH = "/testsuites/testsuite[@errors>0 or @failures>0]";

    private XPathCondition condition;

    @Before
    public void setUp() {
        condition = new XPathCondition();
    }

    @Test(expected = BuildException.class)
    public void testNothingSet() {
        condition.eval();
    }

    @Test(expected = BuildException.class)
    public void testNoFile() {
        condition.setPath("/hello");
        condition.eval();
    }

    @Test(expected = BuildException.class)
    public void testNoExpression() {
        condition.setFile(TESTDIR + "/test.xml");
        condition.eval();
    }

    @Test(expected = BuildException.class)
    public void testInvalidFile() {
        condition.setFile(TESTDIR + "/test_not_exists.xml");
        condition.setPath("/antlib");
        condition.eval();
    }

    @Test
    public void testSimple() {
        condition.setFile(TESTDIR + "/test.xml");
        condition.setPath("/antlib");
        assertTrue(condition.eval());
    }

    @Test
    public void testNegative() {
        condition.setFile(TESTDIR + "/test.xml");
        condition.setPath("/antbob");
        assertFalse(condition.eval());
    }

    @Test
    public void testUnitResultPositive() {
        condition.setFile(TESTDIR + "/TESTS-green.xml");
        condition.setPath(JUNIT_XPATH);
        assertFalse(condition.eval());
    }

    @Test
    public void testUnitResultErrors() {
        condition.setFile(TESTDIR + "/TESTS-errors.xml");
        condition.setPath(JUNIT_XPATH);
        assertTrue(condition.eval());
    }

    @Test
    public void testUnitResultFailure() {
        condition.setFile(TESTDIR + "/TESTS-failure.xml");
        condition.setPath(JUNIT_XPATH);
        assertTrue(condition.eval());
    }

    @Test
    public void testUnitResultAllWrong() {
        condition.setFile(TESTDIR + "/TESTS-mess.xml");
        condition.setPath(JUNIT_XPATH);
        assertTrue(condition.eval());
    }
}
