/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-06-28                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

/**
 * Represents the definition of a service role for a technical service.
 * 
 */
public class VORoleDefinition extends BaseVO {

    private static final long serialVersionUID = -5036106045991442656L;

    private String roleId;

    private String name;

    private String description;

    /**
     * Retrieves the identifier of the service role.
     * 
     * @return the role ID
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * Sets the identifier of the service role.
     * 
     * @param roleId
     *            the role ID
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * Retrieves the name of the service role.
     * 
     * @return the role name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the service role.
     * 
     * @param name
     *            the role name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the text describing the service role.
     * 
     * @return the role description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the text describing the service role.
     * 
     * @param description
     *            the role description
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
