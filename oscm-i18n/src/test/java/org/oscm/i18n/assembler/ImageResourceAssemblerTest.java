/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.i18n.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.ImageResource;
import org.oscm.i18nservice.assembler.ImageResourceAssembler;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.vo.VOImageResource;

public class ImageResourceAssemblerTest {

    private final static byte[] IMAGE_HEADER_JPEG = new byte[] { (byte) 0xff,
            (byte) 0xd8 };

    private final static byte[] IMAGE_HEADER_PNG = new byte[] { (byte) 0x89,
            'P', 'N', 'G' };

    private VOImageResource voImageResource;

    @Before
    public void setupTest() {
        voImageResource = new VOImageResource();
        voImageResource.setBuffer("test string".getBytes());
        voImageResource.setContentType("content type");
        voImageResource.setImageType(ImageType.ORGANIZATION_IMAGE);
    }

    @Test
    public void testToImageResource() {
        ImageResource imageResource = ImageResourceAssembler
                .toImageResource(voImageResource);

        assertEquals(voImageResource.getBuffer(), imageResource.getBuffer());
        assertEquals(voImageResource.getContentType(),
                imageResource.getContentType());
        assertEquals(voImageResource.getImageType(),
                imageResource.getImageType());
        assertEquals(0, imageResource.getObjectKey());
    }

    @Test
    public void testToImageResource_null() {
        assertNull(ImageResourceAssembler.toImageResource(null));
    }

    @Test
    public void testContentType_GIF() {
        // test content based type detection
        voImageResource.setContentType(null);
        voImageResource.setBuffer("GIFandmorebytecontent".getBytes());
        assertEquals("image/gif", voImageResource.getContentType());
    }

    @Test
    public void testContentType_PNG() {
        // test content based type detection
        voImageResource.setContentType("");
        voImageResource.setBuffer(IMAGE_HEADER_PNG);
        assertEquals("image/png", voImageResource.getContentType());
    }

    @Test
    public void testContentType_JPEG() {
        // test content based type detection
        voImageResource.setContentType(null);
        voImageResource.setBuffer(IMAGE_HEADER_JPEG);
        assertEquals("image/jpeg", voImageResource.getContentType());
    }

    @Test
    public void testContentType_Short() {
        // test content based type detection
        voImageResource.setContentType("application/octet-stream");
        voImageResource.setBuffer("1".getBytes());
        assertEquals("application/x-download", voImageResource.getContentType());
    }

    @Test
    public void testContentType_BufferEmpty() {
        // test content based type detection
        voImageResource.setContentType("unchanged");
        voImageResource.setBuffer(new byte[0]);
        assertEquals("unchanged", voImageResource.getContentType());
    }

    @Test
    public void testContentType_BufferNull() {
        // test content based type detection
        voImageResource.setContentType("unchanged");
        voImageResource.setBuffer(null);
        assertEquals("unchanged", voImageResource.getContentType());
    }
}
