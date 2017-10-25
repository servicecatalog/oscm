/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.validator;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.oro.text.perl.Perl5Util;

/**
 * Class which provides base validation methods and constants.
 * 
 */
public class ADMValidator {

    public static final int LENGTH_ID = 40;
    public static final int LENGTH_USERID = 100;
    public static final int LENGTH_NAME = 100;
    public static final int LENGTH_USER_GROUP_NAME = 256;
    public static final int LENGTH_DESCRIPTION = 255;
    public static final int LENGTH_REFERENCE_ID = 255;
    public static final int LENGTH_ACCESSINFO = 4096;
    public static final int LENGTH_LOCALE = 15;
    public static final int LENGTH_DN = 4096;
    /** Length of percent value xxx.xx (0 - 100.00) */
    public static final int LENGTH_PERCENT_VALUE = 6;
    /** Length of discount period MM/yyyy */
    public static final int LENGTH_DISCOUNT_PERIOD = 7;
    public static final int LENGTH_INT = 11; // -2,147,483,648
    public static final int LENGTH_LONG = 20; // -9,223,372,036,854,775,808
    public static final int LENGTH_TAG = 20;
    public static final int LENGTH_COMMENT = 2000;
    public static final int LENGTH_EMAIL_SUBJECT = 50;
    public static final int LENGTH_EMAIL_CONTENT = 2000;
    public static final int LENGTH_TEXT = 32767; // KEY_PRIVACY_POLICY,
                                                 // KEY_TERMS
    public static final int MIN_LENGTH_PASSWORD = 6;
    public static final int LENGTH_TENANT_FIELD = 255;

    private static final Pattern ipv6Authority;

    // Pattern for an authority, which contains an IPv6 address.
    // see RFC3986, chapter 3.2
    static {
        final String H16 = "[0-9A-Fa-f]{1,4}";
        final String H16_COLON = "(" + H16 + ":" + ")";
        final String LS32 = "(" + H16 + ":" + H16 + ")";
        final String IPV6_ADDRESS = "(" + H16_COLON + "{6}" + LS32 + "|::"
                + H16_COLON + "{5}" + LS32 + "|(" + H16 + ")?" + "::"
                + H16_COLON + "{4}" + LS32 + "|(" + H16_COLON + "{0,1}" + H16
                + ")?" + "::" + H16_COLON + "{3}" + LS32 + "|(" + H16_COLON
                + "{0,2}" + H16 + ")?" + "::" + H16_COLON + "{2}" + LS32 + "|("
                + H16_COLON + "{0,3}" + H16 + ")?" + "::" + H16_COLON + LS32
                + "|(" + H16_COLON + "{0,4}" + H16 + ")?" + "::" + LS32 + "|("
                + H16_COLON + "{0,5}" + H16 + ")?" + "::" + H16 + "|("
                + H16_COLON + "{0,6}" + H16 + ")" + "::)";
        final String HOST = "(\\[" + IPV6_ADDRESS + "\\])";
        final String PORT = ":([0-9]{1,5})?";
        final String AUTHORITY = "^" + HOST + PORT;

        ipv6Authority = Pattern.compile(AUTHORITY);
    }

    /**
     * All characters not in the following list:
     * 
     * <pre>
     * (, ), -, ., 0-9, @, A-Z, [, ], _, a-#xD7FF, #xE000-#xFFFD, #x10000-#x10FFFF
     *  (      )      -      .       0  - 9        @  - [        ]      _       a
     * #x28 | #x29 | #x2D | #x2E | [#x30-#x39] | [#x40-#x5B] | #x5D  | #x5F | [#x61-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * </pre>
     */
    public static final Pattern INVALID_ID_CHARS = Pattern
            .compile("[^\\u0020\\u0028\\u0029\\u002D\\u002E\\u005D\\u005F\\u0030-\\u0039\\u0040-\\u005B\\u0061-\\uD7FF\\uE000-\\uFFFD\uD800\uDC00-\uDBFF\uDFFF]");

