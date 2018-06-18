/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.06.2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.openstack.usage;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AesEncrypter {

    private static final Logger LOG = LoggerFactory
            .getLogger(AesEncrypter.class);

    public static final int IV_BYTES = 16;
    public static final int KEY_BYTES = 16;

    private static SecretKey key;

    public static byte[] getKey() {
        return key.getEncoded();
    }

    public static void setKey(byte[] key) {
        AesEncrypter.key = new SecretKeySpec(key, "AES");
    }

    private static String getKeyFilePath() {
        return "./key";
    }

    private static void initEncryption() throws GeneralSecurityException {
        String path = getKeyFilePath();
        File keyFile = new File(path);

        if (keyFile.exists()) {

            try {
                byte[] key = Files.readAllBytes(keyFile.toPath());

                AesEncrypter.setKey(
                        Arrays.copyOfRange(key, 0, AesEncrypter.KEY_BYTES));
            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                throw new GeneralSecurityException("Keyfile at "
                        + keyFile.getAbsolutePath() + " is not readable");
            }
        } else {
            throw new GeneralSecurityException(
                    "Keyfile not accessible. " + keyFile.getAbsolutePath());
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

        initEncryption();

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

        initEncryption();

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
            LOG.error("Failed to decrypt password.", exc);
            throw new RuntimeException(exc);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
