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

import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * History-Object of Subscription, used for auditing. Will be automatically
 * created during persist, save or remove operations (if performed via
 * DataManager). Also contains the primary keys for the related organization and
 * price model, but not the usagelicenses (this relation is held in
 * UsageLicenseHistory, as UsageLicense holds the foreign key to Subscription).
 * 
 * @author schmid
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "SubscriptionHistory.findByObject", query = "select c from SubscriptionHistory c where c.objKey=:objKey order by objversion"),
        @NamedQuery(name = "SubscriptionHistory.getSubscriptionsForOrganization_VersionDesc", query = "SELECT c FROM SubscriptionHistory c WHERE c.dataContainer.external<>:external AND c.organizationObjKey=:organizationKey AND (SELECT count(*) FROM BillingResult br WHERE to_timestamp(br.dataContainer.periodStartTime/1000) = :startDate AND to_timestamp(br.dataContainer.periodEndTime/1000) = :endDate AND br.dataContainer.subscriptionKey = c.objKey) = 0 AND c.dataContainer.activationDate IS NOT NULL AND (0 > :cutOffDay OR c.dataContainer.cutOffDay = :cutOffDay) AND ((c.modDate < :endDate AND c.modDate + '35 days' >= :startDate) OR (c.objVersion = (SELECT max(ish.objVersion) FROM SubscriptionHistory ish WHERE ish.objKey=c.objKey AND ish.dataContainer.activationDate IS NOT NULL AND ish.modDate + '35 days' < :startDate))) ORDER BY c.objKey ASC, c.objVersion DESC, c.modDate DESC"),
        @NamedQuery(name = "SubscriptionHistory.getSubscriptionsForOrganizationAndUnits_VersionDesc", query = "SELECT c FROM SubscriptionHistory c, Subscription s WHERE c.dataContainer.external<>:external AND c.organizationObjKey=:organizationKey AND c.objKey=s.key AND s.userGroup.key IN (:units) AND (SELECT count(*) FROM BillingResult br WHERE to_timestamp(br.dataContainer.periodStartTime/1000) = :startDate AND to_timestamp(br.dataContainer.periodEndTime/1000) = :endDate AND br.dataContainer.subscriptionKey = c.objKey) = 0 AND c.dataContainer.activationDate IS NOT NULL AND (0 > :cutOffDay OR c.dataContainer.cutOffDay = :cutOffDay) AND ((c.modDate < :endDate AND c.modDate + '35 days' >= :startDate) OR (c.objVersion = (SELECT max(ish.objVersion) FROM SubscriptionHistory ish WHERE ish.objKey=c.objKey AND ish.dataContainer.activationDate IS NOT NULL AND ish.modDate + '35 days' < :startDate))) ORDER BY c.objKey ASC, c.objVersion DESC, c.modDate DESC"),
        @NamedQuery(name = "SubscriptionHistory.getHistoriesForSubscriptionsAndBillingPeriod", query = "SELECT c FROM SubscriptionHistory c WHERE c.dataContainer.external<>:external AND c.objKey IN (:subscriptionKeys) AND c.dataContainer.activationDate IS NOT NULL AND ((c.modDate < :endDate AND c.modDate + '35 days' >= :startDate) OR (c.objVersion = (SELECT max(ish.objVersion) FROM SubscriptionHistory ish WHERE ish.objKey=c.objKey AND ish.dataContainer.activationDate IS NOT NULL AND ish.modDate + '35 days' < :startDate))) ORDER BY c.objKey ASC, c.objVersion DESC, c.modDate DESC"),
        @NamedQuery(name = "SubscriptionHistory.findCurrency", query = "SELECT sc FROM SubscriptionHistory c, PriceModelHistory pmh, ProductHistory ph, SupportedCurrency sc WHERE c.objKey=:subscriptionKey AND ph.objKey = c.productObjKey AND ph.priceModelObjKey = pmh.objKey AND pmh.currencyObjKey = sc.key AND pmh.objKey = ph.priceModelObjKey AND ph.objKey = c.productObjKey AND ph.key = (SELECT max(innerPh.key) FROM ProductHistory innerPh WHERE innerPh.objKey = ph.objKey AND innerPh.modDate < :endDate) AND pmh.key = (SELECT max(innerPmh.key) FROM PriceModelHistory innerPmh WHERE innerPmh.objKey = pmh.objKey AND innerPmh.modDate < :endDate)"),
        @NamedQuery(name = "SubscriptionHistory.getVendorKey", query = "SELECT DISTINCT subscriptionProduct.vendorObjKey FROM SubscriptionHistory sh, ProductHistory subscriptionProduct WHERE sh.objKey=:subscriptionKey AND sh.productObjKey = subscriptionProduct.objKey"),
        @NamedQuery(name = "SubscriptionHistory.getSupplierKey", query = "SELECT DISTINCT supplierProduct.vendorObjKey FROM SubscriptionHistory sh, ProductHistory subscriptionProduct, ProductHistory vendorProduct, ProductHistory supplierProduct WHERE sh.objKey=:subscriptionKey AND sh.productObjKey = subscriptionProduct.objKey AND subscriptionProduct.dataContainer.type = :productType AND subscriptionProduct.templateObjKey = vendorProduct.objKey AND vendorProduct.templateObjKey = supplierProduct.objKey"),
        @NamedQuery(name = "SubscriptionHistory.getPaymentTypeId", query = "SELECT pt.dataContainer.paymentTypeId FROM PaymentType pt, PaymentInfoHistory pih, SubscriptionHistory sh "
                + "WHERE sh.objVersion = (SELECT MAX(sh2.objVersion) FROM SubscriptionHistory sh2 WHERE sh2.objKey=:subscriptionKey AND sh2.paymentInfoObjKey IS NOT NULL) AND sh.objKey=:subscriptionKey "
                + "AND pih.objVersion = (SELECT MAX(pih2.objVersion) FROM PaymentInfoHistory pih2 WHERE pih2.objKey = sh.paymentInfoObjKey) AND pih.objKey = sh.paymentInfoObjKey "
                + "AND pt.key = pih.paymentTypeObjKey"),
        @NamedQuery(name = "SubscriptionHistory.findBillingContact", query = "SELECT bch FROM SubscriptionHistory sh, BillingContactHistory bch "
                + "WHERE sh.objVersion = (SELECT MAX(sh2.objVersion) FROM SubscriptionHistory sh2 WHERE sh2.objKey=:subscriptionKey AND sh2.billingContactObjKey IS NOT NULL) "
                + "AND sh.objKey=:subscriptionKey AND bch.objKey = sh.billingContactObjKey ORDER BY bch.objVersion DESC"),
        @NamedQuery(name = "SubscriptionHistory.findWithinPeriod", query = "SELECT sh FROM SubscriptionHistory sh WHERE sh.objKey=:subscriptionKey AND sh.modDate<:modDate ORDER BY sh.objVersion DESC"),
        @NamedQuery(name = "SubscriptionHistory.getVendorRoleNames", query = "SELECT DISTINCT r.dataContainer.roleName FROM SubscriptionHistory sh, ProductHistory ph, OrganizationHistory oh, OrganizationToRoleHistory o2rh, OrganizationRole r WHERE sh.objKey = :subscriptionKey AND ph.objKey = sh.productObjKey AND oh.objKey = ph.vendorObjKey AND o2rh.organizationTKey = oh.objKey AND r.key = o2rh.organizationRoleTKey ORDER BY r.dataContainer.roleName"),
        @NamedQuery(name = "SubscriptionHistory.getCutOffDay", query = "SELECT sh.dataContainer.cutOffDay FROM SubscriptionHistory sh WHERE sh.objKey=:subscriptionKey AND sh.modDate<=:endDate ORDER BY sh.modDate DESC"),
        @NamedQuery(name = "SubscriptionHistory.getAccessInfos", query = "SELECT sh.dataContainer.accessInfo FROM SubscriptionHistory sh WHERE sh.dataContainer.subscriptionId=:subscriptionId AND sh.dataContainer.productInstanceId=:productInstanceId AND sh.dataContainer.status=:status ORDER BY sh.modDate DESC"),
        @NamedQuery(name = "SubscriptionHistory.findPreviousForPriceModel", query = "SELECT DISTINCT sh FROM SubscriptionHistory sh, PriceModelHistory pmh WHERE sh.productObjKey=pmh.productObjKey AND pmh.objKey=:priceModelKey AND sh.modDate<:modDate ORDER BY sh.objVersion DESC"),
        @NamedQuery(name = "SubscriptionHistory.findNextForPriceModelAndState", query = "SELECT DISTINCT sh FROM SubscriptionHistory sh, PriceModelHistory pmh WHERE sh.productObjKey=pmh.productObjKey AND pmh.objKey=:priceModelKey AND sh.modDate>=:modDate AND sh.dataContainer.status IN (:subscriptionStates) ORDER BY sh.objVersion") })
