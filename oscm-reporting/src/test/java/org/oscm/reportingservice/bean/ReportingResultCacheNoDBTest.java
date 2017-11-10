/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-5-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.ReportResultCache;
import org.oscm.reportingservice.bean.ReportingResultCache;
import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;

/**
 * @author Yuyin
 * 
 */
public class ReportingResultCacheNoDBTest {
    private DataService dm;
    private static final String CACHEKEY_BILLING_1 = "SESSION1#0001";
    private long now;
    private final long past = 120 * 1000L + 1L;
    private ReportResultCache result;
    private final byte[] report = ReportingResultCache
            .serializeObject(new RDODetailedBilling());

    @Before
    public void setUp() throws Exception {
        dm = mock(DataService.class);
        now = System.currentTimeMillis();
        result = new ReportResultCache();

    }

    @Test
    public void get_Old() throws Exception {
        // given
        createReport(now - past);
        // when
        Object result = ReportingResultCache.get(dm, CACHEKEY_BILLING_1);

        // then
        assertNull(result);
    }

    @Test
    public void get_New() throws Exception {
        // given
        createReport(System.currentTimeMillis());
        // when
        Object result = ReportingResultCache.get(dm, CACHEKEY_BILLING_1);

        // then
        assertNotNull(result);
    }

    @Test
    public void put_New() throws Exception {
        // given
        Query query = mock(Query.class);
        doNothing().when(dm).persist(any(DomainObject.class));
        doReturn(query).when(dm).createNamedQuery(anyString());
        // when
        ReportingResultCache.put(dm, CACHEKEY_BILLING_1,
                System.currentTimeMillis(), new RDODetailedBilling());

        // then
        verify(dm, times(1)).createNamedQuery(anyString());
    }

    private void createReport(long date) {
        result.setCachekey(CACHEKEY_BILLING_1);
        Date timestamp = new Date(date);
        result.setTimestamp(timestamp);
        result.setReport(report);
        doReturn(result).when(dm).find(any(DomainObject.class));
    }
}
