/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 31.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.startup;

import static org.mockito.Mockito.mock;

import javax.ejb.EJBException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.local.SearchServiceLocal;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.timerservice.bean.TimerServiceBean;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Unit tests for the startup servlet.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ADMUMStartupTest {

    private ConfigurationServiceLocal cfg;
    private ADMUMStartup testClass;

    private boolean throwExceptionWhenRetrievingSettings = false;

    @Before
    public void setUp() {
        cfg = new ConfigurationServiceStub() {
            @Override
            public ConfigurationSetting getConfigurationSetting(
                    ConfigurationKey key, String context) {
                if (throwExceptionWhenRetrievingSettings) {
                    throw new EJBException();
                }
                if (key == ConfigurationKey.LOG_FILE_PATH) {
                    return new ConfigurationSetting(
                            ConfigurationKey.LOG_FILE_PATH,
                            Configuration.GLOBAL_CONTEXT, ".");
                }
                return new ConfigurationSetting();
            }
        };
        testClass = new ADMUMStartup();

        testClass.cs = cfg;
        testClass.localizer = mock(LocalizerServiceLocal.class);
        testClass.prodSessionMgmt = mock(SessionServiceLocal.class);
        testClass.timerMgmt = mock(TimerServiceBean.class);
        testClass.searchService = mock(SearchServiceLocal.class);
    }

    @Test
    public void testService() throws Exception {
        testClass.service(null, null);
    }

    @Test
    public void testInit() throws Exception {
        testClass.init();
    }

    @Test(expected = EJBException.class)
    public void testInitFailureOnReadingSettings() throws Exception {
        throwExceptionWhenRetrievingSettings = true;
        testClass.init();
    }

}
