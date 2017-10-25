/*******************************************************************************
 *                                                                              

 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 30.07.2009                                                      
 *                                                                              
 *  Completion Time: 04.08.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.IOException;
import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.oscm.converter.XMLConverter;

/**
 * Domain object to store the results of a billing run.
 * 
 * @author Mike J&auml;ger
 * 
 */
@NamedQueries({
        @NamedQuery(name = "BillingResult.getForAllCustomers", query = "SELECT br FROM BillingResult br, Organization cust, OrganizationReference ref"
                + " WHERE br.dataContainer.organizationTKey = cust.key AND cust.key = ref.targetKey AND ref.dataContainer.referenceType = :orgreftype AND ref.source = :seller"
                + " AND br.dataContainer.chargingOrgKey = ref.source"
                + " AND br.dataContainer.periodEndTime>:fromDate AND br.dataContainer.periodEndTime<=:toDate ORDER BY br.key ASC"),
        @NamedQuery(name = "BillingResult.getForCustomers", query = "SELECT br FROM BillingResult br, Organization cust, OrganizationReference ref"
                + " WHERE br.dataContainer.organizationTKey = cust.key AND cust.key = ref.targetKey AND ref.dataContainer.referenceType = :orgreftype AND ref.source = :seller"
                + " AND br.dataContainer.chargingOrgKey = ref.source"
                + " AND br.dataContainer.periodEndTime>:fromDate AND br.dataContainer.periodEndTime<=:toDate"
                + " AND cust.dataContainer.organizationId in (:customerIdList) ORDER BY br.key ASC"),
        @NamedQuery(name = "BillingResult.getForOrgAndPeriodMatch", query = "SELECT br FROM BillingResult br WHERE br.dataContainer.organizationTKey = :orgKey AND br.dataContainer.periodStartTime = :startTime AND br.dataContainer.periodEndTime = :endTime"),
        @NamedQuery(name = "BillingResult.getOutstandingBillingResults", query = "SELECT br FROM BillingResult br WHERE br NOT IN (SELECT p.billingResult FROM PaymentResult p)"),
        @NamedQuery(name = "BillingResult.findForSeller", query = "SELECT DISTINCT br FROM BillingResult br, SubscriptionHistory s, ProductHistory p, OrganizationHistory o WHERE  br.dataContainer.periodEndTime > :startTime AND br.dataContainer.periodEndTime <= :endTime AND br.dataContainer.subscriptionKey=s.objKey AND s.productObjKey=p.objKey AND p.vendorObjKey=o.objKey AND o.objKey = :sellerKey"),
        @NamedQuery(name = "BillingResult.findForSupplierWhenSupplierProduct", query = ""
                + "SELECT DISTINCT br FROM BillingResult br, SubscriptionHistory sh, ProductHistory subscriptionProduct"
                + " WHERE br.dataContainer.periodEndTime > :startTime"
                + "   AND br.dataContainer.periodEndTime <= :endTime"
                + "   AND br.dataContainer.subscriptionKey = sh.objKey"
                + "   AND sh.productObjKey=subscriptionProduct.objKey"
                + "   AND subscriptionProduct.vendorObjKey=:supplierKey"),
        @NamedQuery(name = "BillingResult.findForSupplierWhenPartnerProduct", query = ""
                + "SELECT DISTINCT br FROM BillingResult br, SubscriptionHistory sh, ProductHistory subscriptionProduct, ProductHistory vendorProduct, ProductHistory supplierProduct"
                + " WHERE br.dataContainer.periodEndTime > :startTime"
                + "   AND br.dataContainer.periodEndTime <= :endTime"
                + "   AND br.dataContainer.subscriptionKey = sh.objKey"
                + "   AND sh.productObjKey=subscriptionProduct.objKey"
                + "   AND subscriptionProduct.templateObjKey = vendorProduct.objKey"
                + "   AND vendorProduct.templateObjKey = supplierProduct.objKey"
                + "   AND supplierProduct.vendorObjKey=:supplierKey"),
        @NamedQuery(name = "BillingResult.findBillingResult", query = ""
                + "SELECT br FROM BillingResult br WHERE br.dataContainer.periodStartTime = :startPeriod"
                + "   AND br.dataContainer.periodEndTime = :endPeriod"
                + "   AND br.dataContainer.subscriptionKey = :subscriptionKey"),
        @NamedQuery(name = "BillingResult.findForPeriod", query = ""
                + "SELECT DISTINCT br FROM BillingResult br, CatalogEntryHistory ce, SubscriptionHistory sub, ProductHistory prd, MarketplaceHistory mp, SupportedCurrency cur"
                + " WHERE br.currency.dataContainer.currencyISOCode = :isoCode"
                + " AND br.dataContainer.periodEndTime > :startPeriod"
                + " AND br.dataContainer.periodEndTime <= :endPeriod"
                + " AND sub.objKey = br.dataContainer.subscriptionKey"
                + " AND prd.objKey = sub.productObjKey"
                + " AND ce.productObjKey = prd.templateObjKey"
                + " AND ce.marketplaceObjKey = :mpKey") })
