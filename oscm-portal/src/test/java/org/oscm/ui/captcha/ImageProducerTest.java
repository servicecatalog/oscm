/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 * Created by PLGrubskiM on 2016-08-12.
 *
 *******************************************************************************/

package org.oscm.ui.captcha;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


public class ImageProducerTest {

    ImageProducer imageProducer;
    final String message = "message";

    @Before
    public void setup() {
        imageProducer = new ImageProducer(message);
    }

    @Test
    public void testCreateCaptchaKey() {
        //given
        Color color = new Color(255, 255, 255);
        //when
        final BufferedImage image = imageProducer.createImage(color);
        //then
        assertTrue(image.getGraphics().getColor().equals(color));
    }

    @Test
    public void testCreateCaptchaKey_null() {
        //given
        Color color = null;
        Color defaultColor = new Color(255, 255, 255);
        //when
        final BufferedImage image = imageProducer.createImage(color);
        //then
        assertTrue(image.getGraphics().getColor().equals(defaultColor));
    }
}
