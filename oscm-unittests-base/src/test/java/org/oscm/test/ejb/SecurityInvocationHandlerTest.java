/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.transaction.TransactionManager;

import org.junit.Before;
import org.junit.Test;

import org.oscm.test.ejb.IInvocationHandler.IInvocationCtx;

/**
 * Unit tests for {@link SecurityInvocationHandler}.
 * 
 * @author hoffmann
 */
public class SecurityInvocationHandlerTest {

    private TestSessionContext sessionContext;

    private Callable<Object> callable;

    private IInvocationCtx ctx;

    @Before
    public void setup() {
        sessionContext = new TestSessionContext(null, null);
        callable = new Callable<Object>() {
            @Override
            public Void call() {
                return null;
            }
        };
        ctx = new IInvocationCtx() {
            @Override
            public TransactionManager getTransactionManager() {
                return null;
            }

            @Override
            public boolean isApplicationException(Exception e) {
                return false;
            }
        };
    }

    /**
     * No roles required for methods without annotations.
     */
    @Test
    public void testNoAnnotation() throws Exception {
        class Bean {
            @SuppressWarnings("unused")
            public void doit() {
            }
        }
        Method m = Bean.class.getMethod("doit");

        IInvocationHandler h = new SecurityInvocationHandler(sessionContext, m);
        h.call(callable, ctx);
    }

    /**
     * Roles specified with {@link RolesAllowed}.
     */
    @Test
    public void testRolesAllowedAnnotationPositive() throws Exception {
        class Bean {
            @RolesAllowed({ "master", "slave" })
            public void doit() {
            }
        }
        Method m = Bean.class.getMethod("doit");
        sessionContext.setRoles(new String[] { "master" });

        IInvocationHandler h = new SecurityInvocationHandler(sessionContext, m);
        h.call(callable, ctx);
    }

    /**
     * Roles specified with {@link RolesAllowed}.
     */
    @Test(expected = EJBAccessException.class)
    public void testRolesAllowedAnnotationNegative() throws Exception {
        class Bean {
            @RolesAllowed({ "master", "slave" })
            public void doit() {
            }
        }
        Method m = Bean.class.getMethod("doit");
        sessionContext.setRoles(new String[] { "other" });

        IInvocationHandler h = new SecurityInvocationHandler(sessionContext, m);
        h.call(callable, ctx);
    }

}
