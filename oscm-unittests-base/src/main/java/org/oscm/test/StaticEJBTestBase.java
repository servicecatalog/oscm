/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test;

import java.util.concurrent.Callable;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.oscm.interceptor.DateFactory;
import org.oscm.test.EJBTestBase.Caller;
import org.oscm.test.ejb.TestContainer;

/**
 * Test base for all tests that require a EJB test container but only a single
 * setup. Subclasses also have to provide a <code>public static void</code>
 * parameterless method to do the specific setup referencing {@link #container}.
 * 
 * @author weiser
 */
public abstract class StaticEJBTestBase extends BaseAdmUmTest {

    protected static TestContainer container;
    private static Caller caller;
    private static String hsSearchBackup;

    @BeforeClass
    public static void basicSetup() throws Exception {
        enableJndiMock();
        restoreDateFactory();
        // remember state of hibernate search listener property
        hsSearchBackup = System.getProperty(HS_SEARCH_LISTENERS);
        enableHibernateSearchListeners(false);
        PERSISTENCE.initialize();
        container = new TestContainer(PERSISTENCE);
        container.addBean(new TransactionBean());
        caller = container.get(Caller.class);
    }

    @AfterClass
    public static void tearDown() {
        if (hsSearchBackup != null) {
            // reset property value
            System.setProperty(HS_SEARCH_LISTENERS, hsSearchBackup);
        }
    }

    /**
     * This little bit of magic allows our test code to execute in the scope of
     * a container controlled transaction.
     * 
     * @author hoffmann
     */
    @Stateless
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private static class TransactionBean implements Caller {
        @Override
        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }
    }

    protected static <T> T runTX(Callable<T> callable) throws Exception {
        DateFactory.getInstance().takeCurrentTime();
        return caller.call(callable);
    }

    /**
     * The class DateFactory is used to create the history modification
     * timestamps. Sometimes test cases redefine the behavior to set artificial
     * data in the history tables. This method restores the normal date factory
     * in case a test case did not clean up properly.
     */
    private static void restoreDateFactory() {
        DateFactory.setInstance(new DateFactory());
    }

}
