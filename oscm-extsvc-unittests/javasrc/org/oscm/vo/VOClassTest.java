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

package org.oscm.vo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.test.ClassFilter;
import org.oscm.test.PackageClassReader;

/**
 * @author weiser
 * 
 */
public class VOClassTest {

    Set<String> commonMethodNames = new HashSet<String>();

    @Before
    public void setup() {
        Method[] methods = Object.class.getDeclaredMethods();
        for (Method method : methods) {
            commonMethodNames.add(method.getName());
        }
    }

    /**
     * Test if all public API value objects have a default constructor.
     */
    @Test
    public void testVODefaultConstructors() throws Exception {
        StringBuffer errors = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(VOService.class,
                null, ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : classes) {
            try {
                clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                errors.append(e.getMessage());
                errors.append('\n');
            }
        }
        if (errors.length() > 0) {
            Assert.fail("Value Objects without default constructor:\n"
                    + errors.toString());
        }
    }

    /**
     * Each VO member must have a getter, collections must not be initialized
     * with null.
     */
    @Test
    public void testVOGetter() throws Exception {
        StringBuffer errors = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(BaseVO.class,
                null, ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : classes) {
            Constructor<?> constructor = clazz.getConstructor();
            Object voInstance = constructor.newInstance();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    Object value = getValue(field, voInstance, errors);
                    if (isCollection(field) && value == null) {
                        errors.append("Field '").append(field.getName())
                                .append("' of type '").append(clazz.getName())
                                .append("' is initialized with null\n");
                    }

                }
            }
        }
        if (errors.length() > 0) {
            Assert.fail("\n" + errors.toString());
        }
    }

    /**
     * Each VO member must have a setter.
     */
    @Test
    public void testVOSetter() throws Exception {
        StringBuffer errors = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(BaseVO.class,
                null, ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : classes) {
            Constructor<?> constructor = clazz.getConstructor();
            Object voInstance = constructor.newInstance();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    hasSetter(field, voInstance, errors);
                }
            }
        }
        if (errors.length() > 0) {
            Assert.fail("\n" + errors.toString());
        }
    }

    /**
     * A VO must only have getter and setter which have a corresponding field
     * (with some exception which are defined below).
     */
    @Test
    public void testVOMethods() throws Exception {
        Map<Class<?>, Set<String>> allowedMethodNames = new HashMap<Class<?>, Set<String>>();
        Set<String> methodNames;

        methodNames = new HashSet<String>();
        methodNames.add("isValueTypeBoolean");
        methodNames.add("isValueTypeInteger");
        methodNames.add("isValueTypeLong");
        methodNames.add("isValueTypeString");
        methodNames.add("isValueTypeDuration");
        methodNames.add("isValueTypeEnumeration");
        methodNames.add("isValueTypePWD");
        allowedMethodNames.put(VOParameterDefinition.class, methodNames);

        methodNames = new HashSet<String>();
        methodNames.add("getCurrency");
        methodNames.add("isChargeable");
        allowedMethodNames.put(VOPriceModel.class, methodNames);

        methodNames = new HashSet<String>();
        methodNames.add("getNameToDisplay");
        allowedMethodNames.put(VOService.class, methodNames);

        methodNames = new HashSet<String>();
        methodNames.add("addUserRole");
        methodNames.add("removeUserRole");
        allowedMethodNames.put(VOUser.class, methodNames);

        methodNames = new HashSet<String>();
        methodNames.add("hasAdminRole");
        allowedMethodNames.put(VOUserDetails.class, methodNames);

        methodNames = new HashSet<String>();
        methodNames.add("setProperty");
        methodNames.add("getProperty");
        methodNames.add("asProperties");
        methodNames.add("get");
        allowedMethodNames.put(LdapProperties.class, methodNames);

        StringBuffer errors = new StringBuffer();
        List<Class<?>> classes = PackageClassReader.getClasses(BaseVO.class,
                null, ClassFilter.CLASSES_ONLY);
        for (Class<?> clazz : classes) {
            Constructor<?> constructor = clazz.getConstructor();
            Set<String> fieldNames = new HashSet<String>();
            for (Field field : clazz.getDeclaredFields()) {
                fieldNames.add(field.getName().toLowerCase());
            }
            Object voInstance = constructor.newInstance();
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if ((method.getModifiers() & Modifier.PRIVATE) == Modifier.PRIVATE
                        || commonMethodNames.contains(method.getName())) {
                    continue;
                }
                methodNames = allowedMethodNames.get(clazz);
                if (allowedMethodNames.get(clazz) != null
                        && allowedMethodNames.get(clazz).contains(
                                method.getName())) {
                    continue;
                }

                if (method.getName().startsWith("is")) {
                    if (!fieldNames.contains(method.getName().substring(2)
                            .toLowerCase())) {
                        errors.append("Missing property for method '"
                                + method.getName() + "' in type '"
                                + voInstance.getClass().getName() + "'\n");
                    }
                } else if (method.getName().startsWith("set")) {
                    if (!fieldNames.contains(method.getName().substring(3)
                            .toLowerCase())) {
                        errors.append("Missing property for method '"
                                + method.getName() + "' in type '"
                                + voInstance.getClass().getName() + "'\n");
                    }
                } else if (method.getName().startsWith("get")) {
                    if (!fieldNames.contains(method.getName().substring(3)
                            .toLowerCase())) {
                        errors.append("Missing property for method '"
                                + method.getName() + "' in type '"
                                + voInstance.getClass().getName() + "'\n");
                    }
                } else {
                    errors.append("Wrong method name '" + method.getName()
                            + "' in type '" + voInstance.getClass().getName()
                            + "'\n");
                }
            }
        }
        if (errors.length() > 0) {
            Assert.fail("\n" + errors.toString());
        }
    }

    /**
     * Retrieves the field value for the given instance. Will add a message to
     * the errors in case no corresponding getter is available.
     * 
     * @param field
     *            The field containing the desired data.
     * @param voInstance
     *            The instance to read the data from.
     * @param errors
     *            The string buffer to add the error messages for.
     */
    private Object getValue(Field field, Object voInstance, StringBuffer errors)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Method[] methods = voInstance.getClass().getMethods();
        for (Method method : methods) {
            try {
                if (method.getName().equalsIgnoreCase("get" + field.getName())) {
                    return method.invoke(voInstance);
                }
                if (method.getName().equalsIgnoreCase("is" + field.getName())) {
                    return method.invoke(voInstance);
                }
            } catch (IllegalArgumentException e) {
                errors.append("IllegalArgumentException"
                        + " for getter method of field '" + field.getName()
                        + "' of type '" + voInstance.getClass().getName()
                        + "'\n");
            }
        }
        errors.append("No getter method found for field '" + field.getName()
                + "' of type '" + voInstance.getClass().getName() + "'\n");
        return null;
    }

    /**
     * Verifies that there is a setter for the given field. Will add a message
     * to the errors buffer in case no corresponding setter is available.
     * 
     * @param field
     *            The field for which the setter is searched.
     * @param voInstance
     *            The instance of the field and setter.
     * @param errors
     *            The string buffer to add the error messages for.
     */
    private void hasSetter(Field field, Object voInstance, StringBuffer errors) {
        Method[] methods = voInstance.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase("set" + field.getName())) {
                if (method.getParameterTypes().length == 1) {
                    // we found the setter
                    return;
                }
            }
        }
        errors.append("No setter method found for field '" + field.getName()
                + "' of type '" + voInstance.getClass().getName() + "'\n");
    }

    /**
     * Determines if the specified field is a collection.
     * 
     * @param field
     *            The field to check.
     * @return <code>true</code> if the field is a collection field,
     *         <code>false</code> otherwise.
     */
    private boolean isCollection(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }
}
