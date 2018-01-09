/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.impl.SimpleLogger;

/**
 * Mock generation functionality for private class loggers.
 *
 * @author Dirk Bernsau
 *
 */
public class LoggerMocking {

    /**
     * Generates a mocked logger object and tries to replace the
     * <p>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>private static final logger = ...</code>
     * <p>
     * field in the given class. The execution is best-effort, no exception will
     * be thrown.
     * <p>
     * <b>Use in test code only</b>.
     *
     * @param clazz
     *            the class for which the debug mode should be enabled
     * @return the mocked logger object
     */
    public static SimpleLogger setDebugEnabledFor(Class<?> clazz) {
        SimpleLogger mogger = Mockito.mock(SimpleLogger.class);
        Mockito.when(new Boolean(mogger.isDebugEnabled()))
                .thenReturn(Boolean.TRUE);
        if (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("logger");
                field.setAccessible(true);
                Field modifiersField = Field.class
                        .getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field,
                        field.getModifiers() & ~Modifier.FINAL);

                field.set(null, mogger);
            } catch (SecurityException e) {
                // ignore
            } catch (NoSuchFieldException e) {
                // ignore
            } catch (IllegalArgumentException e) {
                // ignore
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        return mogger;
    }

    /**
     * Adds a little (not complete) console debug output to a mocked logger.
     *
     * @param mogger
     *            the mocked logger
     */
    public static void addConsoleDebug(SimpleLogger mogger) {
        Answer<Void> answer = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                if (arguments != null && arguments.length > 0
                        && arguments[0] != null) {
                    System.out.println(arguments[0]);
                }
                return null;
            }
        };
        Mockito.doAnswer(answer).when(mogger).debug(Matchers.anyString());
        Mockito.doAnswer(answer).when(mogger).debug(Matchers.anyString(),
                Matchers.any(Throwable.class));
    }
}
