/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Vitali Ryumshyn                                                      
 *                                                                              
 *  Creation Date: 16.05.2011                                                      
 *                                                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ValidationException;

public class ImageServiceValidatorTest {

    @Test
    public void testConstructor() throws Exception {
        new ImageValidator();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void validate_NoData() throws Exception {
        ImageValidator.validate(null, "image/gif", 0, 0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_NoSizeParams() throws Exception {
        byte[] imageData = getImageData("icon1.png");
        ImageValidator.validate(imageData, null, 0, 0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_NoHeighSizeParam() throws Exception {
        byte[] imageData = getImageData("icon1.png");
        ImageValidator.validate(imageData, null, 80, -1, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_NoWidthSizeParam() throws Exception {
        byte[] imageData = getImageData("icon1.png");
        ImageValidator.validate(imageData, null, 0, 70, 0, 0);
    }

    @Test(expected = ValidationException.class)
    public void validate_NoContentType() throws Exception {
        byte[] imageData = getImageData("icon1.png");
        ImageValidator.validate(imageData, null, 0, 0, 80, 80);
    }

    @Test(expected = ValidationException.class)
    public void validate_WongImageData() throws Exception {
        byte[] imageData = "Just some bytes".getBytes();
        ImageValidator.validate(imageData, "image/gif", 0, 0, 80, 80);
    }

    @Test(expected = ValidationException.class)
    public void validate_EmptyContentType() throws Exception {
        byte[] imageData = getImageData("icon1.png");
        ImageValidator.validate(imageData, "", 0, 0, 80, 80);
    }

    @Test(expected = ValidationException.class)
    public void validate_WrongContentType() throws Exception {
        byte[] imageData = getImageData("icon1.png");
        ImageValidator.validate(imageData, "image/unknown", 0, 0, 80, 80);
    }

    @Test(expected = ValidationException.class)
    public void validate_InvalidBigGifImage() throws Exception {
        byte[] imageData = getImageData("Icon100x100.gif");
        ImageValidator.validate(imageData, "image/gif", 0, 0, 80, 80);
    }

    @Test(expected = ValidationException.class)
    public void validate_InvalidNotImagePngImage() throws Exception {
        byte[] imageData = getImageData("NotImage.png");
        ImageValidator.validate(imageData, "image/png", 0, 0, 80, 80);
    }

    @Test(expected = ValidationException.class)
    public void validate_InvalidSizeJpgImageTooSmall() throws Exception {
        // image is 80x80
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 120, 80, 120, 80);
    }

    @Test(expected = ValidationException.class)
    public void validate_InvalidSizeJpgImageTooBig() throws Exception {
        // image is 80x80
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 12, 8, 40, 70);
    }

    @Test
    public void validate_ValidJpgImageExact() throws Exception {
        // image is 80x80
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 80, 80, 80, 80);
    }

    @Test
    public void validate_ValidRangeLowerLimitExact() throws Exception {
        // image is 80x80
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 80, 80, 90, 90);
    }

    @Test
    public void validate_ValidRangeUpperLimitExact() throws Exception {
        // image is 80x80
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 70, 70, 80, 80);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_InvalidRangeAllToZero() throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 0, 0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_InvalidRangeWidthsToZero() throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 0, 10, 0, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_InvalidRangeHeightsToZero() throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 10, 0, 20, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_InvalidRangeMinHeightGreaterThanMaxHeight()
            throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 10, 10, 20, 9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_InvalidRangeMinWidthGreaterThanMaxWidth()
            throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 10, 10, 9, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_InvalidRangeMinWidthGreaterNullMinWidthLessEqNull()
            throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 10, 0, 9, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_InvalidRangeMaxWidthGreaterNullMaxWidthLessEqNull()
            throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 10, 10, 9, 0);
    }
    
    @Test
    public void validate_ValidPngImage() throws Exception {
        byte[] imageData = getImageData("icon1.png");
        ImageValidator.validate(imageData, "image/png", 0, 0, 80, 80);
    }

    @Test
    public void validate_ValidPngImageWithWrongExt() throws Exception {
        byte[] imageData = getImageData("icon1.pg");
        ImageValidator.validate(imageData, "image/png", 0, 0, 80, 80);
    }

    @Test
    public void validate_ValidGifImage() throws Exception {
        byte[] imageData = getImageData("IconOk.gif");
        ImageValidator.validate(imageData, "image/gif", 0, 0, 80, 80);
    }

    @Test
    public void validate_ValidJpgImage() throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", 0, 0, 80, 80);
    }

    @Test
    public void validate_ValidPngImageForIE7() throws Exception {
        byte[] imageData = getImageData("icon1.png");
        ImageValidator.validate(imageData, "image/x-png", 0, 0, 80, 80);
    }

    @Test
    public void validate_ValidJpgImageForIE7() throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/pjpeg", 0, 0, 80, 80);
    }

    @Test(expected = ValidationException.class)
    public void validate_validateImageTypeNullContentType() throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validateImageType(imageData, null);
    }

    @Test(expected = ValidationException.class)
    public void validate_validateImageTypeEmptyContentType() throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validateImageType(imageData, "");
    }

    @Test(expected = ValidationException.class)
    public void validate_validateImageTypeWrongContentType() throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validateImageType(imageData, "image/testtype");
    }

    @Test
    public void validate_validateImage() throws Exception {
        byte[] imageData = getImageData("IconOk.jpg");
        ImageValidator.validate(imageData, "image/jpg", ImageType.ORGANIZATION_IMAGE);
    }
    
    private byte[] getImageData(String imagePath) throws IOException {
        return getFileAsByteArray(
                ImageServiceValidatorTest.class, imagePath);
    }

    private byte[] getFileAsByteArray(Class<?> clazz, String path)
        throws IOException {
        URL fileURL = clazz.getClassLoader().getResource(path);

        InputStream is = null;
        byte[] bytes;
        try {
            File file = new File(fileURL.getFile());

            is = new FileInputStream(file);

            long length = file.length();
            bytes = new byte[(int) length];

            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file "
                    + file.getName());
            }
        } finally {
            if (is != null)
                is.close();
        }
        return bytes;

    }

}
