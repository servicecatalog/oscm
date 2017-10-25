/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.authorization;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Static utility functions for secure password hashes.
 * 
 * @author hoffmann
 */
public class PasswordHash {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Creates a 256 bit (32 byte) hash from the given password.
     * 
     * @param salt
     * @param password
     * @return
     */
    public static byte[] calculateHash(long salt, String password) {
        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final DataOutputStream data = new DataOutputStream(buffer);
            data.writeLong(salt);
            data.writeUTF(password);
            final MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return digest.digest(buffer.toByteArray());
        } catch (IOException e) {
            // Must not happen for ByteArrayOutputStream
            throw new AssertionError(e);
        } catch (NoSuchAlgorithmException e) {
            // Must not happen for SHA-256
            throw new AssertionError(e);
        }
    }

    /**
     * Verifies that the given password corresponds to the given salt and hash.
     * 
     * @param salt
     * @param hash
     * @param password
     * @return <code>true</code> if the password is valid
     */
    public static boolean verifyPassword(long salt, byte[] hash, String password) {
        return Arrays.equals(calculateHash(salt, password), hash);
    }

}
