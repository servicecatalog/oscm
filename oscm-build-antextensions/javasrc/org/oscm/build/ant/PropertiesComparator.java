/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to compare two Java property files typically used for localizations.
 * The utility reports additional key, missing keys and values with a different
 * set of {x} variables.
 * 
 * @author hoffmann
 */
public class PropertiesComparator {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{[0-9]*\\}");

    private final Properties a;
    private final Properties b;

    public PropertiesComparator(final Properties a, final Properties b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Returns all keys contained in b but not in a.
     * 
     * @return
     */
    public Set<String> getAdditionalKeys() {
        final Set<String> result = new HashSet<String>();
        for (final Object key : b.keySet()) {
            if (!a.containsKey(key)) {
                result.add((String) key);
            }
        }
        return result;
    }

    /**
     * Returns all keys contained in a but not in b.
     * 
     * @return
     */
    public Set<String> getMissingKeys() {
        final Set<String> result = new HashSet<String>();
        for (final Object key : a.keySet()) {
            if (!b.containsKey(key)) {
                result.add((String) key);
            }
        }
        return result;
    }

    /**
     * Returns the list of property keys that exist in both files but do have a
     * different set of variables.
     * 
     * @return
     */
    public Set<String> getDifferentVariables() {
        final Set<String> result = new HashSet<String>();
        for (final Map.Entry<Object, Object> entryA : a.entrySet()) {
            final String valueB = (String) b.get(entryA.getKey());
            if (valueB != null) {
                final Set<String> varsA = findVariables((String) entryA
                        .getValue());
                final Set<String> varsB = findVariables(valueB);
                if (!varsA.equals(varsB)) {
                    result.add((String) entryA.getKey());
                }
            }
        }
        return result;
    }

    protected static Set<String> findVariables(String value) {
        final Set<String> result = new HashSet<String>();
        final Matcher m = VAR_PATTERN.matcher(value);
        while (m.find()) {
            result.add(m.group());
        }
        return result;
    }

}
