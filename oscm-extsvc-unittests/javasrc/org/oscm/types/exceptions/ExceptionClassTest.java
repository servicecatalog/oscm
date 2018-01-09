/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 30.09.2010                                                      
 *                                                                              
 *  Completion Time: 30.09.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebFault;

import org.junit.Test;

import org.oscm.test.ClassFilter;
import org.oscm.test.PackageClassReader;
import org.oscm.types.exceptions.CurrencyException;
import org.oscm.types.exceptions.SaaSApplicationException;
import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * @author weiser
 * 
 */
public class ExceptionClassTest {

    private static final String JACOCO = "$jacocoData";

    /**
     * Test if all public API exceptions have a default constructor.
     */
    @Test
    public void testExceptionDefaultConstructors() throws Exception {
        StringBuffer errors = new StringBuffer();
        StringBuffer nonDefMessage = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(
                CurrencyException.class, Throwable.class,
                ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : classes) {
            try {
                Constructor<?> declaredConstructor = clazz
                        .getDeclaredConstructor();
                Object instance = declaredConstructor.newInstance();
                if (instance instanceof Throwable) {
                    if (((Throwable) instance).getMessage() == null) {
                        nonDefMessage.append(clazz.getSimpleName());
                        nonDefMessage.append('\n');
                    }
                }
            } catch (NoSuchMethodException e) {
                errors.append(e.getMessage());
                errors.append('\n');
            }
        }
        StringBuffer failures = new StringBuffer();
        if (errors.length() > 0) {
            failures.append("Exceptions without default constructor:\n"
                    + errors.toString());
        }
        if (nonDefMessage.length() > 0) {
            failures.append("Exceptions without default message:\n"
                    + nonDefMessage.toString());
        }
        if (failures.length() > 0) {
            fail(failures.toString());
        }
    }

    /**
     * Test if all public API exceptions have a constructor with a string
     * parameter.
     */
    @Test
    public void testExceptionStringConstructors() throws Exception {
        StringBuffer errors = new StringBuffer();
        StringBuffer nonMessage = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(
                CurrencyException.class, Throwable.class,
                ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : classes) {
            try {
                Constructor<?> declaredConstructor = clazz
                        .getDeclaredConstructor(String.class);
                Object instance = declaredConstructor.newInstance("Test");
                if (instance instanceof Throwable) {
                    if (((Throwable) instance).getMessage() == null) {
                        nonMessage.append(clazz.getSimpleName());
                        nonMessage.append('\n');
                    }
                }
            } catch (NoSuchMethodException e) {
                errors.append(e.getMessage());
                errors.append('\n');
            }
        }
        StringBuffer failures = new StringBuffer();
        if (errors.length() > 0) {
            failures.append("Exceptions without constructor(String):\n"
                    + errors.toString());
        }
        if (nonMessage.length() > 0) {
            failures.append("Exceptions not setting a message:\n"
                    + nonMessage.toString());
        }
        if (failures.length() > 0) {
            fail(failures.toString());
        }
    }

    /**
     * Test if all exceptions are designed as beans, to match the JAX-WS
     * requirements.
     */
    @Test
    public void testForBeanConformance() throws Exception {
        StringBuffer errors = new StringBuffer();
        Class<?> beanClass = ApplicationExceptionBean.class;
        // get fields
        Field[] fields = beanClass.getDeclaredFields();
        for (Field currentField : fields) {
            if (!Modifier.isFinal(currentField.getModifiers())
                    && !currentField.getName().contains(JACOCO)) {
                Method[] methods = beanClass.getMethods();
                errors.append(assertGetterExistence(currentField, methods));
                errors.append(assertSetterExistence(currentField, methods));
            }
        }

        if (errors.length() > 0) {
            fail("Exception incompatibilities for bean comformance:\n"
                    + errors.toString());
        }
    }