public class SubscriptionHistory extends DomainHistoryObject<SubscriptionData> {

    private static final long serialVersionUID = 1L;

    public SubscriptionHistory() {
        dataContainer = new SubscriptionData();
    }

    /**
     * Constructs SubscriptionHistory from a Subscription domain object
     * 
     * @param c
     *            - the Subscription
     */
    public SubscriptionHistory(Subscription c) {
        super(c);
        if (c.getOrganization() != null) {
            setOrganizationObjKey(c.getOrganization().getKey());
        }
        if (c.getProduct() != null) {
            setProductObjKey(c.getProduct().getKey());
        }
        if (c.getAsyncTempProduct() != null) {
            setAsyncTempProductObjKey(Long.valueOf(c.getAsyncTempProduct()
                    .getKey()));
        }
        if (c.getMarketplace() != null) {
            setMarketplaceObjKey(Long.valueOf(c.getMarketplace().getKey()));
        }
        if (c.getPaymentInfo() != null) {
            setPaymentInfoObjKey(Long.valueOf(c.getPaymentInfo().getKey()));
        }
        if (c.getBillingContact() != null) {
            setBillingContactObjKey(Long
                    .valueOf(c.getBillingContact().getKey()));
        }
        if (c.getOwner() != null) {
            setOwnerObjKey(Long.valueOf(c.getOwner().getKey()));
        }
        if (c.getUserGroup() != null) {
            setUserGroupObjKey(Long.valueOf(c.getUserGroup().getKey()));
        }
    }

