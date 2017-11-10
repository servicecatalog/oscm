/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageusers;

import java.io.Serializable;

import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author weiser
 * 
 */
public class UserRole implements Serializable {

    private static final long serialVersionUID = -282571836280099816L;

    private boolean selected;
    private UserRoleType type;
    private String name;

    public UserRole() {

    }

    public UserRole(UserRoleType type, String name, boolean selected) {
        this();
        setType(type);
        setName(name);
        setSelected(selected);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public UserRoleType getType() {
        return type;
    }

    public void setType(UserRoleType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