    // the exceptions should not contain non-final members, those should be
    // placed in the ApplicationExceptionBean
    @Test
    public void testNoMembersInExceptions() throws Exception {
        StringBuffer errors = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(
                CurrencyException.class, Throwable.class,
                ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : getApplicationExceptions(classes)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!Modifier.isFinal(field.getModifiers())
                        && !(field.getName().equals("bean"))
                        && !field.getName().contains(JACOCO)) {
                    errors.append("Class ").append(clazz.getName())
                            .append(" contains non-final field ")
                            .append(field.getName()).append("\n");
                }
            }
        }
        if (errors.length() > 0) {
            fail("Exceptions without constructor(String):\n"
                    + errors.toString());
        }
    }

    @Test
    public void testForWebFaultAnnotationInExceptions() throws Exception {
        StringBuffer errors = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(
                CurrencyException.class, Throwable.class,
                ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : getApplicationExceptions(classes)) {
            WebFault annotation = clazz.getAnnotation(WebFault.class);
            if (annotation == null) {
                errors.append("Class ").append(clazz.getName())
                        .append(" misses WebFault annotation\n");
                continue;
            }
            String name = annotation.name();
            if (!name.equals(clazz.getSimpleName())) {
                errors.append("Class ").append(clazz.getName())
                        .append(" uses a WebFault name annotation that ")
                        .append("does not match the class name.\n");
            }
        }
        if (errors.length() > 0) {
            fail("Exceptions without constructor(String):\n"
                    + errors.toString());
        }
    }

    @Test
    public void testForJAXWSConstructorsInExceptions() throws Exception {
        StringBuffer errors = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(
                CurrencyException.class, Throwable.class,
                ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : getApplicationExceptions(classes)) {
            Constructor<?>[] constructors = clazz.getConstructors();
            boolean doesConst1Exist = false;
            boolean doesConst2Exist = false;
            for (Constructor<?> constructor : constructors) {
                Class<?>[] types = constructor.getParameterTypes();
                if (types.length == 2
                        && types[0] == String.class
                        && ApplicationExceptionBean.class
                                .isAssignableFrom(types[1])) {
                    doesConst1Exist = true;
                }
                if (types.length == 3
                        && types[0] == String.class
                        && ApplicationExceptionBean.class
                                .isAssignableFrom(types[1])
                        && types[2] == Throwable.class) {
                    doesConst2Exist = true;
                }

            }
            if (!doesConst1Exist) {
                errors.append("Class ").append(clazz.getName())
                        .append(" misses constructor C(String, Bean)\n");
            }
            if (!doesConst2Exist) {
                errors.append("Class ")
                        .append(clazz.getName())
                        .append(" misses constructor C(String, Bean, Throwable)\n");
            }
        }
        if (errors.length() > 0) {
            fail("Exceptions without constructor(String):\n"
                    + errors.toString());
        }
    }

    @Test
    public void testForJAXWSGetFaultInfoInExceptions() throws Exception {
        StringBuffer errors = new StringBuffer();
        StringBuffer nonConstructors = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(
                CurrencyException.class, Throwable.class,
                ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : getApplicationExceptions(classes)) {
            Method[] methods = clazz.getMethods();
            Method faultInfoMethod = null;
            for (Method method : methods) {
                if (method.getName().equals("getFaultInfo")
                        && method.getParameterAnnotations().length == 0) {
                    if (faultInfoMethod == null
                            || faultInfoMethod.getReturnType()
                                    .isAssignableFrom(method.getReturnType())) {
                        faultInfoMethod = method;
                    }
                }
            }
            if (faultInfoMethod == null) {
                errors.append("Class ").append(clazz.getName())
                        .append(" misses method getFaultInfo\n");
            } else {
                Class<?> beanClass = faultInfoMethod.getReturnType();
                try {
                    Constructor<?> c2 = clazz.getConstructor(String.class,
                            beanClass);
                    Object sEx = c2
                            .newInstance("Test", beanClass.newInstance());
                    if (sEx instanceof SaaSApplicationException) {
                        ((SaaSApplicationException) sEx).getFaultInfo();
                    }
                } catch (NoSuchMethodException e) {
                    nonConstructors.append("missing " + clazz.getSimpleName()
                            + ".<init>(String, " + beanClass.getSimpleName()
                            + ")\n");
                }
                try {
                    Constructor<?> c3 = clazz.getConstructor(String.class,
                            beanClass, Throwable.class);
                    Object sEx = c3
                            .newInstance("Test", beanClass.newInstance(),
                                    new NullPointerException());
                    if (sEx instanceof SaaSApplicationException) {
                        ((SaaSApplicationException) sEx).getFaultInfo();
                    }
                } catch (NoSuchMethodException e) {
                    nonConstructors.append("missing " + clazz.getSimpleName()
                            + ".<init>(String, " + beanClass.getSimpleName()
                            + ", Throwable)\n");
                }
            }
        }
        StringBuffer failures = new StringBuffer();
        if (errors.length() > 0) {
            failures.append("Exceptions without getFaultInfo():\n"
                    + errors.toString());
        }
        if (nonConstructors.length() > 0) {
            failures.append(nonConstructors);
        }
        if (failures.length() > 0) {
            fail(failures.toString());
        }

    }

    private List<Class<?>> getApplicationExceptions(List<Class<?>> allExceptions) {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (Class<?> currentClass : allExceptions) {
            boolean isRuntimeException = false;
            Class<?> localClass = currentClass;
            while (localClass != Object.class) {
                if (localClass == RuntimeException.class) {
                    isRuntimeException = true;
                }
                localClass = localClass.getSuperclass();
            }
            if (!isRuntimeException) {
                result.add(currentClass);
            }
        }
        return result;
    }

    /**
     * Asserts that a getter for the given field exists among the given methods.
     * 
     * @param currentField
     *            The field a getter is needed for.
     * @param methods
     *            The methods.
     */
    private String assertGetterExistence(Field currentField, Method[] methods) {
        List<String> methodNames = new ArrayList<String>();
        for (Method method : methods) {
            methodNames.add(method.getName());
        }
        char firstChar = currentField.getName().charAt(0);
        char upperCaseFirstChar = Character.toUpperCase(firstChar);
        String methodName = "get" + upperCaseFirstChar
                + currentField.getName().substring(1);
        if (!methodNames.contains(methodName)) {
            return "Missing method '" + methodName + "'\n";
        }
        return "";
    }

    /**
     * Asserts that a setter for the given field exists among the given methods.
     * 
     * @param currentField
     *            The field a getter is needed for.
     * @param methods
     *            The methods.
     */
    private String assertSetterExistence(Field currentField, Method[] methods) {
        List<String> methodNames = new ArrayList<String>();
        for (Method method : methods) {
            methodNames.add(method.getName());
        }
        char firstChar = currentField.getName().charAt(0);
        char upperCaseFirstChar = Character.toUpperCase(firstChar);
        String methodName = "set" + upperCaseFirstChar
                + currentField.getName().substring(1);
        if (!methodNames.contains(methodName)) {
            return "Missing method '" + methodName + "'\n";
        }
        return "";
    }

}
