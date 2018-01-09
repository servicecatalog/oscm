/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Nov 30, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.setup;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.oscm.app.domain.ConfigurationSetting;
import org.oscm.app.domain.CustomAttribute;
import org.oscm.app.domain.InstanceAttribute;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.ControllerConfigurationKey;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * Integration test for PasswordSetup
 * 
 * @author miethaner
 */
public class PasswordSetupIT extends EJBTestBase {

    private EntityManager em;
    private APPConfigurationServiceBean config;

    private class PwdSetup extends PasswordSetup {
    }

    private static File file;

    @Override
    protected void setup(TestContainer container) throws Exception {
        em = container.getPersistenceUnit("oscm-app");
        container.addBean(new APPConfigurationServiceBean());
        config = container.get(APPConfigurationServiceBean.class);

        createConfigSetting("APP_KEY_PATH", "./key");

        file = new File("./key");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testUpdatePassword() throws Exception {

        createContorllerConfigSetting("ctrlId", "key_encrypted_PWD",
                encrypt("secret"));
        createContorllerConfigSetting("ctrlId", "key_crypt_PWD",
                "_crypt:secret");
        createCustomAttribute("key", encrypt("secret"), true, "orgId");
        final Long siKey = createServiceInstanceWithAttributesAndParameters(
                "orgId", "subId", "ctrlId", "key", encrypt("secret"), true);

        PlatformConfigurationKey[] keys = PlatformConfigurationKey.values();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != PlatformConfigurationKey.APP_KEY_PATH) {
                String value = "testValue";
                if (keys[i].name().endsWith(PasswordSetup.CRYPT_KEY_SUFFIX)
                        || keys[i].name().endsWith(
                                PasswordSetup.CRYPT_KEY_SUFFIX_PASS)) {
                    value = encrypt(value);
                }
                createConfigSetting(keys[i].name(), value);
            }
        }

        createContorllerConfigSetting("ctrlId",
                ControllerConfigurationKey.BSS_USER_KEY.name(), "key");
        createContorllerConfigSetting("ctrlId",
                ControllerConfigurationKey.BSS_USER_ID.name(), "name");
        createContorllerConfigSetting("ctrlId",
                ControllerConfigurationKey.BSS_USER_PWD.name(),
                encrypt("secret"));
        createContorllerConfigSetting("ctrlId",
                ControllerConfigurationKey.BSS_ORGANIZATION_ID.name(), "orgId");

        ProvisioningSettings settings = runTX(
                new Callable<ProvisioningSettings>() {
                    @Override
                    public ProvisioningSettings call() throws Exception {
                        PwdSetup setup = new PwdSetup();
                        setup.em = em;
                        setup.config = config;
                        setup.startUp();

                        ServiceInstance instance = em
                                .getReference(ServiceInstance.class, siKey);
                        return config.getProvisioningSettings(instance, null);
                    }
                });

        assertEquals("secret", settings.getConfigSettings()
                .get("key_encrypted_PWD").getValue());
        assertEquals("secret",
                settings.getConfigSettings().get("key_crypt_PWD").getValue());

        assertEquals("secret",
                settings.getCustomAttributes().get("key").getValue());

