/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.cdi;

import java.lang.reflect.Method;

import org.oscm.test.ejb.TestContainer;

public class ObserverMethod {
    private TestContainer beanManager;
    private Method method;

    public ObserverMethod(TestContainer beanManager, Method method) {
        this.beanManager = beanManager;
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getParameterType() {
        return method.getParameterTypes()[0];
    }

    public void invoke(Object object) {
        try {
            method.invoke(beanManager.get(method.getDeclaringClass()), object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
