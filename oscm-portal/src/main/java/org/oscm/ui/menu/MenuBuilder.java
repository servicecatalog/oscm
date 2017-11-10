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

import java.util.HashMap;
import java.util.NoSuchElementException;

import org.oscm.ui.authorization.Conditions;
import org.oscm.ui.authorization.UIStatus;


public class MenuBuilder extends MenuGroupBuilder {

    public MenuBuilder(UIStatus uiStatus) {
        super(null, null, new HashMap<String, MenuGroupBuilder>(),
                Conditions.ALWAYS, uiStatus);
    }

    public void toggleGroupExpanded(String id) {
        final MenuGroupBuilder group = allGroups.get(id);
        if (group == null) {
            throw new NoSuchElementException("Group " + id);
        }
        group.toggleExpanded();
    }

}
