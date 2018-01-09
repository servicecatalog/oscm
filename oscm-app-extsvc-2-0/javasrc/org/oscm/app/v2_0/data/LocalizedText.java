/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2014-03-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

import java.io.Serializable;

/**
 * Represents a text resource for a specific locale.
 * 
 */
public class LocalizedText implements Serializable {

    private static final long serialVersionUID = -2167375749373460790L;

    private String locale;
    private String text;

    /**
     * Default constructor.
     */
    public LocalizedText() {
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
    public LocalizedText(String locale, String text) {
        this.locale = locale;
        this.text = text;
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
}
