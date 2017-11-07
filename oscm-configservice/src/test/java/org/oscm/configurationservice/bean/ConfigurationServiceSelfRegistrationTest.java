/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.configurationservice.bean;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * @author weiser
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConfigurationServiceSelfRegistrationTest {

    private ConfigurationServiceBean csb;
    private DataService ds;
    private TypedQuery query;
    private ConfigurationSetting setting;

    @Before
    public void setup() throws Exception {
        csb = new ConfigurationServiceBean();
        ds = mock(DataService.class);
        csb.dm = ds;

        setting = new ConfigurationSetting(
                ConfigurationKey.CUSTOMER_SELF_REGISTRATION_ENABLED, "global",
                "true");

        query = mock(TypedQuery.class);
        when(query.getSingleResult()).thenReturn(setting);

        when(ds.createNamedQuery(anyString(), eq(ConfigurationSetting.class)))
                .thenReturn(query);

        // csb.init();
        csb.setConfigurationSetting(setting);
    }

    @Test
    public void isCustomerSelfRegistrationEnabled() {
        assertTrue(csb.isCustomerSelfRegistrationEnabled());
    }

    @Test
    public void isCustomerSelfRegistrationEnabled_Global() {
        when(query.getSingleResult()).thenReturn(null, setting);

        assertTrue(csb.isCustomerSelfRegistrationEnabled());
    }

    @Test
    public void isCustomerSelfRegistrationEnabled_Fallback() {
        when(query.getSingleResult()).thenReturn(null);

        assertTrue(csb.isCustomerSelfRegistrationEnabled());
    }

}
