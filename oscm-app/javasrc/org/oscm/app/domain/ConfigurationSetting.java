/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 12.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.domain;

import java.io.Serializable;
import java.security.GeneralSecurityException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.encrypter.AESEncrypter;

/**
 * A configuration setting for the asynchronous provisioning proxy.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@IdClass(ConfigurationSetting.ScopedSettingKey.class)
@NamedQueries({
        @NamedQuery(name = "ConfigurationSetting.getForProxyKey", query = "SELECT cs FROM ConfigurationSetting cs WHERE cs.settingKey = :key AND cs.controllerId = 'PROXY'"),
        @NamedQuery(name = "ConfigurationSetting.getAllProxy", query = "SELECT cs FROM ConfigurationSetting cs WHERE cs.controllerId = 'PROXY'"),
        @NamedQuery(name = "ConfigurationSetting.getForController", query = "SELECT cs FROM ConfigurationSetting cs WHERE cs.controllerId = :controllerId"),
        @NamedQuery(name = "ConfigurationSetting.getControllersForKey", query = "SELECT cs FROM ConfigurationSetting cs WHERE cs.settingKey = :key AND cs.controllerId != 'PROXY'"),
        @NamedQuery(name = "ConfigurationSetting.getUserControllers", query = "SELECT cs.controllerId FROM ConfigurationSetting cs WHERE cs.settingKey='BSS_USER_ID' AND cs.settingValue = :username")})
public class ConfigurationSetting {

    /**
     * Reserved word for proxy configuration settings.
     */
    public static final String PROXY_SETTING_ID = "PROXY";

    /**
     * Setting keys ending with this suffix will have their values stored
     * encrypted.
     */
    public static final String CRYPT_KEY_SUFFIX = "_PWD";

    public static final String CRYPT_KEY_SUFFIX_PASS = "_PASS";

    /**
     * The key of the configuration setting.
     */
    @Id
    private String settingKey;

    @Id
    private String controllerId = PROXY_SETTING_ID;

    /**
     * The value of the configuration setting.
     */
    @Column(nullable = false)
    private String settingValue;

    public String getSettingKey() {
        return settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public void setDecryptedValue(String settingValue)
            throws ConfigurationException {
        try {
            this.settingValue = isEncrypted()
                    ? AESEncrypter.encrypt(settingValue) : settingValue;
        } catch (GeneralSecurityException e) {
            throw new ConfigurationException(
                    String.format("Setting for key '%s' could not be encrypted",
                            getSettingKey()));
        }
    }

    public String getDecryptedValue() throws ConfigurationException {
        try {
            return isEncrypted() ? AESEncrypter.decrypt(settingValue)
                    : settingValue;
        } catch (GeneralSecurityException e) {
            throw new ConfigurationException(
                    String.format("Setting for key '%s' could not be decrypted",
                            getSettingKey()));
        }
    }

    public boolean isEncrypted() {
        return settingKey != null && (settingKey.endsWith(CRYPT_KEY_SUFFIX)
                || settingKey.endsWith(CRYPT_KEY_SUFFIX_PASS)) ? true : false;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public static class ScopedSettingKey implements Serializable {

        private static final long serialVersionUID = 6127964456252235253L;

        protected String settingKey;
        protected String controllerId;

        public ScopedSettingKey() {
        }

        public ScopedSettingKey(String settingKey, String controllerId) {
            this.settingKey = settingKey;
            this.controllerId = controllerId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            ScopedSettingKey other = (ScopedSettingKey) obj;
            return ((settingKey == null && other.settingKey == null)
                    || (settingKey != null
                            && settingKey.equals(other.settingKey)))
                    && ((controllerId == null && other.controllerId == null)
                            || (controllerId != null && controllerId
                                    .equals(other.controllerId)));
        }

        @Override
        public int hashCode() {
            int result = (settingKey != null ? (53 * settingKey.hashCode())
                    : 0);
            result = result + (controllerId != null
                    ? (13 * controllerId.hashCode()) : 0);
            return result;
        }
    }
}
