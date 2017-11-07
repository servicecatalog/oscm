/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.local;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


@Ignore
public class TransactionMandatoryTest {

    private List<Method> methods;

    private List<Method> getLocalInterfaceMethods()
            throws ClassNotFoundException {
        Map<Class<?>, List<Class<?>>> mapping = new HashMap<Class<?>, List<Class<?>>>();
        List<Class<?>> classes = new ArrayList<Class<?>>();

        String directory = "..";
        String packageName = "org.oscm.";
        findClasses(new File(directory), packageName, classes, mapping);
        return findMethods(classes, mapping);
    }

    private void findClasses(File directory, String packageName,
            List<Class<?>> classes, Map<Class<?>, List<Class<?>>> mapping)
            throws ClassNotFoundException {

        if (!directory.exists()) {
            return;
        }

        List<Class<?>> foundClasses = PackageClassReader.findClasses(directory,
                packageName, null, ClassFilter.STATELESS_BEANS_ONLY);

        for (Class<?> currentclass : foundClasses) {
            List<Class<?>> lif = getLocalInterfaces(currentclass);
            if (lif.size() > 0) {
                classes.add(currentclass);
                mapping.put(currentclass, lif);
            }
        }
    }

    private List<Class<?>> getLocalInterfaces(Class<?> clazz) {
        List<Class<?>> localInterfaces = new ArrayList<Class<?>>();
        for (Class<?> c : clazz.getInterfaces()) {
            if (c.getAnnotation(Local.class) != null) {
                localInterfaces.add(c);
            }
        }
        return localInterfaces;
    }

    private List<Method> findMethods(List<Class<?>> classes,
            Map<Class<?>, List<Class<?>>> mapping) {
        List<Method> methods = new ArrayList<Method>();
        for (Class<?> clazz : classes) {
            for (Method m : clazz.getMethods()) {
                if (fromLocalInterface(m, mapping.get(clazz))) {
                    methods.add(m);
                }
            }
        }
        return methods;
    }

    private boolean fromLocalInterface(Method m, List<Class<?>> interfaces) {
        for (Class<?> c : interfaces) {
            try {
                c.getMethod(m.getName(), m.getParameterTypes());
                return true;
            } catch (NoSuchMethodException e) {
                // not from current interface
            }
        }

        return false;
    }

    @Before
    public void setUp() throws Exception {
        methods = getLocalInterfaceMethods();
    }

    @Test
    public void testBeanMethods1() {
        String currentClass = null;
        StringBuffer sb = new StringBuffer();
        for (Method currentMethod : methods) {
            TransactionAttribute annotation = currentMethod
                    .getAnnotation(TransactionAttribute.class);
            if (annotation == null) {
                if (currentClass == null
                        || !currentClass.equals(currentMethod
                                .getDeclaringClass().toString())) {
                    currentClass = currentMethod.getDeclaringClass().toString();
                    sb.append(currentMethod.getDeclaringClass()).append(":\n");
                }
                sb.append(currentMethod.getName() + "\n");
            } else {

                switch (annotation.value()) {
                case MANDATORY:
                    break;
                case REQUIRES_NEW:
                    break;

                default:
                    if (currentClass == null
                            || !currentClass.equals(currentMethod
                                    .getDeclaringClass().toString())) {
                        currentClass = currentMethod.getDeclaringClass()
                                .toString();
                        sb.append(currentMethod.getDeclaringClass()).append(
                                ":\n");
                    }
                    sb.append(currentMethod.getName() + "\n");

                    break;
                }
            }
        }

        assertTrue("Methods with missing annotation/mandatory-flag found: \n"
                + sb.toString(), sb.length() == 0);
    }

    @Test
    public void testBeanMethods() {
        StringBuffer sb = new StringBuffer();

        for (Method currentMethod : methods) {
            TransactionAttribute annotation = currentMethod
                    .getAnnotation(TransactionAttribute.class);
            if (annotation == null
                    || annotation.value() != TransactionAttributeType.MANDATORY) {
                sb.append(currentMethod.getDeclaringClass() + "_"
                        + currentMethod.getName() + "\n");
            }
        }

        assertTrue("Methods with missing annotation/mandatory-flag found: \n"
                + sb.toString(), sb.length() == 0);
    }


}
