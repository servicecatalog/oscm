/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

import java.util.AbstractList;
import java.util.List;
import java.util.Spliterator;

/**
 * Specifies the type of the marketplace landingpage.
 * 
 */
public enum LandingpageType {
    PUBLIC, ENTERPRISE;

    public static LandingpageType getDefault() {
        return PUBLIC;
    }

    public boolean isDefault() {
        return isDefault(name());
    }

    public static boolean isDefault(String name) {
        return getDefault().name().equals(name);
    }

    /**
     * Returns true if this enum contains the element given name
     */
    public static boolean contains(String name) {
        return names().contains(name);
    }

    /**
     * Names of all enum elements
     */
    public static List<String> names() {
        final LandingpageType types[] = values();
        return new AbstractList<String>() {

            @Override
            public String get(int index) {
                return types[index].name();
            }

            @Override
            public Spliterator<String> spliterator() {
                return super.spliterator();
            }

            @Override
            public int size() {
                return types.length;
            }

        };
    }
}
