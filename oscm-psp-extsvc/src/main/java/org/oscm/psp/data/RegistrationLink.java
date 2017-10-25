/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.data;

import java.io.Serializable;

/**
 * Provides a link to the registration page of a payment service provider (PSP).
 * The link specifies the URL of the registration page and whether to open the
 * page in the current browser window (e.g. in an iFrame) or in a new one.
 * 
 */
public class RegistrationLink implements Serializable {

    private static final long serialVersionUID = 5922057070490157101L;

    private String url;
    private String browserTarget;

    /**
     * Default constructor
     */
    public RegistrationLink() {
    }

    /**
     * Constructs a registration link with the given URL and browser target.
     * 
     * @param url
     *            the URL of the PSP's registration page
     * @param browserTarget
     *            specify "<code>_ </code>" (an underscore followed by a blank)
     *            to open the registration page in a new browser window, or
     *            <code>null</code> to use the current window
     */
    public RegistrationLink(String url, String browserTarget) {
        this.url = url;
        this.browserTarget = browserTarget;
    }

    /**
     * Returns the URL of the PSP's registration page.
     * 
     * @return the URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the PSP's registration page.
     * 
     * @param url
     *            the URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns whether the PSP's registration page is to be opened in the
     * current browser window (e.g. in an iFrame) or in a new one.
     * 
     * @return "<code>_ </code>" (an underscore followed by a blank) if the
     *         registration page is opened in a new browser window, or
     *         <code>null</code> if the current window is used
     */
    public String getBrowserTarget() {
        return browserTarget;
    }

    /**
     * Specifies whether the PSP's registration page is to be opened in the
     * current browser window (e.g. in an iFrame) or in a new one.
     * 
     * @param browserTarget
     *            specify "<code>_ </code>" (an underscore followed by a blank)
     *            to open the registration page in a new browser window, or
     *            <code>null</code> to use the current window
     */
    public void setBrowserTarget(String browserTarget) {
        this.browserTarget = browserTarget;
    }
}
