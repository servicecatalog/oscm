/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.ApplicationException;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.transaction.TransactionManager;

import org.mockito.Mockito;

import org.oscm.interceptor.DateFactory;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.test.ejb.IInvocationHandler.IInvocationCtx;

/**
 * Wrapper for a deployed session bean.
 * 
 * @author hoffmann
 */
class DeployedSessionBean {

    private final TransactionManager tm;

    private final Object bean;

    private final Map<Method, IInvocationHandler> ihCache = new HashMap<Method, IInvocationHandler>();

    private final InvocationHandler handler = new InvocationHandler() {

        @Override
        public Object invoke(Object proxy, final Method method,
                final Object[] args) throws Throwable {
            final IInvocationHandler ih = getInvocationHandler(method);
            final IInvocationCtx ctx = new InvocationCtxImpl(method);
            final Callable<Object> callable = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    try {
                        invokeInterceptors(method);
                        return method.invoke(bean, args);
                    } catch (InvocationTargetException ex) {
                        Throwable target = ex.getTargetException();
                        if (target instanceof Exception) {
                            throw (Exception) target;
                        }
                        if (target instanceof Error) {
                            throw (Error) target;
                        }
                        throw new RuntimeException(
                                "Unexpected Exception type.", target);
                    }
                }

                /**
                 * If the service bean has any interceptor (is annotated with
                 * <code>@Interceptors</code>), then it calls the method
                 * annotated with <code>@AroundInvoke</code> of each
                 * interceptor.
                 * 
                 * @throws Exception
                 */
                private void invokeInterceptors(Method method) throws Exception {
                    Interceptors interceptors = bean.getClass().getAnnotation(
                            Interceptors.class);
                    if (interceptors != null) {
                        for (Class<?> clazz : interceptors.value()) {
                            // bug10496: implement the InvocationDate
                            // interceptor in the test container,
                            // because of ClassCastException (EjbInvocation).
                            if (clazz.getName().equals(
                                    InvocationDateContainer.class.getName())) {
                                invokeInvocationDateInterceptor(method);

                            } else {
                                invokeInterceptor(method, clazz);
                            }
                        }
                    }
                }

            };
            return ih.call(callable, ctx);
        }
    };

    private InvocationContext createInvocationContext(final Method method) {
        InvocationContext ic = new InvocationContext() {

            @Override
            public Object getTarget() {
                return null;
            }

            @Override
            public Method getMethod() {
                return method;
            }

            @Override
            public Constructor<?> getConstructor() {
                return null;
            }

            @Override
            public Object[] getParameters() {
                return null;
            }

            @Override
            public void setParameters(Object[] params) {
            }

            @Override
            public Map<String, Object> getContextData() {
                return null;
            }

            @Override
            public Object proceed() {
                return null;
            }

            @Override
            public Object getTimer() {
                return null;
            }
        };

        return ic;
    }

    private void invokeInvocationDateInterceptor(Method method) {
        if (method.getDeclaringClass().getAnnotation(Remote.class) != null) {
            DateFactory.getInstance().takeCurrentTime();
        }
    }

    private void invokeInterceptor(Method method, Class<?> clazz)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException {
        for (Method interceptorMethod : clazz.getMethods()) {
            if (interceptorMethod.getAnnotation(AroundInvoke.class) != null) {

                Object instance = clazz.newInstance();

                for (Field field : instance.getClass().getDeclaredFields()) {
                    EJB ejb = field.getAnnotation(EJB.class);
                    Inject inject = field.getAnnotation(Inject.class);
                    if (ejb != null || inject != null) {
                        field.setAccessible(true);
                        field.set(instance, Mockito.mock(field.getType()));
                    }
                }

                InvocationContext ic = createInvocationContext(method);
                interceptorMethod.invoke(instance, ic);
                break;
            }
        }
    }

    private final SessionContext sessionContext;

    public DeployedSessionBean(TransactionManager tm,
            SessionContext sessionContext, Object bean) {
        this.tm = tm;
        this.sessionContext = sessionContext;
        this.bean = bean;
    }

    /**
     * Returns an instance of the given interface type. The bean must implement
     * this interface.
     * 
     * @param <T>
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getInterfaceOrClass(Class<T> type) {
        if (!type.isInstance(bean)) {
            throw new IllegalArgumentException(bean + " does not implement "
                    + type);
        }
        if (type.isInterface()) {
            return type.cast(Proxy.newProxyInstance(bean.getClass()
                    .getClassLoader(), new Class[] { type }, handler));
        } else {
            return (T) bean;
        }
    }

    /**
     * Returns the original bean object.
     */
    public Object getBean() {
        return bean;
    }

    private IInvocationHandler getInvocationHandler(Method interfaceMethod) {
        IInvocationHandler ih = ihCache.get(interfaceMethod);
        if (ih == null) {
            ih = createInvocationHandler(interfaceMethod);
            ihCache.put(interfaceMethod, ih);
        }
        return ih;
    }

    private IInvocationHandler createInvocationHandler(Method interfaceMethod) {
        final Method beanMethod;
        try {
            beanMethod = bean.getClass().getMethod(interfaceMethod.getName(),
                    interfaceMethod.getParameterTypes());

        } catch (Exception e) {
            // Must not happen as the bean has to implement the interface.
            throw new RuntimeException(e);
        }
        final IInvocationHandler h1 = TransactionInvocationHandlers
                .getHandlerFor(bean.getClass(), beanMethod);
        final IInvocationHandler h2 = new SecurityInvocationHandler(
                sessionContext, beanMethod);

        return new ChainedInvocationHandler(h1, h2);
    }

    private class InvocationCtxImpl implements IInvocationCtx {

        private final Method interfaceMethod;

        InvocationCtxImpl(Method interfaceMethod) {
            this.interfaceMethod = interfaceMethod;
        }

        @Override
        public TransactionManager getTransactionManager() {
            return tm;
        }

        @Override
        public boolean isApplicationException(Exception e) {
            for (Class<?> declaredEx : interfaceMethod.getExceptionTypes()) {
                if (declaredEx.isInstance(e)) {
                    return true;
                }
            }
            if (e.getClass().getAnnotation(ApplicationException.class) != null) {
                return true;
            }
            return false;
        }

    }

}
