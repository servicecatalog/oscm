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

import java.util.List;

/**
 * Menu item not containing a link itself but grouping other menu items.
 * 
 * @author hoffmann
 */
public interface MenuGroup extends MenuElement {

    /**
     * @return <code>true</code> if the menu item should be rendered expanded
     */
    public boolean isExpanded();

    /**
     * @return list of menu groups under this group
     */
    public List<MenuGroup> getGroups();

    /**
     * @return list of menu items under this group
     */
    public List<MenuItem> getItems();

}
