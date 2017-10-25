/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.billingservice.service.model.BillingRun;
import org.oscm.converter.DateConverter;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UserGroup;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.reportingservice.business.model.billing.RDOCustomerPaymentPreview;
import org.oscm.reportingservice.business.model.billing.RDOPaymentPreviewSummary;
import org.oscm.reportingservice.business.model.billing.RDOSubscription;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.reportingservice.dao.UnitDao;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author kulle
 * 
 */
public class CustomerPaymentPreviewReport {

    final BillingDao billingDao;
    final UnitDao unitDao;
    final BillingServiceLocal billing;
    final UserGroupServiceLocalBean userGroupService;
    BillingResultParser brParser;
    
    public CustomerPaymentPreviewReport(BillingDao dao, UnitDao unitDao,
            BillingServiceLocal billing, UserGroupServiceLocalBean userGroupService) {
        this.billingDao = dao;
        this.unitDao = unitDao;
        this.billing = billing;
        this.brParser = new BillingResultParser(dao);
        this.userGroupService = userGroupService;
    }

    public RDOCustomerPaymentPreview buildReport(PlatformUser user)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException, SQLException, BillingRunFailed {
        if (user == null || !(user.getOrganization()
                .hasRole(OrganizationRoleType.CUSTOMER))) {
            return new RDOCustomerPaymentPreview();
        }

        TimeZone timeZoneServer = TimeZone.getDefault();
        RDOCustomerPaymentPreview result = new RDOCustomerPaymentPreview();

        // collect subscription information
        BillingRun billingRun = null;
        if (user.isUnitAdmin() && !user.isOrganizationAdmin()) {
            List<Long> unitKeys = unitDao.retrieveUnitKeysForUnitAdmin(user
                    .getKey());
            billingRun = billing.generatePaymentPreviewReport(user
                    .getOrganization().getKey(), unitKeys);
        } else {
            billingRun = billing.generatePaymentPreviewReport(user
                    .getOrganization().getKey());
        }

        List<BillingResult> billingResultList = billingRun
                .getBillingResultList();

        if (billingResultList != null && !billingResultList.isEmpty()) {
            PriceConverter formatter = new PriceConverter(
                    new Locale(user.getLocale()));

            // process billing result
            for (BillingResult br : billingResultList) {
                RDOSummary summaryTemplate = new RDOSummary();
                summaryTemplate.setBillingDate(
                        DateConverter.convertLongToDateTimeFormat(
                                br.getCreationTime(), timeZoneServer,
                                DateConverter.DTP_WITHOUT_MILLIS));

                billingDao.retrieveOrganizationDetails(br.getChargingOrgKey());
                summaryTemplate.setSupplierName(billingDao.getReportData()
                        .getName());
                summaryTemplate.setSupplierAddress(billingDao.getReportData()
                        .getAddress());
                Document document = XMLConverter.convertToDocument(
                        br.getResultXML(), true);

                List<RDOPaymentPreviewSummary> summaries = brParser
                        .evaluateBillingResultForPaymentPreview(summaryTemplate,
                                document, user, formatter,
                                Long.valueOf(billingRun.getEnd()));
                if (summaries != null && !summaries.isEmpty()) {
                    RDOSummary summary = summaries.get(0);
                    RDOSubscription rdoSubscription = new RDOSubscription();
                    rdoSubscription.setCurrency(summary.getCurrency());
                    rdoSubscription.setDiscount(summary.getDiscount());
                    rdoSubscription
                            .setDiscountAmount(summary.getDiscountAmount());
                    rdoSubscription.setGrossAmount(summary.getGrossAmount());
                    rdoSubscription.setAmount(summary.getAmount());
                    rdoSubscription.setPon(summary.getPurchaseOrderNumber());
                    rdoSubscription.setVat(summary.getVat());
                    rdoSubscription.setVatAmount(summary.getVatAmount());
                    rdoSubscription.setNetAmountBeforeDiscount(
                            summary.getNetAmountBeforeDiscount());
                    rdoSubscription
                            .setSubscriptionId(summary.getSubscriptionId());
                    rdoSubscription.setPurchaseOrderNumber(
                            summary.getPurchaseOrderNumber());

                    try {
                        Long unitKey = br.getUsergroupKey();
                        if (unitKey != null) {
                            UserGroup unit = userGroupService
                                    .getUserGroupDetails(unitKey.longValue());
                            summary.setUserGroupName(unit.getName());
                            summary.setUserGroupReferenceId(
                                    unit.getReferenceId());
                        }
                    } catch (ObjectNotFoundException ignored) {
                    }

                    result.getSubscriptions().add(rdoSubscription);
                }
                result.getSummaries().addAll(summaries);
            }

            // add report header information if subscription data is present
            result.setStartDate(DateConverter.convertLongToDateTimeFormat(
                    billingRun.getStart(), timeZoneServer,
                    DateConverter.DTP_WITHOUT_MILLIS));
            result.setEndDate(DateConverter.convertLongToDateTimeFormat(
                    billingRun.getEnd(), timeZoneServer,
                    DateConverter.DTP_WITHOUT_MILLIS));
        }

        return result;
    }
}
