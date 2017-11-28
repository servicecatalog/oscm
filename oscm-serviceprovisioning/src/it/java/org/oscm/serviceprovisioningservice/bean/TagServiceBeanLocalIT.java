/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                                                    
 *                                                                              
 *  Creation Date: 02.05.2010                                                      
 *                                                                              
 *  Completion Time:                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Test;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.app.control.ApplicationServiceBaseStub;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.local.TagServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;
import org.oscm.triggerservice.bean.TriggerQueueServiceBean;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests for the tagging service bean.
 * 
 * @author Enes Sejfi
 * 
 */
public class TagServiceBeanLocalIT extends EJBTestBase {

    private TagServiceLocal ts;

    private static final int TAGGING_MIN_SCORE = 5;
    private static final int TAGGING_MAX_TAGS = 20;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);

        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new TriggerQueueServiceBean());
        container.addBean(new SessionServiceStub());
        container.addBean(new ApplicationServiceBaseStub());
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new ImageResourceServiceStub());
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new TagServiceBean());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());

        ts = container.get(TagServiceLocal.class);
        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);

    }

    @Test
    public void testMinScoreValue() throws Exception {
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return Long.valueOf(ts.getTaggingMinScore());
            }
        });
        assertEquals(TAGGING_MIN_SCORE, result.longValue());
    }

    @Test
    public void testMaxScoreValue() throws Exception {
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return Long.valueOf(ts.getTaggingMaxTags());
            }
        });
        assertEquals(TAGGING_MAX_TAGS, result.longValue());
    }

}
