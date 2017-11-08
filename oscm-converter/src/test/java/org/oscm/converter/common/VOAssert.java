/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 25.01.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author kulle
 */
public class VOAssert {

    private Set<String> warning = new LinkedHashSet<String>();
    public Map<Enum<?>, Enum<?>> mapping = new HashMap<Enum<?>, Enum<?>>();

    public VOAssert(Map<Enum<?>, Enum<?>> mapping) {
        if (mapping != null) {
            this.mapping = mapping;
        }
    }

    public Set<String> getWarning() {
        return warning;
    }

    @SuppressWarnings("rawtypes")
    public static void assertValueObjectLists(List expected, List actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            assertEquals("List size is different", expected.size(),
                    actual.size());
            for (Object o : expected) {
                actual.contains(o);
            }
        }
    }

    @SuppressWarnings("null")
    public void assertValueObjects(Object expected, Object actual)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, SecurityException {

        if (expected == null && actual == null) {
            return;
        }

        List<Method> getterMethods = new ArrayList<Method>();
        List<Method> enumMethods = new ArrayList<Method>();
        List<Method> collectionMethods = new ArrayList<Method>();
        List<Method> voMethods = new ArrayList<Method>();

        Class<?> clazz = expected.getClass();
        for (Method m : clazz.getMethods()) {
            if ((m.getName().startsWith("get") || m.getName().startsWith("is"))
                    && m.getParameterTypes().length == 0) {

                if (m.getReturnType().isPrimitive()
                        || m.getReturnType() == java.lang.String.class
                        || m.getReturnType() == java.math.BigDecimal.class) {
                    getterMethods.add(m);
                } else if (m.getReturnType().isEnum()) {
                    enumMethods.add(m);
                } else if (m.getReturnType().getName()
                        .startsWith("org.oscm.vo")) {
                    voMethods.add(m);
                } else {
                    collectionMethods.add(m);
                }
            }
        }

        // assert non collection types
        for (Method m : getterMethods) {
            try {
                assertEquals(expected.getClass().getName() + ", "
                        + actual.getClass().getName() + ", " + m.getName(),
                        m.invoke(expected), getMethod(actual, m.getName())
                                .invoke(actual));
            } catch (NoSuchMethodException e) {
                warning.add("[" + getCallerMethodName() + "] " + e.getMessage()
                        + " does not exist");
            }
        }

        // assert enum types
        for (Method m : enumMethods) {
            try {
                Enum<?> e = (Enum<?>) m.invoke(expected);
                if (mapping.containsKey(e)) {
                    Enum<?> a = mapping.get(e);
                    assertEquals(m.getName(), a.toString(),
                            getMethod(actual, m.getName()).invoke(actual)
                                    .toString());
                } else {
                    assertEquals(m.getName(), e.toString(),
                            getMethod(actual, m.getName()).invoke(actual)
                                    .toString());
                }
            } catch (NoSuchMethodException e) {
                warning.add("[" + getCallerMethodName() + "] " + e.getMessage()
                        + " does not exist");
            }
        }

        // assert other value objects
        for (Method m : voMethods) {
            try {
                assertValueObjects(m.invoke(expected),
                        getMethod(actual, m.getName()).invoke(actual));
            } catch (NoSuchMethodException e) {
                warning.add("[" + getCallerMethodName() + "] " + e.getMessage()
                        + " does not exist");
            }
        }

        // assert collection types
        for (Method m : collectionMethods) {
            try {
                if (m.getReturnType().getName().equals("java.util.List")) {
                    List<?> expectedList = (List<?>) m.invoke(expected);
                    List<?> actualList = (List<?>) getMethod(actual,
                            m.getName()).invoke(actual);
                    assertEquals(m.getName() + ": list size",
                            expectedList.size(), actualList.size());
                    for (int i = 0; i < expectedList.size(); i++) {
                        assertValueObjects(expectedList.get(i),
                                actualList.get(i));
                    }
                } else if (m.getReturnType().getName().equals("java.util.Set")) {
                    Set<?> expectedSet = ((Set<?>) m.invoke(expected));
                    Set<?> actualSet = ((Set<?>) getMethod(actual, m.getName())
                            .invoke(actual));

                    assertEquals(m.getName() + ": set size",
                            expectedSet.size(), actualSet.size());
                    warning.add("[" + getCallerMethodName() + "] "
                            + "Verify sets in tests: "
                            + expected.getClass().getName() + ": "
                            + m.getName());
                }
            } catch (NoSuchMethodException e) {
                warning.add("[" + getCallerMethodName() + "] " + e.getMessage()
                        + " does not exist");
            }
        }

    }

    private Method getMethod(Object actual, String name)
            throws SecurityException, NoSuchMethodException {
        Class<?> clazz = actual.getClass();
        return clazz.getMethod(name);
    }

    private String getCallerMethodName() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String name = null;
        for (int i = 0; i < trace.length; i++) {
            if ("assertValueObjects".equals(trace[i].getMethodName())
                    && (i + 1) < trace.length) {
                name = trace[i + 1].getMethodName();
            }
        }
        return name;
    }

}
