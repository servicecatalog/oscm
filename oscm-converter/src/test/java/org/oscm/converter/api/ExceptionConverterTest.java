/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.01.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.converter.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.oscm.converter.ExceptionConverterTestBase;
import org.oscm.converter.ExceptionInitializer;
import org.oscm.converter.generator.VOConverterTestGenerator;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author baumann
 */
public class ExceptionConverterTest {

    private static final String[] EXCEPTIONS_CONVERT_TO_UP = new String[] {
            "org.oscm.types.exceptions.ObjectNotFoundException",
            "org.oscm.types.exceptions.OperationNotPermittedException",
            "org.oscm.types.exceptions.PaymentDataException",
            "org.oscm.types.exceptions.PaymentDeregistrationException",
            "org.oscm.types.exceptions.PSPCommunicationException",
            "org.oscm.types.exceptions.PSPProcessingException" };

    private static final String EXCEPTION_PACKAGE = "org.oscm.internal.types.exception";

    // Key: exception class name, Value: test error
    private Map<String, Throwable> errors;

    @Before
    public void setup() {
        ExceptionConverterTestBase.initWarnings();
        errors = new HashMap<String, Throwable>();
    }

    @Test
    public void convertToApi() throws Exception {
        for (String exClassName : getExceptionClassNames()) {
            try {
                // given
                Object ex = ExceptionInitializer.create(exClassName);
                assertNotNull("Exception for class " + exClassName
                        + " not created", ex);

                // when
                Object exV12 = ExceptionConverterTestBase.convertToApi(ex);

                // then
                if (exV12 != null) {
                    ExceptionConverterTestBase.assertExceptions(ex, exV12);
                }
            } catch (Throwable t) {
                errors.put(exClassName, t);
            }
        }

        ExceptionConverterTestBase.outputWarnings();
        if (!errors.isEmpty()) {
            fail(errorMessage());
        }
    }

    private List<String> getExceptionClassNames() {
        List<String> exceptionClassNames = new ArrayList<String>();

        List<Class<?>> exClasses;
        try {
            exClasses = Arrays.asList((Class<?>[]) VOConverterTestGenerator
                    .getClasses(EXCEPTION_PACKAGE));
        } catch (ClassNotFoundException e) {
            return exceptionClassNames;
        } catch (IOException e) {
            return exceptionClassNames;
        }

        for (Class<?> exClass : exClasses) {
            if ((exClass != SaaSApplicationException.class)
                    && SaaSApplicationException.class.isAssignableFrom(exClass)
                    && !Modifier.isAbstract(exClass.getModifiers())) {
                exceptionClassNames.add(exClass.getName());
            }
        }

        Collections.sort(exceptionClassNames);
        return exceptionClassNames;
    }

    private String errorMessage() {
        StringBuffer errorMsg = new StringBuffer();
        errorMsg.append("The test failed because of the following errors:");
        for (String exClassName : errors.keySet()) {
            errorMsg.append(System.getProperty("line.separator"));
            errorMsg.append(exClassName + ": "
                    + errors.get(exClassName).getMessage());
        }

        return errorMsg.toString();
    }

    @Test
    public void convertToUp() throws Exception {
        for (String exClassName : EXCEPTIONS_CONVERT_TO_UP) {
            try {
                // given
                Object exV13 = ExceptionInitializer.create(exClassName);
                assertNotNull("Exception for class " + exClassName
                        + " not created", exV13);

                // when
                Object ex = ExceptionConverterTestBase.convertToUp(exV13);

                // then
                ExceptionConverterTestBase.assertExceptions(exV13, ex);
            } catch (Throwable t) {
                errors.put(exClassName, t);
            }
        }

        ExceptionConverterTestBase.outputWarnings();
        if (!errors.isEmpty()) {
            fail(errorMessage());
        }
    }
}
