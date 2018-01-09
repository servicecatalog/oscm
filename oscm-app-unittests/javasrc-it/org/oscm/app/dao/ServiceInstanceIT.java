/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Test;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.Operation;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * Unit tests for {@link ServiceInstance}.
 * 
 * @author hoffmann
 */
public class ServiceInstanceIT extends EJBTestBase {

    private ServiceInstance instance;
    private EntityManager em;

    @Override
    protected void setup(TestContainer container) throws Exception {
        instance = new ServiceInstance();
        instance.setServiceBaseURL("baseURL");
        instance.setBesLoginURL("besLoginURL");
        instance.setDefaultLocale("de");
        instance.setOrganizationId("orgId");
        instance.setSubscriptionId("subId");
        instance.setInstanceId("appInstanceId");
        instance.setControllerId("ess.vmware");
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        em = container.getPersistenceUnit("oscm-app");
    }

    @Test
    public void testGetOperationForKey() throws Exception {
        // given
        final Operation op = new Operation();
        op.setOperationId("operationId");
        op.setParameters("parameters");
        op.setServiceInstance(instance);
        op.setUserId("userId");

        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                instance.setOperations(Arrays.asList(op));
                em.persist(instance);
                em.flush();
                return null;
            }
        });

        final ServiceInstance dbInstance = runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                ServiceInstance foundInstance = em.find(ServiceInstance.class,
                        Long.valueOf(instance.getTkey()));
                foundInstance.getOperations();
                if (foundInstance.getOperations().size() > 0) {
                    foundInstance.getOperations().get(0);
                }
                return foundInstance;
            }
        });

        // then
        assertNotNull(dbInstance.getOperations());
        assertEquals("operationId", dbInstance.getOperations().get(0)
                .getOperationId());
        assertEquals("parameters", dbInstance.getOperations().get(0)
                .getParameters());
    }

    @Test
    public void setInstanceParameters() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final InstanceParameter p1 = new InstanceParameter();
                p1.setParameterKey(InstanceParameter.BSS_USER);
                p1.setDecryptedValue("username");
                p1.setServiceInstance(instance);

                final InstanceParameter p2 = new InstanceParameter();
                p2.setParameterKey(InstanceParameter.BSS_USER_PWD);
                p2.setDecryptedValue("secret");
                p2.setServiceInstance(instance);

                instance.setInstanceParameters(Arrays.asList(p1, p2));
                em.persist(instance);

                final HashMap<String, Setting> map = instance.getParameterMap();
                map.put(InstanceParameter.BSS_USER, new Setting(
                        InstanceParameter.BSS_USER, "username_new"));
                map.put("param3", new Setting("param3", "value3new"));
                map.put(null, new Setting(null, "null")); // should be silently
                                                          // ignored
                instance.setInstanceParameters(map);

                em.flush();

                return null;
            }
        });

        final Map<String, Setting> params = runTX(new Callable<Map<String, Setting>>() {
            @Override
            public Map<String, Setting> call() throws Exception {
                Query query = em
                        .createQuery("SELECT si FROM ServiceInstance si");
                List<?> resultList = query.getResultList();
                if (resultList.isEmpty()) {
                    return null;
                }
                ServiceInstance si = (ServiceInstance) resultList.get(0);
                return si.getParameterMap();
            }
        });

        final Map<String, String> expected = new HashMap<>();
        expected.put(InstanceParameter.BSS_USER, "username_new");
        expected.put(InstanceParameter.BSS_USER_PWD, "secret");
        expected.put("param3", "value3new");
        assertEquals("value3new", params.get("param3").getValue());
        assertEquals("username_new", params.get(InstanceParameter.BSS_USER)
                .getValue());
        assertEquals("secret", params.get(InstanceParameter.BSS_USER_PWD)
                .getValue());
    }

    @Test(expected = BadResultException.class)
    public void testSecurityException() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                final InstanceParameter p1 = new InstanceParameter();
                p1.setParameterKey(InstanceParameter.BSS_USER_PWD);
                // since setting the not encrypted value here directly, reading
                // will later fail
                p1.setParameterValue("secret");
                p1.setServiceInstance(instance);
                p1.setEncrypted(true);

                instance.setInstanceParameters(Arrays.asList(p1));
                em.persist(instance);
                em.flush();

                return null;
            }
        });

        runTX(new Callable<Map<String, Setting>>() {
            @Override
            public Map<String, Setting> call() throws Exception {
                Query query = em
                        .createQuery("SELECT si FROM ServiceInstance si");
                List<?> resultList = query.getResultList();
                if (resultList.isEmpty()) {
                    return null;
                }
                ServiceInstance si = (ServiceInstance) resultList.get(0);
                return si.getParameterMap();
            }
        });
    }
}
