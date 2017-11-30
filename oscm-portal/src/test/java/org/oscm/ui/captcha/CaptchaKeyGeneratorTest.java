/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 * Created by PLGrubskiM on 2016-08-12.
 *
 *******************************************************************************/

package org.oscm.ui.captcha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


public class CaptchaKeyGeneratorTest {

    CaptchaKeyGenerator captchaKeyGenerator;

    @Before
    public void setup() {
        captchaKeyGenerator = new CaptchaKeyGenerator();
    }

    @Test
    public void testCreateCaptchaKey() {
        //given
        int i = 5;
        //when
        final String captchaKey = captchaKeyGenerator.createCaptchaKey(i);
        //then
        assertEquals(i,captchaKey.length());
    }

    @Test
    public void testCreateCaptchaKey2() {
        //given
        int i = 5;
        //when
        final String captchaKey1 = captchaKeyGenerator.createCaptchaKey(i);
        final String captchaKey2 = captchaKeyGenerator.createCaptchaKey(i);
        //then
        assertTrue(!captchaKey1.equalsIgnoreCase(captchaKey2));
    }

    @Test
    public void testCreateCaptchaKeyZero() {
        //given
        int i = 0;
        //when
        final String captchaKey = captchaKeyGenerator.createCaptchaKey(i);
        //then
        assertEquals(i,captchaKey.length());
    }

}
