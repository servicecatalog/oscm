/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                                    
 *                                                                              
 *  Creation Date: 01.12.2011                                                      
 *                                                                              
 *  Completion Time: 01.12.2011                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * This class represents objects which grant a supplier the right to create
 * marketable services for a technical product.
 * 
 * @author kulle
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "technicalproduct_tkey", "organizationreference_tkey" }))
@BusinessKey(attributes = { "technicalProductKey", "organizationReferenceKey" })
@NamedQueries({
        @NamedQuery(name = "MarketingPermission.findByBusinessKey", query = "SELECT obj FROM MarketingPermission obj WHERE obj.technicalProductKey=:technicalProductKey AND obj.organizationReferenceKey=:organizationReferenceKey"),
        @NamedQuery(name = "MarketingPermission.findForSupplierIds", query = "SELECT mp, supplier FROM MarketingPermission mp, OrganizationReference orgref, Organization tp, Organization supplier WHERE supplier.dataContainer.organizationId IN (:orgIds) AND tp.key = orgref.sourceKey AND orgref.dataContainer.referenceType = :refType AND mp.organizationReference = orgref AND mp.technicalProduct = :tp AND supplier.key = orgref.targetKey"),
        @NamedQuery(name = "MarketingPermission.findForTechnicalService", query = "SELECT mp FROM MarketingPermission mp WHERE mp.technicalProduct = :tp"),
        @NamedQuery(name = "MarketingPermission.getOrgsForUsingTechnicalService", query = "SELECT organization FROM Organization organization, MarketingPermission mp, OrganizationReference orgRef WHERE mp.technicalProduct.key = :tpKey AND mp.organizationReference.key = orgRef.key AND orgRef.target.key = organization.key AND orgRef.dataContainer.referenceType = :refType") })
public class MarketingPermission extends DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = 335153545468338197L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicalproduct_tkey")
    private TechnicalProduct technicalProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationreference_tkey")
    private OrganizationReference organizationReference;

    /**
     * In order to form a complete business key the TechnicalProduct key is
     * needed as explicit field inside this class.
     */
    @Column(name = "technicalproduct_tkey", insertable = false, updatable = false, nullable = false)
    private long technicalProductKey;

    /**
     * In order to form a complete business key the OrganizationReference key is
     * needed as explicit field inside this class.
     */
    @Column(name = "organizationreference_tkey", insertable = false, updatable = false, nullable = false)
    private long organizationReferenceKey;

    public TechnicalProduct getTechnicalProduct() {
        return technicalProduct;
    }

    public void setTechnicalProduct(TechnicalProduct technicalProduct) {
        this.technicalProduct = technicalProduct;
        if (technicalProduct != null) {
            setTechnicalProductKey(technicalProduct.getKey());
        }
    }

    public OrganizationReference getOrganizationReference() {
        return organizationReference;
    }

    public void setOrganizationReference(
            OrganizationReference organizationReference) {
        this.organizationReference = organizationReference;
        if (organizationReference != null) {
            setOrganizationReferenceKey(organizationReference.getKey());
        }
    }

    public long getTechnicalProductKey() {
        return technicalProductKey;
    }

    public void setTechnicalProductKey(long technicalProductKey) {
        this.technicalProductKey = technicalProductKey;
    }

    public long getOrganizationReferenceKey() {
        return organizationReferenceKey;
    }

    public void setOrganizationReferenceKey(long organizationReferenceKey) {
        this.organizationReferenceKey = organizationReferenceKey;
    }

}
