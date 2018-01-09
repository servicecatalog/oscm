/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 19, 2011                                                      
 *                                                                              
 *  Completion Time: July 20, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ejb;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

/**
 * Test stub implementation for a context factory builder.
 * 
 * @author Dirk Bernsau
 * 
 */
public class TestNamingContextFactoryBuilder implements
        InitialContextFactoryBuilder {

    private final TestPersistence PERSISTENCE;
    private Context ctx;

    public TestNamingContextFactoryBuilder() {
        PERSISTENCE = new TestPersistence();
    }

    public TestNamingContextFactoryBuilder(TestPersistence persistence) {
        PERSISTENCE = persistence;
    }

    public InitialContextFactory createInitialContextFactory(
            Hashtable<?, ?> environment) throws NamingException {
        Hashtable persistence = new Hashtable<>();
        persistence.put("java:appserver/TransactionManager", PERSISTENCE.getTransactionManager());
        persistence.put("org.oscm.dataservice.bean.HibernateIndexer", PERSISTENCE.getHibernateIndexer());
        environment.putAll(persistence);
        return new InitialContextFactory() {
            public Context getInitialContext(Hashtable<?, ?> environment)
                    throws NamingException {
                if (ctx == null) {
                    ctx = new TestNamingContext(environment);
                }
                return ctx;
            }
        };
    }
}
