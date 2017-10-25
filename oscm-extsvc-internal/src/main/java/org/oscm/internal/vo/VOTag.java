/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-03                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.vo.BaseVO;

/**
 * Represents a tag of the tag cloud for marketable services.
 * 
 */
public class VOTag extends BaseVO {
    private static final long serialVersionUID = -2100805606029894462L;

    private String locale;
    private String value;
    private long numberReferences;

    /**
     * Retrieves the language of the tag.
     * 
     * @return the language code
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the language of the tag.
     * 
     * @param locale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Retrieves the string displayed for the tag.
     * 
     * @return the tag value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the string to be displayed for the tag.
     * 
     * @param value
     *            the tag value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Retrieves the number of services using the tag.
     * 
     * @return the number of references
     */
    public long getNumberReferences() {
        return numberReferences;
    }

    /**
     * Sets the number of services using the tag.
     * 
     * @param numberReferences
     *            the number of references
     */
    public void setNumberReferences(long numberReferences) {
        this.numberReferences = numberReferences;
    }

    /**
     * Returns a string representation of the object: the value followed by the
     * locale.
     * 
     * @return the string representation
     */
    public String toString() {
        return value + " (" + locale + ")";
    }
}
