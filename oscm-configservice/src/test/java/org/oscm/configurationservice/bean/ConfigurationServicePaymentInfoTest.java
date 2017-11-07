/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-5-20
 *
 *******************************************************************************/
package org.oscm.configurationservice.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

public class ConfigurationServicePaymentInfoTest {
    
    private ConfigurationServiceBean csb;
    private DataService ds;
    private TypedQuery query;
    private ConfigurationSetting setting;

    @Before
    public void setup() throws Exception {
        csb = new ConfigurationServiceBean();
        ds = mock(DataService.class);
        csb.dm = ds;
        query = mock(TypedQuery.class);
    }

    @Test
    public void isPaymentInfoAvailableByDefault() {
        
        //given
        setting = new ConfigurationSetting(ConfigurationKey.HIDE_PAYMENT_INFORMATION, "global","FALSE");
        when(query.getSingleResult()).thenReturn(setting);
        when(ds.createNamedQuery(anyString(), eq(ConfigurationSetting.class))).thenReturn(query);
        csb.setConfigurationSetting(setting);
        
        //when
        boolean paymentInfoAvailable = csb.isPaymentInfoAvailable();
        
        //then
        assertTrue(paymentInfoAvailable);
    }

    @Test
    public void isPaymentInfoAvailableAfterChange() {
        
        //given
        setting = new ConfigurationSetting(ConfigurationKey.HIDE_PAYMENT_INFORMATION, "global","TRUE");
        when(query.getResultList()).thenReturn(Arrays.asList(setting));
        when(ds.createNamedQuery(anyString(), eq(ConfigurationSetting.class))).thenReturn(query);
        csb.setConfigurationSetting(setting);
        csb.refreshCache();
        
        //when
        boolean paymentInfoAvailable = csb.isPaymentInfoAvailable();
        
        //then
        assertFalse(paymentInfoAvailable);
    }
}
