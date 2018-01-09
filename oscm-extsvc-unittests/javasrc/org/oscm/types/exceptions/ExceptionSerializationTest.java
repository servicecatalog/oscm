/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.types.exceptions;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.oscm.test.ClassFilter;
import org.oscm.test.PackageClassReader;
import org.oscm.types.exceptions.SaaSApplicationException;

/**
 * Test XML serialization/deserialization symmetry for all exception classes.
 * 
 * @author hoffmann
 * 
 */
public class ExceptionSerializationTest {

    @Test
    public void testSettersGetters() throws Exception {
        List<Class<?>> classes = PackageClassReader.getClasses(
                SaaSApplicationException.class, Throwable.class,
                ClassFilter.CLASSES_ONLY);
        for (Class<?> c : classes) {
            checkExceptionType(c);
        }
    }

    public void checkExceptionType(Class<?> type) throws Exception {
        final BeanInfo beanInfo = Introspector.getBeanInfo(type);
        for (final PropertyDescriptor desc : beanInfo.getPropertyDescriptors()) {
            final String name = desc.getName();
            if (!EXCLUDED_PROPERTIES.contains(name)) {
                if (desc.getReadMethod().getDeclaringClass().equals(type)
                        && desc.getWriteMethod() == null) {
                    reportProblem(type, name, "missing setter");
                }
            }
        }
        try {
            type.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            reportProblem(type, "message", "missing String constructor");
        }
        try {
            type.getConstructor();
        } catch (NoSuchMethodException e) {
            reportProblem(type, "", "missing default constructor");
        }
    }

    private void reportProblem(Class<?> type, String member, String desc) {
        System.out.printf("%s,%s,%s%n", type.getName(), member, desc);
    }

    // Excluded properties as specified in JAX-WS 2.2 Spec, page 46
    private static final Set<String> EXCLUDED_PROPERTIES;

    static {
        Set<String> set = new HashSet<String>();
        set.add("message");
        set.add("cause");
        set.add("localizedMessage");
        set.add("stackTrace");
        set.add("class");
        EXCLUDED_PROPERTIES = Collections.unmodifiableSet(set);
    }

}
