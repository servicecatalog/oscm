/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.aes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.oscm.app.vmware.encryption.AESEncrypter;

/**
 * @author kulle
 *
 */
public class AesTest {

    @Test
    public void vm() throws Exception {
        // given
        String password = "e$_w=x268WEI3vfr7";
        String vmEncrypted = AESEncrypter.encrypt(password);

        // when
        String vmDecrypted = AESEncrypter.decrypt(vmEncrypted);

        // then
        assertEquals(password, vmDecrypted);
    }

    @Test
    public void ctmg() throws Exception {
        // given
        String password = "e$_w=x268WEI3vfr7";
        String encrypted = AESEncrypterCtmg.encrypt(password);

        // when
        String decrypted = AESEncrypterCtmg.decrypt(encrypted);

        // then
        assertEquals(password, decrypted);
    }

    @Test
    public void ctmg_vm() throws Exception {
        // given
        String password = "e$_w=x268WEI3vfr7";
        String encrypted = AESEncrypterCtmg.encrypt(password);

        // when
        String decrypted = AESEncrypter.decrypt(encrypted);

        // then
        assertEquals("e$_w=x268WEI3vfr7", decrypted);
    }

    @Test
    public void decryptCtmgPassword() throws Exception {
        // given
        String encrypted = "+ULbWgH1mTXCs79dgOqTNw==";

        // when
        String password = AESEncrypterCtmg.decrypt(encrypted);

        // then
        assertEquals("secret", password);
    }

    @Test
    public void umlaute_vm() throws Exception {
        // given
        String password = "e$_w=x268WEI3vf7ä";
        String vmEncrypted = AESEncrypter.encrypt(password);

        // when
        String vmDecrypted = AESEncrypter.decrypt(vmEncrypted);

        // then
        assertEquals(password, vmDecrypted);
    }

    @Test
    public void umlaute() throws Exception {
        // given
        String password = "e$_w=x268WEI3vf7ä";
        String encrypted = AESEncrypterCtmg.encrypt(password);

        // when
        String decrypted = AESEncrypterCtmg.decrypt(encrypted);

        // then
        assertEquals(password, decrypted);
    }

    @Test
    public void uwe() throws Exception {
        // given
        String utf8_encoded = "k95HaoH8WYFxAwdKEdaZq+nx3oyR0hqM9Y3m85U+Lf8=";

        // then
        String utfMinus8_decoded = AESEncrypterCtmg.decrypt(utf8_encoded);

        // then
        assertEquals("e$_w=x268WEI3vf7ä", utfMinus8_decoded);
    }
}
