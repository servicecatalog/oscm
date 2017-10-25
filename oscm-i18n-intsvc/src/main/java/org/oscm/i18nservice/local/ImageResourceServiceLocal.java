/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                    
 *                                                                              
 *  Creation Date: 06.11.2009                                                      
 *                                                                              
 *  Completion Time: 06.11.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.i18nservice.local;

import javax.ejb.Local;

import org.oscm.domobjects.ImageResource;
import org.oscm.internal.types.enumtypes.ImageType;

/**
 * Internal interface providing the functionality to manage image resources in
 * the database.
 * 
 * @author pock
 * 
 */
@Local
public interface ImageResourceServiceLocal {

    /**
     * Deletes the image resource with the given image resource identifier.
     * 
     * @param objectKey
     *            The key of the object the image belongs to.
     * @param imageType
     *            The type of the image.
     */
    public void delete(long objectKey, ImageType imageType);

    /**
     * Reads the image resource for the given object key and image type.
     * 
     * @param objectKey
     *            The technical key of the object the image resource belongs to.
     * @param imageType
     *            The image type of the image resource.
     * @return the read image resource object
     */
    public ImageResource read(long objectKey, ImageType imageType);

    /**
     * Stores (creates or upadates) the given image resource object.
     * 
     * @param imageResource
     *            The ImageResource to store.
     */
    public void save(ImageResource imageResource);

}
