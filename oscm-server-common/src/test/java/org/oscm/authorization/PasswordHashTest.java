/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.authorization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class PasswordHashTest {

    /**
     * The hash function is reproducible.
     */
    @Test
    public void testCalculateHash1() {
        final byte[] hash1 = PasswordHash.calculateHash(123456, "secret");
        final byte[] hash2 = PasswordHash.calculateHash(123456, "secret");
        assertArrayEquals(hash1, hash2);
    }

    /**
     * The salt will modify the hash.
     */
    @Test
    public void testCalculateHash2() {
        final byte[] hash1 = PasswordHash.calculateHash(123456, "secret");
        final byte[] hash2 = PasswordHash.calculateHash(124456, "secret");
        assertFalse(Arrays.equals(hash1, hash2));
    }

    /**
     * Different passwords result in different hashes.
     */
    @Test
    public void testCalculateHash3() {
        final byte[] hash1 = PasswordHash.calculateHash(123456, "secret");
        final byte[] hash2 = PasswordHash.calculateHash(123456, "secrEt");
        assertFalse(Arrays.equals(hash1, hash2));
    }

    @Test
    public void testVerifyPassword() {
        final int salt = 123456;
        final byte[] hash = PasswordHash.calculateHash(salt, "secret");
        assertTrue(PasswordHash.verifyPassword(salt, hash, "secret"));
        assertFalse(PasswordHash.verifyPassword(salt, hash, "Secret"));
    }

    @Test
    public void testVerifyPassword_Empty() {
        final int salt = 123456;
        final byte[] hash = PasswordHash.calculateHash(salt, "      ");
        assertTrue(PasswordHash.verifyPassword(salt, hash, "      "));
    }

    /**
     * Not actully a test case, just to calculate the initial password hash:
     */
    @Test
    public void calculateInitialPasswordHash() {
        final int salt = 0;
        final String password = "admin123";
        final byte[] hash = PasswordHash.calculateHash(salt, password);
        System.out.println("Salt:        " + salt);
        System.out.println("Password:    " + password);
        System.out.printf("Hash Base64: %s", Base64.encode(hash));
        System.out.printf("Hash:        0x%x%n", new BigInteger(hash));
        System.out.print("Hash:        ");
        for (int i = 0; i < hash.length; i++) {
            System.out.printf("\\\\%03o", Integer.valueOf(0xFF & hash[i]));
        }
        System.out.println("");
        System.out.println(Arrays.toString(hash));
    }

}
