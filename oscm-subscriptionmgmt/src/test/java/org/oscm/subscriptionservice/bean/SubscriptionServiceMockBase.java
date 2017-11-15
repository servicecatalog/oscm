/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.02.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.subscriptionservice.bean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 * A base class for subscription service test mocking.
 * 
 * @author goebel
 */
public class SubscriptionServiceMockBase {

    static Object mockAllMembers(Object instance) throws Exception {
        mockInjects(instance);
        mockEJBs(instance);
        mockResources(instance);
        return instance;
    }

    static void copyMocks(Object bean, List<Object> mocks) throws Exception {
        for (Object mock : mocks) {
            for (Field f : bean.getClass().getDeclaredFields()) {
                if (isMockable(f)) {
                    Object member = f.get(bean);
                    copyMock(member, mock);
                }

            }
        }
    }

    static void copyMock(Object member, Object mock) throws Exception {
        for (Field f : member.getClass().getDeclaredFields()) {
            if (isMockable(f)) {
                mockMember(member, mock, f);
            }
        }
    }

    private static void mockMember(Object member, Object mock, Field f)
            throws IllegalAccessException {
        Class<?> t = f.getType();
        if (t.isInstance(mock)) {
            f.setAccessible(true);
            f.set(member, mock);
        }
    }

    static void mockEJBs(Object instance) throws Exception {
        for (Field f : instance.getClass().getDeclaredFields()) {
            EJB ejb = f.getAnnotation(EJB.class);
            if (ejb != null) {
                Class<?> t = f.getType();
                f.setAccessible(true);
                f.set(instance, mock(t));
            }
        }
    }

    static void mockResources(Object instance) throws Exception {
        for (Field f : instance.getClass().getDeclaredFields()) {
            Resource resource = f.getAnnotation(Resource.class);
            if (resource != null) {
                f.setAccessible(true);
                Class<?> t = f.getType();
                f.set(instance, mock(t));
            }
        }
    }

    static void mockInjects(Object instance) throws Exception {
        for (Field f : instance.getClass().getDeclaredFields()) {
            Inject injected = f.getAnnotation(Inject.class);
            if (injected != null) {
                Class<?> t = f.getType();
                f.set(instance, mock(t));
            }
        }
    }

    static void spyInjected(Object instance, List<Class<?>> spys)
            throws Exception {
        for (Field f : instance.getClass().getDeclaredFields()) {
            Inject inject = f.getAnnotation(Inject.class);
            if (inject != null) {
                Class<?> clazz = f.getType();
                Object bean = mockAllMembers(clazz.newInstance());
                f.set(instance, spyAll(bean, spys));
                continue;
            }
        }
    }

    private static Object spyAll(Object bean, List<Class<?>> spys) {
        for (Class<?> clazz : spys) {
            if (clazz.isInstance(bean)) {
                bean = spy(bean);
                return bean;
            }
        }
        return bean;
    }

    private static boolean isMockable(Field f) {
        return ((f.getAnnotation(Inject.class) != null)
                || (f.getAnnotation(Resource.class) != null) || f
                    .getAnnotation(EJB.class) != null);

    }
}
