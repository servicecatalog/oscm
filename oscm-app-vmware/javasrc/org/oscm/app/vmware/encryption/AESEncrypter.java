/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.encryption;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Simple AES Encryption
 */
public class AESEncrypter {
    /** The Constant ENCRYPTION_KEY - generated only once here. */
    private final static byte[] ENCRYPTION_KEY = StringScrambler
            .decode(new long[] { 0x1BD9AC5E8CE971CDL, 0x98034879ACCC8904L,
                    0xF962DCA0907D0398L, 0xF54F221334184933L })
            .getBytes();

    /**
     * Encrypts a given string based on a shared secret.
     *
     * @param text
     * @return the encrypted text as Base64
     * @throws GeneralSecurityException
     *             on any problem during encryption
     */
    public static String encrypt(String text) throws GeneralSecurityException {
        return new String(encrypt(text.getBytes()));
    }

    /**
     * Encrypts a given byte array based on a shared secret.
     *
     * @param bytes
     * @return the encrypted bytes as Base64
     * @throws GeneralSecurityException
     *           on any problem during encryption
     */
    public static byte[] encrypt(byte[] bytes) throws GeneralSecurityException {
        SecretKeySpec skeySpec = new SecretKeySpec(
                Base64.decodeBase64(ENCRYPTION_KEY), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        return Base64.encodeBase64(cipher.doFinal(bytes));
    }

    /**
     * Decrypts a given text.
     *
     * @param encrypted
     *            the encrypted
     *
     * @return the string
     */
    public static String decrypt(String encrypted)
            throws GeneralSecurityException {
        return new String(decrypt(encrypted.getBytes()));
    }

    /**
     * Decrypts a given byte array.
     *
     * @param encrypted
     *            the encrypted
     *
     * @return the byte array
     */
    public static byte[] decrypt(byte[] encrypted)
            throws GeneralSecurityException {
        SecretKeySpec skeySpec = new SecretKeySpec(
                Base64.decodeBase64(ENCRYPTION_KEY), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(Base64.decodeBase64(encrypted));
    }
}
