/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 01.04.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.validator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.validator.GenericValidator;
import org.oscm.converter.PriceConverter;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Helper class which provides convenient methods to perform the validation in
 * the service layer (business logic).
 * 
 */
public class BLValidator {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(BLValidator.class);

    /**
     * Checks if the length of the value is smaller as the given allowed length.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param len
     *            the allowed length.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    private static void maxLength(String member, String value, int len,
            boolean flag) throws ValidationException {
        if (flag) {
            isNotBlank(member, value);
        } else if (value == null) {
            return;
        }
        if (value.length() > len) {
            ValidationException vf = new ValidationException(ReasonEnum.LENGTH,
                    member, new Object[] { value, Integer.valueOf(len) });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Checks if the length of the value is at least the given minimum length.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param len
     *            the minimum length.
     */
    private static void minLength(String member, String value, int len)
            throws ValidationException {
        if (value == null) {
            return;
        }
        if (value.length() < len) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.MIN_LENGTH, member, new Object[] { value,
                            Integer.valueOf(len) });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Checks that the value isn't null.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     */
    public static void isNotNull(String member, Object value)
            throws ValidationException {
        if (value == null) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.REQUIRED, member, new Object[] { member });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Checks that the value isn't null and doesn't only contain whitespace.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     */
    public static void isNotBlank(String member, String value)
            throws ValidationException {
        if (GenericValidator.isBlankOrNull(value)) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.REQUIRED, member, new Object[] { member });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Checks that the value isn't too long for a locale.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isLocale(String member, String value, boolean flag)
            throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_LOCALE, flag);
        isLocaleSupported(member, value);
    }

    private static void isLocaleSupported(String member, String value)
            throws ValidationException {
        if (value == null || value.trim().length() == 0) {
            return;
        }

        boolean invalidLocale = true;
        String[] locales = Locale.getISOLanguages();// .getAvailableLocales();
        for (String locale : locales) {
            if (locale.equals(value)) {
                invalidLocale = false;
                break;
            }
        }
        if (invalidLocale) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.INVALID_LOCALE, member, new Object[] { value });
            logValidationFailure(vf);
            throw vf;
        }

    }

    /**
     * Checks that the value isn't too long for an id, has only valid characters
     * and has no leading an trailings blanks.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isId(String member, String value, boolean flag)
            throws ValidationException {
        isId(member, value, flag, ADMValidator.LENGTH_ID);
    }

    /**
     * Checks if the value contains valid characters, is not to long for an user
     * id and has no leading and tailing blanks
     * 
     * @param member
     *            the name of the member which should be checked.
     * @param value
     *            the value for which the validation should be performed.
     * @param flag
     *            if the the value must contain at least one non whitespace
     *            character.
     */
    public static void isUserId(String member, String value, boolean flag)
            throws ValidationException {
        isId(member, value, flag, ADMValidator.LENGTH_USERID);
    }

    /**
     * Common validation method for IDs. Checks for invalid characters,
     * leading/tailing blanks and length.
     * 
     * @param member
     *            the name of the member which should be checked.
     * @param value
     *            the value for which the validation should be performed.
     * @param flag
     *            if the the value must contain at least one non whitespace
     *            character.
     * @param length
     *            indicates the maximum allowed length.
     */
    protected static void isId(String member, String value, boolean flag,
            int length) throws ValidationException {
        maxLength(member, value, length, flag);
        if (!ADMValidator.containsOnlyValidIdChars(value)) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.ID_CHAR, member, new Object[] { value });
            logValidationFailure(vf);
            throw vf;
        }
        // it is not allowed to have leading and trailing blanks in ID
        if (value != null) {
            String trimmedStr = value.trim();
            if (!trimmedStr.equals(value)) {
                ValidationException vf = new ValidationException(
                        ReasonEnum.ID_CHAR, member, new Object[] { value });
                logValidationFailure(vf);
                throw vf;
            }
        }
    }

    /**
     * Checks that the value isn't too long for a name.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isName(String member, String value, boolean flag)
            throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_NAME, flag);
    }

    /**
     * Checks that the value isn't too long for a user group name.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isUserGroupName(String member, String value, boolean flag)
            throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_USER_GROUP_NAME, flag);
    }
    
    /**
     * Checks that the value is applicable as password.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     */
    public static void isPassword(String member, String value)
            throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_NAME, false);
        minLength(member, value, ADMValidator.MIN_LENGTH_PASSWORD);
    }

    /**
     * Checks that the value isn't too long for a distinguished name.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isDN(String member, String value, boolean flag)
            throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_DN, flag);
    }

    /**
     * Checks that the value isn't too long for a description.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isDescription(String member, String value, boolean flag)
            throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_DESCRIPTION, flag);
    }

    /**
     * Checks that the value isn't too long for a accessinfo.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isAccessinfo(String member, String value, boolean flag)
            throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_ACCESSINFO, flag);
    }

    /**
     * Checks that the value isn't too long for a comment.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isComment(String member, String value, boolean flag)
            throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_COMMENT, flag);
    }

    /**
     * Checks that the value isn't too long for a content of a support email.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isContentOfSupportEmail(String member, String value,
            boolean flag) throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_EMAIL_CONTENT, flag);
    }

    /**
     * Checks that the value isn't too long for a subject of a support email.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isSubjectOfSupportEmail(String member, String value,
            boolean flag) throws ValidationException {
        maxLength(member, value, ADMValidator.LENGTH_EMAIL_SUBJECT, flag);
    }

    /**
     * Checks that the value isn't too long for a name and is a valid email
     * address.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isEmail(String member, String value, boolean flag)
            throws ValidationException {
        if (!flag) {
            if (value == null || value.trim().length() == 0) {
                return;
            }
        }
        if (!ADMValidator.isEmail(value)) {
            ValidationException vf = new ValidationException(ReasonEnum.EMAIL,
                    member, new Object[] { value });
            logValidationFailure(vf);
            throw vf;
        }
        isName(member, value, flag);
    }

    /**
     * Either question and answer are not set or both must be set.
     * 
     * @param question
     *            the security question.
     * @param answer
     *            the security answer.
     */
    public static void isSecurityInfo(String question, String answer)
            throws ValidationException {
        BLValidator.isDescription("securityQuestion", question, false);
        BLValidator.isDescription("securityAnswer", answer, false);

        if (!GenericValidator.isBlankOrNull(question)
                || !GenericValidator.isBlankOrNull(answer)) {
            if (GenericValidator.isBlankOrNull(question)
                    || GenericValidator.isBlankOrNull(answer)) {
                ValidationException vf = new ValidationException(
                        ReasonEnum.SECURITY_INFO, null, null);
                logValidationFailure(vf);
                throw vf;
            }
        }
    }

    /**
     * Checks that the value isn't too long for a description and is a valid
     * absolute URL.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isUrl(String member, String value, boolean flag)
            throws ValidationException {
        if (!flag) {
            if (value == null || value.trim().length() == 0) {
                return;
            }
        }
        if (!ADMValidator.isUrl(value)) {
            ValidationException vf = new ValidationException(ReasonEnum.URL,
                    member, new Object[] { value });
            logValidationFailure(vf);
            throw vf;
        }
        isDescription(member, value, flag);
    }

    /**
     * Checks that the value isn't too long for a description and is a valid
     * relative or absolute URL.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isAbsoluteOrRelativeUrl(String member, String value,
            boolean flag) throws ValidationException {
        if (!flag) {
            if (value == null || value.trim().length() == 0) {
                return;
            }
        }
        if (!ADMValidator.isAbsoluteOrRelativeUrl(value)) {
            ValidationException vf = new ValidationException(ReasonEnum.URL,
                    member, new Object[] { value });
            logValidationFailure(vf);
            throw vf;
        }
        isDescription(member, value, flag);
    }

    /**
     * Checks that the value isn't too long for a description and is a valid
     * relative URL.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param baseUrl
     *            the baseUrl is needed for the length validation.
     * @param flag
     *            if true the value must contain at least one non whitespace
     *            character
     */
    public static void isRelativeUrl(String member, String value,
            String baseUrl, boolean flag) throws ValidationException {
        if (!flag) {
            if (value == null || value.trim().length() == 0) {
                return;
            }
        }

        boolean throwException = true;
        // a relative URL must start with a "/"
        if ((value != null) && value.startsWith("/")) {
            // create a absolute URL to check all the other URL rules
            String tempAbsoluteUrl = "http://xy".concat(value);
            if (ADMValidator.isUrl(tempAbsoluteUrl)) {
                throwException = false;
            }
        }

        if (throwException) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.RELATIVE_URL, member, new Object[] { value });
            logValidationFailure(vf);
            throw vf;
        }

        isDescription(member, baseUrl + value, flag);
    }

    /**
     * Logs a warning indicating that the validation failed to the system.log
     * file, furthermore it logs the given exception if it is not
     * <code>null</code>.
     * 
     * @param vf
     *            The exception to be logged.
     */
    private static void logValidationFailure(ValidationException vf) {
        logger.logWarn(Log4jLogger.SYSTEM_LOG, vf,
                LogMessageIdentifier.WARN_VALIDATION_FAILED);
    }

    /**
     * Validates that the given input string is a valid integer number.
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @throws ValidationException
     *             Thrown in case the value is not a valid integer.
     */
    public static void isInteger(String member, String inputValue)
            throws ValidationException {
        if (!GenericValidator.isInt(inputValue)) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.INTEGER, member, new Object[] { inputValue });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Validates that the given input string is a valid long number.
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @throws ValidationException
     *             Thrown in case the value is not a valid long.
     */
    public static void isLong(String member, String inputValue)
            throws ValidationException {
        if (!GenericValidator.isLong(inputValue)) {
            ValidationException vf = new ValidationException(ReasonEnum.LONG,
                    member, new Object[] { inputValue });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Validates that the given input string is a valid boolean value.
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @throws ValidationException
     *             Thrown in case the value is not a valid boolean.
     */
    public static void isBoolean(String member, String inputValue)
            throws ValidationException {
        if (!ADMValidator.isBoolean(inputValue)) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.BOOLEAN, member, new Object[] { inputValue });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Validates that the given input is not a negative number.
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @throws ValidationException
     *             Thrown in case the value is negative.
     */
    public static void isNonNegativeNumber(String member, long inputValue)
            throws ValidationException {
        if (!GenericValidator.isInRange(inputValue, 0L, Long.MAX_VALUE)) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.POSITIVE_NUMBER, member,
                    new Object[] { Long.valueOf(inputValue) });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Validates that the given input is bigger than zero.
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @throws ValidationException
     *             Thrown in case the value is negative.
     */
    public static void isPositiveAndNonZeroNumber(String member,
            long inputValue) throws ValidationException {
        if (!(Long.valueOf(inputValue).compareTo(Long.valueOf(0)) > 0)) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.POSITIVE_NUMBER, member,
                    new Object[] { Long.valueOf(inputValue) });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Validates that the given input is not a negative number.
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @throws ValidationException
     *             Thrown in case the value is negative.
     */
    public static void isNonNegativeNumber(String member, BigDecimal inputValue)
            throws ValidationException {
        if (inputValue.compareTo(BigDecimal.ZERO) < 0) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.POSITIVE_NUMBER, member,
                    new Object[] { inputValue.toPlainString() });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Validates that the given input is in the range or not.
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @throws ValidationException
     *             Thrown in case the value is negative.
     */
    public static void isInRange(String member, long inputValue, Long minValue,
            Long maxValue) throws ValidationException {
        if ((minValue != null && inputValue < minValue.longValue())
                || (maxValue != null && inputValue > maxValue.longValue())) {
            minValue = (minValue != null) ? minValue : Long
                    .valueOf(Long.MIN_VALUE);
            maxValue = (maxValue != null) ? maxValue : Long
                    .valueOf(Long.MAX_VALUE);
            ValidationException vf = new ValidationException(
                    ReasonEnum.VALUE_NOT_IN_RANGE, member, new Object[] {
                            Long.valueOf(inputValue), minValue, maxValue });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Validates that the given input is in the range or not.
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @param minValue
     *            minimum allowed value
     * @param maxValue
     *            maximum allowed value
     * @throws ValidationException
     *             Thrown in case the value is negative.
     */
    public static void isInRange(String member, BigDecimal inputValue,
            BigDecimal minValue, BigDecimal maxValue)
            throws ValidationException {

        if (inputValue != null && minValue != null
                && inputValue.compareTo(minValue) == -1) {
            // if less than minValue
            ValidationException vf = new ValidationException(
                    ReasonEnum.VALUE_NOT_IN_RANGE, member,
                    new Object[] { inputValue });
            logValidationFailure(vf);
            throw vf;
        }

        if (inputValue != null && maxValue != null
                && inputValue.compareTo(maxValue) == 1) {
            // if greater than maxValue
            ValidationException vf = new ValidationException(
                    ReasonEnum.VALUE_NOT_IN_RANGE, member,
                    new Object[] { inputValue });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Checks if the input value is not equals to check value.
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @param checkValue
     *            input value does not has the value of check value
     * @throws ValidationException
     *             Thrown in case the input value has the same value such as
     *             check value.
     */
    public static void isEqual(String member, BigDecimal inputValue,
            BigDecimal checkValue) throws ValidationException {
        if (inputValue != null && checkValue != null
                && inputValue.compareTo(checkValue) == 0) {
            // if input value is equals to checkValue
            ValidationException vf = new ValidationException(
                    ReasonEnum.VALUE_NOT_IN_RANGE, member,
                    new Object[] { inputValue });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Checks that the value is a valid VAT rate (
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     */
    public static void isVat(String member, BigDecimal value)
            throws ValidationException {
        if (!ADMValidator.isVat(value)) {
            ValidationException vf = new ValidationException(ReasonEnum.VAT,
                    member, new Object[] { value });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Validates that the given input collection is neither null nor empty.
     * 
     * @param <T>
     * 
     * @param member
     *            The indicator of the field to be validated.
     * @param inputValue
     *            The value to be validated.
     * @throws ValidationException
     *             Thrown in case the collection is null or empty.
     */
    public static <T> void isNotEmpty(String member, Collection<T> inputValue)
            throws ValidationException {
        isNotNull(member, inputValue);
        if (inputValue.isEmpty()) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.EMPTY_VALUE, member, new Object[] { member });
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Checks that the value isn't too long for a tag value, contains a least
     * one non-whitespace character and doesn't contain any comma.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     */
    public static void isTag(String member, String value)
            throws ValidationException {
        // check max length
        maxLength(member, value, ADMValidator.LENGTH_TAG, true);

        // check for invalid character
        final String INVALID_CHAR = ",";
        if (value.contains(INVALID_CHAR)) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.INVALID_CHAR, member,
                    new Object[] { INVALID_CHAR });
            logValidationFailure(vf);
            throw vf;
        }

    }

    /**
     * Checks that the value is in the range between 1 and 5.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param rating
     *            the value validation is being performed on.
     * @throws ValidationException
     */
    public static void isRating(String member, int rating)
            throws ValidationException {
        if (rating == 0) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.RATING_REQUIRED, member, new Object[] { member });
            logValidationFailure(vf);
            throw vf;
        }
        isInRange(member, rating, Long.valueOf(1), Long.valueOf(5));
    }

    /**
     * Checks that the given enum value is valid
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param rating
     *            the value validation is being performed on.
     * @throws ValidationException
     */
    public static void isValidEnumValue(String member, Enum<?> arg,
            Enum<?>... validValues) throws ValidationException {
        isNotNull(member, arg);

        for (Enum<?> value : validValues) {
            if (arg == value) {
                return;
            }
        }

        ValidationException vf = new ValidationException(
                ReasonEnum.ENUMERATION, member, new Object[] { arg });
        logValidationFailure(vf);
        throw vf;
    }

    /**
     * Checks that the scale of the given big decimal is not greater than
     * PriceConverter.NUMBER_OF_DECIMAL_PLACES.
     * 
     * @param member
     *            the name of the member which holds the value.
     * @param bigDecimal
     *            the value validation is being performed on.
     * @throws ValidationException
     */
    public static void isValidPriceScale(String member, BigDecimal bigDecimal)
            throws ValidationException {
        if (bigDecimal.scale() > PriceConverter.NUMBER_OF_DECIMAL_PLACES) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.SCALE_TO_LONG, member, null);
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Checks the date range given with the input parameters.
     * 
     * @param fromDate
     *            the value validation is being performed on.
     * @param toDate
     *            the value validation is being performed on.
     * @throws ValidationException
     */
    public static void isValidDateRange(Date fromDate, Date toDate)
            throws ValidationException {
        if (fromDate.after(toDate)) {
            Object[] params = { "From: " + fromDate.toString(),
                    "To: " + toDate.toString() };
            ValidationException vf = new ValidationException(
                    ReasonEnum.INVALID_DATE_RANGE, null, params);
            logValidationFailure(vf);
            throw vf;
        }
    }

    /**
     * Checks that the length of string value is equals as given.
     *
     * @param name
     *            the name of the member which holds the value.
     * @param value
     *            the value validation is being performed on.
     * @param length
     *            the required length.
     * @throws ValidationException
     */
    public static void isLongEnough(String name, String value, Long length) throws ValidationException {
        if (length != null && length != value.length()) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.VALUE_NOT_IN_RANGE, name, new Object[] {
                    Long.valueOf(value), length });
            logValidationFailure(vf);
            throw vf;
        }
    }
}
