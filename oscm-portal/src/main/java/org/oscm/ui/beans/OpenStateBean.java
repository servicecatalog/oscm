/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 12.01.2011                                                      
 *                                                                              
 *  Completion Time: 13.01.2011                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.oscm.ui.common.JSFUtils;

/**
 * Session scope bean that stores the state of collapsable sections.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class OpenStateBean implements Serializable {

    private static final long serialVersionUID = 5050535195203421200L;

    /**
     * Constant for accessing the request attribute providing information about
     * sections to expand on error. Expects a {@link Set} as value.
     */
    public static final String SECTIONS_TO_EXPAND = "sectionsToExpand";

    /**
     * Map to store the identifiers of the collapsable sections as key and their
     * state as value.
     */
    private final OpenStateMap states;

    public OpenStateBean() {
        this.states = new OpenStateMap();
    }

    /**
     * Returns all currently known collapse states.
     * 
     * @return The state map.
     */
    public Map<String, String> getStates() {
        return states;
    }

    /**
     * Auxiliary class to store the collapsing states. It will never return
     * <code>null</code>, but provide <code>true</code> instead.
     * 
     * @author Mike J&auml;ger
     * 
     */
    class OpenStateMap extends HashMap<String, String> {

        private static final long serialVersionUID = 8409747823896471675L;

        /**
         * Returns the currently stored opening state for the object with the
         * given identifier. If no entry with this identifier is found,
         * <code>true</code> will be returned.
         */
        @Override
        public String get(Object key) {
            updateErrorSections();
            if (!containsKey(key)) {
                // if we have the initial opened state, put it to remember it.
                put(key.toString(), Boolean.TRUE.toString());
            }
            return super.get(key);
        }

        /**
         * Check if sections should be expanded due to an error.
         */
        private void updateErrorSections() {
            HttpServletRequest request = JSFUtils.getRequest();
            Set<?> sectionsToExpand = (Set<?>) request
                    .getAttribute(SECTIONS_TO_EXPAND);
            if (sectionsToExpand != null) {
                for (Object o : sectionsToExpand) {
                    put(o.toString(), Boolean.TRUE.toString());
                }
            }
        }

        /**
         * Adds the opening state for the given identifier to the map. If
         * <code>null</code> is tried to be stored, it will be replaced by
         * <code>true</code>.
         */
        @Override
        public String put(String key, String value) {
            String valueToStore = (value == null) ? Boolean.TRUE.toString()
                    : value;
            return super.put(key, valueToStore);
        }

    }

    /**
     * Toggles the opening state for the given identifier.
     */
    public void setToggleState(String key) {
        boolean oldState = Boolean.parseBoolean(states.get(key));
        states.put(key, Boolean.toString(!oldState));
    }
    
    public void setInitState(String key, boolean value) {
        if (!states.containsKey(key)) {
            states.put(key, Boolean.toString(value));
        }
    }

}
