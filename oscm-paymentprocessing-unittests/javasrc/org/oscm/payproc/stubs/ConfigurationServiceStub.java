/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 18.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.payproc.stubs;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * @author Mike J&auml;ger
 * 
 */
@Stateless
public class ConfigurationServiceStub implements ConfigurationServiceLocal {

    private boolean isPSPUsageEnabled = true;

    /*
     * (non-Javadoc)
     * 
     * @seeorg.oscm.configsvc.intf.IConfigurationServiceLocal#
     * getConfigurationSetting
     * (org.oscm.types.enumtypes.ConfigurationKey, java.lang.String)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public ConfigurationSetting getConfigurationSetting(
            ConfigurationKey informationId, String contextId) {
        if (informationId == ConfigurationKey.PSP_USAGE_ENABLED) {
            return new ConfigurationSetting(ConfigurationKey.PSP_USAGE_ENABLED,
                    Configuration.GLOBAL_CONTEXT,
                    String.valueOf(isPSPUsageEnabled));
        }
        return null;
    }

    @Override
    public long getLongConfigurationSetting(ConfigurationKey informationId,
            String contextId) {
        return 0;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public String getNodeName() {

        return null;
    }

    public void setPSPUsageEnabled(boolean isPSPUsageEnabled) {
        this.isPSPUsageEnabled = isPSPUsageEnabled;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void setConfigurationSetting(ConfigurationSetting configSetting) {

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<ConfigurationSetting> getAllConfigurationSettings() {

        return null;
    }

    @Override
    public boolean isCustomerSelfRegistrationEnabled() {
        return true;
    }

    @Override
    public boolean isServiceProvider() {
        return false;
    }

    @Override
    public long getBillingRunOffsetInMs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getBillingRunStartTimeInMs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBaseURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBaseUrlHttps() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPaymentInfoAvailable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isKafkaEnabled() {
        return false;
    }
}
