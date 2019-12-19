/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.configurationservice.bean;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.oscm.configurationservice.assembler.ConfigurationSettingAssembler;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.DateConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.enumtypes.AuthenticationMode;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Session Bean implementation class ConfigurationServiceBean
 */
@Singleton
@Startup
@Local(ConfigurationServiceLocal.class)
@Remote(ConfigurationService.class)
@Interceptors({ ExceptionMapper.class })
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
public class ConfigurationServiceBean
        implements ConfigurationService, ConfigurationServiceLocal {

    private static Log4jLogger logger = LoggerFactory
            .getLogger(ConfigurationServiceBean.class);

    private static final String NODENAME_PROPKEY = "bss.nodename";
    private static final String NODENAME_DEFAULT = "SingleNode";

    @EJB(beanInterface = DataService.class)
    DataService dm;

    Map<String, ConfigurationSetting> cache;

    @PostConstruct
    public void init() {
        refreshCache();

        String path = getConfigurationSetting(ConfigurationKey.KEY_FILE_PATH,
                Configuration.GLOBAL_CONTEXT).getValue();

        File keyFile = new File(path);

        if (keyFile.exists()) {

            try {
                byte[] key = Files.readAllBytes(keyFile.toPath());

                AESEncrypter.setKey(
                        Arrays.copyOfRange(key, 0, AESEncrypter.KEY_BYTES));
            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                throw new SaaSSystemException(
                        "Keyfile at " + path + " is not readable");
            }
        } else {

            try {
                AESEncrypter.generateKey();
                byte[] key = AESEncrypter.getKey();
                Files.write(keyFile.toPath(), key,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new SaaSSystemException(
                        "Keyfile at " + path + " could not be generated");
            }

        }

    }

    @Schedule(minute = "*/10")
    @Lock(LockType.WRITE)
    public void refreshCache() {
        cache = new HashMap<>();
        for (ConfigurationSetting configurationSetting : getAllConfigurationSettings()) {
            addToCache(configurationSetting);
        }
    }

    private void addToCache(ConfigurationSetting configSetting) {
        cache.put(getKey(configSetting.getInformationId(),
                configSetting.getContextId()), configSetting);
    }

    private String getKey(ConfigurationKey informationId, String contextId) {
        return informationId.name() + "_" + contextId;
    }

    @Override
    public ConfigurationSetting getConfigurationSetting(
            ConfigurationKey informationId, String contextId) {
        if (contextId == null) {
            throw new IllegalArgumentException(
                    "Context identifier must not be null");
        }

        ConfigurationSetting result = cache
                .get(getKey(informationId, contextId));
        if (result == null) {
            result = cache
                    .get(getKey(informationId, Configuration.GLOBAL_CONTEXT));
        }

        if (result == null) {
            if (informationId.isMandatory()) {
                throw new SaaSSystemException("Mandatory property '"
                        + informationId.getKeyName() + "' not set!");
            }

            // get default value
            result = new ConfigurationSetting(informationId,
                    Configuration.GLOBAL_CONTEXT,
                    informationId.getFallBackValue());
        }

        return result;
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
    public VOConfigurationSetting getVOConfigurationSetting(
            ConfigurationKey informationId, String contextId) {
        ConfigurationSetting setting = getConfigurationSetting(informationId,
                contextId);
        return ConfigurationSettingAssembler.toValueObject(setting);
    }

    @Override
    public String getNodeName() {
        String nodeName = System.getProperty(NODENAME_PROPKEY);
        // if the node name is not set, use a default
        if (nodeName == null) {
            nodeName = NODENAME_DEFAULT;
        }
        return nodeName;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<ConfigurationSetting> getAllConfigurationSettings() {
        return dm.createNamedQuery("ConfigurationSetting.getAll",
                ConfigurationSetting.class).getResultList();
    }

    private static boolean isEmpty(String string) {
        return (string == null || string.trim().length() == 0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean isCustomerSelfRegistrationEnabled() {
        return getConfigurationSetting(
                ConfigurationKey.CUSTOMER_SELF_REGISTRATION_ENABLED,
                Configuration.GLOBAL_CONTEXT).getValue()
                        .equalsIgnoreCase("true");
    }

    @Override
    public boolean isServiceProvider() {
        return AuthenticationMode.OIDC.name()
                .equals(getConfigurationSetting(ConfigurationKey.AUTH_MODE,
                        Configuration.GLOBAL_CONTEXT).getValue());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @Lock(LockType.WRITE)
    public void setConfigurationSetting(ConfigurationSetting configSetting) {
        ConfigurationSetting setting = getConfigurationSettingExactMatch(
                configSetting.getInformationId(), configSetting.getContextId());
        if (!isEmpty(configSetting.getValue())) {
            // check the type and trim the string if necessary
            String value = configSetting.getValue();

            if (configSetting.getInformationId()
                    .getType() == ConfigurationKey.TYPE_BOOLEAN
                    || configSetting.getInformationId()
                            .getType() == ConfigurationKey.TYPE_LONG
                    || configSetting.getInformationId()
                            .getType() == ConfigurationKey.TYPE_URL
                    || configSetting.getInformationId()
                            .getType() == ConfigurationKey.TYPE_STRING
                    || configSetting.getInformationId()
                            .getType() == ConfigurationKey.TYPE_PASSWORD) {

                value = value.trim();
            }

            // if the value is not empty, update or create the setting
            if (setting != null) {
                // if entry is already present, update it
                setting.setValue(value);
            } else {
                // if not, create a new one
                try {
                    configSetting.setValue(value);
                    dm.persist(configSetting);
                } catch (NonUniqueBusinessKeyException e) {
                    logger.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR_PERSIST_CONFIGURATION_SETTING);
                }
            }
        } else {
            // remove optional empty settings so that the default value can be
            // used again
            if (setting != null) {
                dm.remove(setting);
            }
        }
        refreshCache();
    }

    @Override
    @Lock(LockType.WRITE)
    public void setConfigurationSetting(String informationId, String value) {
        ConfigurationSetting configSetting = new ConfigurationSetting(
                ConfigurationKey.valueOf(informationId),
                Configuration.GLOBAL_CONTEXT, value);
        setConfigurationSetting(configSetting);
    }

    /**
     * Returns the exact match of a particular configuration setting,
     * <code>null</code> if it can't be found.
     * 
     * @param informationId
     *            The key for the setting.
     * @param contextId
     *            The context information.
     * @return The exact match for the given setting data.
     */
    private ConfigurationSetting getConfigurationSettingExactMatch(
            ConfigurationKey informationId, String contextId) {
        TypedQuery<ConfigurationSetting> query = dm.createNamedQuery(
                "ConfigurationSetting.findByInfoAndContext",
                ConfigurationSetting.class);
        query.setParameter("informationId", informationId);
        query.setParameter("contextId", contextId);
        ConfigurationSetting result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            // do nothing here
        }
        return result;
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
            // although the initialization data is invalid (negative
            // offset), the timer must be initialized with offset 0
            configuredDays = new BigDecimal(0);
        } else {
            BigDecimal oneDay = new BigDecimal(
                    DateConverter.MILLISECONDS_PER_DAY);
            configuredDays = new BigDecimal(configuredBillingOffset)
                    .divide(oneDay, RoundingMode.DOWN);
        }
        return configuredDays.longValue() * DateConverter.MILLISECONDS_PER_DAY;
    }

    long getConfiguredBillingOffsetInMs() {
        long configuredValue = getLongConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                Configuration.GLOBAL_CONTEXT);

        long maxAllowedValue = 28 * DateConverter.MILLISECONDS_PER_DAY;

        if (configuredValue < 0) {
            // although the initialization data is invalid (negative
            // offset), the timer must be initialized with offset 0
            configuredValue = 0;
        } else if (configuredValue > maxAllowedValue) {
            // max value is 28 days
            configuredValue = maxAllowedValue;
        }
        return configuredValue;
    }

    @Override
    public String getBaseURL() {
        String baseUrl = getConfigurationSetting(ConfigurationKey.BASE_URL,
                Configuration.GLOBAL_CONTEXT).getValue();
        if (baseUrl == null || baseUrl.length() == 0) {
            baseUrl = getConfigurationSetting(ConfigurationKey.BASE_URL_HTTPS,
                    Configuration.GLOBAL_CONTEXT).getValue();
        }
        return baseUrl;
    }

    @Override
    public String getBaseUrlHttps() {
        String baseUrlHttps = getConfigurationSetting(
                ConfigurationKey.BASE_URL_HTTPS, Configuration.GLOBAL_CONTEXT)
                        .getValue();
        return baseUrlHttps;
    }

    @Override
    public boolean isPaymentInfoAvailable() {

        String setting = getConfigurationSetting(
                ConfigurationKey.HIDE_PAYMENT_INFORMATION,
                Configuration.GLOBAL_CONTEXT).getValue();

        return !Boolean.parseBoolean(setting);
    }

    public DataService getDm() {
        return dm;
    }

    public void setDm(DataService dm) {
        this.dm = dm;
    }

    @Override
    public boolean isKafkaEnabled() {
        String setting = getConfigurationSetting(
                ConfigurationKey.KAFKA_BOOTSTRAP_SERVERS,
                Configuration.GLOBAL_CONTEXT).getValue();
        if (setting == null || setting.trim().isEmpty()) {
            return false;
        }        
        return true;
    }
}
