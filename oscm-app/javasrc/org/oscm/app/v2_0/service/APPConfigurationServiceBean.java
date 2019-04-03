/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 17.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.*;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.domain.ConfigurationSetting;
import org.oscm.app.domain.CustomAttribute;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v2_0.data.ControllerConfigurationKey;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.string.Strings;
import org.oscm.vo.VOUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the configuration service.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Stateless
@LocalBean
public class APPConfigurationServiceBean {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(APPConfigurationServiceBean.class);

    private static final String PROXY_ID = "PROXY";
    private static final String APP_SUSPEND = "APP_SUSPEND";
    private static final String KEY_PATH = "APP_KEY_PATH";

    /**
     * Initialized the encryption for the APP. If the key file specified in the
     * settings is present, it reads the key, otherwise it is generated and
     * saved in file.
     * 
     * @return true if a new key was generated
     * @throws ConfigurationException
     */
    public boolean initEncryption() throws ConfigurationException {

        String path = getKeyFilePath();
        File keyFile = new File(path);

        if (keyFile.exists()) {

            try {
                byte[] key = Files.readAllBytes(keyFile.toPath());

                AESEncrypter.setKey(
                        Arrays.copyOfRange(key, 0, AESEncrypter.KEY_BYTES));

                return false;
            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                throw new ConfigurationException(
                        "Keyfile at " + path + " is not readable");
            }
        } else {

            try {
                AESEncrypter.generateKey();
                byte[] key = AESEncrypter.getKey();
                Files.write(keyFile.toPath(), key,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE);

                return true;
            } catch (IOException e) {
                throw new ConfigurationException(
                        "Keyfile at " + path + " could not be generated");
            }
        }
    }

