/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;

import org.junit.Test;

/**
 * @author malhotra
 * 
 */
public class StackTraceAnalyzerTest {

    StackTraceAnalyzer stackTrace = new StackTraceAnalyzer();

    /**
     * Caller info is executed from test, so it must return true
     */
    @Test
    public void containsTestClass() {
        assertTrue(stackTrace.containsTestClass());
    }

    @Test
    public void containsTestClass_ProductiveClass() {

        // given a mock stack trace which is similar to production
        StackTraceAnalyzer info = spy(new StackTraceAnalyzer());
        StackTraceElement element = new StackTraceElement("CallerInfoBean", "",
                "", 2);
        StackTraceElement[] listStackTraceElement = { element };
        given(info.getStackTraceList()).willReturn(listStackTraceElement);

        // when searching for test class
        boolean found = info.containsTestClass();

        // then no test class is found in stack trace
        assertFalse(found);
    }

    @Test
    public void isTestClassName() {
        assertTrue(stackTrace.isTestClassName("SearchServiceBeanTest"));
    }

    @Test
    public void isTestClassName_oneDigitSuffix() {
        assertTrue(stackTrace.isTestClassName("SomeClassNameWithTest5"));
    }

    @Test
    public void isTestClassName_wrongSuffix() {
        assertFalse(stackTrace.isTestClassName("SomeClassNameWithTest5a"));
    }

    @Test
    public void isTestClassName_twoDigitSuffix() {
        assertTrue(stackTrace.isTestClassName("SomeClassNameWithTest15"));
    }

    @Test
    public void isTestClassName_productiveClass() {
        assertFalse(stackTrace.isTestClassName("SearchServiceBean"));
    }
}