    /**
     * Reference to the Organization (only id)
     */
    @Column(nullable = false)
    private long organizationObjKey;

    /**
     * Reference to the Product (only id).
     */
    @Column(nullable = false)
    private long productObjKey;

    /**
     * Reference to the Product (only id).
     */
    @Column(nullable = true)
    private Long asyncTempProductObjKey;

    /**
     * Reference to the marketplace the product has been subscribed over.
     */
    @Column(nullable = true)
    private Long marketplaceObjKey;

    /**
     * Reference to the owner of the subscription.
     */
    @Column(nullable = true)
    private Long ownerObjKey;

    /**
     * Reference to the user group owner of the subscription.
     */
    @Column(nullable = true)
    private Long userGroupObjKey;

    /**
     * Reference to the payment information of the subscription.
     */
    @Column(nullable = true)
    private Long paymentInfoObjKey;

    /**
     * Reference to the billing contact of the subscription.
     */
    @Column(nullable = true)
    private Long billingContactObjKey;

    public void setOrganizationObjKey(long organization_objid) {
        this.organizationObjKey = organization_objid;
    }

    public long getOrganizationObjKey() {
        return organizationObjKey;
    }

    public long getProductObjKey() {
        return productObjKey;
    }

    public void setProductObjKey(long productObjKey) {
        this.productObjKey = productObjKey;
    }

    public Long getAsyncTempProductObjKey() {
        return asyncTempProductObjKey;
    }

    public void setAsyncTempProductObjKey(Long asyncTempProductObjKey) {
        this.asyncTempProductObjKey = asyncTempProductObjKey;
    }

    public void setMarketplaceObjKey(Long marketplaceObjKey) {
        this.marketplaceObjKey = marketplaceObjKey;
    }

    public Long getMarketplaceObjKey() {
        return marketplaceObjKey;
    }

    public Long getPaymentInfoObjKey() {
        return paymentInfoObjKey;
    }

    public void setPaymentInfoObjKey(Long paymentInfoObjKey) {
        this.paymentInfoObjKey = paymentInfoObjKey;
    }

    public void setBillingContactObjKey(Long billingContactObjKey) {
        this.billingContactObjKey = billingContactObjKey;
    }

    public Long getBillingContactObjKey() {
        return billingContactObjKey;
    }

    public Long getOwnerObjKey() {
        return ownerObjKey;
    }

    public void setOwnerObjKey(Long ownerObjKey) {
        this.ownerObjKey = ownerObjKey;
    }

    public SubscriptionStatus getStatus() {
        return getDataContainer().getStatus();
    }

    public void setCutOffDay(int cutOffDay) {
        getDataContainer().setCutOffDay(cutOffDay);
    }

    public int getCutOffDay() {
        return getDataContainer().getCutOffDay();
    }

    public void setUserGroupObjKey(Long userGroupObjKey) {
        this.userGroupObjKey = userGroupObjKey;
    }

    public Long getUserGroupObjKey() {
        return userGroupObjKey;
    }
}
