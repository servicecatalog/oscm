/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-11-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the available types of images in the image resource table.
 */
public enum ImageType {

    /**
     * The image of an organization.
     */
    ORGANIZATION_IMAGE(ImageOwnerType.ORGANIZATION, new ImageSize(1, 154, 1, 154)),

    /**
     * The logo background image of a marketplace.
     */
    SHOP_LOGO_BACKGROUND(ImageOwnerType.SHOP),

    /**
     * The left logo of a marketplace.
     */
    SHOP_LOGO_LEFT(ImageOwnerType.SHOP),

    /**
     * The right logo of a marketplace.
     */
    SHOP_LOGO_RIGHT(ImageOwnerType.SHOP),

    /**
     * A service image.
     */
    SERVICE_IMAGE(ImageOwnerType.SERVICE);

    private ImageOwnerType imageOwnerType;
    private ImageSize imageSize;

    /**
     * Constructor that accepts the type of the image owner.
     * 
     * @param imageOwnerType
     *            the type of the image owner
     */
    private ImageType(ImageOwnerType imageOwnerType) {
        this.imageOwnerType = imageOwnerType;
    }

    /**
     * Constructor that accepts the type of the image owner.
     * 
     * @param imageOwnerType
     *            the type of the image owner
     * @param imageSize
     *            the image size
     */
    private ImageType(ImageOwnerType imageOwnerType, ImageSize imageSize) {
        this.imageOwnerType = imageOwnerType;
        this.imageSize = imageSize;
    }

    /**
     * Retrieves the minimum and maximum height and width defined for the images
     * of the current image type.
     * 
     * @return the image size, or <code>null</code> if none exists
     */
    public ImageSize getImageSize() {
        return imageSize;
    }

    /**
     * Retrieves the owner type for the current image type. This can be a
     * marketplace, an organization, or a service.
     * 
     * @return the owner type
     */
    public ImageOwnerType getOwnerType() {
        return imageOwnerType;
    }

    /**
     * Specifies the owner types for images.
     */
    public enum ImageOwnerType {
        /**
         * The image belongs to a marketplace.
         */
        SHOP,
        /**
         * The image belongs to a service.
         */
        SERVICE,
        /**
         * The image belongs to an organization.
         */
        ORGANIZATION;
    }

    /**
     * Specifies the minimum and maximum height and width for the images of a
     * specific image type.
     */
    public static class ImageSize {

        private int minWidth;
        private int minHeight;
        private int maxWidth;
        private int maxHeight;

        /**
         * Create an image size object.
         * 
         * @param minWidth
         *            the minimum image width in pixels
         * @param maxWidth
         *            the maximum image width in pixels
         * @param minHeight
         *            the minimum image height in pixels
         * @param maxHeight
         *            the maximum image height in pixels
         * 
         */
        public ImageSize(int minWidth, int maxWidth, int minHeight,
                int maxHeight) {
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }

        /**
         * Returns the minimum image width.
         * 
         * @return the minimum width in pixels
         */
        public int getMinWidth() {
            return minWidth;
        }

        /**
         * Returns the minimum image height.
         * 
         * @return the minimum height in pixels
         */
        public int getMinHeight() {
            return minHeight;
        }

        /**
         * Returns the maximum image width.
         * 
         * @return the maximum width in pixels
         */
        public int getMaxWidth() {
            return maxWidth;
        }

        /**
         * Returns the maximum image height.
         * 
         * @return the maximum height in pixels
         */
        public int getMaxHeight() {
            return maxHeight;
        }

    }
}
