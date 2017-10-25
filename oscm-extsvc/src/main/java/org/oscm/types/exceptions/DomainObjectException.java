/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import org.oscm.types.exceptions.beans.DomainObjectExceptionBean;

/**
 * Abstract class for exceptions related to domain object operations which use a
 * business key.
 * 
 */
public abstract class DomainObjectException extends SaaSApplicationException {

    private static final long serialVersionUID = 7800776145815565455L;

    private DomainObjectExceptionBean bean = new DomainObjectExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public DomainObjectException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public DomainObjectException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public DomainObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message and bean for
     * JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     */
    public DomainObjectException(String message, DomainObjectExceptionBean bean) {
        super(message, bean);
        this.bean = bean;
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * bean for JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     * @param cause
     *            the cause
     */
    public DomainObjectException(String message,
            DomainObjectExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * parameters.
     * 
     * @param message
     *            the detail message
     * @param classEnum
     *            a <code>classEnum</code> specifying the type of the object for
     *            which the exception is thrown
     * @param businessKey
     *            the business key of the object for which the exception is
     *            thrown
     */
    public DomainObjectException(String message, ClassEnum classEnum,
            String businessKey) {
        super(message, new Object[] { businessKey });
        setDomainObjectClassEnum(classEnum);
    }

    /**
     * Initializes the message key based on the provided class enumeration.
     * 
     * @param classEnum
     *            The class enum.
     */
    private void initMessageKey(ClassEnum classEnum) {
        String result = getMessageKey();
        if (classEnum != null) {
            String enumName = classEnum.toString();
            enumName = enumName.substring(enumName.lastIndexOf(".") + 1);
            result += "." + enumName;
        }
        setMessageKey(result);
    }

    /**
     * Returns the type of the object the exception is related to.
     * 
     * @return the type
     */
    public ClassEnum getDomainObjectClassEnum() {
        return bean.getClassEnum();
    }

    /**
     * Sets the object type for this exception and updates the message key
     * accordingly.
     * 
     * @param classEnum
     *            a <code>classEnum</code> specifying the object type to be set.
     *            Must not be <code>null</code>.
     */
    public void setDomainObjectClassEnum(ClassEnum classEnum) {
        bean.setClassEnum(classEnum);
        initMessageKey(classEnum);
    }

    /* javadoc is copied from super class */
    public DomainObjectExceptionBean getFaultInfo() {
        return new DomainObjectExceptionBean(super.getFaultInfo(),
                bean.getClassEnum());
    }

    /**
     * Enumeration of object types a {@link DomainObjectException} can be
     * related to.
     * 
     */
    public static enum ClassEnum {
        /**
         * Organization.
         */
        ORGANIZATION("organizationId"),
        /**
         * Service.
         */
        SERVICE("productId"),
        /**
         * Subscription.
         */
        SUBSCRIPTION("subscriptionId"),
        /**
         * Registered user.
         */
        USER("userId"),
        /**
         * Payment information.
         */
        PAYMENT_INFO("id"),
        /**
         * Technical service.
         */
        TECHNICAL_SERVICE("technicalProductId"),
        /**
         * Organization role.
         */
        ORGANIZATION_ROLE("roleName"),
        /**
         * Parameter definition of a technical service.
         */
        PARAMETER_DEFINITION("parameterId"),
        /**
         * Option of a parameter defined for a technical service.
         */
        PARAMETER_OPTION("optionId"),
        /**
         * Parameter of a marketable service.
         */
        PARAMETER("id"),
        /**
         * Priced parameter of a marketable service.
         */
        PRICED_PARAMETER("id"),
        /**
         * Report.
         */
        REPORT("reportName"),
        /**
         * Event.
         */
        EVENT("eventIdentifier"),
        /**
         * Role defined for a technical service.
         */
        ROLE_DEFINITION("roleId"),
        /**
         * Operation defined for a technical service.
         */
        TECHNICAL_SERVICE_OPERATION("operationId"),
        /**
         * Parameter of a technical service operation.
         */
        OPERATION_PARAMETER("id"),
        /**
         * Payment type.
         */
        PAYMENT_TYPE("paymentTypeId"),
        /**
         * Supported currency.
         */
        SUPPORTED_CURRENCY("currencyISOCode"),
        /**
         * Definition of a custom attribute.
         */
        UDA_DEFINITION("udaId"),

        /**
         * Custom attribute.
         */
        UDA("id"),

        /**
         * Country supported for an organization.
         */
        ORGANIZATION_TO_COUNTRY("id"),

        /**
         * Supported country.
         */
        SUPPORTED_COUNTRY("countryISOCode"),

        /**
         * VAT rate.
         */
        VAT_RATES("id"),

        /**
         * Organization reference.
         */
        ORGANIZATION_REFERENCE("id"),

        /**
         * Marketplace.
         */
        MARKETPLACE("marketplaceId"),

        /**
         * Trigger definition.
         */
        TRIGGER_DEFINITION("id"),

        /**
         * Trigger process.
         */
        TRIGGER_PROCESS("id"),

        /**
         * Tag for marketable services.
         */
        TAG("id"),

        /**
         * Tag of a technical service.
         */
        TECHNICAL_SERVICE_TAG("id"),

        /**
         * User role.
         */
        USER_ROLE("roleName"),

        /**
         * User who wrote a review for a marketable service.
         */
        PRODUCT_REVIEW("platformuser"),

        /**
         * Billing contact of an organization.
         */
        BILLING_CONTACT("id"),

        /**
         * Reference between marketplace and organization.
         */
        MARKETPLACE_TO_ORGANIZATION("id"),

        /**
         * Reference between subscription and marketplace.
         */
        SUBSCRIPTION_TO_MARKETPLACE("id"),

        /**
         * Reference between service and payment type.
         */
        PRODUCT_TO_PAYMENTTYPE("id"),

        /**
         * Configured payment service provider.
         */
        PSP("identifier"),

        /**
         * Category.
         */
        CATEGORY("marketplaceKey,categoryId"),

        /**
         * Permission to create marketable services based on a specific
         * technical service.
         */
        MARKETING_PERMISSION("technicalProductKey,organizationReferenceKey"),

        /**
         * Report result cache.
         */
        REPORT_RESULT_CACHE("cachekey"),

        /**
         * Organization setting.
         */
        ORGANIZATION_SETTING("organization, settingType"),

        /**
         * Platform setting.
         */
        PLATFORM_SETTING("settingType"),

        /**
         * Localized resource.
         */
        LOCALIZED_RESOURCE("objectKey, locale, objectType"),

        /**
         * User group.
         */
        USER_GROUP("name, organization_tkey");

        /**
         * Stores the name of the fields containing the attribute name that
         * identifies the object to the user. In case such a field is not known
         * (but only the key could serve) the field is named 'id'.
         */
        private String idFieldName;

        ClassEnum(String idFieldName) {
            this.idFieldName = idFieldName;
        }

        /**
         * Returns the identifier field for the current type, for example, "id".
         * 
         * @return the name of the identifier field
         */
        public String getIdFieldName() {
            return idFieldName;
        }
    }

}
