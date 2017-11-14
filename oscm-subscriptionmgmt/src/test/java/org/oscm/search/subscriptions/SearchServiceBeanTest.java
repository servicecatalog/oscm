/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2009                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.search.subscriptions;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.search.BooleanQuery;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.domobjects.Subscription;
import org.oscm.subscriptionservice.bean.SubscriptionSearchServiceBean;

public class SearchServiceBeanTest {

    @Test
    public void searchSubscriptions() throws Exception {
        DataServiceBean bean = spy(new DataServiceBean());
        SubscriptionSearchServiceBean ssb = spy(
                new SubscriptionSearchServiceBean());
        doReturn(bean).when(ssb).getDm();

        FullTextEntityManager ftem = mock(FullTextEntityManager.class,
                Mockito.RETURNS_DEEP_STUBS);
        doReturn(ftem).when(ssb).getFtem();

        Subscription sub = new Subscription();
        sub.setKey(1L);
        FullTextQuery fullTextQuery = mock(FullTextQuery.class);
        when(ftem.createFullTextQuery(any(BooleanQuery.class),
                any(Class.class))).thenReturn(fullTextQuery);
        doReturn(Arrays.asList(sub)).when(fullTextQuery).getResultList();

        Collection<Long> result = ssb.searchSubscriptions("searchphrase");
        assertTrue(result.contains(new Long(1L)));
    }
}
