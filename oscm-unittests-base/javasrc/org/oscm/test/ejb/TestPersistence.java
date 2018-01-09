/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.test.ejb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;

import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.managed.LocalXAConnectionFactory;
import org.apache.commons.dbcp.managed.ManagedDataSource;
import org.apache.commons.dbcp.managed.XAConnectionFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.bean.HibernateIndexer;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.db.ITestDB;
import org.oscm.test.stubs.DataServiceStub;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Backend for persistence services for testing. A instance can be shared among
 * multiple test cases as long as {@link #initialize()} is called before every
 * test suite. Sharing the a instance is recommended, as Hibernate configuration
 * is a very expensive process.
 * 
 * TODO: Remove dependency on TransactionManager
 * 
 * @author hoffmann
 */
@SuppressWarnings("deprecation")

public class TestPersistence {

    private final TransactionManager transactionManager;
    private final HibernateIndexer hibernateIndexer;

    private Map<String, EntityManagerFactory> factoryCache = new HashMap<String, EntityManagerFactory>();

    private Set<ITestDB> initializedDBs = new HashSet<ITestDB>();

    private boolean runOnProductiveDB = false;
    private SessionFactory sf;

    public TestPersistence() {
        try {
            this.transactionManager = new TransactionManagerImpl();
            this.hibernateIndexer = mock(HibernateIndexer.class);
            doNothing().when(hibernateIndexer).handleIndexing(any(), any(ModificationType.class));
            hibernateIndexer.setDataService(mock(DataService.class));
        } catch (XAException e) {
            throw new RuntimeException(e);
        }

    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
    public HibernateIndexer getHibernateIndexer() {
        return hibernateIndexer;
    }


    public void initialize() throws Exception {
        initializedDBs.clear();
        runOnProductiveDB = false;
    }

    public void initializeProductiveDB() throws Exception {
        initializedDBs.clear();
        this.runOnProductiveDB = true;
    }


    public EntityManagerFactory getEntityManagerFactory(String unitName)
            throws Exception {
        final ITestDB testDb = TestDataSources.get(unitName, runOnProductiveDB);
        if (!initializedDBs.contains(testDb) && !runOnProductiveDB) {
            testDb.initialize();
            initializedDBs.add(testDb);
        }

        EntityManagerFactory f = factoryCache.get(unitName);
        if (f == null) {
            f = buildEntityManagerFactory(testDb, unitName);
            factoryCache.put(unitName, f);
        }
        return f;
    }

    private EntityManagerFactory buildEntityManagerFactory(ITestDB testDb,
            String unitName) throws Exception {
        Map<Object, Object> properties = new HashMap<Object, Object>();
        properties.put(Environment.HBM2DDL_AUTO, "");
        properties.put(Environment.DATASOURCE, createManagedDataSource(testDb
                .getDataSource()));
        properties.put("hibernate.search.autoregister_listeners", System.getProperty("hibernate.search.autoregister_listeners"));
        properties.put("hibernate.transaction.jta.platform", "org.hibernate.service.jta.platform.internal.SunOneJtaPlatform");
        properties.put("hibernate.id.new_generator_mappings", "false");
        properties.put("org.hibernate.SQL", "false");
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(unitName, properties);
        sf = ((EntityManagerFactoryImpl) entityManagerFactory).getSessionFactory();
        return entityManagerFactory;
    }

    public SessionFactory getSf() {
        return sf;
    }

    private DataSource createManagedDataSource(DataSource ds) {

        // wrap it with a LocalXAConnectionFactory
        XAConnectionFactory xaConnectionFactory = new LocalXAConnectionFactory(
                transactionManager, new DataSourceConnectionFactory(ds));

        GenericObjectPool pool = new GenericObjectPool();

        // create the pool object factory
        PoolableConnectionFactory factory = new PoolableConnectionFactory(
                xaConnectionFactory, pool, null, "SELECT DUMMY FROM DUAL",
                false, false);
        pool.setFactory(factory);

        ManagedDataSource managedDs = new ManagedDataSource(pool,
                xaConnectionFactory.getTransactionRegistry());
        managedDs.setAccessToUnderlyingConnectionAllowed(true);
        return managedDs;
    }

    public DataSource getDataSourceByName(String unitName) {
        final ITestDB testDb = TestDataSources.get(unitName, runOnProductiveDB);
        if (testDb != null) {
            return testDb.getDataSource();
        }
        return null;
    }

    /**
     * Clear cached EntityManagerFactory objects in order to enable
     * reconfiguring them (e.g. changing Hibernate Search auto listener).
     */
    public void clearEntityManagerFactoryCache() {
        factoryCache.clear();
    }


}
