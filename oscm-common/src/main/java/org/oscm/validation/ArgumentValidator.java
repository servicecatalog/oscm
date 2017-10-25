/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.validation;

import java.util.Collection;

import org.oscm.converter.WhiteSpaceConverter;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * Utility class to validate arguments in API methods.
 * 
 * @author hoffmann
 */
public class ArgumentValidator {

    public static void notNull(String name, Object arg)
            throws IllegalArgumentException {
        if (arg == null) {
            String msg = String.format("Parameter %s must not be null.", name);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notEmptyString(String name, String arg)
            throws IllegalArgumentException {
        notNull(name, arg);

        String tmp = WhiteSpaceConverter.replace(arg);
        if (tmp.trim().length() == 0) {
            String msg = String.format("Parameter %s must not be empty.", name);
            throw new IllegalArgumentException(msg);
        }
    }

    public static void notNullNotEmpty(String name, Collection<?> arg) {
        notNull(name, arg);
        if (arg.isEmpty()) {
            String msg = String.format("Parameter %s must not be empty.", name);
            throw new IllegalArgumentException(msg);
        }
    }

}
