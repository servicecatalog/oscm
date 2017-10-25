/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                     
 *                                                                              
 *  Creation Date: 30.04.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.enums;

/**
 * This enumeration lists all information within the domain objects that is
 * supported in several languages (i18n).
 * 
 * @author Mike J&auml;ger
 */
public enum LocalizedObjectTypes {

    /**
     * Identifies the description of an operation status.
     */
    OPERATION_STATUS_DESCRIPTION(InformationSource.DATABASE),

    /**
     * Identifies the description of an organization.
     */
    ORGANIZATION_DESCRIPTION(InformationSource.DATABASE),

    /**
     * Identifies the marketing name of a product.
     */
    PRODUCT_MARKETING_NAME(InformationSource.DATABASE),

    /**
     * Identifies the marketing description of a product.
     */
    PRODUCT_MARKETING_DESC(InformationSource.DATABASE),

    /**
     * Identifies the description of a product related event event.
     */
    EVENT_DESC(InformationSource.DATABASE_AND_RESOURCE_BUNDLE,
            "PlatformLocalizations"),

    /**
     * Identifies the information to be used in the mail header.
     */
    MAIL_CONTENT(InformationSource.RESOURCE_BUNDLE, "Mail"),

    /**
     * Identifies the description of a technical product.
     */
    TEC_PRODUCT_TECHNICAL_DESC(InformationSource.DATABASE),

    /**
     * Identifies the license description of a product.
     */
    PRODUCT_LICENSE_DESC(InformationSource.DATABASE),

    /**
     * Identifies the description of a price model.
     */
    PRICEMODEL_DESCRIPTION(InformationSource.DATABASE),

    /**
     * Identifies the description of a report.
     */
    REPORT_DESC(InformationSource.DATABASE_AND_RESOURCE_BUNDLE,
            "PlatformLocalizations"),

    /**
     * Identifies the description of a parameter definition.
     */
    PARAMETER_DEF_DESC(InformationSource.DATABASE_AND_RESOURCE_BUNDLE,
            "PlatformLocalizations"),

    /**
     * Identifies the description of a parameter definition.
     */
    OPTION_PARAMETER_DEF_DESC(InformationSource.DATABASE),

    /**
     * Identifies the message properties of a shop (customized i18n strings)
     */
    SHOP_MESSAGE_PROPERTIES(InformationSource.DATABASE),

    /**
     * Identifies the description of the login parameter.
     */
    TEC_PRODUCT_LOGIN_ACCESS_DESC(InformationSource.DATABASE),

    /**
     * The service role description.
     */
    ROLE_DEF_DESC(InformationSource.DATABASE),

    /**
     * The service role name.
     */
    ROLE_DEF_NAME(InformationSource.DATABASE),

    /**
     * The technical product operation name.
     */
    TECHNICAL_PRODUCT_OPERATION_NAME(InformationSource.DATABASE),

    /**
     * The technical product operation description.
     */
    TECHNICAL_PRODUCT_OPERATION_DESCRIPTION(InformationSource.DATABASE),

    /**
     * The technical product operation parameter name.
     */
    TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME(InformationSource.DATABASE),

    /**
     * The subscription provisioning process information.
     */
    SUBSCRIPTION_PROVISIONING_PROGRESS(InformationSource.DATABASE),

    /**
     * The reason data stored for a subscription modification process .
     */
    SUBSCRIPTION_MODIFICATION_REASON(InformationSource.DATABASE),

    /**
     * The reason data stored for a trigger process.
     */
    TRIGGER_PROCESS_REASON(InformationSource.DATABASE),

    /**
     * Identifies the license description of price model.
     */
    PRICEMODEL_LICENSE(InformationSource.DATABASE),

    /**
     * The name of a marketplace.
     */
    MARKETPLACE_NAME(InformationSource.DATABASE),

    /**
     * The name of a payment type.
     */
    PAYMENT_TYPE_NAME(InformationSource.DATABASE_AND_RESOURCE_BUNDLE,
            "PlatformLocalizations"),

