/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                                    
 *                                                                              
 *  Creation Date: 11.08.2011                                                      
 *                                                                              
 *  Completion Time: 11.08.2011                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.converter.DateConverter;
import org.oscm.converter.PriceConverter;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;
import org.oscm.reportingservice.business.model.billing.RDOEvent;
import org.oscm.reportingservice.business.model.billing.RDOEventFees;
import org.oscm.reportingservice.business.model.billing.RDOOption;
import org.oscm.reportingservice.business.model.billing.RDOParameter;
import org.oscm.reportingservice.business.model.billing.RDOPriceModel;
import org.oscm.reportingservice.business.model.billing.RDORole;
import org.oscm.reportingservice.business.model.billing.RDOSteppedPrice;
import org.oscm.reportingservice.business.model.billing.RDOSubscriptionFees;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.business.model.billing.RDOUserFees;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;

/**
 * @author kulle
 */
public class ReportingServiceBeanBillingTest extends BaseBillingReport {

    private String millisToString(long millis) {
        return DateConverter.convertLongToDateTimeFormat(millis,
                TimeZone.getDefault(), DateConverter.DTP_WITHOUT_MILLIS);
    }

    private void checkBillingReportResult(RDODetailedBilling result,
            String serviceName) {
        assertEquals(1, result.getSummaries().size());
        RDOSummary bd = result.getSummaries().get(0);
        assertNotNull(bd.getPriceModel());
        checkBillingReportData1(bd);
        assertEquals(serviceName, bd.getPriceModel().getServiceName());
    }

    private void checkBillingReportData1(RDOSummary bd) {
        Assert.assertNotNull(bd);
        Assert.assertEquals("14,681.00", bd.getAmount());
        Assert.assertEquals("14,691.00", bd.getGrossAmount());
        Assert.assertEquals("Address of TestOrg", bd.getOrganizationAddress());
        Assert.assertEquals("TestOrg", bd.getOrganizationName());
        Assert.assertEquals("1970-01-01 01:00:00", bd.getBillingDate());
        Assert.assertEquals("2010-01-01 00:00:00", bd.getPriceModel()
                .getStartDate());
        Assert.assertEquals("2010-02-01 00:00:00", bd.getPriceModel()
                .getEndDate());
        Assert.assertEquals("EUR", bd.getCurrency());
        Assert.assertEquals("10.00", bd.getDiscountAmount());
        Assert.assertEquals("5.00", bd.getDiscount());
        Assert.assertEquals("10.00", bd.getVatAmount());
        Assert.assertEquals("5.00", bd.getVat());
        Assert.assertEquals("INVOICE", bd.getPaymentType());
        Assert.assertEquals("Supplier", bd.getSupplierName());
        Assert.assertEquals("SupAddress", bd.getSupplierAddress());
        validateEventValues(bd.getPriceModel());
    }

    private void validateEventValues(RDOPriceModel pmData) {
        RDOEventFees eventFees = pmData.getEventFees();
        Assert.assertNotNull(eventFees);
        Assert.assertEquals("3,385.00", eventFees.getSubtotalAmount());
        List<RDOEvent> events = eventFees.getEvents();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());
        Iterator<RDOEvent> i = events.iterator();

