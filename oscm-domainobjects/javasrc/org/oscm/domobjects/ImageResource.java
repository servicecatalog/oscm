/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 06.11.2009                                                      
 *                                                                              
 *  Completion Time: 06.11.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.converters.ITConverter;
import org.oscm.internal.types.enumtypes.ImageType;

/**
 * Represents one concrete image.
 * 
 * @author pock
 * 
 */
@Entity
@NamedQueries({ @NamedQuery(name = "ImageResource.findByBusinessKey", query = "SELECT ir FROM ImageResource ir WHERE imageType = :imageType AND objectKey = :objectKey") })
@BusinessKey(attributes = { "objectKey", "imageType" })
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "objectKey",
        "imageType" }))
public class ImageResource extends
        DomainObjectWithVersioning<EmptyDataContainer> {

    private static final long serialVersionUID = -1210081185962661197L;

    public ImageResource() {
        super();
    }

    public ImageResource(long objectKey, ImageType imageType) {
        super();
        this.objectKey = objectKey;
        this.imageType = imageType;
    }

    @Lob
    @Column(nullable = false)
    private byte[] buffer;

    private String contentType;

    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    private long objectKey;

    public byte[] getBuffer() {
        return buffer;
    }

    public String getContentType() {
        return contentType;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public long getObjectKey() {
        return objectKey;
    }

    public void setBuffer(byte[] value) {
        this.buffer = value;
    }

    public void setContentType(String locale) {
        this.contentType = locale;
    }

    public void setImageType(ImageType objectType) {
        this.imageType = objectType;
    }

    public void setObjectKey(long objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * Copies this ImageResource and sets the specified object key in the copy
     * 
     * @param objectKey
     *            an object key
     * @return the new created ImageResource
     */
    public ImageResource copy(long objectKey) {
        ImageResource newImageResource = new ImageResource();
        newImageResource.setObjectKey(objectKey);
        newImageResource.setBuffer(this.getBuffer());
        newImageResource.setImageType(this.getImageType());
        newImageResource.setContentType(this.getContentType());
        return newImageResource;
    }

}