    /**
     * Defines the content of the stage of the landing page.
     */
    MARKETPLACE_STAGE(InformationSource.DATABASE),

    /**
     * Identifies the short description of a marketable product.
     */
    PRODUCT_SHORT_DESCRIPTION(InformationSource.DATABASE, null, ""),

    /**
     * The name of a category.
     */
    CATEGORY_NAME(InformationSource.DATABASE),

    /**
     * Reseller can overwrite the price model license specified by the supplier
     * organization of a service. This tag indicates such a refinement.
     */
    RESELLER_PRICEMODEL_LICENSE(InformationSource.DATABASE),

    /**
     * Identifies the message properties of platform (customized i18n strings)
     */
    MESSAGE_PROPERTIES(InformationSource.DATABASE),

    /**
     * Identifies the mail properties of platform (customized i18n strings)
     */
    MAIL_PROPERTIES(InformationSource.DATABASE),

    /**
     * Identifies the exception properties. These messages can be overwritten by
     * the user for each marketplace independently and for the platform in
     * general.
     */
    EXCEPTION_PROPERTIES(InformationSource.RESOURCE_BUNDLE, "ExceptionMessages"),

    /**
     * Name of the custom tab on my subscriptions page
     */
    PRODUCT_CUSTOM_TAB_NAME(InformationSource.DATABASE),

    /**
     * Name of custom user attribute.
     */
    CUSTOM_ATTRIBUTE_NAME(InformationSource.DATABASE);

    private InformationSource source;
    private String sourceLocation;
    private String defaultValue;

    /**
     * Constructor that accepts the source of information, the concrete source
     * location if required and a default value. if the source is
     * {@link InformationSource#DATABASE} the sourceLocation can be left as
     * <code>null</code>.
     * 
     * @param source
     *            The source of the information.
     * @param sourceLocation
     *            The concrete location of the information source, e.g. a bundle
     *            name.
     * @param defaultValue
     *            The default value if no translation can be found.
     */
    private LocalizedObjectTypes(InformationSource source,
            String sourceLocation, String defaultValue) {
        this.source = source;
        this.sourceLocation = sourceLocation;
        this.defaultValue = defaultValue;
    }

    /**
     * Constructor that accepts the source of information and the concrete
     * source name if required. if the source is
     * {@link InformationSource#DATABASE} the sourceLocation can be left as
     * <code>null</code>.
     * 
     * @param source
     *            The source of the information.
     * @param sourceLocation
     *            The concrete location of the information source, e.g. a bundle
     *            name.
     */
    private LocalizedObjectTypes(InformationSource source, String sourceLocation) {
        this.source = source;
        this.sourceLocation = sourceLocation;
    }

    /**
     * Constructor that sets the sourceLocation to null, refer to
     * {@link #LocalizedObjectTypes(InformationSource, String)}.
     * 
     * @param source
     *            The source of the information.
     */
    private LocalizedObjectTypes(InformationSource source) {
        this(source, null);
    }

    public InformationSource getSource() {
        return source;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Internal class to indicate the supported information sources.
     */
    public enum InformationSource {
        DATABASE, RESOURCE_BUNDLE, DATABASE_AND_RESOURCE_BUNDLE;

        public boolean canBeModified() {
            return this != RESOURCE_BUNDLE;
        }
    }

    /**
     * Checks if the given localized object type is product related.
     * 
     * @param type
     *            The localized object type to check
     * @return
     */
    public boolean isProductRelated() {
        return this == PRODUCT_MARKETING_NAME || this == PRODUCT_MARKETING_DESC
                || this == PRODUCT_LICENSE_DESC
                || this == PRODUCT_SHORT_DESCRIPTION;
    }

    /**
     * Checks if the given localized object type is price model related.
     * 
     * @param type
     *            The localized object type to check
     * @return
     */
    public boolean isPriceModelRelated() {
        return this == LocalizedObjectTypes.PRICEMODEL_DESCRIPTION;
    }
}
