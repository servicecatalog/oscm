/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to make syntax check of the property files. The utility reports the
 * single quoted parameters in the values.
 * 
 * @author stavreva
 */
public class PropertiesSyntaxChecker {

    private final Properties a;

    public PropertiesSyntaxChecker(final Properties a) {
        this.a = a;
    }

    /**
     * Returns all keys where syntax error single quotes around parameter exists
     * in the value. Example: Value '{0}' not allowed.
     * 
     * @return
     */
    public Set<String> getSyntaxSingleQuotesErrorKeys() {

        String singleQuoted = "'\\{[0-9]+\\}'";
        String notQuote = "[^']{1}";
        String begin = "^(" + singleQuoted + notQuote + ")";
        String end = "(" + notQuote + singleQuoted + ")$";
        String middle = notQuote + singleQuoted + notQuote;
        String exact = "^(" + singleQuoted + ")$";
        String pattern = "(" + begin + ")|(" + middle + ")|(" + end + ")|("
                + exact + ")";

        final Pattern VAR_PATTERN_SYNTAX = Pattern.compile(pattern);

        Set<Entry<Object, Object>> s = a.entrySet();
        Iterator<Entry<Object, Object>> it = s.iterator();

        final Set<String> result = new HashSet<String>();

        while (it.hasNext()) {
            final Entry<Object, Object> propEntry = it.next();
            String text = propEntry.getValue().toString();
            final Matcher m = VAR_PATTERN_SYNTAX.matcher(text);
            if (m.find()) {
                result.add(propEntry.getKey().toString());
            }
        }

        return result;
    }
}
