/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.profile;

import java.io.Serializable;

import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.vo.VOImageResource;

public class POImageResource implements Serializable {

    private static final long serialVersionUID = 1L;

    private byte[] buffer;
    private String contentType;
    private ImageType imageType;

    public POImageResource(VOImageResource voImageResource) {
        this.buffer = voImageResource.getBuffer();
        this.contentType = voImageResource.getContentType();
        this.imageType = voImageResource.getImageType();
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

}
