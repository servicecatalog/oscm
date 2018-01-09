/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.domain.ServiceInstance;

public class ServiceInstanceDAOTest {

    private static ServiceInstanceDAO siDAO;

    @Before
    public void setup() {
        siDAO = spy(new ServiceInstanceDAO());
        EntityManager em = mock(EntityManager.class);
        siDAO.em = em;
        Query query = mock(Query.class);
        doReturn(query).when(em).createNamedQuery(anyString());
    }

    @Test(expected = ServiceInstanceNotFoundException.class)
    public void getInstance_null() throws Exception {
        siDAO.getInstance(null, null, null);
    }

    @Test(expected = ServiceInstanceNotFoundException.class)
    public void getInstanceById_null() throws Exception {
        siDAO.getInstanceById(null);
    }

    @Test(expected = ServiceInstanceNotFoundException.class)
    public void getInstanceBySubscriptionAndOrganization_null()
            throws Exception {
        siDAO.getInstanceBySubscriptionAndOrganization(null, null);
    }

    @Test
    public void getInstance_instanId_null() throws Exception {
        // when
        siDAO.getInstance(null, "subId", "orgId");

        // then
        verify(siDAO, times(1)).getInstanceBySubscriptionAndOrganization(
                "subId", "orgId");
    }

    @Test
    public void getInstance_instanId_notNull() throws Exception {
        // when
        siDAO.getInstance("instanceId", "subId", "orgId");

        // then
        verify(siDAO, times(1)).getInstanceById("instanceId");
    }

    @Test
    public void prepareDeletionInstance() throws Exception {
        // given
        ServiceInstance instance = new ServiceInstance();
        instance.setSubscriptionId("subscriptionId");
        doReturn(instance).when(siDAO.em).find(eq(ServiceInstance.class),
                anyLong());
        // when
        siDAO.markAsDeleted(instance);
        // given
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(instance.getSubscriptionId().contains("#")));
        assertEquals(Boolean.FALSE, Boolean.valueOf(instance.isLocked()));
    }
}
