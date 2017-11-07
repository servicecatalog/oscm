/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedBillingResource;

public class LocalizedBillingResourceDAOTest {

    private static LocalizedBillingResourceDAO resourceDAO = spy(new LocalizedBillingResourceDAO());
    private static DataService ds = mock(DataService.class);
    private static LocalizedBillingResource r1 = new LocalizedBillingResource();
    private static LocalizedBillingResource r2 = new LocalizedBillingResource();

    private final static long KEY1 = 1000L;
    private final static long KEY2 = 2000L;

    @Before
    public void setup() throws Exception {
        resourceDAO.dm = ds;
        r1.setKey(KEY1);
        r2.setKey(KEY2);

        doReturn(r1).when(ds).find(LocalizedBillingResource.class, KEY1);
        doReturn(r2).when(ds).find(LocalizedBillingResource.class, KEY2);
        doReturn(r2).when(ds).find(any(LocalizedBillingResource.class));
        doNothing().when(ds).persist(any(LocalizedBillingResource.class));
    }

    @Test
    public void getWithKey() {
        // when
        LocalizedBillingResource result = resourceDAO.get(r1);

        // then
        assertTrue(result.getKey() == KEY1);
    }

    @Test
    public void getListWithKey() {
        // given
        List<LocalizedBillingResource> resourceList = Arrays.asList(r1, r2);

        // when
        List<LocalizedBillingResource> result = resourceDAO.get(resourceList);

        // then
        assertEquals(2, result.size());
    }

    @Test
    public void getNoKey() {
        // given
        LocalizedBillingResource resource = new LocalizedBillingResource();

        // when
        LocalizedBillingResource result = resourceDAO.get(resource);

        // then
        assertTrue(result.getKey() == KEY2);
    }
}
