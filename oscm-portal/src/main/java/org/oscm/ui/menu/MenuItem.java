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
 * Interface for a menu containing a link (leaf node).
 * 
 * @author hoffmann
 */
public interface MenuItem extends MenuElement {

    /**
     * @return the link for this menu item
     */
    public String getLink();

    /**
     * @return <code>true</code> if the item is currently selected
     */
    public boolean isSelected();

}
