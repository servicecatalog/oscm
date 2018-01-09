/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.service.APPConcurrencyServiceBean;

/**
 * Unit tests for {@link APPConcurrencyServiceBean}.
 * 
 * @author soehnges
 */
public class APPConcurrencyBeanIT extends EJBTestBase {

    private final static String CTRL_ID = "test.controller";
    private final static String CTRL_ID2 = "test2.controller";
    private APPConcurrencyServiceBean proxy;

    private EntityManager em;
    private ServiceInstanceDAO instanceDAO;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(instanceDAO = new ServiceInstanceDAO());
        container.addBean(proxy = new APPConcurrencyServiceBean());
        em = instanceDAO.em;
        proxy.em = em;
    }

    @Test
    public void testServiceLock() throws Exception {
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId1", "sub1");
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId2", "sub2");
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId3", "sub3");

        // Lock first service
        assertTrue(lockService(CTRL_ID, "appId1"));

        // => others can't be locked anymore
        assertFalse(lockService(CTRL_ID, "appId2"));
        assertFalse(lockService(CTRL_ID, "appId3"));

        unlockService(CTRL_ID, "appId1");

        // Now another one
        assertTrue(lockService(CTRL_ID, "appId2"));

        // => others can't be locked anymore
        assertFalse(lockService(CTRL_ID, "appId1"));
        assertFalse(lockService(CTRL_ID, "appId3"));

        // But same is still possible...
        assertTrue(lockService(CTRL_ID, "appId2"));

        unlockService(CTRL_ID, "appId1");
        unlockService(CTRL_ID, "appId2");
        unlockService(CTRL_ID, "appId3");
    }

    @Test
    public void testServiceLockTwoControllers() throws Exception {
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId1", "sub1");
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId2", "sub2");
        createService(ProvisioningStatus.COMPLETED, CTRL_ID2, "appId1", "sub3");
        createService(ProvisioningStatus.COMPLETED, CTRL_ID2, "appId3", "sub4");

        // Lock first and second service
        assertTrue(lockService(CTRL_ID, "appId1"));
        assertTrue(lockService(CTRL_ID2, "appId1"));

        // => others can't be locked anymore
        assertFalse(lockService(CTRL_ID, "appId2"));
        assertFalse(lockService(CTRL_ID2, "appId3"));

        unlockService(CTRL_ID, "appId1");

        // Now another one
        assertTrue(lockService(CTRL_ID, "appId2"));

        // => others can't be locked anymore
        assertFalse(lockService(CTRL_ID, "appId1"));
        assertFalse(lockService(CTRL_ID2, "appId3"));

        // But same is still possible...
        assertTrue(lockService(CTRL_ID, "appId2"));
        assertTrue(lockService(CTRL_ID2, "appId1"));

        unlockService(CTRL_ID, "appId1");
        unlockService(CTRL_ID, "appId2");
        unlockService(CTRL_ID2, "appId1");
        unlockService(CTRL_ID2, "appId3");
    }

    @Test
    public void testServiceExists() throws Exception {
        // given
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId1", "sub1");

        // when
        boolean exists = exists(CTRL_ID, "appId1");

        // then
        assertTrue(exists);
    }

    @Test
    public void testServiceNotExistsWrongController() throws Exception {
        // given
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId1", "sub1");

        // when
        boolean exists = exists("ctrl.other", "appId1");

        // then
        assertFalse(exists);
    }

    @Test
    public void testServiceNotExists() throws Exception {
        // given
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId1", "sub1");

        // when
        boolean exists = exists(CTRL_ID, "appId1_noex");

        // then
        assertFalse(exists);
    }

    @Test(expected = APPlatformException.class)
    public void testLockServiceNotExists() throws Exception {
        // when
        lockService(CTRL_ID, "appId1_noex");
    }

    @Test
    public void testGetInstanceParameter_Empty() throws Exception {
        // given
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId1", "sub1");

        // when
        List<InstanceParameter> params = getInstanceParameters("appId1");

        // then
        assertTrue(params.isEmpty());
    }

    @Test
    public void testGetInstanceParameter_Existing() throws Exception {
        // given
        String[] given = givenParameterKeyValues();
        createService(ProvisioningStatus.COMPLETED, CTRL_ID, "appId1", "sub1",
                given);

        // when
        List<InstanceParameter> params = getInstanceParameters("appId1");

        // then
        assertParameters(given, params);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceParameter_InstanceNull() throws Exception {

        // when
        getInstanceParameters(null);
    }

    private String createService(final ProvisioningStatus status,
            final String controllerId, final String instanceId,
            final String subscriptionId, final String... paramKeyValues)
            throws Exception {
        return runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                ServiceInstance si = new ServiceInstance();
                si.setOrganizationId("org123");
                si.setSubscriptionId(subscriptionId);
                si.setServiceBaseURL("http://localhost/");
                si.setBesLoginURL("http://localhost/");
                si.setDefaultLocale("en");
                si.setInstanceId(instanceId);
                si.setControllerId(controllerId);
                si.setProvisioningStatus(status);
                final List<InstanceParameter> params = new ArrayList<InstanceParameter>();
                final Iterator<String> i = Arrays.asList(paramKeyValues)
                        .iterator();
                while (i.hasNext()) {
                    final InstanceParameter p = new InstanceParameter();
                    p.setParameterKey(i.next());
                    p.setParameterValue(i.next());
                    p.setServiceInstance(si);
                    params.add(p);
                }
                si.setInstanceParameters(params);
                em.persist(si);
                return String.valueOf(si.getTkey());
            }
        });
    }

    private boolean lockService(final String controllerId,
            final String serviceId) throws Exception {
        Boolean rc = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(proxy.lockServiceInstance(controllerId,
                        serviceId));
            }
        });

        return rc.booleanValue();
    }

    public List<InstanceParameter> getInstanceParameters(final String instanceId)
            throws Exception {

        List<InstanceParameter> res = runTX(new Callable<List<InstanceParameter>>() {
            @Override
            public List<InstanceParameter> call() throws Exception {
                ServiceInstance si = null;
                if (instanceId != null)
                    si = instanceDAO.getInstanceById(instanceId);
                return instanceDAO.getInstanceParameters(si);
            }
        });
        return res;
    }

    private void unlockService(final String controllerId, final String serviceId)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                proxy.unlockServiceInstance(controllerId, serviceId);
                return null;
            }
        });
    }

    private boolean exists(final String controllerId, final String serviceId)
            throws Exception {
        Boolean rc = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(instanceDAO.exists(controllerId,
                        serviceId));
            }
        });

        return rc.booleanValue();
    }

    private String[] givenParameterKeyValues() {
        return new String[] { "param1", "val1", "param2", "val2", "param3",
                "val3" };
    }

    private void assertParameters(String[] expected,
            List<InstanceParameter> params) {
        List<String> paramList = new ArrayList<String>();
        for (InstanceParameter p : params) {
            paramList.add(p.getParameterKey());
            paramList.add(p.getParameterValue());
        }
        assertArrayEquals(expected, paramList.toArray());
    }
}
