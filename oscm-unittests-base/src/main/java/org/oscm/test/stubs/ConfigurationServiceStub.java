/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oscm.configurationservice.assembler.ConfigurationSettingAssembler;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.DateConverter;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.types.constants.Configuration;

/**
 * Simple stub that hold config settings in-memory.
 * 
 * @author hoffmann
 */
public class ConfigurationServiceStub implements ConfigurationService,
        ConfigurationServiceLocal {

    private final Map<ConfigurationKey, ConfigurationSetting> settings = new HashMap<ConfigurationKey, ConfigurationSetting>();

    @Override
    public VOConfigurationSetting getVOConfigurationSetting(
            ConfigurationKey informationId, String contextId) {
        ConfigurationSetting setting = getConfigurationSetting(informationId,
                contextId);
        return ConfigurationSettingAssembler.toValueObject(setting);
    }

    @Override
    public ConfigurationSetting getConfigurationSetting(
            ConfigurationKey informationId, String contextId) {
        if (ConfigurationKey.SSO_DEFAULT_TENANT_ID.equals(informationId)) {
            return new ConfigurationSetting(informationId,
                    Configuration.GLOBAL_CONTEXT, "8f96dede");
        }
        final ConfigurationSetting vo = settings.get(informationId);

        if (vo != null) {
            return vo;
        }

        return new ConfigurationSetting(informationId,
                Configuration.GLOBAL_CONTEXT, informationId.getFallBackValue());
    }

    @Override
    public long getLongConfigurationSetting(ConfigurationKey informationId,
            String contextId) {
        long configValue = 0;

        if (informationId != null) {
            final ConfigurationSetting setting = getConfigurationSetting(
                    informationId, contextId);
            final String value = setting.getValue();
            if (value != null) {
                configValue = Long.parseLong(value);
            }
        }

        return configValue;
    }

    @Override
    public String getNodeName() {
        return "UnitTestNode";
    }

    @Override
    public void setConfigurationSetting(ConfigurationSetting configSetting) {
        settings.put(configSetting.getInformationId(), configSetting);
    }

    @Override
    public void setConfigurationSetting(String informationId, String value) {
        setConfigurationSetting(ConfigurationKey.valueOf(informationId), value);
    }

    public void setConfigurationSetting(ConfigurationKey key, String value) {
        ConfigurationSetting configSetting = new ConfigurationSetting(key,
                Configuration.GLOBAL_CONTEXT, value);
        settings.put(configSetting.getInformationId(), configSetting);
    }

    @Override
    public List<ConfigurationSetting> getAllConfigurationSettings() {
        return null;
    }

    @Override
    public boolean isCustomerSelfRegistrationEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.oscm.internal.intf.ConfigurationService#isServiceProvider()
     */
    @Override
    public boolean isServiceProvider() {
        return false;
    }

    @Override
    public long getBillingRunOffsetInMs() {
        long configuredValue = getConfiguredBillingOffsetInMs();
        return getConfiguredDaysInMs(configuredValue);
    }

    @Override
    public long getBillingRunStartTimeInMs() {
        long configuredValue = getConfiguredBillingOffsetInMs();
        long configuredDays = getConfiguredDaysInMs(configuredValue);
        return configuredValue - configuredDays;
    }

    long getConfiguredDaysInMs(long configuredBillingOffset) {
        BigDecimal configuredDays = null;
        if (configuredBillingOffset < 0) {
            configuredDays = new BigDecimal(0);
        } else {
            BigDecimal oneDay = new BigDecimal(
                    DateConverter.MILLISECONDS_PER_DAY);
            configuredDays = new BigDecimal(configuredBillingOffset).divide(
                    oneDay, RoundingMode.DOWN);
        }
        return configuredDays.longValue() * DateConverter.MILLISECONDS_PER_DAY;
    }

    long getConfiguredBillingOffsetInMs() {
        long configuredValue = getLongConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                Configuration.GLOBAL_CONTEXT);

        long maxAllowedValue = 28 * DateConverter.MILLISECONDS_PER_DAY;

        if (configuredValue < 0) {
            configuredValue = 0;
        } else if (configuredValue > maxAllowedValue) {
            configuredValue = maxAllowedValue;
        }
        return configuredValue;
    }

    @Override
    public String getBaseURL() {
        return getConfigurationSetting(ConfigurationKey.BASE_URL,
                Configuration.GLOBAL_CONTEXT).getValue();
    }

    @Override
    public String getBaseUrlHttps() {
        return getConfigurationSetting(ConfigurationKey.BASE_URL_HTTPS,
                Configuration.GLOBAL_CONTEXT).getValue();
    }

    @Override
    public boolean isPaymentInfoAvailable() {
        return true;
    }

    @Override
    public boolean isKafkaEnabled() {
        return false;
    }

}
