/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.functions;

import org.apache.commons.lang3.StringUtils;

/**
 * Class contains set of functions to perform matching of {@link java.lang.String} for the sake of
 * tables and filtration.
 *
 * @author trebskit
 */
public class TableFilterFunctions {

    /**
     * Allows to compare two values ({@link java.lang.String}) in the following manner.
     * Only begin of the <b>string</b> takes part in comparison. Last argument allows
     * to control whether or not comparison should be case-sensitive or not.
     *
     * @param value         value to compare [left side]
     * @param filterExpr    filter value to compare to [right side]
     * @param caseSensitive true/false, if true case-sensitive comparison will be applied
     * @return true/false, true if {@code value} matched {@code filterExpr}
     */
    public static boolean matchBegin(final String value, final String filterExpr, Boolean caseSensitive) {
        boolean caseSens = Boolean.valueOf(caseSensitive != null && caseSensitive.booleanValue()).booleanValue();
        final MatchBeginFilter matchBeginFilter = caseSens ? new CaseSensitiveFilter() : new CaseInsensitiveFilter();
        return matchBeginFilter.filter(value, filterExpr);
    }

    private static abstract class MatchBeginFilter {
        boolean filter(String value, String filterExp) {
            if (StringUtils.isNotEmpty(filterExp)) {
                value = this.adjustString(value);
                filterExp = this.adjustString(filterExp);
                return value.startsWith(filterExp);
            }
            return true;
        }

        /**
         * Allows to adjust {@code str} before actual comparison takes place
         *
         * @param str {@link java.lang.String} to adjust
         * @return adjusted value
         */
        abstract String adjustString(final String str);
    }

    private static class CaseInsensitiveFilter extends MatchBeginFilter {
        @Override
        String adjustString(String str) {
            return str.toLowerCase();
        }
    }

    private static class CaseSensitiveFilter extends MatchBeginFilter {
        @Override
        String adjustString(String str) {
            return str;
        }
    }

}
