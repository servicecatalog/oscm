/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 02.07.2013                                                      
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

/**
 * @author stavreva
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConfigurationServiceIsServiceProviderTest {

    private ConfigurationServiceBean csb;
    private DataService ds;
    private TypedQuery query;
    private ConfigurationSetting setting;

    @Before
    public void setup() throws Exception {
        csb = new ConfigurationServiceBean();
        ds = mock(DataService.class);
        csb.dm = ds;

        setting = new ConfigurationSetting(ConfigurationKey.AUTH_MODE,
                "global", "SAML_SP");

        query = mock(TypedQuery.class);
        when(query.getResultList()).thenReturn(Arrays.asList(setting));

        when(ds.createNamedQuery(anyString(), eq(ConfigurationSetting.class)))
                .thenReturn(query);

        csb.refreshCache();
    }

    @Test
    public void isServiceProvider() {
        assertTrue(csb.isServiceProvider());
    }

    @Test
    public void isServiceProvider_Negative() {
        setting = new ConfigurationSetting(ConfigurationKey.AUTH_MODE,
                "global", "INTERNAL");
        when(query.getResultList()).thenReturn(Arrays.asList(setting));
        csb.refreshCache();
        assertFalse(csb.isServiceProvider());
    }

}
