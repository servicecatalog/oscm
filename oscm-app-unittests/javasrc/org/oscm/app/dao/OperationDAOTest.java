/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.dao;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.app.domain.Operation;
import org.oscm.app.domain.ServiceInstance;

public class OperationDAOTest {

    private static OperationDAO opDAO = spy(new OperationDAO());
    private static EntityManager em = mock(EntityManager.class);

    @Before
    public void setup() {
        opDAO.em = em;
        doNothing().when(em).persist(anyObject());
        doReturn(mock(Query.class)).when(em).createNamedQuery(anyString());
    }

    @Test
    public void addOperationForQueue() {
        // given
        Operation op = new Operation();
        doReturn(op).when(opDAO).createOperationForQueue(
                any(ServiceInstance.class), any(Properties.class), anyString());

        // when
        opDAO.addOperationForQueue(new ServiceInstance(), new Properties(),
                new String());

        // then
        verify(em).persist(op);
    }

    @Test
    public void getOperation_emptyQueue() {
        // given
        doThrow(new NoResultException()).when(opDAO).createOperationForQueue(
                any(ServiceInstance.class), any(Properties.class), anyString());

        // when
        assertNull(opDAO.getOperationFromQueue("anyInstanceId"));
    }

}
