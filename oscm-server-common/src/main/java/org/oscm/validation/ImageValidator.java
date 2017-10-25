/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.validation;

import javax.swing.ImageIcon;

import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.ImageType.ImageSize;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

/**
 * Utility class to validate service icon images.
 * 
 * @author Vitali Ryumshyn
 */

public class ImageValidator {

    /**
     * Validates if the image is one of the following types: jpg, png, gif.
     * 
     * @param imageData
     * @param contentType
     * @throws ValidationException
     *             Is thrown if the image data is not of the expected type.
     */
    public static void validateImageType(byte[] imageData, String contentType)
            throws ValidationException {

        if (contentType == null || contentType.length() == 0) {
            throw new ValidationException(ReasonEnum.IMAGE_TYPE, "image type",
                    null);
        }

        final ImageIcon image = new ImageIcon(imageData);
        if (image.getImage() == null) {
            throw new ValidationException(ReasonEnum.IMAGE_TYPE,
                    "image format", null);
        }

        if (!contentType.equalsIgnoreCase("image/jpeg")
                && !contentType.equalsIgnoreCase("image/jpg")
                && !contentType.equalsIgnoreCase("image/pjpeg")
                && !contentType.equalsIgnoreCase("image/png")
                && !contentType.equalsIgnoreCase("image/x-png")
                && !contentType.equalsIgnoreCase("image/gif")) {
            throw new ValidationException(ReasonEnum.IMAGE_TYPE, "image type",
                    new Object[] { contentType });
        }

    }

    /**
     * Validates service's image, checks content type and width/height in
     * pixels. Either minimum values must both be greater than zero or maximum
     * values must both be greater than zero or both.
     * 
     * @param imageData
     *            image content
     * @param contentType
     *            of image
     * @param imageType
     *            the type of the image
     * @throws ValidationException
     */
    public static void validate(byte[] imageData, String contentType,
            ImageType imageType) throws ValidationException {
        ImageSize imageSize = imageType.getImageSize();
        validate(imageData, contentType, imageSize.getMinWidth(),
                imageSize.getMinHeight(), imageSize.getMaxWidth(),
                imageSize.getMaxHeight());
    }

    /**
     * Validates service's image, checks content type and width/height in
     * pixels. Either minimum values must both be greater than zero or maximum
     * values must both be greater than zero or both.
     * 
     * @param imageData
     *            image content
     * @param contentType
     *            of image
     * @param minWidth
     *            minimum width in pixel to check, 0 or less indicates to omit
     *            check
     * @param minHeight
     *            minimum height in pixel to check, 0 or less indicates to omit
     *            check
     * @param maxWidth
     *            maximum width in pixel to check, 0 or less indicates to omit
     *            check
     * @param maxHeight
     *            maximum geight in pixel to check, 0 or less indicates to omit
     *            check
     * @throws ValidationException
     */
    public static void validate(byte[] imageData, String contentType,
            int minWidth, int minHeight, int maxWidth, int maxHeight)
            throws ValidationException {

        if (imageData == null) {
            throw new IllegalArgumentException(
                    "Parameter imageData must not be greater zero");
        }
        if (minWidth <= 0 && maxWidth <= 0) {
            throw new IllegalArgumentException(
                    "Parameter minWidth or maxWidth must not be greater zero");
        }
        if (minHeight <= 0 && maxHeight <= 0) {
            throw new IllegalArgumentException(
                    "Parameter minHeight or maxHeight must not be greater zero");
        }

        if (minWidth > 0 && minHeight <= 0 || minWidth <= 0 && minHeight > 0) {
            throw new IllegalArgumentException(
                    "Parameter pair minWidth/minHeight must both be zero or both be greater than zero");
        }

        if (maxWidth > 0 && maxHeight <= 0 || maxWidth <= 0 && maxHeight > 0) {
            throw new IllegalArgumentException(
                    "Parameter pair maxWidth/maxHeight must both be zero or both be greater than zero");
        }

        if (maxWidth > 0 && minWidth > 0 && maxWidth < minWidth) {
            throw new IllegalArgumentException(
                    "Parameter maxWidth must be greater or equal than minWidth");
        }

        if (maxHeight > 0 && minHeight > 0 && maxHeight < minHeight) {
            throw new IllegalArgumentException(
                    "Parameter maxHeight must be greater or equal than minHeight");
        }

        if (contentType == null || contentType.length() == 0) {
            throw new ValidationException(ReasonEnum.IMAGE_TYPE, "image type",
                    null);
        }
        final ImageIcon image = new ImageIcon(imageData);

        if (image.getImage() == null) {
            throw new ValidationException(ReasonEnum.IMAGE_TYPE,
                    "image format", null);
        }

        if (!contentType.equalsIgnoreCase("image/jpeg")
                && !contentType.equalsIgnoreCase("image/jpg")
                && !contentType.equalsIgnoreCase("image/pjpeg")
                && !contentType.equalsIgnoreCase("image/png")
                && !contentType.equalsIgnoreCase("image/x-png")
                && !contentType.equalsIgnoreCase("image/gif")) {
            throw new ValidationException(ReasonEnum.IMAGE_TYPE, "image type",
                    new Object[] { contentType });
        }

        if (image.getIconHeight() == -1 || image.getIconWidth() == -1) {
            throw new ValidationException(ReasonEnum.IMAGE_TYPE,
                    "image format", null);
        }

        if (minWidth > 0
                && (image.getIconHeight() < minHeight || image.getIconWidth() < minWidth)) {
            throw new ValidationException(ReasonEnum.IMAGE_SIZE_TOO_SMALL,
                    "image size (min values " + minWidth + 'x' + minHeight
                            + ')', new Object[] { image.getIconWidth() + "",
                            image.getIconHeight() + "" });
        }

        if (maxWidth > 0
                && (image.getIconHeight() > maxHeight || image.getIconWidth() > maxWidth)) {
            throw new ValidationException(ReasonEnum.IMAGE_SIZE_TOO_BIG,
                    "image size (max values " + maxWidth + 'x' + maxHeight
                            + ')', new Object[] { image.getIconWidth() + "",
                            image.getIconHeight() + "" });
        }
    }

}
