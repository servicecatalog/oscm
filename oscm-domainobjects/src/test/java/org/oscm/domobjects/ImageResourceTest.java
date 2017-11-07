/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 12.03.2010                                                      
 *                                                                              
 *  Completion Time: 12.03.2010                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.ImageType;

public class ImageResourceTest {

    @Test
    public void settersAndGetters() {
        ImageResource ir = new ImageResource();
        ir.setContentType("contentType");
        ir.setImageType(ImageType.SERVICE_IMAGE);
        ir.setObjectKey(1L);
        byte[] bufferContent = "buffer".getBytes();
        ir.setBuffer(bufferContent);

        assertEquals("Wrong object key", 1L, ir.getObjectKey());
        assertEquals("Wrong content type", "contentType", ir.getContentType());
        assertEquals("Wrong image type", ImageType.SERVICE_IMAGE,
                ir.getImageType());
        assertEquals("Wrong buffer content", bufferContent, ir.getBuffer());
    }

}
