/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;

/**
 * Represents a text resource for a specific locale.
 * 
 */
public class VOLocalizedText implements Serializable {

    private static final long serialVersionUID = 7663119009187679295L;

    private String locale;

    private String text;

    // While we have a version, we do not have a corresponding database key.
    // That's why this type is not derived from BaseVo.
    private int version = -1;

    /**
     * Default constructor.
     */
    public VOLocalizedText() {
    }

    /**
     * Constructs a text resource with the specified language and text.
     * 
     * @param locale
     *            the language the information is given in. Specify a language
     *            code as returned by <code>getLanguage()</code> of
     *            <code>java.util.Locale</code>.
     * @param text
     *            the localized text
     */
    public VOLocalizedText(String locale, String text) {
        this.locale = locale;
        this.text = text;
    }

    /**
     * Constructs a text resource with the specified language, text, and
     * version.
     * 
     * @param locale
     *            the language the information is given in. Specify a language
     *            code as returned by <code>getLanguage()</code> of
     *            <code>java.util.Locale</code>.
     * @param text
     *            the localized text
     * @param version
     *            the version of the value object
     */
    public VOLocalizedText(String locale, String text, int version) {
        this.locale = locale;
        this.text = text;
        this.version = version;
    }

    /**
     * Retrieves the language of the text resource.
     * 
     * @return the language code
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the language for the text resource.
     * 
     * @param locale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Retrieves the localized information of the text resource.
     * 
     * @return the localized text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the localized information of the text resource.
     * 
     * @param text
     *            the localized text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Retrieves the version of the value object.
     * 
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version of the value object.
     * 
     * @param version
     *            the version
     */

    public void setVersion(int version) {
        this.version = version;
    }

}
