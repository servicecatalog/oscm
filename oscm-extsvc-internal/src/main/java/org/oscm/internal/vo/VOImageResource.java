/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-11-09                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.vo;

import java.io.Serializable;

import org.oscm.internal.types.enumtypes.ImageType;

/**
 * Represents an image stored in the database.
 * 
 */
public class VOImageResource implements Serializable {

    private static final long serialVersionUID = -3341708633056931255L;

    private final static byte[] IMAGE_HEADER_GIF = new byte[] { 'G', 'I', 'F' };

    private final static byte[] IMAGE_HEADER_JPEG = new byte[] { (byte) 0xff,
            (byte) 0xd8 };

    private final static byte[] IMAGE_HEADER_PNG = new byte[] { (byte) 0x89,
            'P', 'N', 'G' };

    private byte[] buffer;

    private String contentType;

    private ImageType imageType;

    /**
     * @param a1
     *            array
     * @param a2
     *            array
     * @return true if the larger array begins with the same bytes as the
     *         smaller one.
     */
    private boolean arrayCompare(byte[] a1, byte[] a2) {
        if (a1.length < a2.length) {
            for (int i = 0; i < a1.length; i++) {
                if (a1[i] != a2[i]) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < a2.length; i++) {
                if (a1[i] != a2[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Retrieves the buffer size used for the image.
     * 
     * @return the buffer size in bytes
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * Sets the buffer size to be used for the image.
     * 
     * @param buffer
     *            the buffer size in bytes
     */
    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    /**
     * Retrieves the content type of the image, for example, jpeg, gif, png, or
     * another downloadable format.
     * 
     * @return the content type
     */
    public String getContentType() {
        if ((contentType == null || contentType.length() == 0 || contentType
                .equalsIgnoreCase("application/octet-stream"))
                && buffer != null && buffer.length > 0) {
            if (arrayCompare(IMAGE_HEADER_JPEG, buffer)) {
                contentType = "image/jpeg";
            } else if (arrayCompare(IMAGE_HEADER_GIF, buffer)) {
                contentType = "image/gif";
            } else if (arrayCompare(IMAGE_HEADER_PNG, buffer)) {
                contentType = "image/png";
            } else {
                contentType = "application/x-download";
            }
        }
        return contentType;
    }

    /**
     * Sets the content type for the image, for example, jpeg, gif, png, or
     * another downloadable format.
     * 
     * @param contentType
     *            the content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Retrieves the type of the image, for example, a logo or parts thereof, or
     * a service image.
     * 
     * @return the image type
     */
    public ImageType getImageType() {
        return imageType;
    }

    /**
     * Sets the type of the image, for example, a logo or parts thereof, or a
     * service image.
     * 
     * @param imageType
     *            the image type
     */
    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

}