    /**
     * Modified UrlValidator to support host names without points. see [5157]
     * 
     */
    private static class PatchedUrlValidator extends
            org.apache.commons.validator.routines.UrlValidator {
        private static final long serialVersionUID = 7046783464031105409L;

        private final boolean relative;

        public PatchedUrlValidator(boolean relative) {
            super();
            // may solve problems with localhost: super(ALLOW_LOCAL_URLS);
            this.relative = relative;
        }

        @Override
        protected boolean isValidAuthority(String authority) {
            if (relative && authority == null) {
                return true;
            }

            boolean isValid = super.isValidAuthority(authority);

            if (!isValid && !GenericValidator.isBlankOrNull(authority)) {
                if (isValidAuthorityHostNoDot(authority)) {
                    return true;
                } else if (isValidAuthorityHostNoTld(authority)) {
                    return true;
                } else {
                    return isValidAuthorityIPV6Host(authority);
                }
            } else {
                return isValid;
            }
        }

        /**
         * Check if the authority contains a valid hostname without "."
         * characters and an optional port number
         * 
         * @param authority
         * @return <code>true</code> if the authority is valid
         */
        private boolean isValidAuthorityHostNoDot(String authority) {
            Perl5Util authorityMatcher = new Perl5Util();
            if (authority != null
                    && authorityMatcher.match(
                            "/^([a-zA-Z\\d\\-\\.]*)(:\\d*)?(.*)?/", authority)) {
                String hostIP = authorityMatcher.group(1);
                if (hostIP.indexOf('.') < 0) {
                    // the hostname contains no dot, add domain validation to check invalid hostname like "g08fnstd110825-"
                    DomainValidator domainValidator = DomainValidator.getInstance(true);
                    if(!domainValidator.isValid(hostIP)) {
                        return false;
                    }
                    String port = authorityMatcher.group(2);
                    if (!isValidPort(port)) {
                        return false;
                    }
                    String extra = authorityMatcher.group(3);
                    return GenericValidator.isBlankOrNull(extra);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        /**
         * Check if the authority contains a hostname without top level domain
         * and an optional port number
         * 
         * @param authority
         * @return <code>true</code> if the authority is valid
         */
        private boolean isValidAuthorityHostNoTld(String authority) {
            Perl5Util authorityMatcher = new Perl5Util();
            if (authority != null
                    && authorityMatcher.match(
                            "/^([a-zA-Z\\d\\-\\.]*)(:\\d*)?(.*)?/", authority)) {
                String host = authorityMatcher.group(1);
                if (host.indexOf('.') > 0) {
                    DomainValidator domainValidator = DomainValidator.getInstance();
                    // Make the host have a valid TLD, so that the "no TLD" host can pass the domain validation.
                    String patchedHost = host + ".com";
                    if(!domainValidator.isValid(patchedHost)) {
                        return false;
                    }
                    String port = authorityMatcher.group(2);
                    if (!isValidPort(port)) {
                        return false;
                    }
                    String extra = authorityMatcher.group(3);
                    return GenericValidator.isBlankOrNull(extra);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        private boolean isValidPort(String port) {
            if (port != null) {
                Perl5Util portMatcher = new Perl5Util();
                if (!portMatcher.match("/^:(\\d{1,5})$/", port))
                    return false;
            }
            return true;
        }

        /**
         * Check if the authority contains a valid IPv6 address (in brackets)
         * and an optional port number (see RFC3986, chapter 3.2)
         * 
         * @param authority
         * @return <code>true</code> if the authority is valid
         */
        private boolean isValidAuthorityIPV6Host(String authority) {
            Matcher matcher = ipv6Authority.matcher(authority);
            return matcher.matches();
        }

        @Override
        protected boolean isValidScheme(String scheme) {
            if (relative && scheme == null) {
                return true;
            }
            return super.isValidScheme(scheme);
        }

    }

    private final static PatchedUrlValidator URL_VALIDATOR = new ADMValidator.PatchedUrlValidator(
            false);
    private final static PatchedUrlValidator RELATIVE_URL_VALIDATOR = new ADMValidator.PatchedUrlValidator(
            true);

    private final static Perl5Util URL_SCHEME_MATCHER = new Perl5Util();
    private final static String HTTPS_SCHEME = "/^https/";

    /**
     * Checks that the value only contains the following characters:
     * 
     * <pre>
     * (, ), -, ., 0-9, @, A-Z, [, ], _, a-#xD7FF, #xE000-#xFFFD, #x10000-#x10FFFF
     *  (      )      -      .       0  - 9        @  - [        ]      _       a
     * #x28 | #x29 | #x2D | #x2E | [#x30-#x39] | [#x40-#x5B] | #x5D  | #x5F | [#x61-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * </pre>
     * 
     * @param value
     *            the value which is checked
     * @return true if the value only contains allowed characters
     */
    public static boolean containsOnlyValidIdChars(String value) {
        if (value == null) {
            return true;
        }
        Matcher matcher = INVALID_ID_CHARS.matcher(value);
        return !matcher.find();
    }

    /**
     * Checks if a field has a valid absolute URL address.
     * 
     * @param value
     *            The value validation is being performed on. A null value is
     *            considered invalid.
     * @return true if the URL is valid.
     */
    public static boolean isUrl(String value) {
        return URL_VALIDATOR.isValid(value);
    }

    /**
     * Checks if a field has a valid relative or absolute URL address.
     * 
     * @param value
     *            The value validation is being performed on. A null value is
     *            considered invalid.
     * @return true if the URL is valid.
     */
    public static boolean isAbsoluteOrRelativeUrl(String value) {
        return RELATIVE_URL_VALIDATOR.isValid(value);
    }

    /**
     * Checks if the https Scheme applies to the passed URL. This method doesn't
     * validate the URL itself.
     * 
     * @param value
     *            The value validation is being performed on.
     * @return true if the url starts with the https scheme.
     */
    public static boolean isHttpsScheme(String value) {
        if (value == null) {
            return false;
        }
        return URL_SCHEME_MATCHER.match(HTTPS_SCHEME, value);
    }

    /**
     * Checks if a field has and is a valid email address.
     * 
     * @param value
     *            The value validation is being performed on. A null value is
     *            considered invalid.
     * @return true if the value is a valid email.
     */
    public static boolean isEmail(String value) {
        if (GenericValidator.isEmail(value)) {
            return Pattern.matches("\\S.*@[a-zA-Z0-9\\-\\.]*", value);
        }
        return false;
    }

    /**
     * Checks that the value is a valid VAT rate.
     * 
     * @param value
     *            The value validation is being performed on. A null value is
     *            considered invalid.
     * @return true if the VAT is valid.
     */
    public static boolean isVat(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.valueOf(100)) <= 0
                && value.compareTo(BigDecimal.ZERO) >= 0 && value.scale() < 3;
    }

    /**
     * Checks that the value is a valid boolean value.
     * 
     * @param value
     *            The value validation is being performed on. A
     *            <code>null</code> value is not considered a valid boolean.
     * @return <code>true</code> if the value equals either "true" or "false"
     *         ignoring case, <code>false</code> otherwise.
     */
    public static boolean isBoolean(String value) {
        return value != null
                && ("false".equals(value.toLowerCase()) || "true".equals(value
                        .toLowerCase()));
    }

}
