/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 24.07.2012                                                      
 *                                                                                                                        
 *******************************************************************************/

package org.oscm.test.matchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.internal.vo.BaseVO;

/**
 * Hamcrest matchers for the bes.
 * 
 * @author cheld
 * 
 */
public class BesMatchers {

    /**
     * Checks if the VO object has a key set. In this case it is assumed that
     * the object is persistently stored.
     */
    public static Matcher<BaseVO> isPersisted() {
        return new BaseMatcher<BaseVO>() {
            @Override
            public boolean matches(Object object) {
                BaseVO baseVO = (BaseVO) object;
                return (baseVO.getKey() != 0);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("object has no key set");
            }
        };
    }

    public static Matcher<EJBException> isAccessDenied() {
        return new BaseMatcher<EJBException>() {

            @Override
            public boolean matches(Object exception) {
                return ((EJBException) exception).getCausedByException() instanceof EJBAccessException;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("exception is not an EJBAccessException");
            }
        };
    }

    @SuppressWarnings("rawtypes")
    public static Matcher<List<? extends DomainHistoryObject>> haveVersions(
            final long[] expectedVersions) {
        return new BaseMatcher<List<? extends DomainHistoryObject>>() {
            private long missingVersion;

            @SuppressWarnings({ "boxing", "unchecked" })
            @Override
            public boolean matches(Object object) {
                List<? extends DomainHistoryObject> domainObjectList = (List<? extends DomainHistoryObject>) object;

                assertEquals(expectedVersions.length, domainObjectList.size());

                HashSet<Long> set = new HashSet<Long>();
                for (int i = 0; i < expectedVersions.length; i++) {
                    set.add(expectedVersions[i]);
                }
                for (DomainHistoryObject<?> dho : domainObjectList) {
                    if (!set.contains(dho.getObjVersion())) {
                        missingVersion = dho.getObjVersion();
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Version " + missingVersion
                        + " is missing");
            }
        };
    }

    public static Matcher<Class<?>> containsInterceptor(final Class<?> beanClass) {
        return new BaseMatcher<Class<?>>() {
            private Class<?> testClass;

            @Override
            public boolean matches(Object object) {
                testClass = (Class<?>) object;
                Interceptors interceptors = testClass
                        .getAnnotation(Interceptors.class);

                boolean interceptorSet = false;
                if (interceptors != null) {
                    for (Class<?> definedInterceptorClass : interceptors
                            .value()) {
                        if (definedInterceptorClass == beanClass) {
                            interceptorSet = true;
                        }
                    }
                }

                assertTrue(interceptorSet);
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Class " + testClass.getName()
                        + " has no interceptor " + beanClass.getName());
            }
        };
    }

    public static Matcher<Object> hasAnnotation(
            final List<Annotation> expectedAnnotations) {
        return new BaseMatcher<Object>() {
            private Class<?> aClass;
            private Method method;
            private Annotation annotation;

            @Override
            public boolean matches(Object object) {
                boolean result = false;
                if (object instanceof Class<?>) {
                    aClass = (Class<?>) object;
                    for (Annotation expectedAnnotation : expectedAnnotations) {
                        annotation = expectedAnnotation;
                        assertNotNull(aClass.getAnnotation(expectedAnnotation
                                .annotationType()));
                    }
                    result = true;

                } else if (object instanceof Method) {
                    method = (Method) object;
                    for (Annotation expectedAnnotation : expectedAnnotations) {
                        annotation = expectedAnnotation;
                        assertNotNull(method.getAnnotation(expectedAnnotation
                                .annotationType()));
                    }
                    result = true;
                }

                return result;
            }

            @Override
            public void describeTo(Description description) {
                if (aClass != null) {
                    description.appendText("Class " + aClass.getName()
                            + " has no Annotation "
                            + annotation.annotationType().getName());

                } else if (method != null) {
                    description.appendText("Method " + method.getName()
                            + " has no Annotation "
                            + annotation.annotationType().getName());

                }
            }
        };
    }
}
