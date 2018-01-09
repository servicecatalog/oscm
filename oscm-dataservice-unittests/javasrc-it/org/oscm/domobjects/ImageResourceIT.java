/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 13.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.ImageType;

/**
 * @author barzu
 */
public class ImageResourceIT extends DomainObjectTestBase {

    @Test
    public void persist_VeryLongResultXML() throws Exception {
        // given
        byte[] veryLongByteArray = createVeryLongByteArray(10); // 10MB
        ImageResource result = new ImageResource();
        result.setImageType(ImageType.ORGANIZATION_IMAGE);
        result.setBuffer(veryLongByteArray);

        // when
        long key = persist(result).longValue();

        // then
        ImageResource storedResult = load(ImageResource.class, key);
        for (int i = 0; i < veryLongByteArray.length; i++) {
            assertEquals("Stored byte array is corrupt at index " + i + ".",
                    veryLongByteArray[i], storedResult.getBuffer()[i]);
        }
    }

    private byte[] createVeryLongByteArray(int sizeInMB) {
        byte[] veryLongByteArray = new byte[1024 * 1024 * sizeInMB];
        Arrays.fill(veryLongByteArray, (byte) 2);
        return veryLongByteArray;
    }

}
