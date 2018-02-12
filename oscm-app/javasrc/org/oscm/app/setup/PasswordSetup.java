/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Nov 29, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.setup;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.codec.binary.Base64;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.domain.ConfigurationSetting;
import org.oscm.app.domain.CustomAttribute;
import org.oscm.app.domain.InstanceAttribute;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migrates the old passwords to the new method.
 * 
 * @author miethaner
 */
@Singleton
@Startup
public class PasswordSetup {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PasswordSetup.class);

    /**
     * EntityManager to be used for all persistence operations
     */
    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    protected EntityManager em;

    @EJB
    protected APPConfigurationServiceBean config;

    /**
     * Old encryption key
     */
    public final static byte[] ENCRYPTION_KEY = decode(
            new long[] { 0x1BD9AC5E8CE971CDL, 0x98034879ACCC8904L,
                    0xF962DCA0907D0398L, 0xF54F221334184933L }).getBytes();

    /**
     * When value is prefixed with this, encryption is applied to the value and
     * the encryption is written back into the database.
     */
    public static final String CRYPT_PREFIX = "_crypt:";

    /**
     * Setting keys ending with this suffix will have their values stored
     * encrypted.
     */
    public static final String CRYPT_KEY_SUFFIX = "_PWD";

    public static final String CRYPT_KEY_SUFFIX_PASS = "_PASS";

    @PostConstruct
    public void startUp() {

        try {
            if (config.initEncryption()) {
                updatePasswords();
            } else {
                encryptedSettingsWithPrefix();
            }

        } catch (ConfigurationException | GeneralSecurityException
                | BadResultException e) {
            LOGGER.error("unable to update old passwords");
            throw new RuntimeException("unable to update old passwords", e);
        }
    }

    /**
     * Encrypts all configuration settings with the _crypt: prefix with the new
     * method.
     * 
     * @throws ConfigurationException
     */
    private void encryptedSettingsWithPrefix() throws ConfigurationException {
        String csSQL = "SELECT cs FROM ConfigurationSetting cs WHERE cs.settingKey like '%"
                + CRYPT_KEY_SUFFIX + "' OR cs.settingKey like '%"
                + CRYPT_KEY_SUFFIX_PASS + "'";
        TypedQuery<ConfigurationSetting> csQuery = em.createQuery(csSQL,
                ConfigurationSetting.class);
        List<ConfigurationSetting> csList = csQuery.getResultList();

        for (ConfigurationSetting cs : csList) {
            String value = cs.getSettingValue();
            if (value != null && value.startsWith(CRYPT_PREFIX)) {
                value = value.substring(CRYPT_PREFIX.length());
                cs.setDecryptedValue(value);
            }
        }

        em.flush();
    }

    /**
     * Decrypts all old style passwords in the database and saves them encrypted
     * with the new method.
     * 
     * @throws ConfigurationException
     * @throws GeneralSecurityException
     * @throws BadResultException
     */
    private void updatePasswords() throws ConfigurationException,
            GeneralSecurityException, BadResultException {

        String csSQL = "SELECT cs FROM ConfigurationSetting cs WHERE cs.settingKey like '%"
                + CRYPT_KEY_SUFFIX + "' OR cs.settingKey like '%"
                + CRYPT_KEY_SUFFIX_PASS + "'";
        TypedQuery<ConfigurationSetting> csQuery = em.createQuery(csSQL,
                ConfigurationSetting.class);
        List<ConfigurationSetting> csList = csQuery.getResultList();

        for (ConfigurationSetting cs : csList) {
            String value = cs.getSettingValue();
            if (value != null && value.startsWith(CRYPT_PREFIX)) {
                value = value.substring(CRYPT_PREFIX.length());
            } else {
                value = decrypt(value);
            }
            cs.setDecryptedValue(value);
        }

        String caSQL = "SELECT ca FROM CustomAttribute ca WHERE ca.encrypted = true";
        TypedQuery<CustomAttribute> caQuery = em.createQuery(caSQL,
                CustomAttribute.class);
        List<CustomAttribute> caList = caQuery.getResultList();

        for (CustomAttribute ca : caList) {
            String value = ca.getAttributeValue();
            ca.setDecryptedValue(decrypt(value));
        }

        String iaSQL = "SELECT ia FROM InstanceAttribute ia WHERE ia.encrypted = true";
        TypedQuery<InstanceAttribute> iaQuery = em.createQuery(iaSQL,
                InstanceAttribute.class);
        List<InstanceAttribute> iaList = iaQuery.getResultList();

        for (InstanceAttribute ia : iaList) {
            String value = ia.getAttributeValue();
            ia.setDecryptedValue(decrypt(value));
        }

        String ipSQL = "SELECT ip FROM InstanceParameter ip WHERE ip.encrypted = true";
        TypedQuery<InstanceParameter> ipQuery = em.createQuery(ipSQL,
                InstanceParameter.class);
        List<InstanceParameter> ipList = ipQuery.getResultList();

        for (InstanceParameter ip : ipList) {
            String value = ip.getParameterValue();
            ip.setDecryptedValue(decrypt(value));
        }

        em.flush();
    }

    private static String decode(final long[] obfuscated) {
        final int length = obfuscated.length;
        final byte[] encoded = new byte[8 * (length - 1)];
        final long seed = obfuscated[0];
        final Random prng = new Random(seed);

        for (int i = 1; i < length; i++) {
            final long key = prng.nextLong();
            final int off = 8 * (i - 1);
            long l = obfuscated[i] ^ key;
            final int end = Math.min(encoded.length, off + 8);
            for (int i2 = off; i2 < end; i2++) {
                encoded[i2] = (byte) l;
                l >>= 8;
            }
        }

        final String decoded;
        try {
            decoded = new String(encoded, "UTF8");
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }

        final int i = decoded.indexOf(0);
        return i != -1 ? decoded.substring(0, i) : decoded;
    }

    private String decrypt(String text) throws GeneralSecurityException {
        SecretKeySpec skeySpec = new SecretKeySpec(
                Base64.decodeBase64(ENCRYPTION_KEY), "AES");

        byte[] result;
        try {
            byte[] decoded = Base64.decodeBase64(text.getBytes());
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            result = cipher.doFinal(decoded);
        } catch (InvalidKeyException | NoSuchAlgorithmException
                | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new GeneralSecurityException(
                    "unable to decrypt old encryption");
        }

        return new String(result);
    }
}
