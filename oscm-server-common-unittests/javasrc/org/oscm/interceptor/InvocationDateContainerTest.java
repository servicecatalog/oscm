/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: barzu                                   
 *                                                                              
 *  Creation Date: Oct 6, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 7, 2011                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Remote;
import javax.interceptor.Interceptors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.test.ClassFilter;
import org.oscm.test.PackageClassReader;

import static org.junit.Assert.assertTrue;

/**
 * Checks that all beans implementing remote interfaces are annotated with
 * <code>@Interceptors(InvocationDateContainer.class)</code>.
 * 
 * @author barzu
 */
@Ignore
public class InvocationDateContainerTest {

    private Map<Class<?>, List<Class<?>>> mapping;
    private List<Class<?>> classes;
    private static List<String> excludes;

    private static boolean hasInterceptor(Class<?> clazz,
            Class<?> interceptorClass) {
        Interceptors annotation = clazz.getAnnotation(Interceptors.class);
        if (annotation != null) {
            for (Class<?> currentInterceptorClazz : annotation.value()) {
                if (interceptorClass.equals(currentInterceptorClazz)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void findAnnotatedStatelessBeans(File directory,
            String packageName, Class<? extends Annotation> annotationClazz,
            List<Class<?>> classes, Map<Class<?>, List<Class<?>>> mapping)
            throws ClassNotFoundException {

        if (!directory.exists()) {
            return;
        }

        List<Class<?>> foundClasses = PackageClassReader.findClasses(directory,
                packageName, null, ClassFilter.STATELESS_BEANS_ONLY);

        for (Class<?> currentclass : foundClasses) {
            List<Class<?>> lif = getAnnotatedInterfaces(currentclass,
                    annotationClazz);
            String name = currentclass.getName();
            boolean isExcluded = false;
            for (String excluded : excludes) {
                if (name.startsWith(excluded)) {
                    isExcluded = true;
                    break;
                }
            }
            if (!isExcluded && lif.size() > 0) {
                classes.add(currentclass);
                mapping.put(currentclass, lif);
            }
        }
    }

    private static List<Class<?>> getAnnotatedInterfaces(Class<?> clazz,
            Class<? extends Annotation> annotationClazz) {
        List<Class<?>> localInterfaces = new ArrayList<Class<?>>();
        for (Class<?> c : clazz.getInterfaces()) {
            if (c.getAnnotation(annotationClazz) != null) {
                localInterfaces.add(c);
            }
        }
        return localInterfaces;
    }

    @Before
    public void setUp() throws Exception {
        mapping = new HashMap<Class<?>, List<Class<?>>>();
        classes = new ArrayList<Class<?>>();
        excludes = new ArrayList<String>();
        // do not check the APP beans
        excludes.add("org.oscm.app.");
        String directory = "..";
        findAnnotatedStatelessBeans(new File(directory), "org.oscm.",
                Remote.class, classes, mapping);
    }

    @Test
    public void testBeanClasses() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Class<?> clazz : classes) {
            if (!hasInterceptor(clazz, InvocationDateContainer.class)) {
                sb.append(clazz.getName() + "\n");
            }
        }
        assertTrue("Bean classes with missing @Interceptors("
                + InvocationDateContainer.class.getSimpleName()
                + ".class) annotation found: \n" + sb.toString(),
                sb.length() == 0);
    }

}