        RDOEvent firstEvent = i.next();
        RDOEvent secondEvent = i.next();
        checkEventData(firstEvent, null, null, null, "eventId1");
        checkEventData(secondEvent, "25.00", "25.00", String.valueOf(1L),
                "eventId2");
        validateSteppedPrices(firstEvent);
    }

    private void checkEventData(RDOEvent ed, String amount, String basePrice,
            String numOfOccurrence, String id) {
        Assert.assertNotNull(ed);
        Assert.assertEquals(amount, ed.getPrice());
        Assert.assertEquals(basePrice, ed.getBasePrice());
        Assert.assertEquals(numOfOccurrence, ed.getNumberOfOccurences());
        Assert.assertEquals(id, ed.getId());

    }

    private void validateSteppedPrices(RDOEvent event) {
        List<RDOSteppedPrice> steppedPrices = event.getSteppedPrices();
        Assert.assertNotNull(steppedPrices);
        Assert.assertEquals(3, steppedPrices.size());

        Iterator<RDOSteppedPrice> i = steppedPrices.iterator();
        RDOSteppedPrice steppedPrice = i.next();

        Assert.assertEquals("100.00", steppedPrice.getBasePrice());
        Assert.assertEquals("10", steppedPrice.getLimit());
        Assert.assertEquals("10", steppedPrice.getFactor());

        steppedPrice = i.next();
        Assert.assertEquals("90.00", steppedPrice.getBasePrice());
        Assert.assertEquals("20", steppedPrice.getLimit());
        Assert.assertEquals("10", steppedPrice.getFactor());

        steppedPrice = i.next();
        Assert.assertEquals("80.00", steppedPrice.getBasePrice());
        Assert.assertEquals("null", steppedPrice.getLimit());
        Assert.assertEquals("17", steppedPrice.getFactor());

    }

    private void checkBillingDetailsReportSinglePaymentInfo(RDOSummary summary) {
        assertNotNull(summary);
        assertEquals("TestOrg", summary.getOrganizationName());
        assertEquals("Address of TestOrg", summary.getOrganizationAddress());
        assertEquals("14,681.00", summary.getAmount());
        assertEquals("14,691.00", summary.getGrossAmount());

        RDOPriceModel priceModel = summary.getPriceModel();
        assertEquals("222.00", priceModel.getOneTimeFee());
        assertEquals(millisToString(1262300400000l), priceModel.getStartDate());
        assertEquals(millisToString(1264978800000l), priceModel.getEndDate());

        RDOSubscriptionFees subscrFees = priceModel.getSubscriptionFees();
        assertEquals("MONTH", subscrFees.getBasePeriod());
        assertEquals(
                "RDO must contain the server time zone",
                DateConverter.convertToUTCString(Calendar.getInstance()
                        .getTimeZone().getRawOffset()),
                subscrFees.getServerTimeZone());
        assertEquals(PriceModelType.PRO_RATA.name(),
                subscrFees.getCalculationMode());
        assertEquals("1,000.00", subscrFees.getBasePrice());
        assertEquals("1.00000", subscrFees.getFactor());
        assertEquals("1,000.00", subscrFees.getPrice());

        List<RDOParameter> subscrFeesPars = subscrFees.getParameters();
        for (RDOParameter par : subscrFeesPars) {
            assertNotNull(par.getId());
            if ("CONCURRENT_USER".equals(par.getId())) {
                assertEquals("1", par.getValue());
                assertEquals("1.00", par.getValueFactor());
                assertEquals("1,100.00", par.getBasePrice());
                assertEquals("1.00000", par.getFactor());
                assertEquals("1,100.00", par.getPrice());
            }
        }

        RDOUserFees userFees = priceModel.getUserFees();
        assertEquals("MONTH", userFees.getBasePeriod());
        assertEquals("2.00000", userFees.getFactor());
        assertEquals("666.00", userFees.getNumberOfUsersTotal());
        assertEquals("", userFees.getPrice());
    }

    private RDOParameter getParameter(String name, List<RDOParameter> parameters) {
        RDOParameter param = null;
        for (RDOParameter p : parameters) {
            if (name.equals(p.getId())) {
                param = p;
                break;
            }
        }

        return param;
    }

    @Test
    public void getBillingDetailsReport() throws Exception {
        // given
        
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        mockResultData(XML_FILE_1, "TheServiceId", "TheServiceLocalized");
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        checkBillingReportResult(result, "TheServiceLocalized");
    }

    @Test
    public void getBillingDetailsReport_noLocalizedServiceName()
            throws Exception {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        mockResultData(XML_FILE_1, "TheServiceID", null);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        checkBillingReportResult(result, "TheServiceID");
    }

    @Test
    public void getBillingDetailsReport_nonExistingBillingKey()
            throws Exception {
        // given
        doReturn(new DataSet()).when(dm).executeQueryForRawData(
                any(SqlQuery.class));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, -1);

        // then
        assertNotNull("Result must not be null", result);
        assertEquals("Result must not contain entries", 0, result
                .getSummaries().size());
    }

    @Test
    public void getBillingDetailsReport_invalidUser() throws Exception {
        // given
        session.setPlatformUserKey(INVALID_USER_ID);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        doReturn(null).when(dm).find(eq(PlatformUser.class), anyLong());

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertNotNull("Result must not be null", result);
        assertNull("Result must not contain entries", result.getSummaries());
    }

    @Test
    public void getBillingDetailsReport_unauthorizedAccessAsSupplier()
            throws Exception {
        // given
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));
        doReturn(new DataSet()).when(dm).executeQueryForRawData(
                any(SqlQuery.class));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertNotNull("Result must not be null", result);
        assertEquals("Result must not contain entries", 0, result
                .getSummaries().size());
    }

    @Test
    public void getBillingDetailsReport_unauthorizedAccessAsCustomer()
            throws Exception {
        // given
        roles.add(addOrgToRole(organization, OrganizationRoleType.CUSTOMER));
        doReturn(new DataSet()).when(dm).executeQueryForRawData(
                any(SqlQuery.class));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertNotNull("Result must not be null", result);
        assertEquals("Result must not contain entries", 0, result
                .getSummaries().size());
    }

    /*
     * should work, any organization has customer role
     */
    @Test
    public void getBillingDetailsReport_accessAsTechnologyProvider()
            throws Exception {
        // given
        roles.add(addOrgToRole(organization,
                OrganizationRoleType.TECHNOLOGY_PROVIDER));
        mockResultData(XML_FILE_1);

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertNotNull("Result must not be null", result);
        assertNotNull("Result must contain entries", result.getSummaries());
    }

    @Test
    public void detailedBillingReport_roles() throws Exception {
        // given
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // assert all roles are mapped
        RDOUserFees userFees = result.getSummaries().get(0).getPriceModel()
                .getUserFees();
        assertEquals(3, userFees.getRoles().size());

        // assert role 1
        RDORole role1 = userFees.getRole("role1");
        assertEquals("role1", role1.getRoleId());
        assertEquals("1.00", role1.getBasePrice());
        assertEquals("10.00", role1.getPrice());
        assertEquals("10.00000", role1.getFactor());

        // assert parameters of role 1
        assertEquals(2, role1.getParameters().size());
        RDOParameter param = role1.getParameter("CONCURRENT_USER");
        assertEquals("1.00", param.getBasePrice());
        assertEquals("10.00000", param.getFactor());
        assertEquals("10.00", param.getPrice());
        assertEquals("1", param.getValue());
        assertEquals("1.00", param.getValueFactor());
        assertEquals(0, param.getOptions().size());

        param = role1.getParameter("OPTIONS_WITH_ROLES");
        assertEquals(1, param.getOptions().size());
        RDOOption option = param.getOptions().get(0);
        assertEquals("1.00", option.getBasePrice());
        assertEquals("10.00000", option.getFactor());
        assertEquals("10.00", option.getPrice());
        assertEquals("1", option.getValue());
    }

    @Test
    public void detailedBillingReport_Roles2() throws Exception {
        // given
        mockResultData(XML_FILE_UPGRADE);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        List<RDORole> roles = result.getSummaries().get(0).getPriceModel()
                .getUserFees().getRoles();
        assertEquals(2, roles.size());

        // USER role
        assertEquals("USER", roles.get(0).getRoleId());
        assertEquals("", roles.get(0).getBasePrice());
        assertEquals("", roles.get(0).getFactor());
        assertNull(roles.get(0).getPrice());
        assertEquals(4, roles.get(0).getParameters().size());

        // USER role - parameter 1
        RDOParameter parameter = roles.get(0).getParameters().get(0);
        assertEquals("CONCURRENT_USER", parameter.getId());
        assertEquals("0.20", parameter.getBasePrice());
        assertEquals("398.09486", String.valueOf(parameter.getFactor()));
        assertEquals("79.62", parameter.getPrice());
        assertEquals("4", parameter.getValue());
        assertEquals("4.00", parameter.getValueFactor());
        assertEquals(0, parameter.getSteppedPrices().size());
        assertEquals(0, parameter.getOptions().size());

        // USER role - parameter 2
        parameter = roles.get(0).getParameters().get(1);
        assertEquals("NAMED_USER", parameter.getId());
        assertEquals("0.10", parameter.getBasePrice());
        assertEquals("398.09486", String.valueOf(parameter.getFactor()));
        assertEquals("39.81", parameter.getPrice());
        assertEquals("4", parameter.getValue());
        assertEquals("4.00", parameter.getValueFactor());
        assertEquals(0, parameter.getSteppedPrices().size());
        assertEquals(0, parameter.getOptions().size());

        // USER role - parameter 3
        parameter = roles.get(0).getParameters().get(2);
        assertEquals("PERIOD", parameter.getId());
        assertEquals("0.05", parameter.getBasePrice());
        assertEquals("398.09486", String.valueOf(parameter.getFactor()));
        assertEquals("19.90", parameter.getPrice());
        assertEquals("345600000", parameter.getValue());
        assertEquals("4.00", parameter.getValueFactor());
        assertEquals(0, parameter.getSteppedPrices().size());
        assertEquals(0, parameter.getOptions().size());

        // USER role - parameter 4
        parameter = roles.get(0).getParameters().get(3);
        assertEquals("DISK_SPACE", parameter.getId());
        assertEquals("", parameter.getBasePrice());
        assertEquals("", String.valueOf(parameter.getFactor()));
        assertEquals("", parameter.getPrice());
        assertEquals("", parameter.getValue());
        assertEquals("", parameter.getValueFactor());
        assertEquals(0, parameter.getSteppedPrices().size());
        assertEquals(1, parameter.getOptions().size());
        RDOOption option = parameter.getOptions().get(0);
        assertEquals("50.00", option.getBasePrice());
        assertEquals("398.09486", String.valueOf(option.getFactor()));
        assertEquals("19,904.74", option.getPrice());
        assertEquals("3", option.getValue());

        // GUEST role
        assertEquals("GUEST", roles.get(1).getRoleId());
        assertEquals("", roles.get(1).getBasePrice());
        assertEquals("", roles.get(1).getFactor());
        assertNull(roles.get(1).getPrice());
        assertEquals(4, roles.get(0).getParameters().size());

        // GUEST role - parameter 1
        parameter = roles.get(1).getParameters().get(0);
        assertEquals("CONCURRENT_USER", parameter.getId());
        assertEquals("0.30", parameter.getBasePrice());
        assertEquals("398.09486", String.valueOf(parameter.getFactor()));
        assertEquals("119.43", parameter.getPrice());
        assertEquals("4", parameter.getValue());
        assertEquals("4.00", parameter.getValueFactor());
        assertEquals(0, parameter.getSteppedPrices().size());
        assertEquals(0, parameter.getOptions().size());

        // GUEST role - parameter 2
        parameter = roles.get(1).getParameters().get(1);
        assertEquals("NAMED_USER", parameter.getId());
        assertEquals("0.30", parameter.getBasePrice());
        assertEquals("398.09486", String.valueOf(parameter.getFactor()));
        assertEquals("119.43", parameter.getPrice());
        assertEquals("4", parameter.getValue());
        assertEquals("4.00", parameter.getValueFactor());
        assertEquals(0, parameter.getSteppedPrices().size());
        assertEquals(0, parameter.getOptions().size());

        // USER role - parameter 3
        parameter = roles.get(1).getParameters().get(2);
        assertEquals("PERIOD", parameter.getId());
        assertEquals("0.30", parameter.getBasePrice());
        assertEquals("398.09486", String.valueOf(parameter.getFactor()));
        assertEquals("119.43", parameter.getPrice());
        assertEquals("345600000", parameter.getValue());
        assertEquals("4.00", parameter.getValueFactor());
        assertEquals(0, parameter.getSteppedPrices().size());
        assertEquals(0, parameter.getOptions().size());

        // USER role - parameter 4
        parameter = roles.get(1).getParameters().get(3);
        assertEquals("DISK_SPACE", parameter.getId());
        assertEquals("", parameter.getBasePrice());
        assertEquals("", String.valueOf(parameter.getFactor()));
        assertEquals("", parameter.getPrice());
        assertEquals("", parameter.getValue());
        assertEquals("", parameter.getValueFactor());
        assertEquals(0, parameter.getSteppedPrices().size());
        assertEquals(1, parameter.getOptions().size());
        option = parameter.getOptions().get(0);
        assertEquals("100.20", option.getBasePrice());
        assertEquals("398.09486", String.valueOf(option.getFactor()));
        assertEquals("39,889.11", option.getPrice());
        assertEquals("3", option.getValue());
    }

    @Test
    public void detailedBillingReport_oneTimeFee() throws Exception {
        // given
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertEquals("222.00", result.getSummaries().get(0).getPriceModel()
                .getOneTimeFee());
    }

    @Test
    public void detailedBillingReport_singlePaymentInformation()
            throws Exception {
        // given
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertEquals(1, result.getSummaries().size());
        checkBillingDetailsReportSinglePaymentInfo(result.getSummaries().get(0));
    }

    @Test
    public void detailedBillingReport_emptyBasePriceInCaseOfSteppedPrice()
            throws Exception {
        // given
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel priceModel = result.getSummaries().get(0).getPriceModel();
        RDOUserFees userFees = priceModel.getUserFees();
        RDOParameter param = priceModel.getSubscriptionFees().getParameters()
                .get(4);
        assertEquals("", userFees.getBasePrice());
        assertEquals("", param.getBasePrice());
    }

    @Test
    public void detailedBillingReport_multiplePaymentInformation()
            throws Exception {
        // given
        mockResultData(XML_FILE_1);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertEquals(1, result.getSummaries().size());
        assertNotNull(result.getSummaries().get(0).getPriceModel());
        assertEquals("444.00", result.getSummaries().get(0).getPriceModel()
                .getOneTimeFee());
    }

    @Test
    public void detailedBillingReport_subscriptionFeeOptions() throws Exception {
        // given
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        RDOParameter param = getParameter("OPTIONS", pm.getSubscriptionFees()
                .getParameters());
        assertNotNull(
                "Billing result should have a parameter with id = OPTIONS",
                param);
        assertEquals("111.00", param.getOptions().get(0).getBasePrice());
        assertEquals("0.00025", param.getOptions().get(0).getFactor());
        assertEquals("0.03", param.getOptions().get(0).getPrice());
        assertEquals("ParamOption1", param.getOptions().get(0).getValue());
    }

    @Test
    public void detailedBillingReport_userFeeOptions() throws Exception {
        // given
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        RDOParameter param = getParameter("OPTIONS", pm.getUserFees()
                .getParameters());
        assertNotNull(
                "Billing result should have a parameter with id = OPTIONS",
                param);
        assertEquals("111.00", param.getOptions().get(0).getBasePrice());
        assertEquals("0.00025", param.getOptions().get(0).getFactor());
        assertEquals("0.03", param.getOptions().get(0).getPrice());
        assertEquals("ParamOption1", param.getOptions().get(0).getValue());
    }

    @Test
    public void detailedBillingReport_eventSummary() throws Exception {
        // given
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        assertEquals("3,385.00", pm.getEventFees().getSubtotalAmount());
    }

    @Test
    public void detailedBillingReport_steppedPricesOfUserFees()
            throws Exception {
        // given
        mockResultData(XML_FILE_STEPPEDPRICES);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        List<RDOSteppedPrice> steppedPrices = pm.getUserFees()
                .getSteppedPrices();
        assertEquals(1, steppedPrices.size());
        Iterator<RDOSteppedPrice> i = steppedPrices.iterator();
        RDOSteppedPrice steppedPrice = i.next();
        assertEquals("100.00", steppedPrice.getBasePrice());
        assertEquals("10", steppedPrice.getLimit());
        assertEquals("2.00000", steppedPrice.getFactor());
        assertEquals("200.00", steppedPrice.getPrice());
    }

    @Test
    public void detailedBillingReport_steppedPricesOfSubscriptionParameters()
            throws Exception {
        // given
        mockResultData(XML_FILE_STEPPEDPRICES);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        RDOParameter parameter = pm.getSubscriptionFees().getParameters()
                .get(0);
        List<RDOSteppedPrice> steppedPrices = parameter.getSteppedPrices();

        assertEquals("", parameter.getBasePrice());
        assertEquals("", parameter.getPrice());
        assertEquals("", parameter.getValue());
        assertEquals("", parameter.getValueFactor());

        assertEquals(1, steppedPrices.size());
        Iterator<RDOSteppedPrice> i = steppedPrices.iterator();
        RDOSteppedPrice steppedPrice = i.next();
        assertEquals("100.00", steppedPrice.getBasePrice());
        assertEquals("10", steppedPrice.getLimit());
        assertEquals("10", steppedPrice.getQuantity());
        assertEquals("1.00000", steppedPrice.getFactor());
        assertEquals("1,000.00", steppedPrice.getPrice());
    }

    @Test
    public void detailedBillingReport_steppedPricesOfEvents() throws Exception {
        // given
        mockResultData(XML_FILE_STEPPEDPRICES);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        RDOEvent event = pm.getEventFees().getEvent("eventId1");
        assertNull(event.getBasePrice());
        assertNull(event.getPrice());
        assertNull(event.getNumberOfOccurences());

        List<RDOSteppedPrice> steppedPrices = event.getSteppedPrices();
        assertEquals(3, steppedPrices.size());

        Iterator<RDOSteppedPrice> i = steppedPrices.iterator();
        RDOSteppedPrice steppedPrice = i.next();
        assertEquals("100.00", steppedPrice.getBasePrice());
        assertEquals("10", steppedPrice.getLimit());
        assertEquals("10", steppedPrice.getFactor());
        assertEquals("1,000.00", steppedPrice.getPrice());

        steppedPrice = i.next();
        assertEquals("90.00", steppedPrice.getBasePrice());
        assertEquals("20", steppedPrice.getLimit());
        assertEquals("10", steppedPrice.getFactor());
        assertEquals("900.00", steppedPrice.getPrice());

        steppedPrice = i.next();
        assertEquals("80.00", steppedPrice.getBasePrice());
        assertEquals("null", steppedPrice.getLimit());
        assertEquals("17", steppedPrice.getFactor());
        assertEquals("1,360.00", steppedPrice.getPrice());
    }

    @Test
    public void detailedBillingReport_optionsAtSubscriptionFeeParameter()
            throws Exception {
        // given
        mockResultData(XML_FILE_OPTIONS);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        List<RDOParameter> subscriptionParams = pm.getSubscriptionFees()
                .getParameters();
        assertEquals(3, subscriptionParams.size());

        // parameter 1 - one option
        Iterator<RDOParameter> iterator = subscriptionParams.iterator();
        RDOParameter param = iterator.next();
        assertEquals(1, param.getOptions().size());
        RDOOption option = param.getOptions().get(0);
        assertEquals("111.00", option.getBasePrice());
        assertEquals("0.00025", option.getFactor());
        assertEquals("0.03", option.getPrice());
        assertEquals("ParamOption1", option.getValue());

        // parameter 2 - one option
        param = iterator.next();
        assertEquals(1, param.getOptions().size());
        option = param.getOptions().get(0);
        assertEquals("111.00", option.getBasePrice());
        assertEquals("2.50000", option.getFactor());
        assertEquals("0.03", option.getPrice());
        assertEquals("EnumValue1", option.getValue());

        // parameter 3 - 2 options
        param = iterator.next();
        assertEquals(2, param.getOptions().size());

        option = param.getOptions().get(0);
        assertEquals("1", option.getValue());
        assertEquals("1,400.00", option.getBasePrice());
        assertEquals("1.00000", option.getFactor());
        assertEquals("1,400.00", option.getPrice());

        option = param.getOptions().get(1);
        assertEquals("2", option.getValue());
        assertEquals("700.00", option.getBasePrice());
        assertEquals("2.00000", option.getFactor());
        assertEquals("1,400.00", option.getPrice());
    }

    @Test
    public void detailedBillingReport_optionsAtUserFeeParameter()
            throws Exception {
        // given
        mockResultData(XML_FILE_OPTIONS);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        List<RDOParameter> params = pm.getUserFees().getParameters();
        assertEquals(3, params.size());

        // parameter 1
        Iterator<RDOParameter> iterator = params.iterator();
        RDOParameter param = iterator.next();
        assertEquals(1, param.getOptions().size());
        RDOOption option = param.getOptions().get(0);
        assertEquals("111.00", option.getBasePrice());
        assertEquals("0.00025", option.getFactor());
        assertEquals("0.03", option.getPrice());
        assertEquals("ParamOption1", option.getValue());

        // parameter 2
        param = iterator.next();
        assertEquals(1, param.getOptions().size());
        option = param.getOptions().get(0);
        assertEquals("111.00", option.getBasePrice());
        assertEquals("2.50000", option.getFactor());
        assertEquals("0.03", option.getPrice());
        assertEquals("EnumValue1", option.getValue());

        // parameter 3
        param = iterator.next();
        // Option "1" of parameter OPTIONS_WITH_ROLES has a factor "0.0"
        // in the UserAssignmentCosts -> no RDOOption!
        assertEquals(1, param.getOptions().size());

        option = param.getOptions().get(0);
        assertEquals("2", option.getValue());
        assertEquals("400.00", option.getBasePrice());
        assertEquals("2.00000", option.getFactor());
        assertEquals("800.00", option.getPrice());

    }

    @Test
    public void detailedBillingReport_roleAtOption() throws Exception {
        // given
        mockResultData(XML_FILE_OPTIONS);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();
        RDOUserFees userFees = pm.getUserFees();
        List<RDOParameter> parameters = userFees.getParameters();
        assertEquals("Wrong number of parameters", 3, parameters.size());
        List<RDORole> roles = userFees.getRoles();
        assertEquals("Wrong number of roles", 3, roles.size());

        // Check Parameter "OPTIONS_WITH_ROLES"
        RDOParameter optionWithRolesPar = parameters.get(2);
        assertEquals("Wrong parameter name", optionWithRolesPar.getId(),
                "OPTIONS_WITH_ROLES");
        assertEquals("Wrong parameter value", "", optionWithRolesPar.getValue());
        assertEquals("Wrong parameter basePrice", "",
                optionWithRolesPar.getBasePrice());
        assertEquals("Wrong parameter factor", "",
                optionWithRolesPar.getFactor());
        assertEquals("Wrong parameter price", "", optionWithRolesPar.getPrice());

        // Option "1" of parameter OPTIONS_WITH_ROLES has a factor "0.0"
        // in the UserAssignmentCosts -> no RDOOption
        assertEquals("Wrong number of options", 1, optionWithRolesPar
                .getOptions().size());
        RDOOption option = optionWithRolesPar.getOptions().get(0);
        assertEquals("Wrong option name", "2", option.getValue());
        assertEquals("Wrong option basePrice", "400.00", option.getBasePrice());
        assertEquals("Wrong option factor", "2.00000", option.getFactor());
        assertEquals("Wrong option price", "800.00", option.getPrice());

        // Check roles
        // --- role1 ---
        RDORole role = pm.getUserFees().getRoles().get(0);
        assertEquals("role1", role.getRoleId());
        assertEquals("10.00", role.getBasePrice());
        assertEquals("2.00000", String.valueOf(role.getFactor()));
        assertEquals("20.00", role.getPrice());

        assertEquals(1, role.getParameters().size());
        RDOParameter roleParameter = role.getParameters().get(0);
        assertEquals("Wrong parameter name", "OPTIONS_WITH_ROLES",
                roleParameter.getId());
        assertEquals("Wrong parameter value", "", roleParameter.getValue());
        assertEquals("Wrong parameter basePrice", "",
                roleParameter.getBasePrice());
        assertEquals("Wrong parameter factor", "", roleParameter.getFactor());
        assertEquals("Wrong parameter price", "", roleParameter.getPrice());

        assertEquals("Wrong number of options", 2, roleParameter.getOptions()
                .size());
        RDOOption roleOption = roleParameter.getOptions().get(0);
        assertEquals("Wrong option name", "1", roleOption.getValue());
        assertEquals("Wrong option basePrice", "1.00",
                roleOption.getBasePrice());
        assertEquals("Wrong option factor", "10.00000", roleOption.getFactor());
        assertEquals("Wrong option price", "10.00", roleOption.getPrice());
        roleOption = roleParameter.getOptions().get(1);
        assertEquals("Wrong option name", "2", roleOption.getValue());
        assertEquals("Wrong option basePrice", "21.00",
                roleOption.getBasePrice());
        assertEquals("Wrong option factor", "5.00000", roleOption.getFactor());
        assertEquals("Wrong option price", "105.00", roleOption.getPrice());

        // --- role2 ---
        role = pm.getUserFees().getRoles().get(1);
        assertEquals("role2", role.getRoleId());
        assertEquals("20.00", role.getBasePrice());
        assertEquals("2.00000", String.valueOf(role.getFactor()));
        assertEquals("40.00", role.getPrice());

        assertEquals(1, role.getParameters().size());
        roleParameter = role.getParameters().get(0);
        assertEquals("Wrong parameter name", "OPTIONS_WITH_ROLES",
                roleParameter.getId());
        assertEquals("Wrong parameter value", "", roleParameter.getValue());
        assertEquals("Wrong parameter basePrice", "",
                roleParameter.getBasePrice());
        assertEquals("Wrong parameter factor", "", roleParameter.getFactor());
        assertEquals("Wrong parameter price", "", roleParameter.getPrice());

        assertEquals("Wrong number of options", 2, roleParameter.getOptions()
                .size());
        roleOption = roleParameter.getOptions().get(0);
        assertEquals("Wrong option name", "1", roleOption.getValue());
        assertEquals("Wrong option basePrice", "2.00",
                roleOption.getBasePrice());
        assertEquals("Wrong option factor", "10.00000", roleOption.getFactor());
        assertEquals("Wrong option price", "20.00", roleOption.getPrice());
        roleOption = roleParameter.getOptions().get(1);
        assertEquals("Wrong option name", "2", roleOption.getValue());
        assertEquals("Wrong option basePrice", "22.00",
                roleOption.getBasePrice());
        assertEquals("Wrong option factor", "5.00000", roleOption.getFactor());
        assertEquals("Wrong option price", "110.00", roleOption.getPrice());

        // --- role3 ---
        role = pm.getUserFees().getRoles().get(2);
        assertEquals("role3", role.getRoleId());
        assertEquals("30.00", role.getBasePrice());
        assertEquals("2.00000", String.valueOf(role.getFactor()));
        assertEquals("60.00", role.getPrice());

        assertEquals(1, role.getParameters().size());
        roleParameter = role.getParameters().get(0);
        assertEquals("Wrong parameter name", "OPTIONS_WITH_ROLES",
                roleParameter.getId());
        assertEquals("Wrong parameter value", "", roleParameter.getValue());
        assertEquals("Wrong parameter basePrice", "",
                roleParameter.getBasePrice());
        assertEquals("Wrong parameter factor", "", roleParameter.getFactor());
        assertEquals("Wrong parameter price", "", roleParameter.getPrice());

        assertEquals("Wrong number of options", 2, roleParameter.getOptions()
                .size());
        roleOption = roleParameter.getOptions().get(0);
        assertEquals("Wrong option name", "1", roleOption.getValue());
        assertEquals("Wrong option basePrice", "3.00",
                roleOption.getBasePrice());
        assertEquals("Wrong option factor", "10.00000", roleOption.getFactor());
        assertEquals("Wrong option price", "30.00", roleOption.getPrice());
        roleOption = roleParameter.getOptions().get(1);
        assertEquals("Wrong option name", "2", roleOption.getValue());
        assertEquals("Wrong option basePrice", "23.00",
                roleOption.getBasePrice());
        assertEquals("Wrong option factor", "5.00000", roleOption.getFactor());
        assertEquals("Wrong option price", "115.00", roleOption.getPrice());
    }

    @Test
    public void detailedBillingReport_upgrade() throws Exception {
        // given
        mockResultData(XML_FILE_UPGRADE);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertEquals(2, result.getSummaries().size());
        assertNotNull(result.getSummaries().get(0).getPriceModel());
        assertNotNull(result.getSummaries().get(1).getPriceModel());
    }

    @Test
    public void detailedBillingReport_subTotals1() throws Exception {
        // given
        mockResultData(XML_FILE_4);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        // assert - summary one
        RDOSummary rdoSummary = result.getSummaries().get(0);
        assertEquals("sub total of gathered events wrong,", "0.00", rdoSummary
                .getPriceModel().getEventFees().getSubtotalAmount());
        assertEquals("sub total of subscription fees wrong,", "0.86",
                rdoSummary.getPriceModel().getSubscriptionFees()
                        .getSubtotalAmount());
        assertEquals("sub total of user fees wrong,", "0.94", rdoSummary
                .getPriceModel().getUserFees().getSubtotalAmount());

        // assert - summary two
        rdoSummary = result.getSummaries().get(1);
        assertEquals("sub total of gathered events wrong,", "0.00", rdoSummary
                .getPriceModel().getEventFees().getSubtotalAmount());
        assertEquals("sub total of subscription fees wrong,", "0.00",
                rdoSummary.getPriceModel().getSubscriptionFees()
                        .getSubtotalAmount());
        assertEquals("sub total of user fees wrong,", "0.00", rdoSummary
                .getPriceModel().getUserFees().getSubtotalAmount());
    }

    @Test
    public void detailedBillingReport_subTotals2() throws Exception {
        // given
        mockResultData(XML_FILE_UPGRADE);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        // assert - summary one
        RDOSummary rdoSummary = result.getSummaries().get(0);
        assertEquals("sub total of gathered events wrong,", "10.02", rdoSummary
                .getPriceModel().getEventFees().getSubtotalAmount());
        assertEquals("sub total of subscription fees wrong,", "24,761.50",
                rdoSummary.getPriceModel().getSubscriptionFees()
                        .getSubtotalAmount());
        assertEquals("sub total of user fees wrong,", "148,370.06", rdoSummary
                .getPriceModel().getUserFees().getSubtotalAmount());

        // assert - summary two
        rdoSummary = result.getSummaries().get(1);
        assertEquals("sub total of gathered events wrong,", "15.00", rdoSummary
                .getPriceModel().getEventFees().getSubtotalAmount());
        assertEquals("sub total of subscription fees wrong,", "5,751.57",
                rdoSummary.getPriceModel().getSubscriptionFees()
                        .getSubtotalAmount());
        assertEquals("sub total of user fees wrong,", "4,205.41", rdoSummary
                .getPriceModel().getUserFees().getSubtotalAmount());
    }

    @Test
    public void detailedBillingReport_userFeeSteppedPricesFactor()
            throws Exception {
        // given
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel priceModel = result.getSummaries().get(0).getPriceModel();
        assertEquals("2.00000", priceModel.getUserFees().getSteppedPrices()
                .get(0).getFactor());
    }

    @Test
    public void detailedBillingReport_parameterSteppedPricesFactor()
            throws Exception {
        // given
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel priceModel = result.getSummaries().get(0).getPriceModel();
        assertEquals("1.00000", priceModel.getSubscriptionFees()
                .getParameters().get(4).getSteppedPrices().get(0).getFactor());
        assertEquals("10", priceModel.getSubscriptionFees().getParameters()
                .get(4).getSteppedPrices().get(0).getQuantity());
    }

    @Test
    public void detailedBillingReport_priceModelCosts() throws Exception {
        // given
        mockResultData(XML_FILE_UPGRADE);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertEquals("Number of summaries not correct", 2, result
                .getSummaries().size());
        RDOSummary summary = result.getSummaries().get(0);
        RDOPriceModel priceModel = summary.getPriceModel();
        assertEquals("Price model ID wrong", "11001", priceModel.getId());
        assertEquals("Total price model costs wrong", "173,141.58",
                priceModel.getCosts());
        assertEquals("Price model currency wrong", "EUR",
                priceModel.getCurrency());

        summary = result.getSummaries().get(1);
        priceModel = summary.getPriceModel();
        assertEquals("Price model ID wrong", "10001", priceModel.getId());
        assertEquals("Total price model costs wrong", "10,271.98",
                priceModel.getCosts());
        assertEquals("Price model currency wrong", "EUR",
                priceModel.getCurrency());
    }

    @Test
    public void detailedBillingReport_entryNumbers() throws Exception {
        // given
        mockResultData(XML_FILE_UPGRADE);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        assertEquals("Number of summaries not correct", 2, result
                .getSummaries().size());

        Integer summaryEntryNr = null;
        for (RDOSummary summary : result.getSummaries()) {
            if (summaryEntryNr != null) {
                assertTrue("Summary entry numbers must be different",
                        summaryEntryNr != new Integer(summary.getEntryNr()));
            }
            summaryEntryNr = new Integer(summary.getEntryNr());

            RDOPriceModel priceModel = summary.getPriceModel();
            assertEquals("PriceModel parentnr wrong", summary.getEntryNr(),
                    priceModel.getParentEntryNr());

            RDOEventFees eventFees = priceModel.getEventFees();
            RDOUserFees userFees = priceModel.getUserFees();
            RDOSubscriptionFees subscriptionFees = priceModel
                    .getSubscriptionFees();

            assertEquals("EventsFees parentnr wrong", priceModel.getEntryNr(),
                    eventFees.getParentEntryNr());
            assertEquals("UserFees parentnr wrong", priceModel.getEntryNr(),
                    userFees.getParentEntryNr());
            assertEquals("SubscriptionFees parentnr wrong",
                    priceModel.getEntryNr(),
                    subscriptionFees.getParentEntryNr());

            assertTrue("Entry numbers must be different",
                    eventFees.getEntryNr() != userFees.getEntryNr());
            assertTrue("Entry numbers must be different",
                    eventFees.getEntryNr() != subscriptionFees.getEntryNr());
            assertTrue("Entry numbers must be different",
                    subscriptionFees.getEntryNr() != userFees.getEntryNr());

            Integer eventEntryNr = null;
            for (RDOEvent event : eventFees.getEvents()) {
                if (eventEntryNr != null) {
                    assertTrue("Event entry numbers must be different",
                            eventEntryNr.intValue() != event.getEntryNr());
                }
                eventEntryNr = new Integer(event.getEntryNr());

                assertEquals("Event parentnr wrong", eventFees.getEntryNr(),
                        event.getParentEntryNr());

                Integer steppedPriceEntryNr = null;
                for (RDOSteppedPrice steppedPrice : event.getSteppedPrices()) {
                    if (steppedPriceEntryNr != null) {
                        assertTrue(
                                "Event entry numbers must be different",
                                steppedPriceEntryNr != new Integer(steppedPrice
                                        .getEntryNr()));
                    }
                    steppedPriceEntryNr = new Integer(steppedPrice.getEntryNr());

                    assertEquals("Stepped price parentnr wrong",
                            event.getEntryNr(), steppedPrice.getParentEntryNr());
                }
            }

            Integer subFeesParEntryNr = null;
            for (RDOParameter parameter : subscriptionFees.getParameters()) {
                if (subFeesParEntryNr != null) {
                    assertTrue(
                            "Event entry numbers must be different",
                            subFeesParEntryNr != new Integer(parameter
                                    .getEntryNr()));
                }
                subFeesParEntryNr = new Integer(parameter.getEntryNr());

                assertEquals("Parameter parentnr wrong",
                        subscriptionFees.getEntryNr(),
                        parameter.getParentEntryNr());

                Integer steppedPriceEntryNr = null;
                for (RDOSteppedPrice steppedPrice : parameter
                        .getSteppedPrices()) {
                    if (steppedPriceEntryNr != null) {
                        assertTrue(
                                "Event entry numbers must be different",
                                steppedPriceEntryNr != new Integer(steppedPrice
                                        .getEntryNr()));
                    }
                    steppedPriceEntryNr = new Integer(steppedPrice.getEntryNr());

                    assertEquals("Stepped price parentnr wrong",
                            parameter.getEntryNr(),
                            steppedPrice.getParentEntryNr());
                }

                Integer optionEntryNr = null;
                for (RDOOption option : parameter.getOptions()) {
                    if (optionEntryNr != null) {
                        assertTrue(
                                "Event entry numbers must be different",
                                optionEntryNr != new Integer(option
                                        .getEntryNr()));
                    }
                    optionEntryNr = new Integer(option.getEntryNr());

                    assertEquals("Stepped price parentnr wrong",
                            parameter.getEntryNr(), option.getParentEntryNr());
                }
            }

            Integer steppedPriceEntryNr = null;
            for (RDOSteppedPrice steppedPrice : userFees.getSteppedPrices()) {
                if (steppedPriceEntryNr != null) {
                    assertTrue(
                            "Event entry numbers must be different",
                            steppedPriceEntryNr != new Integer(steppedPrice
                                    .getEntryNr()));
                }
                steppedPriceEntryNr = new Integer(steppedPrice.getEntryNr());

                assertEquals("Stepped price parentnr wrong",
                        userFees.getEntryNr(), steppedPrice.getParentEntryNr());
            }

            Integer roleEntryNr = null;
            for (RDORole role : userFees.getRoles()) {
                if (roleEntryNr != null) {
                    assertTrue("Event entry numbers must be different",
                            roleEntryNr != new Integer(role.getEntryNr()));
                }
                roleEntryNr = new Integer(role.getEntryNr());

                assertEquals("Stepped price parentnr wrong",
                        userFees.getEntryNr(), role.getParentEntryNr());

                Integer roleParEntryNr = null;
                for (RDOParameter parameter : role.getParameters()) {
                    if (roleParEntryNr != null) {
                        assertTrue(
                                "Event entry numbers must be different",
                                roleParEntryNr != new Integer(parameter
                                        .getEntryNr()));
                    }
                    roleParEntryNr = new Integer(parameter.getEntryNr());

                    assertEquals("Parameter parentnr wrong", role.getEntryNr(),
                            parameter.getParentEntryNr());

                    Integer optionEntryNr = null;
                    for (RDOOption option : parameter.getOptions()) {
                        if (optionEntryNr != null) {
                            assertTrue(
                                    "Event entry numbers must be different",
                                    optionEntryNr != new Integer(option
                                            .getEntryNr()));
                        }
                        optionEntryNr = new Integer(option.getEntryNr());

                        assertEquals("Stepped price parentnr wrong",
                                parameter.getEntryNr(),
                                option.getParentEntryNr());
                    }
                }
            }

            Integer userFeesParEntryNr = null;
            for (RDOParameter parameter : userFees.getParameters()) {
                if (userFeesParEntryNr != null) {
                    assertTrue(
                            "Event entry numbers must be different",
                            userFeesParEntryNr != new Integer(parameter
                                    .getEntryNr()));
                }
                userFeesParEntryNr = new Integer(parameter.getEntryNr());

                assertEquals("Parameter parentnr wrong", userFees.getEntryNr(),
                        parameter.getParentEntryNr());

                Integer optionEntryNr = null;
                for (RDOOption option : parameter.getOptions()) {
                    if (optionEntryNr != null) {
                        assertTrue(
                                "Event entry numbers must be different",
                                optionEntryNr != new Integer(option
                                        .getEntryNr()));
                    }
                    optionEntryNr = new Integer(option.getEntryNr());

                    assertEquals("Stepped price parentnr wrong",
                            parameter.getEntryNr(), option.getParentEntryNr());
                }
            }
        }
    }

    @Test
    public void detailedBillingReport_sums() throws Exception {
        // given
        mockResultData(XML_FILE_UPGRADE);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        PriceConverter parser = new PriceConverter(Locale.ENGLISH);
        assertEquals("Number of summaries not correct", 2, result
                .getSummaries().size());
        for (RDOSummary summary : result.getSummaries()) {
            RDOPriceModel priceModel = summary.getPriceModel();
            assertEquals("Wrong summary total amount", "183,413.56",
                    summary.getAmount());

            RDOEventFees eventFees = priceModel.getEventFees();
            RDOUserFees userFees = priceModel.getUserFees();
            RDOSubscriptionFees subscriptionFees = priceModel
                    .getSubscriptionFees();

            assertEquals(
                    "Wrong price model total amount",
                    parser.parse(priceModel.getCosts()),
                    parser.parse(priceModel.getOneTimeFee())
                            .add(parser.parse(eventFees.getSubtotalAmount()))
                            .add(parser.parse(userFees.getSubtotalAmount()))
                            .add(parser.parse(

                            subscriptionFees.getSubtotalAmount())));
        }
    }

    @Test
    public void detailedBillingReport_noSteppedPricesParameterAttributes()
            throws Exception {
        // given
        mockResultData(XML_FILE_2);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        List<RDOParameter> parameters = result.getSummaries().get(0)
                .getPriceModel().getSubscriptionFees().getParameters();
        assertEquals("0.00787", String.valueOf(parameters.get(0).getFactor()));
        assertEquals("1.00", parameters.get(0).getPrice());
        assertEquals("200", parameters.get(0).getValue());
    }

    @Test
    public void detailedBillingReport_perTimeUnit() throws Exception {
        // given
        mockResultData(XML_PER_TIMEUNIT);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();

        // event
        List<RDOSteppedPrice> steppedPricesForEvent = pm.getEventFees()
                .getEvents().get(0).getSteppedPrices();
        assertEquals("10", steppedPricesForEvent.get(0).getFactor());
        assertEquals("10", steppedPricesForEvent.get(1).getFactor());
        assertEquals("17", steppedPricesForEvent.get(2).getFactor());

        // subscription
        RDOSubscriptionFees subscriptionFees = pm.getSubscriptionFees();
        assertEquals("1.00000", subscriptionFees.getFactor());

        RDOParameter paramForSubscription1 = subscriptionFees.getParameters()
                .get(0);
        assertEquals("CONCURRENT_USER", paramForSubscription1.getId());
        assertEquals("1.00000", paramForSubscription1.getFactor());

        RDOParameter paramForSubscription2 = subscriptionFees.getParameters()
                .get(1);
        assertEquals("OPTIONS", paramForSubscription2.getId());
        assertEquals("", paramForSubscription2.getFactor());
        assertEquals("1.00000", paramForSubscription2.getOptions().get(0)
                .getFactor());

        RDOParameter paramForSubscription3 = subscriptionFees.getParameters()
                .get(4);
        assertEquals("ParameterWithSteppedPrices",
                paramForSubscription3.getId());
        assertEquals("", paramForSubscription3.getFactor());
        assertEquals("1.00000", paramForSubscription3.getSteppedPrices().get(0)
                .getFactor());
        assertEquals("10", paramForSubscription3.getSteppedPrices().get(0)
                .getQuantity());

        // user and role
        RDOUserFees userFees = pm.getUserFees();
        assertEquals("2.00000", userFees.getFactor());

        RDORole userRole1 = userFees.getRole("role1");
        assertEquals("10.00000", userRole1.getFactor());

        List<RDOSteppedPrice> steppedPricesForUser = userFees
                .getSteppedPrices();
        assertEquals("2.00000", steppedPricesForUser.get(0).getFactor());
        assertEquals("", steppedPricesForUser.get(0).getQuantity());

        RDOParameter paramForUserRole = userRole1
                .getParameter("CONCURRENT_USER");
        assertEquals("10.00000", paramForUserRole.getFactor());
    }

    @Test
    public void detailedBillingReport_noCalculationMode() throws Exception {
        // given
        mockResultData(XML_NO_CALCULATIONMODE, "TheServiceId",
                "TheServiceLocalized");
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOPriceModel pm = result.getSummaries().get(0).getPriceModel();

        // event
        List<RDOSteppedPrice> steppedPricesForEvent = pm.getEventFees()
                .getEvents().get(0).getSteppedPrices();
        assertEquals("10", steppedPricesForEvent.get(0).getFactor());
        assertEquals("10", steppedPricesForEvent.get(1).getFactor());
        assertEquals("17", steppedPricesForEvent.get(2).getFactor());

        // subscription
        RDOSubscriptionFees subscriptionFees = pm.getSubscriptionFees();
        assertEquals("1.00000", subscriptionFees.getFactor());

        RDOParameter paramForSubscription1 = subscriptionFees.getParameters()
                .get(0);
        assertEquals("CONCURRENT_USER", paramForSubscription1.getId());
        assertEquals("1.00000", paramForSubscription1.getFactor());

        RDOParameter paramForSubscription2 = subscriptionFees.getParameters()
                .get(1);
        assertEquals("OPTIONS", paramForSubscription2.getId());
        assertEquals("", paramForSubscription2.getFactor());
        assertEquals("0.00025", paramForSubscription2.getOptions().get(0)
                .getFactor());

        RDOParameter paramForSubscription3 = subscriptionFees.getParameters()
                .get(4);
        assertEquals("ParameterWithSteppedPrices",
                paramForSubscription3.getId());
        assertEquals("", paramForSubscription3.getFactor());
        assertEquals("1.00000", paramForSubscription3.getSteppedPrices().get(0)
                .getFactor());
        assertEquals("10", paramForSubscription3.getSteppedPrices().get(0)
                .getQuantity());

        // user and role
        RDOUserFees userFees = pm.getUserFees();
        assertEquals("2.00000", userFees.getFactor());

        RDORole userRole1 = userFees.getRole("role1");
        assertEquals("10.00000", userRole1.getFactor());

        List<RDOSteppedPrice> steppedPricesForUser = userFees
                .getSteppedPrices();
        assertEquals("2.00000", steppedPricesForUser.get(0).getFactor());
        assertEquals("", steppedPricesForUser.get(0).getQuantity());

        RDOParameter paramForUserRole = userRole1
                .getParameter("CONCURRENT_USER");
        assertEquals("10.00000", paramForUserRole.getFactor());
    }
    
    @Test
    public void detailedBillingReport_hidePaymentInformation()
            throws Exception {
        // given
        doReturn(false).when(cnfgServLocal).isPaymentInfoAvailable();
        mockResultData(XML_FILE_3);
        roles.add(addOrgToRole(organization, OrganizationRoleType.SUPPLIER));

        // when
        RDODetailedBilling result = reporting.getBillingDetailsReport(
                VALID_SESSION_ID, 2);

        // then
        RDOSummary summary = result.getSummaries().get(0);
        assertThat(summary.getOrganizationAddress(), is(EMPTY));
        assertThat(summary.getPaymentType(), is(EMPTY));
    }
}
