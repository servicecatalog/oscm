/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Jul 7, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.operator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.test.stubs.OperatorServiceStub;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOConfigurationSetting;

/**
 * @author farmaki
 * 
 */
public class ConfigurationSettingsBeanTest {

    private ConfigurationSettingsBean configSettingsBean;

    private static final String SOP_STARTING_PREFIX = "SOP_";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        final OperatorServiceStub stub = new OperatorServiceStub() {

            private List<VOConfigurationSetting> settings = new ArrayList<VOConfigurationSetting>();

            public List<VOConfigurationSetting> getConfigurationSettings()
                    throws OrganizationAuthoritiesException {
                return settings;
            }

            public void saveConfigurationSetting(VOConfigurationSetting setting)
                    throws OrganizationAuthoritiesException {
                settings.add(setting);
            }

        };

        configSettingsBean = new ConfigurationSettingsBean() {

            private static final long serialVersionUID = 4876492312368306202L;

            protected OperatorService getOperatorService() {
                return stub;
            }
        };
    }

    /**
     * Test method for
     * {@link org.oscm.ui.beans.operator.ConfigurationSettingsBean#getConfigurationSettings()}
     * . Tests if there are no existing SOP configuration settings in case the
     * SOP_ORGANIZATION_IDENTIFIER is not set.
     */
    @Test
    public final void testGetConfigurationSettings_NoSOPSettings() {
        for (VOConfigurationSetting configSetting : configSettingsBean
                .getConfigurationSettings()) {
            Assert.assertTrue(!configSetting.getInformationId().getKeyName()
                    .startsWith(SOP_STARTING_PREFIX));
        }
    }

}
