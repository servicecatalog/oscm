/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                               
 *                                                                              
 *  Creation Date: 22.02.2012                                                      
 *                                                                              
 *  Completion Time: 22.02.2012                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

import javax.ejb.EJBException;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityExistsException;
import javax.persistence.OptimisticLockException;

import org.junit.Test;

import org.oscm.types.exceptions.ConnectException;
import org.oscm.types.exceptions.InvalidUserSession;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.TechnicalServiceActiveException;
//import com.sun.xml.ws.rx.rm.runtime.sequence.persistent.PersistenceException;

/**
 * Test cases for the EJBExceptionMapper.
 * 
 * @author cheld
 * 
 */
public class ExceptionMapperTest {

    ExceptionMapper mapper = new ExceptionMapper();

    @Test
    public void mapToBesException_null() {
        assertNull(mapper.mapToBesException(null));
    }

    @Test
    public void mapToBesException_noKnownCause() {
        EJBException e = new EJBException();
        Exception mappedException = mapper.mapToBesException(e);
        assertEquals(e, mappedException);
    }

    @Test
    public void mapToBesException_entityExistsException() {
        EJBException e = new EJBException(new EntityExistsException());
        Exception mappedException = mapper.mapToBesException(e);
        assertTrue(mappedException instanceof ConcurrentModificationException);
    }

    @Test
    public void mapToBesException_optimisticLockException() {
        Exception e = new OptimisticLockException();
        Exception mappedException = mapper.mapToBesException(e);
        assertTrue(mappedException instanceof ConcurrentModificationException);
    }

    @Test
    public void mapToBesException_optimisticLockExceptionWrapped() {
        EJBException e = new EJBException(new OptimisticLockException());
        Exception mappedException = mapper.mapToBesException(e);
        assertTrue(mappedException instanceof ConcurrentModificationException);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void mapEJBExceptions() throws Exception {
        mapper.mapEJBExceptions(new InvocationContextStub() {
            public Object proceed() throws Exception {
                throw new EJBException(new EntityExistsException());
            }
        });
    }

    @Test(expected = ConnectException.class)
    public void mapEJBExceptions_WrappedConnectionException() throws Exception {

        mapper.mapEJBExceptions(new InvocationContextStub() {
            public Object proceed() throws Exception {
                throw givenHeavyNestedConnectionException();
            }
        });
    }

    @Test
    public void mapEJBExceptions_validateCause() throws Exception {
        EJBException e = new EJBException(new OptimisticLockException());
        Exception mappedException = mapper.mapToBesException(e);
        assertTrue(mappedException instanceof ConcurrentModificationException);
        assertEquals(null, mappedException.getCause());
    }

    @Test
    public void mapEJBExceptions_noExceptionThrown() throws Exception {
        mapper.mapEJBExceptions(new InvocationContextStub());
    }

    @Test
    public void isCausedByConnectionException_WrappedConnectionException() {
        // given
        Exception ex = givenHeavyNestedConnectionException();
        // when
        boolean isConnectionException = mapper
                .isCausedByConnectionException(ex);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(isConnectionException));
    }

    @Test
    public void isCausedByConnectionException_MessageException() {
        // given
        javax.mail.MessagingException ex = new javax.mail.MessagingException(
                "mail cannot connect", new java.net.ConnectException());
        // when
        boolean isConnectionException = mapper
                .isCausedByConnectionException(ex);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(isConnectionException));
    }

    @Test
    public void isCausedByConnectionException_Bug10465() {
        // given
        SaaSApplicationException saasEx = new SaaSApplicationException(
                "mail cannot connect", new java.net.ConnectException());
        TechnicalServiceActiveException ex = new TechnicalServiceActiveException();
        ex.initCause(saasEx);
        // when
        boolean isConnectionException = mapper
                .isCausedByConnectionException(ex);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(isConnectionException));
    }

    @Test
    public void isCausedByConnectionException_OtherEJBException() {
        // given
        EJBException ex = new EJBException();

        // when
        boolean isConnectionException = mapper
                .isCausedByConnectionException(ex);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(isConnectionException));
    }

    @Test
    public void mapToBesException_connectionException_B10167() {
        // given
        Exception ex = givenHeavyNestedConnectionException();

        // when
        Exception mappedException = mapper.mapToBesException(ex);

        // then
        assertTrue(mappedException instanceof ConnectException);
        assertTrue(mappedException instanceof RuntimeException);
    }

    private class InvocationContextStub implements InvocationContext {

        public Map<String, Object> getContextData() {
            return null;
        }

        public Method getMethod() {
            return null;
        }

        public Object[] getParameters() {
            return null;
        }

        public Object getTarget() {
            return null;
        }

        public Object proceed() throws Exception {
            return null;
        }

        public void setParameters(Object[] arg0) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.interceptor.InvocationContext#getTimer()
         */
        @Override
        public Object getTimer() {
            return null;
        }

        @Override
        public Constructor<?> getConstructor() {
            return null;
        }

    }

    /**
     * In scenario of bug 10167, in case of a stopped DB, an InvalidUserSession
     * is thrown with a connect exception very deep in the cause chain.
     */
    private Exception givenHeavyNestedConnectionException() {
//        EJBException e = new EJBException(new PersistenceException("PE",
        EJBException e = new EJBException(new RuntimeException("PE",
                new SQLException(new java.net.ConnectException())));
        return new InvalidUserSession("Invalid User", e);
    }
}
