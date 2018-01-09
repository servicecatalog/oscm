/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 15.03.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.jws.WebMethod;

import org.junit.Before;
import org.junit.Test;

import org.oscm.test.ClassFilter;
import org.oscm.test.PackageClassReader;

/**
 * Test that verifies that there are no clashes with the web service names.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class WebMethodNamesTest {

    private List<Class<?>> classes;

    private ClassFilter filter = new ClassFilter() {
        @Override
        public boolean isNeglectableClass(Class<?> clazz) {
            return (clazz.getAnnotation(Remote.class) == null);
        }
    };

    @Before
    public void setUp() throws Exception {
        classes = PackageClassReader.getClasses(IdentityService.class, null,
                filter);
    }

    /**
     * Determines the methods annotated as web methods, and marks them in a map
     * according to the class name of the interface.
     */
    @Test
    public void testMethodNames() throws Exception {
        StringBuffer sb = new StringBuffer();
        Map<String, Method> classToMethodNameMap = new HashMap<>();
        for (Class<?> currentInterface : classes) {
            for (Method currentMethod : currentInterface.getMethods()) {
                if (currentMethod.getAnnotation(WebMethod.class) != null) {
                    String key = currentMethod.getName();
                    Method oldValue = classToMethodNameMap.put(key,
                            currentMethod);
                    if (oldValue != null) {
                        sb.append(currentMethod).append(" conflicts with ")
                                .append(oldValue).append("\n");
                    }
                }
            }
            classToMethodNameMap.clear();
        }
        assertTrue("Conflicting methods found: \n" + sb.toString(),
                sb.length() == 0);
    }
}
