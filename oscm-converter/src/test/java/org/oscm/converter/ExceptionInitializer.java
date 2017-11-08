/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.02.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author baumann
 */
public class ExceptionInitializer {

    private static final String MESSAGE = "Exception message";
    private static final String MESSAGE_KEY = "Message Key";
    private static final String[] MESSAGE_PARAMS = new String[] { "Param1",
            "Param2" };
    private static final String ID = "4711";
    private static final String CAUSE_STACKTRACE = "the cause stack";
    private static final StackTraceElement[] STACK_TRACE = new StackTraceElement[] { new StackTraceElement(
            "MyClass", "aMethod", "MyClass.java", 55) };

    public static Object create(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Object ex = constructor.newInstance(MESSAGE);

            Method m = clazz.getMethod("setStackTrace",
                    StackTraceElement[].class);
            m.invoke(ex, (Object) STACK_TRACE);
            m = clazz.getMethod("setMessageKey", String.class);
            m.invoke(ex, (Object) MESSAGE_KEY);
            m = clazz.getMethod("setMessageParams", String[].class);
            m.invoke(ex, (Object) MESSAGE_PARAMS);
            m = clazz.getMethod("setId", String.class);
            m.invoke(ex, (Object) ID);
            m = clazz.getMethod("setCauseStackTrace", String.class);
            m.invoke(ex, (Object) CAUSE_STACKTRACE);

            return ex;
        } catch (Exception e) {
            return null;
        }
    }

}
