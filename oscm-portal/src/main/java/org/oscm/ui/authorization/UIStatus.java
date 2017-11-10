/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: hoffmann                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.authorization;

/**
 * Abstraction for all callbacks the menu needs to dynamically update its state.
 * 
 * @author hoffmann
 */
public interface UIStatus {

    /**
     * @return the link to the currently displayed page
     */
    public String getCurrentPageLink();

    /**
     * Test for generally hidden menu items.
     * 
     * @param id
     *            menu id to test
     * @return <code>false</code> if the menu with the given id should be
     *         generally hidden
     */
    public boolean isHidden(String id);

}
