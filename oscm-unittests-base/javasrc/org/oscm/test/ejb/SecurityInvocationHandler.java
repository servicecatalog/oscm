/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.ejb.SessionContext;

/**
 * Invocation handler that encapsulates EJB security concerns.
 * 
 * @author hoffmann
 */
class SecurityInvocationHandler implements IInvocationHandler {

    private final SessionContext sessionContext;

    private final String[] rolesAllowed;

    SecurityInvocationHandler(SessionContext sessionContext, Method beanMethod) {
        this.sessionContext = sessionContext;
        RolesAllowed rolesAllowed = beanMethod.getAnnotation(RolesAllowed.class);

        // a somewhat nasty scenario: a bean is spied using Mockito, so the
        // roles allowed annotations have to be retrieved from the superclass...
        Class<?> declaringClass = beanMethod.getDeclaringClass();
        Class<?> superclass = declaringClass.getSuperclass();
        if (declaringClass.getName().contains("Mockito")
                && !superclass.equals(Object.class)) {
            try {
                Method method = superclass.getMethod(beanMethod.getName(),
                        beanMethod.getParameterTypes());
                rolesAllowed = method.getAnnotation(RolesAllowed.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (rolesAllowed == null) {
            this.rolesAllowed = new String[0];
        } else {
            this.rolesAllowed = rolesAllowed.value();
        }
    }

    public Object call(Callable<Object> callable, IInvocationCtx ctx)
            throws Exception {
        checkAllowed();
        return callable.call();
    }

    private void checkAllowed() {
        if (rolesAllowed.length == 0) {
            return;
        }
        for (String r : rolesAllowed) {
            if (sessionContext.isCallerInRole(r)) {
                return;
            }
        }
        throw new EJBAccessException("Allowed roles are: "
                + Arrays.asList(rolesAllowed));
    }

}
