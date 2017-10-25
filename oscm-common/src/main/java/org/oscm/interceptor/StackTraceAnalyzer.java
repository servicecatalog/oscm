/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.12.2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.interceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds if the chain of caller classes contains the test class name pattern
 * 
 * @author malhotra
 * 
 */
public class StackTraceAnalyzer {

    Pattern TEST_CLASS_NAME_PATTERN = Pattern.compile("^(?:[a-z]?t)?est\\d*$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Checks if the chain of invoker classes contains the test class name.
     * 
     * @return true if it contains the test classes otherwise false.
     * 
     */

    public boolean containsTestClass() {
        for (StackTraceElement stackTraceElement : getStackTraceList()) {
            String className = stackTraceElement.getClassName();
            if (isTestClassName(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A test class is considered to have the suffix "Test" with an optional one
     * or two digit suffix like "BillingTest15"
     */
    boolean isTestClassName(String className) {
        String subClassName = className.substring(className.length() - 5,
                className.length());
        Matcher m = TEST_CLASS_NAME_PATTERN.matcher(subClassName);
        return m.matches();
    }

    StackTraceElement[] getStackTraceList() {
        StackTraceElement[] listStackTraceElement = new Throwable()
                .fillInStackTrace().getStackTrace();
        return listStackTraceElement;
    }
}
