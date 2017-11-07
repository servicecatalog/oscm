/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ExceptionConverterTestBase {
    private static List<String> warnings = new ArrayList<String>();

    public static void assertExceptions(Object expected, Object actual) {
        Class<?> expectedClazz = expected.getClass();
        Class<?> actualClazz = actual.getClass();

        try {
            assertArrayEquals(
                    "StackTrace is different",
                    (StackTraceElement[]) expectedClazz.getMethod(
                            "getStackTrace").invoke(expected),
                    (StackTraceElement[]) actualClazz
                            .getMethod("getStackTrace").invoke(actual));
            assertEquals("Exception message is different", expectedClazz
                    .getMethod("getMessage").invoke(expected), actualClazz
                    .getMethod("getMessage").invoke(actual));
            assertEquals("Message key is different",
                    expectedClazz.getMethod("getMessageKey").invoke(expected),
                    actualClazz.getMethod("getMessageKey").invoke(actual));
            assertArrayEquals("Message parameters are different",
                    (String[]) expectedClazz.getMethod("getMessageParams")
                            .invoke(expected), (String[]) actualClazz
                            .getMethod("getMessageParams").invoke(actual));
            assertEquals("Exception Identifier is different", expectedClazz
                    .getMethod("getId").invoke(expected), actualClazz
                    .getMethod("getId").invoke(actual));
            assertEquals("Cause stacktrace is different", expectedClazz
                    .getMethod("getCauseStackTrace").invoke(expected),
                    actualClazz.getMethod("getCauseStackTrace").invoke(actual));
        } catch (Exception e) {
            fail("Exception " + e.getMessage() + " occurred");
        }
    }

    public static Object convert(Class<?> exceptionConverterClazz,
            String methodName, Object exception) {
        Method m;
        try {
            m = exceptionConverterClazz.getMethod(methodName,
                    exception.getClass());
        } catch (SecurityException e) {
            fail("Security Exception when getting method " + methodName
                    + "() for class " + exception.getClass().getName());
            return null;
        } catch (NoSuchMethodException e) {
            warnings.add("Method " + methodName + "() for class "
                    + exception.getClass().getName() + " doesn't exist");
            return null;
        }

        try {
            return m.invoke(null, exception);
        } catch (IllegalArgumentException e) {
            fail("IllegalArgumentException when invoking method " + methodName
                    + "() for class " + exception.getClass().getName());
            return null;
        } catch (IllegalAccessException e) {
            fail("IllegalAccessException when invoking method " + methodName
                    + "() for class " + exception.getClass().getName());
            return null;
        } catch (InvocationTargetException e) {
            fail("InvocationTargetException when invoking method " + methodName
                    + "() for class " + exception.getClass().getName());
            return null;
        }
    }

    public static Object convertToApi(Object exception) {
        return convert(org.oscm.converter.api.ExceptionConverter.class,
                "convertToApi", exception);
    }

    public static Object convertToUp(Object exception) {
        return convert(org.oscm.converter.api.ExceptionConverter.class,
                "convertToUp", exception);
    }

    public static void initWarnings() {
        warnings = new ArrayList<String>();
    }

    public static void outputWarnings() {
        for (String warning : warnings) {
            System.out.println(warning);
        }
    }
}
