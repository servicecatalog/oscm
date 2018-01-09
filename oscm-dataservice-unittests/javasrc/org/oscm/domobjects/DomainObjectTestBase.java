/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: schmid                                
 *                                                                              
 *  Creation Date: 09.02.2009                                                      
 *                                                                              
 *  Completion Time:                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Base class for all Domain Object Tests
 * <nl>
 * <li>Bootstraps openEJB</li>
 * <li>Deploys transactionBean for transactional calls</li>
 * <li>Get reference to DataManager</li>
 * </nl>
 * 
 * @author schmid
 * 
 */
public class DomainObjectTestBase extends EJBTestBase {

    protected static final String USER_GUEST = "guest";

    protected DataService mgr;

    @Override
    public void setup(final TestContainer container) throws Exception {

        AESEncrypter.generateKey();
        container.login(USER_GUEST);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());

        mgr = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(mgr);
                dataSetup();
                return null;
            }
        });
    }

    protected void dataSetup() throws Exception {

    }

    /**
     * Verifies that the elements of the the given list are stored correctly in
     * the database. This includes the verification of the last history object.
     */
    protected <T extends DomainObject<?>> void verify(
            final ModificationType modType, final List<T> list,
            final Class<T> clazz) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (T obj : list) {
                    Assert.assertTrue("Nothing to verify", list.size() > 0);
                    try {
                        DomainObject<?> savedObj = mgr.getReference(clazz,
                                obj.getKey());
                        Assert.assertTrue(
                                ReflectiveCompare.showDiffs(obj, savedObj),
                                ReflectiveCompare.compare(obj, savedObj));
                    } catch (ObjectNotFoundException e) {
                        if (modType != ModificationType.DELETE) {
                            throw e;
                        }
                    }
                    if (!obj.hasHistory()) {
                        return null;
                    }

                    List<DomainHistoryObject<?>> history = mgr.findHistory(obj);
                    if (modType == ModificationType.ADD) {
                        Assert.assertEquals(
                                "Wrong number of history objects found", 1,
                                history.size());
                    } else {
                        Assert.assertTrue(
                                "Wrong number of history objects found",
                                history.size() > 1);
                    }

                    // the last entry in the history table must reflect the
                    // current object
                    DomainHistoryObject<?> historyObj = history
                            .get(history.size() - 1);
                    Assert.assertEquals("modtype", modType,
                            historyObj.getModtype());
                    Assert.assertEquals("moduser", "guest",
                            historyObj.getModuser());
                    Assert.assertTrue(
                            ReflectiveCompare.showDiffs(obj, historyObj),
                            ReflectiveCompare.compare(obj, historyObj));
                    Assert.assertEquals("objkey in history different",
                            obj.getKey(), historyObj.getObjKey());
                }
                return null;
            }
        });
    }

    protected <T extends DomainObject<?>> T load(final Class<T> clazz,
            final long key) throws Exception {
        return runTX(new Callable<T>() {
            @Override
            public T call() {
                return mgr.find(clazz, key);
            }
        });
    }

    protected Long persist(final DomainObject<?> dObj) throws Exception {
        return runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                mgr.persist(dObj);
                return Long.valueOf(dObj.getKey());
            }
        });
    }

}