    /**
     * EntityManager to be used for all persistence operations
     */
    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    protected EntityManager em;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public String getProxyConfigurationSetting(PlatformConfigurationKey key)
            throws ConfigurationException {

        LOGGER.debug("Retrieving proxy configuration setting for key {}",
                key.name());
        Query query = em
                .createNamedQuery("ConfigurationSetting.getForProxyKey");
        query.setParameter("key", key.name());
        try {
            ConfigurationSetting result = (ConfigurationSetting) query
                    .getSingleResult();
            return result.getDecryptedValue();
        } catch (NoResultException e) {
            if (key.name().equals(APP_SUSPEND)) {
                return "";
            } else {
                LOGGER.warn("No entry found for key {}", key.name());
                throw new ConfigurationException(String.format(
                        "No configuration setting found for key '%s'",
                        key.name()), key.name());
            }
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public HashMap<String, String> getControllerOrganizations() {

        LOGGER.debug("Retrieving configured controllers");
        HashMap<String, String> result = new HashMap<>();
        Query query = em
                .createNamedQuery("ConfigurationSetting.getControllersForKey");
        query.setParameter("key",
                ControllerConfigurationKey.BSS_ORGANIZATION_ID.name());
        List<?> resultList = query.getResultList();
        for (Object entry : resultList) {
            ConfigurationSetting currentCs = (ConfigurationSetting) entry;
            result.put(currentCs.getControllerId(),
                    currentCs.getSettingValue());
        }
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void storeControllerOrganizations(
            HashMap<String, String> controllerOrganizations) {

        LOGGER.debug("Storing configured controllers");
        Query query = em
                .createNamedQuery("ConfigurationSetting.getControllersForKey");
        query.setParameter("key",
                ControllerConfigurationKey.BSS_ORGANIZATION_ID.name());
        List<?> resultList = query.getResultList();
        for (Object entry : resultList) {
            ConfigurationSetting currentCs = (ConfigurationSetting) entry;
            String cId = currentCs.getControllerId();
            if (controllerOrganizations.containsKey(cId)) {
                String value = controllerOrganizations.get(cId);
                if (value == null || value.trim().length() == 0) {
                    em.remove(currentCs);
                } else {
                    currentCs.setSettingValue(value);
                    em.persist(currentCs);
                }
                controllerOrganizations.remove(cId);
            }
        }
        for (String key : controllerOrganizations.keySet()) {
            if (controllerOrganizations.get(key) != null
                    && controllerOrganizations.get(key).trim().length() > 0) {
                ConfigurationSetting newSetting = new ConfigurationSetting();
                newSetting.setControllerId(key);
                newSetting.setSettingKey(
                        ControllerConfigurationKey.BSS_ORGANIZATION_ID.name());
                newSetting.setSettingValue(controllerOrganizations.get(key));
                em.persist(newSetting);
            }
        }
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public HashMap<String, Setting> getAllProxyConfigurationSettings()
            throws ConfigurationException {
        LOGGER.debug("Retrieving all configuration settings for proxy");
        HashMap<String, Setting> result = new HashMap<>();
        Query query = em.createNamedQuery("ConfigurationSetting.getAllProxy");
        List<?> resultList = query.getResultList();
        for (Object entry : resultList) {
            ConfigurationSetting currentCs = (ConfigurationSetting) entry;
            result.put(currentCs.getSettingKey(), new Setting(
                    currentCs.getSettingKey(), currentCs.getDecryptedValue()));
        }
        PlatformConfigurationKey[] keys = PlatformConfigurationKey.values();
        StringBuffer missing = new StringBuffer();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].isMandatory() && !result.containsKey(keys[i].name())) {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append(keys[i].name());
            }
        }
        if (missing.length() > 0) {
            throw new ConfigurationException(
                    "The configuration is missing the following parameter(s): "
                            + missing.toString(),
                    missing.toString());
        }
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public HashMap<String, Setting> getProxyConfigurationSettings()
            throws ConfigurationException {
        LOGGER.debug("Retrieving all configuration settings for proxy");
        HashMap<String, Setting> result = new HashMap<>();
        Query query = em.createNamedQuery("ConfigurationSetting.getAllProxy");
        List<?> resultList = query.getResultList();
        for (Object entry : resultList) {
            ConfigurationSetting currentCs = (ConfigurationSetting) entry;
            result.put(currentCs.getSettingKey(), new Setting(
                    currentCs.getSettingKey(), currentCs.getDecryptedValue()));
        }
        PlatformConfigurationKey[] keys = PlatformConfigurationKey.values();
        StringBuffer missing = new StringBuffer();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].isMandatory() && !result.containsKey(keys[i].name())) {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append(keys[i].name());
            }
        }
        if (missing.length() > 0) {
            throw new ConfigurationException(
                    "The configuration is missing the following parameter(s): "
                            + missing.toString(),
                    missing.toString());
        }
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public VOUserDetails getAPPAdministrator() throws ConfigurationException {
        VOUserDetails adminuser = new VOUserDetails();
        String adminemail = getProxyConfigurationSetting(
                PlatformConfigurationKey.APP_ADMIN_MAIL_ADDRESS);
        if (!Strings.isEmpty(adminemail)) {
            adminuser.setEMail(adminemail);
            adminuser.setLocale(Messages.DEFAULT_LOCALE);
        }
        return adminuser;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public HashMap<String, Setting> getControllerConfigurationSettings(
            String controllerId) throws ConfigurationException {

        LOGGER.debug("Retrieving configuration settings for controller '{}'",
                controllerId);
        HashMap<String, Setting> result = new HashMap<>();
        if (controllerId != null) {
            Query query = em
                    .createNamedQuery("ConfigurationSetting.getForController");
            query.setParameter("controllerId", controllerId);
            List<?> resultList = query.getResultList();
            for (Object entry : resultList) {
                ConfigurationSetting currentCs = (ConfigurationSetting) entry;
                result.put(currentCs.getSettingKey(),
                        new Setting(currentCs.getSettingKey(),
                                currentCs.getDecryptedValue()));
            }
        }
        ControllerConfigurationKey[] keys = ControllerConfigurationKey.values();
        StringBuffer missing = new StringBuffer();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].isMandatory() && !result.containsKey(keys[i].name())) {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append(keys[i].name());
            }
        }
        if (missing.length() > 0) {
            throw new ConfigurationException(
                    "The controller configuration is missing the following parameter(s): "
                            + missing.toString(),
                    missing.toString());
        }
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public HashMap<String, Setting> getCustomAttributes(String organizationId)
            throws ConfigurationException {

        LOGGER.debug("Retrieving custom settings for organization '{}'",
                organizationId);

        HashMap<String, Setting> result = new HashMap<>();

        if (organizationId != null) {
            TypedQuery<CustomAttribute> query = em.createNamedQuery(
                    "CustomAttribute.getForOrg", CustomAttribute.class);
            query.setParameter("oid", organizationId);
            List<CustomAttribute> resultList = query.getResultList();
            try {
                for (CustomAttribute entry : resultList) {
                    result.put(entry.getAttributeKey(), new Setting(
                            entry.getAttributeKey(), entry.getDecryptedValue(),
                            entry.isEncrypted(), entry.getControllerId()));
                }
            } catch (BadResultException e) {
                throw new ConfigurationException(e.getMessage());
            }
        }

        return result;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void setAPPSuspend(String isSupsend) {
        LOGGER.debug("Storing suspend setting for APP");
        HashMap<String, Setting> setting = new HashMap<>();
        setting.put(APP_SUSPEND, new Setting(APP_SUSPEND, isSupsend));
        try {
            storeControllerConfigurationSettings(PROXY_ID, setting);
        } catch (ConfigurationException exception) {
            // this exception should not happen due to no encryption needed for
            // APP_SUSPEND, no handle needed
        }
    }

    public boolean isAPPSuspend() {
        String isSuspend = "";
        try {
            isSuspend = getProxyConfigurationSetting(
                    PlatformConfigurationKey.APP_SUSPEND);
        } catch (ConfigurationException exception) {
            // this exception should not happen due to no decryption needed for
            // APP_SUSPEND, no handle needed
        }
        if (!Strings.isEmpty(isSuspend)) {
            return Boolean.valueOf(isSuspend).booleanValue();
        }
        return false;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void storeControllerConfigurationSettings(String controllerId,
            HashMap<String, Setting> settings) throws ConfigurationException {

        LOGGER.debug("Storing configuration settings for controller '{}'",
                controllerId);
        if (controllerId == null || settings == null) {
            throw new IllegalArgumentException("All parameters must be set");
        }
        Query query = em
                .createNamedQuery("ConfigurationSetting.getForController");
        query.setParameter("controllerId", controllerId);
        List<?> resultList = query.getResultList();
        for (Object entry : resultList) {
            ConfigurationSetting setting = (ConfigurationSetting) entry;
            String key = setting.getSettingKey();
            if (settings.containsKey(key)) {
                if (settings.get(key) == null
                        || settings.get(key).getValue() == null) {
                    em.remove(setting);
                } else {
                    setting.setDecryptedValue(settings.get(key).getValue());
                    em.persist(setting);
                }
                settings.remove(key);
            }
        }
        for (String newKey : settings.keySet()) {
            ConfigurationSetting newSetting = new ConfigurationSetting();
            newSetting.setControllerId(controllerId);
            newSetting.setSettingKey(newKey);
            newSetting.setDecryptedValue(settings.get(newKey) != null
                    ? settings.get(newKey).getValue() : null);
            em.persist(newSetting);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void storeAppConfigurationSettings(HashMap<String, String> settings) throws ConfigurationException, GeneralSecurityException {

        LOGGER.debug("Storing configuration settings for APP platform");
        if (settings == null) {
            throw new IllegalArgumentException("All parameters must be set");
        }
        Query query = em
                .createNamedQuery("ConfigurationSetting.getForController");
        query.setParameter("controllerId", PROXY_ID);
        List<?> resultList = query.getResultList();
        for (Object entry : resultList) {
            ConfigurationSetting setting = (ConfigurationSetting) entry;
            String key = setting.getSettingKey();
            if (settings.containsKey(key)) {
                if (settings.get(key) == null) {
                    em.remove(setting);
                } else {
                    setting.setDecryptedValue(settings.get(key));
                    em.persist(setting);
                }
            }
            settings.remove(key);
        }
    }

  @TransactionAttribute(TransactionAttributeType.MANDATORY)
  public List<String> getUserConfiguredControllers(String username) {

    LOGGER.info("Getting user[" + username + "] configured controller");
    Query query = em.createNamedQuery("ConfigurationSetting.getUserControllers");
    query.setParameter("username", username);

    List<?> resultList = query.getResultList();
    LOGGER.info(resultList.toString());

    return null;
  }

    /**
     * Creates settings instance from given service instance.
     */
    public ProvisioningSettings getProvisioningSettings(
            final ServiceInstance instance, final ServiceUser requestingUser)
            throws BadResultException, ConfigurationException {
        return getProvisioningSettings(instance, requestingUser, false);
    }

    /**
     * Creates settings instance from given service instance.
     */
    public ProvisioningSettings getProvisioningSettings(
            final ServiceInstance instance, final ServiceUser requestingUser,
            boolean overwrite)
            throws BadResultException, ConfigurationException {
        final HashMap<String, Setting> controllerSettings = getControllerConfigurationSettings(
                instance.getControllerId());
        final HashMap<String, Setting> customAttributes = getCustomAttributes(
                instance.getOrganizationId());
        final ProvisioningSettings settings = new ProvisioningSettings(
                instance.getParameterMap(), instance.getAttributeMap(),
                customAttributes, controllerSettings,
                instance.getDefaultLocale());

        settings.setOrganizationId(instance.getOrganizationId());
        settings.setOrganizationName(instance.getOrganizationName());
        settings.setSubscriptionId(instance.getSubscriptionId());
        settings.setReferenceId(instance.getReferenceId());
        settings.setBesLoginUrl(instance.getBesLoginURL());
        settings.setRequestingUser(requestingUser);
        settings.setAuthentication(
                getAuthenticationForBESTechnologyManager(null, instance, null));
        settings.setServiceAccessInfo(instance.getServiceAccessInfo());

        if (overwrite) {
            settings.overwriteProperties(instance.getControllerId());
        }

        copyCredentialsFromControllerSettings(settings, controllerSettings);

        return settings;
    }

    /**
     * For backwards compatibility the technology manager credentials from the
     * controller configuration are applied to the instance parameters.
     * 
     * @param settings
     * @param controllerSettings
     */
    public void copyCredentialsFromControllerSettings(
            ProvisioningSettings settings,
            HashMap<String, Setting> controllerSettings) {
        Setting userKey = controllerSettings
                .get(ControllerConfigurationKey.BSS_USER_KEY.name());
        Setting userPwd = controllerSettings
                .get(ControllerConfigurationKey.BSS_USER_PWD.name());
        if (userKey != null && !Strings.isEmpty(userKey.getValue())
                && userPwd != null && !Strings.isEmpty(userPwd.getValue())) {
            // override technology manager user credentials in parameters
            // (for backwards compatibility)
            settings.getParameters().put(InstanceParameter.BSS_USER, userKey);
            settings.getParameters().put(InstanceParameter.BSS_USER_PWD,
                    userPwd);
        }
    }

    public PasswordAuthentication getWebServiceAuthentication(
            ServiceInstance serviceInstance, Map<String, Setting> proxySettings, Optional<String> controllerId)
            throws ConfigurationException {
        if (serviceInstance != null || controllerId.isPresent()) {
            return getAuthenticationForBESTechnologyManager(controllerId.orElseGet(() -> serviceInstance.getControllerId()),
                    serviceInstance, proxySettings);
        }
        return getAuthenticationForAPPAdmin(proxySettings);
    }

    public PasswordAuthentication getAuthenticationForBESTechnologyManager(
            String controllerId, ServiceInstance serviceInstance,
            Map<String, Setting> proxySettings) throws ConfigurationException {
        if (proxySettings == null) {
            proxySettings = getAllProxyConfigurationSettings();
        }
        boolean isSso = isSsoMode(proxySettings);
        if (serviceInstance != null) {
            controllerId = serviceInstance.getControllerId();
        }
        HashMap<String, Setting> controllerSettings = getControllerConfigurationSettings(
                controllerId);

        String usernameKey = isSso
                ? ControllerConfigurationKey.BSS_USER_ID.name()
                : ControllerConfigurationKey.BSS_USER_KEY.name();
        Setting user = controllerSettings.get(usernameKey);
        Setting userPwd = controllerSettings
                .get(ControllerConfigurationKey.BSS_USER_PWD.name());

        if (user == null || Strings.isEmpty(user.getValue()) || userPwd == null
                || Strings.isEmpty(userPwd.getValue())) {
            LOGGER.warn(
                    "The controller settings for controller '{}' define incomplete technology manager credentials. Please define values for both {} and {}.",
                    new String[] { controllerId, usernameKey,
                            ControllerConfigurationKey.BSS_USER_PWD.name() });
        }
        String ws_username = null;
        String ws_password = null;

        if (user != null && !Strings.isEmpty(user.getValue()) && userPwd != null
                && !Strings.isEmpty(userPwd.getValue())) {
            ws_username = user.getValue();
            ws_password = userPwd.getValue();
        } else {
            if (serviceInstance != null) {
                InstanceParameter userParam = serviceInstance
                        .getParameterForKey(InstanceParameter.BSS_USER);
                InstanceParameter userPwdParam = serviceInstance
                        .getParameterForKey(InstanceParameter.BSS_USER_PWD);
                try {
                    ws_username = userParam == null ? null
                            : userParam.getDecryptedValue();
                    ws_password = userPwdParam == null ? null
                            : userPwdParam.getDecryptedValue();
                } catch (BadResultException e) {
                    throw new ConfigurationException(e.getMessage(),
                            InstanceParameter.BSS_USER_PWD);
                }
            }
            if (Strings.isEmpty(ws_username) || ws_password == null) {
                LOGGER.error(
                        "Request context for web service call is incomplete due to missing credentials. Please check controller settings [{}].",
                        new String[] { controllerId });
                throw new ConfigurationException(
                        "The controller settings for controller '"
                                + controllerId
                                + "' are missing complete technology manager credentials. Please define values for both "
                                + usernameKey + " and "
                                + ControllerConfigurationKey.BSS_USER_PWD
                                        .name(),
                        usernameKey);

            }
        }
        return new PasswordAuthentication(ws_username, ws_password);
    }

    public PasswordAuthentication getAuthenticationForAPPAdmin(
            Map<String, Setting> proxySettings) throws ConfigurationException {
        if (proxySettings == null) {
            proxySettings = getAllProxyConfigurationSettings();
        }
        boolean isSso = isSsoMode(proxySettings);
        String usernameKey = isSso ? PlatformConfigurationKey.BSS_USER_ID.name()
                : PlatformConfigurationKey.BSS_USER_KEY.name();
        String ws_username = proxySettings.get(usernameKey) != null
                ? proxySettings.get(usernameKey).getValue() : null;
        String ws_password = proxySettings
                .get(PlatformConfigurationKey.BSS_USER_PWD.name()) != null
                        ? proxySettings.get(
                                PlatformConfigurationKey.BSS_USER_PWD.name())
                                .getValue()
                        : null;
        if (Strings.isEmpty(ws_username) || ws_password == null) {
            LOGGER.error(
                    "Request context for web service call is incomplete due to missing credentials. Please check platform settings.");
            throw new ConfigurationException(
                    "The APP configuration settings define incomplete admin credentials. Please define values for both "
                            + usernameKey + " and "
                            + PlatformConfigurationKey.BSS_USER_PWD.name(),
                    usernameKey);
        }
        return new PasswordAuthentication(ws_username, ws_password);
    }

    private boolean isSsoMode(Map<String, Setting> settings) {
        return "SAML_SP".equals(settings
                .get(PlatformConfigurationKey.BSS_AUTH_MODE.name()).getValue());
    }

    public String getKeyFilePath() throws ConfigurationException {
        TypedQuery<ConfigurationSetting> query = em.createNamedQuery(
                "ConfigurationSetting.getForProxyKey",
                ConfigurationSetting.class);
        query.setParameter("key", KEY_PATH);

        ConfigurationSetting setting;
        try {
            setting = query.getSingleResult();
        } catch (NoResultException e) {
            throw new ConfigurationException("Setting for keyfile is not set");
        }

        return setting.getSettingValue();
    }
}
