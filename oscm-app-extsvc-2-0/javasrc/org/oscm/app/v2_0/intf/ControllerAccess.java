/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 28.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.intf;

import java.io.Serializable;
import java.util.List;

/**
 * Interface to the specific controller implementation used by common controller
 * code (to avoid dependencies on specific implementations).
 */
public interface ControllerAccess extends Serializable {

    /**
     * Returns the unique id of the controller implementation.
     * 
     * @return the controller id
     */
    public String getControllerId();

    /**
     * Returns the localized message for the given key, locale and arguments.
     * 
     * @param locale
     *            the locale
     * @param key
     *            the message key
     * @param arguments
     *            optional arguments
     * @return the localized message
     */
    public String getMessage(String locale, String key, Object... arguments);

    /**
     * Return a list of controller parameter keys. The list defines the keys of
     * parameters that should be shown on the configuration user interface.
     * 
     * @return the key list (may be empty but not <code>null</code>
     */
    public List<String> getControllerParameterKeys();

}
