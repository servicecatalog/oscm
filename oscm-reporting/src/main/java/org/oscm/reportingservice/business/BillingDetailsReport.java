/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oscm.converter.DateConverter;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UserGroup;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;
import org.oscm.reportingservice.business.model.billing.RDOSubscription;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.reportingservice.dao.BillingDao.ReportBillingData;
import org.oscm.reportingservice.dao.UnitDao;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author kulle
 * 
 */
public class BillingDetailsReport {

    private final BillingDao billingDao;
    final UserGroupServiceLocalBean userGroupService;
    private final UnitDao unitDao;
    
    public BillingDetailsReport(BillingDao billingDao, UnitDao unitDao,
            UserGroupServiceLocalBean userGroupService) {
        this.billingDao = billingDao;
        this.unitDao = unitDao;
        this.userGroupService = userGroupService;
    }

    public RDODetailedBilling buildReport(PlatformUser user,
            long billingResultTkey) throws ParserConfigurationException,
            SAXException, IOException, XPathExpressionException, SQLException {
        RDODetailedBilling result = new RDODetailedBilling();

        result.setSummaries(new ArrayList<RDOSummary>());
        result.setSubscriptions(new ArrayList<RDOSubscription>());

        List<ReportBillingData> billingDetails = new ArrayList<BillingDao.ReportBillingData>();

        if (user.isUnitAdmin() && !user.isOrganizationAdmin()) {
            List<Long> unitKeys = unitDao.retrieveUnitKeysForUnitAdmin(user
                    .getKey());
            billingDetails = billingDao.retrieveBillingDetails(
                    billingResultTkey, user.getOrganization().getKey(),
                    unitKeys);

        } else {
            billingDetails = billingDao.retrieveBillingDetails(
                    billingResultTkey, user.getOrganization().getKey());
        }

        Map<RDOSummary, Document> summaryTemplToDoc = getBillingData(billingDetails);

        List<RDOSummary> summaries = evaluateBillingResult(user,
                summaryTemplToDoc);
        if (summaries != null && !summaries.isEmpty()) {
            RDOSummary summary = summaries.get(0);

            RDOSubscription rdoSubscription = new RDOSubscription();
            rdoSubscription.setCurrency(summary.getCurrency());
            rdoSubscription.setDiscount(summary.getDiscount());
            rdoSubscription.setDiscountAmount(summary.getDiscountAmount());
            rdoSubscription.setGrossAmount(summary.getGrossAmount());
            rdoSubscription.setAmount(summary.getAmount());
            rdoSubscription.setPon(summary.getPurchaseOrderNumber());
            rdoSubscription.setVat(summary.getVat());
            rdoSubscription.setVatAmount(summary.getVatAmount());
            rdoSubscription.setNetAmountBeforeDiscount(
                    summary.getNetAmountBeforeDiscount());
            rdoSubscription.setSubscriptionId(summary.getSubscriptionId());
            result.getSubscriptions().add(rdoSubscription);
        }
        
        result.getSummaries().addAll(summaries);

        return result;
    }

    Map<RDOSummary, Document> getBillingData(
            List<ReportBillingData> billingDetails)
            throws ParserConfigurationException, SAXException, IOException {
        Map<RDOSummary, Document> summaryTemplToDoc = new HashMap<RDOSummary, Document>();
        for (ReportBillingData bd : billingDetails) {
            RDOSummary summaryTemplate = new RDOSummary();
            summaryTemplate.setBillingDate(
                    DateConverter.convertLongToDateTimeFormat(bd.getDate(),
                            TimeZone.getDefault(),
                            DateConverter.DTP_WITHOUT_MILLIS));
            summaryTemplate.setSupplierName(bd.getSupplierName());
            summaryTemplate.setSupplierAddress(bd.getSupplierAddress());

            try {
                Long unitKey = bd.getUserGroup();
                if (unitKey != null) {
                    UserGroup unit = userGroupService
                            .getUserGroupDetails(unitKey.longValue());
                    summaryTemplate.setUserGroupName(unit.getName());
                    summaryTemplate
                            .setUserGroupReferenceId(unit.getReferenceId());
                }
            } catch (ObjectNotFoundException ignored) {
            }

            summaryTemplToDoc.put(summaryTemplate, XMLConverter
                    .convertToDocument(bd.getBillingResult(), false));
        }
        return summaryTemplToDoc;
    }

    List<RDOSummary> evaluateBillingResult(PlatformUser user,
            Map<RDOSummary, Document> summaryTemplToDoc)
                    throws XPathExpressionException, SQLException {

        List<RDOSummary> result = new ArrayList<>();
        BillingResultParser brParser = new BillingResultParser(billingDao);
        PriceConverter formatter = new PriceConverter(
                new Locale(user.getLocale()));
        for (RDOSummary summaryTemplate : summaryTemplToDoc.keySet()) {
            Document details = summaryTemplToDoc.get(summaryTemplate);
            result.addAll(brParser.evaluateBillingResultForBillingDetails(
                    summaryTemplate, details, user, formatter));
        }
        return result;
    }
}
