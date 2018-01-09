/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Oct 28, 2014                                                      
 *                                                                                                                        
 *                                                                              
 *******************************************************************************/
package org.oscm.app.setup;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.RegexValidator;

/**
 * Overwrite Url validator
 * 
 * Author: Mao
 */
public class UrlValidator extends
        org.apache.commons.validator.routines.UrlValidator {

    private static final long serialVersionUID = 7557161713937335013L;

    /**
     * This expression derived/taken from the BNF for URI (RFC2396).
     */
    private static final String URL_REGEX = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
    // 12 3 4 5 6 7 8 9
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    /**
     * Schema/Protocol (ie. http:, ftp:, file:, etc).
     */
    private static final int PARSE_URL_SCHEME = 2;

    private static final int PARSE_URL_QUERY = 7;

    private static final int PARSE_URL_FRAGMENT = 9;

    private static final String LEGAL_ASCII_REGEX = "^\\p{ASCII}+$";
    private static final Pattern ASCII_PATTERN = Pattern
            .compile(LEGAL_ASCII_REGEX);

    /**
     * Holds the set of current validation options.
     */
    private final long options;

    /**
     * The set of schemes that are allowed to be in a URL.
     */
    private final Set<String> allowedSchemes;

    /**
     * If no schemes are provided, default to this set.
     */
    private static final String[] DEFAULT_SCHEMES = { "http", "https", "ftp" };

    /**
     * Customizable constructor. Validation behavior is modifed by passing in
     * options.
     * 
     * @param schemes
     *            the set of valid schemes
     * @param authorityValidator
     *            Regular expression validator used to validate the authority
     *            part
     * @param options
     *            Validation options. Set using the public constants of this
     *            class. To set multiple options, simply add them together:
     *            <p>
     *            <code>ALLOW_2_SLASHES + NO_FRAGMENTS</code>
     *            </p>
     *            enables both of those options.
     */
    public UrlValidator(String[] schemes, RegexValidator authorityValidator,
            long options) {
        this.options = options;

        if (isOn(ALLOW_ALL_SCHEMES)) {
            this.allowedSchemes = Collections.emptySet();
        } else {
            if (schemes == null) {
                schemes = DEFAULT_SCHEMES;
            }
            this.allowedSchemes = new HashSet<String>();
            this.allowedSchemes.addAll(Arrays.asList(schemes));
        }
    }

    /**
     * <p>
     * Checks if a field has a valid url address.
     * </p>
     * 
     * @param value
     *            The value validation is being performed on. A
     *            <code>null</code> value is considered invalid.
     * @return true if the url is valid.
     */
    @Override
    public boolean isValid(String value) {
        if (value == null) {
            return false;
        }

        if (!ASCII_PATTERN.matcher(value).matches()) {
            return false;
        }

        // Check the whole url address structure
        Matcher urlMatcher = URL_PATTERN.matcher(value);
        if (!urlMatcher.matches()) {
            return false;
        }

        String scheme = urlMatcher.group(PARSE_URL_SCHEME);
        if (!isValidScheme(scheme)) {
            return false;
        }

        if (!isValidQuery(urlMatcher.group(PARSE_URL_QUERY))) {
            return false;
        }

        if (!isValidFragment(urlMatcher.group(PARSE_URL_FRAGMENT))) {
            return false;
        }

        return true;
    }

    /**
     * Tests whether the given flag is on. If the flag is not a power of 2 (ie.
     * 3) this tests whether the combination of flags is on.
     * 
     * @param flag
     *            Flag value to check.
     * 
     * @return whether the specified flag value is on.
     */
    private boolean isOn(long flag) {
        return (this.options & flag) > 0;
    }

}
