/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: hoffmann                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.menu;

/**
 * Common super type for elements that can be rendered in a menu.
 * 
 * @author hoffmann
 */
public interface MenuElement {

    /**
     * @return unique id of the menu element
     */
    public String getId();

    /**
     * @return lookup key for the label
     */
    public String getLabelKey();

    /**
     * @return true if the element should be rendered
     */
    public boolean isVisible();

}
