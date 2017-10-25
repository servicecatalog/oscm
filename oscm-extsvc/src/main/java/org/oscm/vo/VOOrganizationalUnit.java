/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-07-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

/**
 * Represents an organizational unit and provides data related to it.
 */
public class VOOrganizationalUnit extends BaseVO {

    private static final long serialVersionUID = 3901367900656380907L;

    /**
     * A string for referencing the organizational unit, for example, in
     * customer billing data.
     */
    private String referenceId;

    /**
     * A description of the organizational unit.
     */
    private String description;

    /**
     * The name of the organizational unit.
     */
    private String name;

    /**
     * Specifies whether the organizational unit is the default unit of its
     * organization.
     */
    private boolean defaultGroup;

    /**
     * The identifier (business key) of the organization the unit belongs to.
     */
    private String organizationId;

    /**
     * Sets the reference ID of the organizational unit, which allows to 
     * identify the unit, for example, in customer billing data.
     * 
     * @param referenceId
     *            the identifier
     */
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    /**
     * Sets the description of the organizational unit.
     * 
     * @param description
     *            the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the name of the organizational unit.
     * 
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Specifies whether the organizational unit is the default unit of its
     * organization.
     * 
     * @param defaultGroup
     *            <code>true</code> if the unit is to be the default unit,
     *            <code>false</code> otherwise
     */
    public void setDefaultGroup(boolean defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    /**
     * Specifies the organization the organizational unit is part of.
     * 
     * @param organizationId
     *            the unique identifier (business key) of the organization
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Sets the reference ID of the organizational unit, which allows to
     * identify the unit, for example, in customer billing data.
     * 
     * @return the identifier
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Returns the description of the organizational unit.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the name of the organizational unit.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether the organizational unit is the default unit of its
     * organization.
     * 
     * @return <code>true</code> if the unit is the default unit,
     *         <code>false</code> otherwise
     */
    public boolean isDefaultGroup() {
        return defaultGroup;
    }

    /**
     * Returns the organization the organizational unit is part of.
     * 
     * @return the unique identifier of the organization
     */
    public String getOrganizationId() {
        return organizationId;
    }
}
