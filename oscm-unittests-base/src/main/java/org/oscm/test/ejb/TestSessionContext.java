/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

class TestSessionContext implements SessionContext {

    private final TransactionManager tm;

    private final InterfaceMap<DeployedSessionBean> sessionBeans;

    private String username = null;

    private Set<String> roles = Collections.emptySet();


    TestSessionContext(TransactionManager tm,
            InterfaceMap<DeployedSessionBean> sessionBeans) {
        this.tm = tm;
        this.sessionBeans = sessionBeans;
    }

    public void setUsername(String username) {
        System.out.println("Setting user: " + username);
        this.username = username;
    }

    public void setRoles(String[] roles) {
        System.out.println("Setting roles: ");
        Arrays.stream(roles).forEach(System.out::println);
        this.roles = new HashSet<String>();
        this.roles.addAll(Arrays.asList(roles));
    }

    @Override
    @SuppressWarnings("deprecation")
    public java.security.Identity getCallerIdentity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getCallerPrincipal() {
        System.out.println("getCallerPrincipal: " + username);
        if (username == null) {
            return null;
        }
        return new Principal() {

            @Override
            public String getName() {
                return username;
            }
        };
    }

    @Override
    public EJBHome getEJBHome() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EJBLocalHome getEJBLocalHome() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties getEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getRollbackOnly() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimerService getTimerService() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserTransaction getUserTransaction() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isCallerInRole(java.security.Identity arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCallerInRole(String role) {
        return this.roles.contains(role);
    }

    @Override
    public Object lookup(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
        try {
            tm.setRollbackOnly();
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T> T getBusinessObject(Class<T> type) {
        DeployedSessionBean bean = sessionBeans.get(type);
        return bean.getInterfaceOrClass(type);
    }

    @Override
    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public EJBObject getEJBObject() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getInvokedBusinessInterface() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageContext getMessageContext() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getContextData() {
        return null;
    }

    @Override
    public boolean wasCancelCalled() throws IllegalStateException {
        return false;
    }

}
