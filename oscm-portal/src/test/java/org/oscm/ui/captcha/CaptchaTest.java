/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 * Created by PLGrubskiM on 2016-08-12.
 *
 *******************************************************************************/

package org.oscm.ui.captcha;

import static org.junit.Assert.assertTrue;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.junit.Before;
import org.junit.Test;


public class CaptchaTest {

    Captcha captcha;
    private int charLen = 4;
    private BufferedImage bufferedImage;

    @Before
    public void setup() {
        captcha = new Captcha(charLen, bufferedImage);
    }

    @Test
    public void testCreateCaptchaKey() {
        //given
        //when
        Color color = new Color(255, 255, 255);
        captcha.createCaptcha("someKey", color);
        //then
        Color resultColor = captcha.getImage().getGraphics().getColor();
        assertTrue(resultColor.equals(color));
        assertTrue(captcha.getKey().equalsIgnoreCase("someKey"));
    }

    @Test
    public void testCreateCaptchaKey_NullKey() {
        //given
        //when
        Color color = new Color(255, 255, 255);
        captcha.createCaptcha(null, color);
        //then
        Color resultColor = captcha.getImage().getGraphics().getColor();
        assertTrue(resultColor.equals(color));
        assertTrue(!captcha.getKey().isEmpty());
    }

}
