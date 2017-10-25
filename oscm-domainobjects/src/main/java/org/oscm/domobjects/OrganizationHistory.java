/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History-Object of Organization, used for auditing. Will be automatically
 * created during persist, save or remove operations (if performed via
 * DataManager)
 * 
 * @see Organization
 * 
 * @author schmid
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "OrganizationHistory.findByObject", query = "select c from OrganizationHistory c where c.objKey=:objKey order by objVersion"),
        @NamedQuery(name = "OrganizationHistory.findByObjectDesc", query = "select c from OrganizationHistory c where c.objKey=:objKey order by objVersion DESC"),
        @NamedQuery(name = "OrganizationHistory.getAllOrganizationKeys", query = "select distinct c.objKey from OrganizationHistory c "),
        @NamedQuery(name = "OrganizationHistory.getByTKeyDesc", query = "SELECT c from OrganizationHistory c WHERE c.objKey = :objKey ORDER BY c.objVersion DESC, c.modDate DESC"),
        @NamedQuery(name = "OrganizationHistory.findAllOrganizationsWithRole", query = "SELECT o.objKey from OrganizationHistory o,  OrganizationToRoleHistory otr, OrganizationRole role WHERE o.objKey=otr.organizationTKey AND otr.organizationRoleTKey=role.key AND role.dataContainer.roleName= :roleName AND otr.modDate < :modDate GROUP BY o.objKey"),
        @NamedQuery(name = "OrganizationHistory.findAllMarketplaceOwnerKeys", query = "SELECT DISTINCT m.organizationObjKey FROM MarketplaceHistory m WHERE m.modDate < :modDate AND m.key = (SELECT max(innerM.key) FROM MarketplaceHistory innerM WHERE innerM.objKey = m.objKey AND innerM.modDate < :modDate)"),
        @NamedQuery(name = "OrganizationHistory.findVendorOfProduct", query = "SELECT o FROM ProductHistory vendorProduct, OrganizationHistory o WHERE vendorProduct.objKey=:productKey AND vendorProduct.vendorObjKey = o.objKey ORDER BY o.objVersion DESC"),
        @NamedQuery(name = "OrganizationHistory.findSupplierOfPartnerProduct", query = "SELECT o FROM ProductHistory vendorProduct, ProductHistory supplierProduct, OrganizationHistory o WHERE vendorProduct.objKey = :productKey AND vendorProduct.templateObjKey = supplierProduct.objKey AND supplierProduct.vendorObjKey = o.objKey ORDER BY o.objVersion DESC") })
public class OrganizationHistory extends DomainHistoryObject<OrganizationData> {

    private static final long serialVersionUID = 1L;

    private Long domicileCountryObjKey;

    @Column(nullable = true)
    private Long operatorPriceModelObjKey;

    public OrganizationHistory() {
        dataContainer = new OrganizationData();
    }

    /**
     * Constructs OrganizationHistory from a Organization domain object
     * 
     * @param org
     *            - the organization
     */
    public OrganizationHistory(Organization org) {
        super(org);

        if (org.getDomicileCountry() != null) {
            setDomicileCountryObjKey(Long.valueOf(org.getDomicileCountry()
                    .getKey()));
        }

        if (org.getOperatorPriceModel() != null) {
            setOperatorPriceModelObjKey(Long.valueOf(org
                    .getOperatorPriceModel().getKey()));
        }
    }

    public String getAddress() {
        return dataContainer.getAddress();
    }

    public String getOrganizationName() {
        return dataContainer.getName();
    }

    public String getOrganizationId() {
        return dataContainer.getOrganizationId();
    }

    public String getEmail() {
        return dataContainer.getEmail();
    }

    public String getSupportEmail() {
        return dataContainer.getSupportEmail();
    }

    public void setSupportEmail(String supportEmail) {
        dataContainer.setSupportEmail(supportEmail);
    }

    public void setDomicileCountryObjKey(Long domicileCountryObjKey) {
        this.domicileCountryObjKey = domicileCountryObjKey;
    }

    public Long getDomicileCountryObjKey() {
        return domicileCountryObjKey;
    }

    public void setCutOffDay(int cutOffDay) {
        dataContainer.setCutOffDay(cutOffDay);
    }

    public int getCutOffDay() {
        return dataContainer.getCutOffDay();
    }

    public Long getOperatorPriceModelObjKey() {
        return operatorPriceModelObjKey;
    }

    public void setOperatorPriceModelObjKey(Long operatorPriceModelObjKey) {
        this.operatorPriceModelObjKey = operatorPriceModelObjKey;
    }
}
