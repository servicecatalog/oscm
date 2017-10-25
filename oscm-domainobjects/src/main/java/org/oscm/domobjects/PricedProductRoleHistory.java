/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
        @NamedQuery(name = "PricedProductRoleHistory.findByObject", query = "select c from PricedProductRoleHistory c where c.objKey=:objKey order by objversion, modDate"),
        @NamedQuery(name = "PricedProductRoleHistory.getForPMKeyAndEndDate", query = "SELECT pprh FROM PricedProductRoleHistory pprh WHERE pprh.priceModelObjKey = :pmObjKey AND pprh.modDate <= :modDate ORDER BY pprh.objVersion DESC"),
        @NamedQuery(name = "PricedProductRoleHistory.getForParameterAndEndDate", query = "SELECT pprh, rdh.dataContainer.roleId FROM PricedProductRoleHistory pprh, PricedParameterHistory pph, RoleDefinitionHistory rdh WHERE pprh.pricedParameterObjKey = pph.objKey AND pph.priceModelObjKey = :pmKey AND pprh.modDate <= :endDate AND pprh.roleDefinitionObjKey = rdh.objKey AND rdh.objVersion = (SELECT MAX(srdh.objVersion) FROM RoleDefinitionHistory srdh WHERE rdh.objKey = srdh.objKey) ORDER BY pprh.objVersion DESC"),
        @NamedQuery(name = "PricedProductRoleHistory.getForParameterOptionAndEndDate", query = "SELECT pph.objKey, pprh, rdh.dataContainer.roleId FROM PricedProductRoleHistory pprh, PricedOptionHistory poh, PricedParameterHistory pph, RoleDefinitionHistory rdh WHERE poh.objKey = pprh.pricedOptionObjKey AND poh.pricedParameterObjKey = pph.objKey AND pprh.modDate <= :modDate AND pph.priceModelObjKey = :pmObjKey AND pprh.roleDefinitionObjKey = rdh.objKey AND rdh.objVersion = (SELECT MAX(srdh.objVersion) FROM RoleDefinitionHistory srdh WHERE rdh.objKey = srdh.objKey) ORDER BY pph.objKey ASC, pprh.objVersion DESC") })
public class PricedProductRoleHistory extends
        DomainHistoryObject<PricedProductRoleData> {

    private static final long serialVersionUID = -6230091310131428098L;

    /**
     * Role definition product key.
     */
    private long roleDefinitionObjKey;

    /**
     * Price model key.
     */
    private Long priceModelObjKey;

    /**
     * The key of the referenced priced parameter.
     */
    private Long pricedParameterObjKey;

    /**
     * The key of the referenced priced option.
     */
    private Long pricedOptionObjKey;

    /**
     * Default constructor.
     */
    public PricedProductRoleHistory() {
        super();
        dataContainer = new PricedProductRoleData();
    }

    /**
     * Parameterized constructor.
     * 
     * @param discount
     */
    public PricedProductRoleHistory(PricedProductRole pricedRole) {
        super(pricedRole);
        if (pricedRole.getRoleDefinition() != null) {
            setRoleDefinitionObjKey(pricedRole.getRoleDefinition().getKey());
        }
        if (pricedRole.getPriceModel() != null) {
            setPriceModelObjKey(Long.valueOf(pricedRole.getPriceModel()
                    .getKey()));
        }
        if (pricedRole.getPricedParameter() != null) {
            setPricedParameterObjKey(Long.valueOf(pricedRole
                    .getPricedParameter().getKey()));
        }
        if (pricedRole.getPricedOption() != null) {
            setPricedOptionObjKey(Long.valueOf(pricedRole.getPricedOption()
                    .getKey()));
        }
    }

    /**
     * 
     * @param roleDefinitionObjKey
     */
    public void setRoleDefinitionObjKey(long roleDefinitionObjKey) {
        this.roleDefinitionObjKey = roleDefinitionObjKey;
    }

    /**
     * 
     * @return
     */
    public long getRoleDefinitionObjKey() {
        return roleDefinitionObjKey;
    }

    /**
     * 
     * @param priceModelObjKey
     */
    public void setPriceModelObjKey(Long priceModelObjKey) {
        this.priceModelObjKey = priceModelObjKey;
    }

    /**
     * 
     * @return
     */
    public Long getPriceModelObjKey() {
        return priceModelObjKey;
    }

    /**
     * Setter for price.
     * 
     * @param pricePerUser
     */
    public void setPricePerUser(BigDecimal pricePerUser) {
        dataContainer.setPricePerUser(pricePerUser);
    }

    /**
     * Getter for price.
     * 
     * @return
     */
    public BigDecimal getPricePerUser() {
        return dataContainer.getPricePerUser();
    }

    /**
     * Returns the technical key of the referenced priced parameter,
     * <code>null</code> if none was set.
     * 
     * @return The priced parameter key.
     */
    public Long getPricedParameterObjKey() {
        return pricedParameterObjKey;
    }

    /**
     * Returns the technical key of the referenced priced option,
     * <code>null</code> if none was set.
     * 
     * @return The priced option key.
     */
    public Long getPricedOptionObjKey() {
        return pricedOptionObjKey;
    }

    /**
     * Sets the object key of the referenced priced parameter.
     * 
     * @param pricedParameterObjKey
     *            The priced parameter key.
     */
    public void setPricedParameterObjKey(Long pricedParameterObjKey) {
        this.pricedParameterObjKey = pricedParameterObjKey;
    }

    /**
     * Sets the object key of the referenced priced option.
     * 
     * @param pricedParameterObjKey
     *            The priced option key.
     */
    public void setPricedOptionObjKey(Long pricedOptionObjKey) {
        this.pricedOptionObjKey = pricedOptionObjKey;
    }

}