@Entity
public class BillingResult extends
        DomainObjectWithVersioning<BillingResultData> {

    private static final long serialVersionUID = -3603714111176979355L;

    @OneToOne(mappedBy = "billingResult", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private PaymentResult paymentResult;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private SupportedCurrency currency;

    public BillingResult() {
        super();
        dataContainer = new BillingResultData();
    }

    /**
     * Refer to {@link BillingResultData#getResultXML()}
     */
    public String getResultXML() {
        return dataContainer.getResultXML();
    }

    /**
     * Refer to {@link BillingResultData#creationTime}
     */
    public long getCreationTime() {
        return dataContainer.getCreationTime();
    }

    /**
     * Refer to {@link BillingResultData#setResultXML(String)}
     */
    public void setResultXML(String resultXML) {
        dataContainer.setResultXML(resultXML);
    }

    /**
     * Refer to {@link BillingResultData#creationTime}
     */
    public void setCreationTime(long creationTime) {
        dataContainer.setCreationTime(creationTime);
    }

    /**
     * Refer to {@link BillingResultData#organizationTKey}
     */
    public long getOrganizationTKey() {
        return dataContainer.getOrganizationTKey();
    }

    /**
     * Refer to {@link BillingResultData#organizationTKey}
     */
    public void setOrganizationTKey(long organizationTKey) {
        dataContainer.setOrganizationTKey(organizationTKey);
    }

    public PaymentResult getPaymentResult() {
        return paymentResult;
    }

    public void setPaymentResult(PaymentResult paymentResult) {
        this.paymentResult = paymentResult;
    }

    /**
     * Refer to {@link BillingResultData#periodEndTime}
     */
    public long getPeriodEndTime() {
        return dataContainer.getPeriodEndTime();
    }

    /**
     * Refer to {@link BillingResultData#periodStartTime}
     */
    public long getPeriodStartTime() {
        return dataContainer.getPeriodStartTime();
    }

    /**
     * Refer to {@link BillingResultData#periodEndTime}
     */
    public void setPeriodEndTime(long periodEndTime) {
        dataContainer.setPeriodEndTime(periodEndTime);
    }

    /**
     * Refer to {@link BillingResultData#periodStartTime}
     */
    public void setPeriodStartTime(long periodStartTime) {
        dataContainer.setPeriodStartTime(periodStartTime);
    }

    public void setChargingOrgKey(long chargingOrgKey) {
        dataContainer.setChargingOrgKey(chargingOrgKey);
    }

    public long getChargingOrgKey() {
        return dataContainer.getChargingOrgKey();
    }

    public SupportedCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(SupportedCurrency currency) {
        this.currency = currency;
    }

    public String getCurrencyCode() {
        return getCurrency().getCurrencyISOCode();
    }

    public BigDecimal getGrossAmount() {
        return dataContainer.getGrossAmount();
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        dataContainer.setGrossAmount(grossAmount);
    }

    public BigDecimal getNetAmount() {
        return dataContainer.getNetAmount();
    }

    public void setNetAmount(BigDecimal netAmount) {
        dataContainer.setNetAmount(netAmount);
    }

    public void setVendorKey(long vendorKey) {
        dataContainer.setVendorKey(vendorKey);
    }

    public long getVendorKey() {
        return dataContainer.getVendorKey();
    }

    /**
     * Returns the vat costs of the this billing result. The XML may contain
     * multiple price models with multiple VATs. Currently, only the first is
     * returned.
     * 
     * @return The vat costs.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    public BigDecimal getVATAmount() throws ParserConfigurationException,
            SAXException, IOException, XPathExpressionException {
        Document doc = dataContainer.getDocument();
        if (doc == null) {
            return null;
        }
        String vatAmount = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/VAT/@amount");
        BigDecimal result = BigDecimal.ZERO;
        if (vatAmount != null) {
            result = new BigDecimal(vatAmount);// Long.valueOf(vatAmount.longValue());
        }
        return result;
    }

    /**
     * Returns the vat percentage of this billing result. The XML may contain
     * multiple price models with multiple VATs. Currently, only the first is
     * returned.
     * 
     * @return The vat percentage.
     */
    public String getVAT() throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException {
        Document doc = dataContainer.getDocument();
        if (doc == null) {
            return null;
        }
        String result = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/VAT/@percent");
        if (result == null) {
            result = "0";
        }
        return result;
    }

    /**
     * Returns the net amount discount of this billing result. The XML may
     * contain multiple price models with multiple discounts. Currently, only
     * the first is returned.
     */
    public BigDecimal getNetDiscount() throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException {
        BigDecimal result = null;
        Document doc = dataContainer.getDocument();
        if (doc == null) {
            return null;
        }

        Node costsAttribute = XMLConverter.getNodeByXPath(doc,
                "/BillingDetails/OverallCosts/Discount/@discountNetAmount");

        if (costsAttribute != null) {
            String costsAsString = costsAttribute.getTextContent();
            result = new BigDecimal(costsAsString);
        }
        return result;
    }

    public void setSubscriptionKey(Long subscriptionKey) {
        dataContainer.setSubscriptionKey(subscriptionKey);
    }

    public Long getSubscriptionKey() {
        return dataContainer.getSubscriptionKey();
    }

    public Long getUsergroupKey() {
        return dataContainer.getUsergroup_tkey();
    }

    public void setUsergroupKey(Long usergroupKey) {
        this.dataContainer.setUsergroup_tkey(usergroupKey);
    }

}
