/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides information on a customer's payment information for processing at a
 * payment service provider (PSP).
 * 
 */
public class RequestData implements Serializable {

    private static final long serialVersionUID = -7152136822791994565L;
    private Long organizationKey;
    private String organizationId;
    private String organizationName;
    private String organizationEmail;
    private String currentUserLocale;
    private Long paymentTypeKey;
    private String paymentTypeId;
    private Long paymentInfoKey;
    private String paymentInfoId;
    private String pspIdentifier;
    private String externalIdentifier;

    private List<Property> properties;
    private transient Map<String, String> propertiesMap;

    /**
     * Returns the identifier provided by the PSP for the supplier or reseller
     * organization to which the customer is related.
     * 
     * @return the identifier
     */
    public String getPspIdentifier() {
        return pspIdentifier;
    }

    /**
     * Sets the identifier provided by the PSP for the supplier or reseller
     * organization to which the customer is related.
     * 
     * @param pspIdentifier
     *            the identifier
     */
    public void setPspIdentifier(String pspIdentifier) {
        this.pspIdentifier = pspIdentifier;
    }

    /**
     * Returns the numeric key of the customer organization.
     * 
     * @return the organization key
     */
    public Long getOrganizationKey() {
        return organizationKey;
    }

    /**
     * Sets the numeric key of the customer organization.
     * 
     * @param organizationKey
     *            the organization key
     */
    public void setOrganizationKey(Long organizationKey) {
        this.organizationKey = organizationKey;
    }

    /**
     * Returns the language set for the calling user.
     * 
     * @return the language code
     */
    public String getCurrentUserLocale() {
        return currentUserLocale;
    }

    /**
     * Sets the language for the calling user.
     * 
     * @param currentUserLocale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */
    public void setCurrentUserLocale(String currentUserLocale) {
        this.currentUserLocale = currentUserLocale;
    }

    /**
     * Returns the identifier of the payment type selected by the customer.
     * 
     * @return the payment type ID
     */
    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    /**
     * Sets the identifier of the payment type selected by the customer.
     * 
     * @param paymentTypeId
     *            the payment type ID
     */
    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    /**
     * Returns the numeric key of the payment information object stored in the
     * platform. Note that this is not available with the first registration.
     * 
     * @return the payment information key
     */
    public Long getPaymentInfoKey() {
        return paymentInfoKey;
    }

    /**
     * Sets the numeric key of the payment information object stored in the
     * platform. Note that this is not available with the first registration.
     * 
     * @param paymentInfoKey
     *            the payment information key
     */
    public void setPaymentInfoKey(Long paymentInfoKey) {
        this.paymentInfoKey = paymentInfoKey;
    }

    /**
     * Returns the identifier provided by the PSP for the payment information.
     * 
     * @return the identifier
     */
    public String getExternalIdentifier() {
        return externalIdentifier;
    }

    /**
     * Sets the identifier provided by the PSP for the payment information.
     * 
     * @param externalIdentifier
     *            the identifier
     */
    public void setExternalIdentifier(String externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    /**
     * Returns the platform's identifier of the customer organization.
     * 
     * @return the organization ID
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the platform's identifier of the customer organization.
     * 
     * @param organizationId
     *            the organization ID
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Returns the name of the customer organization.
     * 
     * @return the organization name
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * Sets the name of the customer organization.
     * 
     * @param organizationName
     *            the organization name
     */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * Returns the email address of the customer organization.
     * 
     * @return the email address
     */
    public String getOrganizationEmail() {
        return organizationEmail;
    }

    /**
     * Sets the email address of the customer organization.
     * 
     * @param organizationEmail
     *            the email address
     */
    public void setOrganizationEmail(String organizationEmail) {
        this.organizationEmail = organizationEmail;
    }

    /**
     * Returns the numeric key of the payment type selected by the customer.
     * 
     * @return the payment type key
     */
    public Long getPaymentTypeKey() {
        return paymentTypeKey;
    }

    /**
     * Sets the numeric key of the payment type selected by the customer.
     * 
     * @param paymentTypeKey
     *            the payment type key
     */
    public void setPaymentTypeKey(Long paymentTypeKey) {
        this.paymentTypeKey = paymentTypeKey;
    }

    /**
     * Returns the identifier of the payment information as specified by the
     * customer.
     * 
     * @return the payment information ID
     */
    public String getPaymentInfoId() {
        return paymentInfoId;
    }

    /**
     * Sets the identifier of the payment information as specified by the
     * customer.
     * 
     * @param paymentInfoId
     *            the payment information ID
     */
    public void setPaymentInfoId(String paymentInfoId) {
        this.paymentInfoId = paymentInfoId;
    }

    /**
     * Returns the configuration keys and their values which are needed for the
     * communication with the PSP.
     * 
     * @return the list of properties
     */
    public List<Property> getProperties() {
        return properties;
    }

    /**
     * Sets the configuration keys and their values which are needed for the
     * communication with the PSP.
     * 
     * @param properties
     *            the list of properties
     */
    public void setProperties(List<Property> properties) {
        this.properties = properties;
        propertiesMap = null;
    }

    /**
     * Returns the value of the specified property.
     * 
     * @param key
     *            the key of the property
     * @return the value
     */
    public String getProperty(String key) {
        if (properties == null) {
            return null;
        }
        if (propertiesMap == null) {
            propertiesMap = new HashMap<String, String>();
            for (Property p : properties) {
                propertiesMap.put(p.getKey(), p.getValue());
            }
        }
        return propertiesMap.get(key);
    }
}
