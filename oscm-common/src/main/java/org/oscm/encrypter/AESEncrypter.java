/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-10-10
 *
 *******************************************************************************/
package org.oscm.encrypter;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Created by BadziakP on 2016-10-10.
 */
public class AESEncrypter {

    private static final Log4jLogger LOG = LoggerFactory
            .getLogger(AESEncrypter.class);

    public static final int IV_BYTES = 16;
    public static final int KEY_BYTES = 16;

    private static SecretKey key;

    public static byte[] getKey() {
        return key.getEncoded();
    }

    public static void setKey(byte[] key) {
        AESEncrypter.key = new SecretKeySpec(key, "AES");
    }

    public static void generateKey() {

        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(KEY_BYTES * 8);
            key = generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            // ignore
        }
    }

    /**
     * Encrypts a given string based on a shared secret.
     *
     * @param text
     *            the text to encrypt
     * @return the iv and encrypted text as Base64 separated with ':'.
     * @throws GeneralSecurityException
     *             on any problem during encryption
     */
    public static String encrypt(String text) throws GeneralSecurityException {

        if (text == null) {
            return null;
        }

        byte[] decrypted;
        try {
            decrypted = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        byte[] iv = new byte[IV_BYTES];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        byte[] encrypted = cipher.doFinal(decrypted);
        return new String(Base64.encodeBase64(iv)) + ":"
                + new String(Base64.encodeBase64(encrypted));
    }

    /**
     * Decrypts a given text.
     *
     * @param encrypted
     *            the encrypted text. optionally leaded by the iv and separated
     *            with ':'.
     * @return the string
     */
    public static String decrypt(String encrypted)
            throws GeneralSecurityException {

        if (encrypted == null) {
            return null;
        }

        byte[] iv;
        byte[] decoded;
        Cipher cipher;
        try {
            // ensure backward compatibility to encrypted texts without iv
            if (encrypted.contains(":")) {
                String[] split = encrypted.split(":");

                iv = Base64.decodeBase64(split[0].getBytes("UTF-8"));
                decoded = Base64.decodeBase64(split[1].getBytes("UTF-8"));

                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            } else {

                decoded = Base64.decodeBase64(encrypted.getBytes("UTF-8"));
                cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, key);
            }

            return new String(cipher.doFinal(decoded));
        } catch (BadPaddingException exc) {
            LOG.logError(Log4jLogger.SYSTEM_LOG, exc,
                    LogMessageIdentifier.ERROR_BAD_PASSWORD);
            throw new RuntimeException(exc);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}