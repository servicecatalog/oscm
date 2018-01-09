/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.adapter.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.context.ContextValueParameterMap;
import org.oscm.billing.external.context.ContextValueString;

/**
 * A multi-valued map used for queries to the file billing application
 *
 */
public class QueryParamMultiValuedMap {

    public static final String LOCALES_PARAMETER = "LOCALES";
    public static final String CONTEXT_KEYS_PARAMETER = "CONTEXT_KEYS";
    public static final String CONTEXT_VALUES_PARAMETER = "CONTEXT_VALUES";

    MultivaluedMap<String, String> map;

    public QueryParamMultiValuedMap() {
        map = new MultivaluedHashMap();
    }

    /**
     * Get the multi-valued map
     */
    public MultivaluedMap<String, String> getMap() {
        return map;
    }

    /**
     * Add the string representation of a set of locales to the multi-valued map
     */
    public void add(Set<Locale> locales) {

        if (locales != null && locales.size() > 0) {
            List<String> localeList = new ArrayList<String>();
            for (Locale locale : locales) {
                localeList.add(locale.toString());
            }
            map.put(LOCALES_PARAMETER, localeList);
        }
    }

    /**
     * Add the OSCM context as lists of context keys and context values to the
     * multi-valued map
     * 
     * @param context
     *            the OSCM context
     * @return <code>true</code> if the context could be added
     */
    public boolean add(Map<ContextKey, ContextValue<?>> context) {

        if (context == null) {
            return false;
        }

        List<String> contextKeys = new ArrayList<String>();
        List<String> contextValues = new ArrayList<String>();

        for (ContextKey key : context.keySet()) {
            if (key != null) {
                if (!key.equals(ContextKey.SERVICE_PARAMETERS)) {
                    ContextValueString stringValue = (ContextValueString) context
                            .get(key);
                    String value = stringValue.getValue();
                    if (value != null && !value.isEmpty()) {
                        contextKeys.add(key.name());
                        contextValues.add(value);
                    }
                } else {
                    Map<String, String> parameterMap = getParameterMap(key,
                            context);
                    if (parameterMap != null) {
                        for (String parameterName : parameterMap.keySet()) {
                            if (parameterName != null
                                    && !parameterName.isEmpty()) {
                                String parameterValue = parameterMap
                                        .get(parameterName);
                                if (parameterValue != null
                                        && !parameterValue.isEmpty()) {
                                    contextKeys.add(key.name() + "_"
                                            + parameterName);
                                    contextValues.add(parameterValue);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (contextKeys.size() > 0 && contextValues.size() > 0) {
            map.put(CONTEXT_KEYS_PARAMETER, contextKeys);
            map.put(CONTEXT_VALUES_PARAMETER, contextValues);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper method for getting a parameter map out of the OSCM context
     * 
     * @param key
     *            the key of the parameter map
     * @param context
     *            the OSCM context
     * @return the parameter map
     */
    private Map<String, String> getParameterMap(ContextKey key,
            Map<ContextKey, ContextValue<?>> context) {

        ContextValueParameterMap contextParameterMap = (ContextValueParameterMap) context
                .get(key);
        if (contextParameterMap != null) {
            Map<String, String> parameters = contextParameterMap.getValue();
            if (parameters != null && parameters.size() > 0) {
                return parameters;
            }
        }

        return null;
    }

}
