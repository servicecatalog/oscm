/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;

import org.oscm.test.ejb.TestContainer;

public class ContextManager {
    private TestContainer beanManager;
    List<ObserverMethod> observerMethods;

    public ContextManager(TestContainer beanManager) {
        this.beanManager = beanManager;
        observerMethods = new ArrayList<ObserverMethod>();
    }

    public TestContainer getBeanManager() {
        return beanManager;
    }

    public void scanMethods(Object bean) {
        for (Method method : bean.getClass().getMethods()) {
            if (containsParameterAnnotation(method, Observes.class)) {
                observerMethods.add(new ObserverMethod(beanManager, method));
            }
        }
    }

    public boolean containsParameterAnnotation(Method method,
            Class<?> annotationClass) {
        return searchAnnotation(method, annotationClass) != null;
    }

    public Annotation searchAnnotation(Method method, Class<?> annotationClass) {
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == annotationClass) {
                    return annotation;
                }
            }
        }
        return null;
    }

    public void executeObserverMethod(Object object) {
        for (ObserverMethod observerMethod : findObserverMethods(object)) {
            observerMethod.invoke(object);
        }
    }

    public List<ObserverMethod> findObserverMethods(Object object) {
        List<ObserverMethod> result = new ArrayList<ObserverMethod>();
        for (ObserverMethod observerMethod : observerMethods) {
            if (observerMethod.getParameterType().isInstance(object)) {
                result.add(observerMethod);
            }
        }
        return result;
    }
}
