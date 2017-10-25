/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                 
 *                                                                              
 *  Creation Date: 16.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History-Object of VatRate, used for auditing. Will be automatically created
 * during persist, save or remove operations (if performed via DataManager).
 * 
 * @author pock
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "VatRateHistory.findByObject", query = "SELECT h FROM VatRateHistory h WHERE h.objKey=:objKey ORDER BY objversion"),
        @NamedQuery(name = "VatRateHistory.findForCustomerAndSupplier", query = "SELECT vat FROM VatRateHistory vat, OrganizationHistory cust, OrganizationReferenceHistory ref "
                + "WHERE vat.owningOrganizationObjKey = :supplierKey AND cust.objKey = :customerKey AND ref.sourceObjKey = vat.owningOrganizationObjKey "
                + "AND ref.dataContainer.referenceType = org.oscm.domobjects.enums.OrganizationReferenceType.SUPPLIER_TO_CUSTOMER AND ref.targetObjKey = cust.objKey "
                + "AND vat.objVersion = (SELECT max(innerVat.objVersion) FROM VatRateHistory innerVat WHERE innerVat.objKey = vat.objKey AND innerVat.modDate < :endDate ) "
                + "AND cust.objVersion = (SELECT max(innerCust.objVersion) FROM OrganizationHistory innerCust WHERE innerCust.objKey = cust.objKey) "
                + "AND (vat.targetCountryObjKey IS NULL OR vat.targetCountryObjKey = cust.domicileCountryObjKey) "
                + "AND (vat.targetOrganizationObjKey IS NULL OR vat.targetOrganizationObjKey = cust.objKey)") })
public class VatRateHistory extends DomainHistoryObject<VatRateData> {

    private static final long serialVersionUID = -1724927475290762224L;

    private long owningOrganizationObjKey;
    private Long targetCountryObjKey;
    private Long targetOrganizationObjKey;

    public VatRateHistory() {
        setDataContainer(new UdaData());
    }

    public VatRateHistory(VatRate vatRate) {
        super(vatRate);
        setOwningOrganizationObjKey(vatRate.getOwningOrganization().getKey());
        SupportedCountry country = vatRate.getTargetCountry();
        if (country != null) {
            setTargetCountryObjKey(Long.valueOf(country.getKey()));
        }
        Organization org = vatRate.getTargetOrganization();
        if (org != null) {
            setTargetOrganizationObjKey(Long.valueOf(org.getKey()));
        }
    }

    public long getOwningOrganizationObjKey() {
        return owningOrganizationObjKey;
    }

    public void setOwningOrganizationObjKey(long owningOrganizationObjKey) {
        this.owningOrganizationObjKey = owningOrganizationObjKey;
    }

    public Long getTargetCountryObjKey() {
        return targetCountryObjKey;
    }

    public void setTargetCountryObjKey(Long referencedCountryObjKey) {
        this.targetCountryObjKey = referencedCountryObjKey;
    }

    public Long getTargetOrganizationObjKey() {
        return targetOrganizationObjKey;
    }

    public void setTargetOrganizationObjKey(Long targetOrganizationObjKey) {
        this.targetOrganizationObjKey = targetOrganizationObjKey;
    }
}
