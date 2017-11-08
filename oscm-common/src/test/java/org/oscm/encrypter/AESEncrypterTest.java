/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 11, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.encrypter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for AESEncrypter
 * 
 * @author miethaner
 */
public class AESEncrypterTest {

    @Test
    public void testEncryptDecrypt() throws Exception {

        String value = "value to encrypt";

        AESEncrypter.generateKey();

        String encrypted = AESEncrypter.encrypt(value);
        String decrypted = AESEncrypter.decrypt(encrypted);

        assertEquals(value, decrypted);
    }

    @Test
    public void testEncryptDecryptWithExternalKey() throws Exception {

        String value = "value to encrypt";

        AESEncrypter.generateKey();

        byte[] key = AESEncrypter.getKey();
        AESEncrypter.setKey(key);

        String encrypted = AESEncrypter.encrypt(value);
        String decrypted = AESEncrypter.decrypt(encrypted);

        assertEquals(value, decrypted);
    }

}
