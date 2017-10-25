/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries( {
        @NamedQuery(name = "RoleDefinitionHistory.findByObject", query = "select c from RoleDefinitionHistory c where c.objKey=:objKey order by objversion"),
        @NamedQuery(name = "RoleDefinitionHistory.getRolesForPriceModelKey", query = "SELECT rdh FROM RoleDefinitionHistory rdh, TechnicalProductHistory tp, ProductHistory prod, PriceModelHistory pm WHERE rdh.technicalProductObjKey = tp.objKey AND prod.technicalProductObjKey = tp.objKey AND pm.productObjKey = prod.objKey AND pm.objKey = :pmKey AND rdh.modDate <= :modDate ORDER BY rdh.objVersion DESC") })
public class RoleDefinitionHistory extends
        DomainHistoryObject<RoleDefinitionData> {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = 5815478096080895926L;

    /**
     * Technical product key.
     */
    private long technicalProductObjKey;

    /**
     * Default constructor.
     */
    public RoleDefinitionHistory() {
        super();
        dataContainer = new RoleDefinitionData();
    }

    /**
     * Parameterized constructor.
     * 
     * @param roleDefinition
     *            Role definition.
     */
    public RoleDefinitionHistory(RoleDefinition roleDefinition) {
        super(roleDefinition);
        if (roleDefinition.getTechnicalProduct() != null) {
            setTechnicalProductObjKey(roleDefinition.getTechnicalProduct()
                    .getKey());
        }
    }

    /**
     * 
     * @param technicalProductObjKey
     */
    public void setTechnicalProductObjKey(long technicalProductObjKey) {
        this.technicalProductObjKey = technicalProductObjKey;
    }

    /**
     * 
     * @return Technical product key.
     */
    public long getTechnicalProductObjKey() {
        return technicalProductObjKey;
    }

    /**
     * Setter for role ID.
     * 
     * @param roleId
     */
    public void setRoleId(String roleId) {
        dataContainer.setRoleId(roleId);
    }

    /**
     * Getter for role ID.
     * 
     * @return Role id.
     */
    public String getRoleId() {
        return dataContainer.getRoleId();
    }

}