        assertEquals("secret", settings.getAttributes().get("key").getValue());
        assertEquals("secret", settings.getParameters().get("key").getValue());

    }

    @Test
    public void testEncryptSettingsWithPrefix() throws Exception {

        AESEncrypter.generateKey();
        byte[] key = AESEncrypter.getKey();
        Files.write(file.toPath(), key, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);

        createContorllerConfigSetting("ctrlId", "key_crypt_PWD",
                "_crypt:secret");
        final Long siKey = createServiceInstanceWithAttributesAndParameters(
                "orgId", "subId", "ctrlId", "key",
                AESEncrypter.encrypt("secret"), true);

        PlatformConfigurationKey[] keys = PlatformConfigurationKey.values();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != PlatformConfigurationKey.APP_KEY_PATH) {
                String value = "testValue";
                if (keys[i].name().endsWith(PasswordSetup.CRYPT_KEY_SUFFIX)
                        || keys[i].name().endsWith(
                                PasswordSetup.CRYPT_KEY_SUFFIX_PASS)) {
                    value = AESEncrypter.encrypt(value);
                }
                createConfigSetting(keys[i].name(), value);
            }
        }

        createContorllerConfigSetting("ctrlId",
                ControllerConfigurationKey.BSS_USER_KEY.name(), "key");
        createContorllerConfigSetting("ctrlId",
                ControllerConfigurationKey.BSS_USER_ID.name(), "name");
        createContorllerConfigSetting("ctrlId",
                ControllerConfigurationKey.BSS_USER_PWD.name(),
                AESEncrypter.encrypt("secret"));
        createContorllerConfigSetting("ctrlId",
                ControllerConfigurationKey.BSS_ORGANIZATION_ID.name(), "orgId");

        ProvisioningSettings settings = runTX(
                new Callable<ProvisioningSettings>() {
                    @Override
                    public ProvisioningSettings call() throws Exception {
                        PwdSetup setup = new PwdSetup();
                        setup.em = em;
                        setup.config = config;
                        setup.startUp();

                        ServiceInstance instance = em
                                .getReference(ServiceInstance.class, siKey);
                        return config.getProvisioningSettings(instance, null);
                    }
                });

        assertEquals("secret",
                settings.getConfigSettings().get("key_crypt_PWD").getValue());

        assertEquals("secret", settings.getAttributes().get("key").getValue());
        assertEquals("secret", settings.getParameters().get("key").getValue());

    }

    /**
     * Creates and persists a service instance.
     * 
     * @param key
     * @param value
     * @throws Exception
     */
    private Long createServiceInstanceWithAttributesAndParameters(
            final String organizationId, final String subscriptionId,
            final String controllerId, final String key, final String value,
            final boolean encrypted) throws Exception {
        return runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                ServiceInstance si = new ServiceInstance();
                si.setOrganizationId(organizationId);
                si.setSubscriptionId(subscriptionId);
                si.setControllerId(controllerId);
                si.setDefaultLocale("EN");
                si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
                si.setRequestTime(System.currentTimeMillis());
                si.setInstanceId("id");

                InstanceAttribute ia = new InstanceAttribute();
                ia.setServiceInstance(si);
                ia.setAttributeKey(key);
                ia.setAttributeValue(value);
                ia.setEncrypted(encrypted);
                ia.setControllerId(controllerId);
                si.setInstanceAttributes(Arrays.asList(ia));

                InstanceParameter ip = new InstanceParameter();
                ip.setServiceInstance(si);
                ip.setParameterKey(key);
                ip.setParameterValue(value);
                ip.setEncrypted(encrypted);
                si.setInstanceParameters(Arrays.asList(ip));

                em.persist(si);
                em.flush();
                return new Long(si.getTkey());
            }
        });
    }

    /**
     * Creates and persists a custom attribute.
     * 
     * @param key
     * @param value
     * @throws Exception
     */
    private void createCustomAttribute(final String key, final String value,
            final boolean encrypted, final String organizationId)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String val = value == null ? "testValue" : value;
                CustomAttribute ca = new CustomAttribute();
                ca.setAttributeKey(key);
                ca.setOrganizationId(organizationId);
                ca.setEncrypted(encrypted);
                ca.setAttributeValue(val);
                em.persist(ca);
                return null;
            }
        });
    }

    /**
     * Creates and persists a configuration setting.
     * 
     * @param key
     * @param value
     * @throws Exception
     */
    private void createConfigSetting(final String key, final String value)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String val = value == null ? "testValue" : value;
                ConfigurationSetting cs = new ConfigurationSetting();
                cs.setSettingKey(key);
                cs.setSettingValue(val);
                em.persist(cs);
                return null;
            }
        });
    }

    /**
     * Creates and persists a controller specific configuration setting.
     * 
     * @param controllerId
     * @param settingKey
     * @param value
     * @throws Exception
     */
    private void createContorllerConfigSetting(final String controllerId,
            final String settingKey, final String value) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ConfigurationSetting cs = new ConfigurationSetting();
                cs.setControllerId(controllerId);
                cs.setSettingKey(settingKey);
                cs.setSettingValue(value);
                em.persist(cs);
                return null;
            }
        });
    }

    public static String encrypt(String text) throws GeneralSecurityException {

        SecretKeySpec skeySpec = new SecretKeySpec(
                Base64.decodeBase64(PasswordSetup.ENCRYPTION_KEY), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(text.getBytes());
        return new String(Base64.encodeBase64(encrypted));
    }
}
