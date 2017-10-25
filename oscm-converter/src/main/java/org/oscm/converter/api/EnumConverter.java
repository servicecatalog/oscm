/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.06.2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.converter.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnumConverter {

    private static final Map<Enum<?>, Enum<?>> MAPPING;

    static {
        Map<Enum<?>, Enum<?>> map = new HashMap<Enum<?>, Enum<?>>();
        MAPPING = Collections.unmodifiableMap(map);
    }

    /**
     * Convert the given internal enum value into a api enum value or vice
     * versa.
     * 
     * @param value
     *            the enum value to convert
     * @param targetClass
     *            the target enum to convert
     * 
     * @return the converted enum value or null if a conversion is not possible
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T convert(Enum<?> value,
            Class<T> targetClass) {

        if (value == null) {
            return null;
        }

        T result;
        try {
            result = Enum.valueOf(targetClass, value.name());
        } catch (IllegalArgumentException e) {
            result = null;
            if (MAPPING.containsKey(value)) {
                result = (T) MAPPING.get(value);
            }
        }

        return result;
    }

    /**
     * Convert a list of enum values from internal to api or vice versa.
     * 
     * @param <T>
     *            the resulting enum type
     * @param values
     *            the list of enum values to convert
     * @param targetClass
     *            the target class
     * @return the converted list
     */
    public static <T extends Enum<T>> List<T> convertList(
            List<? extends Enum<?>> values, Class<T> targetClass) {
        List<T> list = new ArrayList<T>();
        if (values == null) {
            return null;
        }
        for (Enum<?> e : values) {
            T result = convert(e, targetClass);
            if (result != null) {
                list.add(result);
            }
        }
        return list;
    }

    /**
     * Convert a set of enum values from internal to api or vice versa.
     * 
     * @param <T>
     *            the resulting enum type
     * @param values
     *            the set of enum values to convert
     * @param targetClass
     *            the target class
     * @return the converted set
     */
    public static <T extends Enum<T>> Set<T> convertSet(
            Set<? extends Enum<?>> values, Class<T> targetClass) {
        Set<T> list = new HashSet<T>();
        if (values == null) {
            return null;
        }
        for (Enum<?> e : values) {
            T result = convert(e, targetClass);
            if (result != null) {
                list.add(result);
            }
        }
        return list;
    }
}
