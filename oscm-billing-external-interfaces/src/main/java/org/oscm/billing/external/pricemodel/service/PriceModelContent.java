/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-10-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.pricemodel.service;

import java.io.Serializable;

/**
 * Represents the content and tag of a price model for a specific locale.
 * <p>
 * For the content type, the <a
 * href="http://www.iana.org/assignments/media-types/media-types.xhtml"> IANA
 * media types</a> can be used. Be aware, however, that Catalog Manager only
 * supports the <code>application/pdf</code> type and the content must be in
 * valid PDF format. Non-ASCII characters have to be converted to Unicode escape
 * sequences.
 * <p>
 * A tag is a string with short information on the price model, such as a price
 * range, which is to be shown directly at a service on a marketplace.
 * 
 */
public class PriceModelContent implements Serializable {

    private static final long serialVersionUID = -8724898141388542458L;

    private String contentType;
    private byte[] content;
    private String tag;
    private String filename;

    /**
     * Default constructor.
     */
    public PriceModelContent() {
    }

    /**
     * Constructs a price model content object with the given content type and
     * content.
     * 
     * @param contentType
     *            the content type. This can be any of the IANA media types. Be
     *            aware, however, that Catalog Manager only supports the
     *            <code>application/pdf</code> type.
     * @param content
     *            the content in the format specified by
     *            <code>contentType</code>. Non-ASCII characters have to be
     *            converted to Unicode escape sequences.
     * 
     */
    public PriceModelContent(String contentType, byte[] content) {
        this.contentType = contentType;
        this.content = content;
        this.tag = null;
    }

    /**
     * Constructs a price model content object with the given content type,
     * content, and tag.
     * 
     * @param contentType
     *            the content type. This can be any of the IANA media types. Be
     *            aware, however, that Catalog Manager only supports the
     *            <code>application/pdf</code> type.
     * @param content
     *            the content in the format specified by
     *            <code>contentType</code>. Non-ASCII characters have to be
     *            converted to Unicode escape sequences.
     * @param tag
     *            a string with short information on the price model. Strings
     *            longer than 30 bytes will not be displayed on a marketplace.
     *            Non-ASCII characters have to be converted to Unicode escape
     *            sequences.
     */
    public PriceModelContent(String contentType, byte[] content, String tag) {
        this.contentType = contentType;
        this.content = content;
        this.tag = tag;
    }

    /**
     * Returns the type of the price model content.
     * 
     * @return the type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the type of the price model content.
     * 
     * @param contentType
     *            the content type. This can be any of the IANA media types. Be
     *            aware, however, that Catalog Manager only supports the
     *            <code>application/pdf</code> type.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the content of the price model.
     * 
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the content of the price model.
     * 
     * @param content
     *            the content in the format specified by
     *            <code>contentType</code>. Non-ASCII characters have to be
     *            converted to Unicode escape sequences.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Returns the tag of the price model.
     * 
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets the tag of the price model.
     * 
     * @param tag
     *            a string with short information on the price model. Strings
     *            longer than 30 bytes will not be displayed on a marketplace.
     *            Non-ASCII characters have to be converted to Unicode escape
     *            sequences.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Returns the path and name of the file where the price model is stored.
     * 
     * @return the path and file name
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the path and name of the file where the price model is stored.
     * 
     * @param filename
     *            the path and file name
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

}
