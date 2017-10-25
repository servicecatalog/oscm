/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-03-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import java.util.Arrays;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ValidationExceptionBean;

/**
 * Exception thrown when the validation of a parameter fails.
 * 
 */
@WebFault(name = "ValidationException", targetNamespace = "http://oscm.org/xsd")
public class ValidationException extends SaaSApplicationException {

    private static final long serialVersionUID = 130279298073246937L;

    private ValidationExceptionBean bean = new ValidationExceptionBean();

    private static String getMessage(ReasonEnum reason, String member,
            Object[] params) {
        final String memberstr;
        if (member == null) {
            memberstr = "";
        } else {
            memberstr = " for member " + member;
        }
        final String paramsstr;
        if (params == null) {
            paramsstr = "";
        } else {
            paramsstr = escapeParam(String.format(" (parameters=%s)",
                    Arrays.asList(params)));
        }
        return String.format("Validation failed%s with reason %s%s.",
                memberstr, reason, paramsstr);
    }

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ValidationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ValidationException(String message) {
        super(message);
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
    public ValidationException(String message, ValidationExceptionBean bean) {
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
    public ValidationException(String message, ValidationExceptionBean bean,
            Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
    }

    /**
     * Constructs a new exception with a generated detail message and the given
     * message parameters, and appends the specified reason to the message key.
     * The generated detail message depends on the specified reason, member
     * field, and message parameters.
     * 
     * @param reason
     *            the reason
     * @param member
     *            the member field that could not be validated
     * @param params
     *            the message parameters
     * 
     */
    public ValidationException(ReasonEnum reason, String member, Object[] params) {
        super(getMessage(reason, member, params), params);
        bean.setReason(reason);
        bean.setMember(member);
        setMessageKey(initMessageKey());
    }

    private String initMessageKey() {
        ReasonEnum reason = bean.getReason();
        if (reason.getMessageKey() != null) {
            return reason.getMessageKey();
        } else {
            String enumName = reason.toString();
            return super.getMessageKey() + "." + enumName;
        }
    }

    /**
     * Returns the reason for this exception.
     * 
     * @return the reason
     */
    public ReasonEnum getReason() {
        return bean.getReason();
    }

    /**
     * Returns the name of the member field that could not be validated.
     * 
     * @return the field name
     */
    public String getMember() {
        return bean.getMember();
    }

    /* javadoc copied from super class */
    @Override
    public ValidationExceptionBean getFaultInfo() {
        return new ValidationExceptionBean(super.getFaultInfo(),
                bean.getReason(), bean.getMember());
    }

    /**
     * Enumeration of possible reasons for a {@link ValidationException}.
     * 
     */
    public static enum ReasonEnum {
        /**
         * The specified value is not a valid email address.
         */
        EMAIL(),
        /**
         * The specified value exceeds the permitted length.
         */
        LENGTH(),
        /**
         * The specified text exceeds the permitted length.
         */
        LENGTH_TEXT(),
        /**
         * Required information is missing.
         */
        REQUIRED("javax.faces.component.UIInput.REQUIRED"),
        /**
         * The given security data is not valid, for example, required
         * information is missing.
         */
        SECURITY_INFO(),
        /**
         * The specified value is not a valid integer.
         */
        INTEGER(),
        /**
         * The given data contains characters which are not allowed in IDs.
         */
        ID_CHAR(),
        /**
         * The specified value is not a valid long value.
         */
        LONG(),
        /**
         * The specified value is not a valid boolean.
         */
        BOOLEAN(),
        /**
         * The specified value is a negative numeric value, which is not
         * permitted in the current context.
         */
        POSITIVE_NUMBER(),

        /**
         * The specified value is not a valid duration in days. A valid value is
         * a positive long value that does not exceed 106751991167 and is a
         * multiple of the amount of milliseconds per day.
         */
        DURATION(),

        /**
         * <code>null</code> or a value of length zero is expected in this
         * field.
         */
        EMPTY_VALUE(),

        /**
         * The specified value is not a valid CSS color definition.
         */
        CSS_COLOR(),

        /**
         * The specified value is not a valid CSS value.
         */
        CSS_VALUE(),

        /**
         * The specified value is not a valid URL.
         */
        URL(),

        /**
         * The specified value is not a valid relative URL.
         */
        RELATIVE_URL(),

        /**
         * The remote LDAP system refused the connection.
         */
        LDAP_CONNECTION_REFUSED(),

        /**
         * The user with the given ID is not found in the remote LDAP system.
         */
        LDAP_USER_NOT_FOUND(),

        /**
         * A given user ID does not uniquely identify a user in the remote LDAP
         * system.
         */
        LDAP_USER_NOT_UNIQUE(),

        /**
         * The user ID created for LDAP is not valid, for example, it exceeds
         * the allowed length. This problem may arise if a platform user with
         * the given user ID already exists.
         */
        LDAP_CREATED_ID_INVALID(),

        /**
         * A value entered by a user does not match the corresponding value in
         * the remote LDAP system.
         */
        LDAP_VALUE_MISMATCH(),

        /**
         * The value specified in the "dn" attribute does not point to the root
         * or is not found in the remote LDAP system.
         */
        LDAP_BASE_DN_INVALID,

        /**
         * One of the mandatory LDAP settings is not defined (neither a specific
         * value for the current organization nor a default value set for the
         * platform can be found).
         */
        LDAP_MANDATORY_PROPERTY_MISSING,

        /**
         * The LDAP setting must have a valid key and a valid non-empty value.
         */
        LDAP_INVALID_PLATFORM_PROPERTY,

        /**
         * The specified value is not a valid Enumeration value.
         */
        ENUMERATION(),

        /**
         * The specified value is not in the allowed range.
         */
        VALUE_NOT_IN_RANGE(),

        /**
         * The specified value is not a reference to a valid object.
         */
        INVALID_REFERENCE(),

        /**
         * A price without steps as well as stepped prices are defined for the
         * recurring charge for users.
         */
        STEPPED_USER_PRICING(),

        /**
         * A price without steps as well as stepped prices are defined for an
         * event.
         */
        STEPPED_EVENT_PRICING(),

        /**
         * A price without steps as well as stepped prices are defined for a
         * parameter.
         */
        STEPPED_PARAMETER_PRICING(),

        /**
         * Stepped pricing is only allowed for numeric parameters.
         */
        STEPPED_PRICING(),

        /**
         * Two or more steps specified for stepped pricing have the same limit.
         */
        STEPPED_PRICING_DUPLICATE_LIMIT(),

        /**
         * The specified value is already used by another catalog entry.
         */
        DUPLICATE_VALUE(),

        /**
         * The specified position is already in use by another catalog entry.
         */
        DUPLICATE_CATALOG_POSITION(),

        /**
         * Another PSP account for the same organization and payment service
         * provider already exists.
         */
        DUPLICATE_PSP_ACCOUNT(),

        /**
         * The priced parameter does not have a reference to a parameter
         * definition, or the value type of the underlying parameter definition
         * is not allowed for priced parameters (e.g. type String).
         */
        PRICED_PARAM_WRONG_BASE(),

        /**
         * The given country code is not supported by the current organization.
         */
        COUNTRY_NOT_SUPPORTED,

        /**
         * The specified value is not a valid VAT rate.
         */
        VAT(),

        /**
         * VAT support is disabled.
         */
        VAT_NOT_SUPPORTED(),

        /**
         * The specified value is not a valid decimal value.
         */
        DECIMAL(),

        /**
         * User-based prices in price models are not allowed for services with
         * direct access.
         */
        DIRECT_ACCESS_USER_PRICE(),

        /**
         * A service can be published to one catalog entry only.
         */
        INVALID_NUMBER_TARGET_CATALOG_ENTRIES,

        /**
         * The specified value contains an invalid character.
         */
        INVALID_CHAR,

        /**
         * The format of the given image is invalid.
         */
        IMAGE_TYPE,

        /**
         * The given image is too small.
         */
        IMAGE_SIZE_TOO_SMALL,

        /**
         * The given image is too large.
         */
        IMAGE_SIZE_TOO_BIG,

        /**
         * The allowed maximum number of tags has been exceeded.
         */
        TAGS_MAX_COUNT,

        /**
         * No rating was given.
         */
        RATING_REQUIRED,

        /**
         * The specified locale is invalid.
         */
        INVALID_LOCALE,

        /**
         * The specified entity type for the user operation log is not
         * supported.
         */
        USER_OPERATION_LOG_ENTITY_TYPE,

        /**
         * The specified setting value is invalid.
         */
        INVALID_CONFIGURATION_SETTING,

        /**
         * A service with the external access type must be free of charge.
         */
        EXTERNAL_SERVICE_MUST_BE_FREE_OF_CHARGE,

        /**
         * The import of the file failed. The reason may be, for example, an
         * incorrect file format or invalid input parameters.
         */
        FILE_IMPORT_FAILED,

        /**
         * The specified value is too short.
         */
        MIN_LENGTH,

        /**
         * The specified currency is invalid. It cannot be resolved into a valid
         * Java currency.
         */
        INVALID_CURRENCY,

        /**
         * The trigger definition specifies that the action given as the trigger
         * type is to be suspended, but the action does not support this
         * behavior.
         */
        TRIGGER_TYPE_SUPPORTS_NO_PROCESS_SUSPENDING,

        /**
         * The action specified as the trigger type is not available for the
         * organization role.
         */
        TRIGGER_TYPE_NOT_ALLOWED,

        /**
         * Either the broker revenue share or the reseller revenue share must be
         * set.
         */
        ONE_OF_PARTNER_REVENUE_SHARE_MANDATORY,

        /**
         * The parameter value cannot be modified because the parameter
         * definition only allows for setting it once.
         */
        ONE_TIME_PARAMETER_NOT_ALLOWED,

        /**
         * The scale of a big decimal exceeds the maximum value.
         */
        SCALE_TO_LONG,

        /**
         * The date range is invalid.
         */
        INVALID_DATE_RANGE;

        private String messageKey;

        private ReasonEnum(String messageKey) {
            this.messageKey = messageKey;
        }

        private ReasonEnum() {

        }

        /**
         * Returns the message key of the exception. This is the key used for
         * the resource bundle message files.
         * 
         * @return the message key
         */
        public String getMessageKey() {
            return messageKey;
        }
    }

}
