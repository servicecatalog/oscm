/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oscm.internal.intf.OperatorService;

/**
 * Execution context of a command.
 * 
 * @author hoffmann
 */
public class CommandContext {

    private final OperatorService service;

    private final Map<String, String> args;

    private final PrintWriter out;

    private final PrintWriter err;

    public CommandContext(OperatorService service, Map<String, String> args,
            PrintWriter out, PrintWriter err) {
        this.service = service;
        this.args = args;
        this.out = out;
        this.err = err;
    }

    /**
     * Writer for normal messages.
     */
    public PrintWriter out() {
        return out;
    }

    /**
     * Writer for error messages.
     */
    public PrintWriter err() {
        return err;
    }

    /**
     * returns the operator service to use.
     */
    public OperatorService getService() {
        return service;
    }

    /**
     * Returns the optional string attribute with the given key.
     */
    public String getStringOptional(final String key) {
        return args.get(key);
    }

    /**
     * Returns the mandatory string attribute with the given key.
     */
    public String getString(final String key) {
        final String value = getStringOptional(key);
        if (value == null) {
            throw new IllegalArgumentException(String.format(
                    "Please specify mandatory parameter %s.", key));
        }
        return value;
    }

    private <T extends Enum<T>> T getEnum(final String key, final Set<T> all,
            boolean isOptional) {
        String value = (isOptional ? getStringOptional(key) : getString(key));
        for (T t : all) {
            if (t.name().equals(value)) {
                return t;
            }
        }
        if (isOptional) {
            return null;
        } else {
            final String msg = String
                    .format("Invalid parameter value '%s' for %s. Valid values are %s.",
                            value, key, all);
            throw new IllegalArgumentException(msg);
        }
    }

    public <T extends Enum<T>> T getEnumOptional(final String key,
            final Set<T> all) {
        return getEnum(key, all, true);
    }

    public <T extends Enum<T>> T getEnum(final String key, final Set<T> all) {
        return getEnum(key, all, false);
    }

    /**
     * Returns the mandatory attribute with the given key. The attribute is a
     * comma separated list of string values.
     */
    public List<String> getList(final String key) {
        final String value = getString(key);
        if (value.length() == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }

    public <T extends Enum<T>> Set<T> getEnumList(final String key,
            final Set<T> all) {
        final Map<String, T> names = new HashMap<String, T>();
        for (final T t : all) {
            names.put(t.name(), t);
        }
        final Set<T> result = new HashSet<T>();
        for (final String name : getList(key)) {
            final T e = names.get(name);
            if (e == null) {
                final String msg = String
                        .format("Invalid parameter value '%s' for %s. Valid values are %s.",
                                name, key, names.keySet());
                throw new IllegalArgumentException(msg);
            }
            result.add(e);
        }
        return result;
    }

}
