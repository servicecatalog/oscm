/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pravi                                                       
 *                                                                              
 *  Creation Date: Aug 4, 2009                                                      
 *                                                                              
 *  Completion Time: <date>                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.captcha;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * @author pravi
 * 
 */

public class Captcha {

    private String key;

    private int charLen = 4;

    private BufferedImage buffredImage = null;

    /**
     * 
     * @param charLen
     * @param buffredImage
     */
    public Captcha(final int charLen, final BufferedImage buffredImage) {
        this.charLen = charLen;
        this.buffredImage = buffredImage;
    }

    /**
     * 
     * @param charLen
     * @param buffredImage
     */
    public Captcha(final int charLen) {
        this(charLen, null);
    }

    /**
     * @return the image
     */
    public BufferedImage getImage() {
        return buffredImage;
    }

    /**
     * @param image
     *            the image to set
     */
    public void setImage(BufferedImage image) {
        buffredImage = image;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    public void createCaptcha(final String key, final Color bgColor) {
        final CaptchaKeyGenerator ci = new CaptchaKeyGenerator();
        if (key == null) {
            setKey(ci.createCaptchaKey(charLen));
        } else {
            setKey(key);
        }
        final ImageProducer ip = new ImageProducer(this.key);
        setImage(ip.createImage(bgColor));
    }

}
