/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 29, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import org.oscm.converter.DateConverter;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.billing.RDOPaymentPreviewSummary;
import org.oscm.reportingservice.business.model.billing.RDOPriceModel;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * @author tokoda
 * 
 */
public class BillingResultParserPaymentPreviewTest {

    private static final Long INVOCATION_TIME = Long.valueOf(DateTimeHandling
            .calculateMillis("2013-01-28 22:00:00"));

    private BillingDao dao;
    private BillingResultParser parser;
    private PriceConverter formatter;
    private PlatformUser user;

    private final File XML_FILE_PAYMENT_PREVIEW = new File(
            "javares/BillingResultForPaymentPreviewTest.xml");
    private final File XML_FILE_PAYMENT_PREVIEW_2 = new File(
            "javares/BillingResultForPaymentPreviewTest2.xml");

    @Before
    public void setup() {
        dao = mock(BillingDao.class);
        parser = new BillingResultParser(dao);
        formatter = mock(PriceConverter.class);
        when(formatter.getActiveLocale()).thenReturn(Locale.ENGLISH);
    }

    void givenUser(OrganizationRoleType... roles) {
        Organization o = new Organization();
        o.setKey(1L);
        Set<OrganizationToRole> grantedRoles = new HashSet<OrganizationToRole>();
        for (OrganizationRoleType roleType : roles) {
            OrganizationToRole otr = new OrganizationToRole();
            otr.setOrganizationRole(new OrganizationRole(roleType));
            otr.setOrganization(o);
            grantedRoles.add(otr);
        }
        o.setGrantedRoles(grantedRoles);

        user = new PlatformUser();
        user.setKey(10L);
        user.setOrganization(o);
        user.setLocale("en");
    }

    @Test
    public void evaluateBillingResultForPaymentPreview_InvocationBeforeTimeSliceEnd()
            throws Exception {
        // given
        givenUser(OrganizationRoleType.CUSTOMER);
        RDOSummary summaryTemplate = new RDOSummary();
        Document document = XMLConverter.convertToDocument(
                readXmlFromFile(XML_FILE_PAYMENT_PREVIEW), true);

        // when
        List<RDOPaymentPreviewSummary> result = parser
                .evaluateBillingResultForPaymentPreview(summaryTemplate,
                        document, user, formatter, INVOCATION_TIME);

        // then
        RDOPaymentPreviewSummary summary = result.get(0);
        assertEquals("2013-01-28 22:00:00", summary.getPriceModels().get(0)
                .getEndDate());

    }

    @Test
    public void evaluateBillingResultForPaymentPreview_SubscriptionTerminatedBeforeInvocation()
            throws Exception {
        // given
        givenUser(OrganizationRoleType.CUSTOMER);
        RDOSummary summaryTemplate = new RDOSummary();
        Document document = XMLConverter.convertToDocument(
                readXmlFromFile(XML_FILE_PAYMENT_PREVIEW_2), true);

        // when
        List<RDOPaymentPreviewSummary> result = parser
                .evaluateBillingResultForPaymentPreview(summaryTemplate,
                        document, user, formatter, INVOCATION_TIME);

        // then
        RDOPaymentPreviewSummary summary = result.get(0);
        assertEquals(DateConverter.convertLongToDateTimeFormat(1359384117064L,
                Calendar.getInstance().getTimeZone(),
                DateConverter.DTP_WITHOUT_MILLIS), summary.getPriceModels()
                .get(0).getEndDate());
    }

    @Test
    public void convertToPaymentPreviewSummary() {
        // given
        List<RDOSummary> summaries = givenSummaries();

        // when
        RDOPaymentPreviewSummary result = parser
                .convertToPaymentPreviewSummary(summaries);

        // then
        assertEquals(2, result.getPriceModels().size());
        assertEquals(21, result.getEntryNr());
        assertEquals("supplierName", result.getSupplierName());
        assertEquals("address", result.getSupplierAddress());
        assertEquals("orderNumber", result.getPurchaseOrderNumber());
        assertEquals("organizationName", result.getOrganizationName());
        assertEquals("organizationAddress", result.getOrganizationAddress());
        assertEquals("paymentType", result.getPaymentType());
        assertEquals("currency", result.getCurrency());
        assertEquals("discount", result.getDiscount());
        assertEquals("discountAmount", result.getDiscountAmount());
        assertEquals("grossAmount", result.getGrossAmount());
        assertEquals("amount", result.getAmount());
        assertEquals("vat", result.getVat());
        assertEquals("vatAmount", result.getVatAmount());
        assertEquals("netAmount", result.getNetAmountBeforeDiscount());
        assertEquals("subscriptionId", result.getSubscriptionId());
        assertEquals(null, result.getPriceModelStartDate());
        assertEquals(null, result.getPriceModelEndDate());

        assertEquals("id1", result.getPriceModels().get(0).getId());
        assertEquals("id2", result.getPriceModels().get(1).getId());

        assertEquals("startDate1", result.getPriceModels().get(0)
                .getStartDate());
        assertEquals("endDate1", result.getPriceModels().get(0).getEndDate());
        assertEquals("startDate2", result.getPriceModels().get(1)
                .getStartDate());
        assertEquals("endDate2", result.getPriceModels().get(1).getEndDate());

        assertEquals(21, result.getPriceModels().get(0).getParentEntryNr());
        assertEquals(21, result.getPriceModels().get(1).getParentEntryNr());

    }

    private List<RDOSummary> givenSummaries() {
        List<RDOSummary> summaries = new ArrayList<RDOSummary>();
        RDOSummary summary1 = new RDOSummary();
        RDOSummary summary2 = new RDOSummary();

        summary1.setEntryNr(21);
        summary1.setSupplierName("supplierName");
        summary1.setSupplierAddress("address");
        summary1.setPurchaseOrderNumber("orderNumber");
        summary1.setOrganizationName("organizationName");
        summary1.setOrganizationAddress("organizationAddress");
        summary1.setPaymentType("paymentType");
        summary1.setCurrency("currency");
        summary1.setDiscount("discount");
        summary1.setDiscountAmount("discountAmount");
        summary1.setGrossAmount("grossAmount");
        summary1.setAmount("amount");
        summary1.setVat("vat");
        summary1.setVatAmount("vatAmount");
        summary1.setNetAmountBeforeDiscount("netAmount");
        summary1.setSubscriptionId("subscriptionId");
        summary1.setPriceModelStartDate("startDate1");
        summary1.setPriceModelEndDate("endDate1");

        RDOPriceModel priceModel1 = new RDOPriceModel();
        priceModel1.setId("id1");
        summary1.setPriceModel(priceModel1);

        summary2.setPriceModelStartDate("startDate2");
        summary2.setPriceModelEndDate("endDate2");

        RDOPriceModel priceModel2 = new RDOPriceModel();
        priceModel2.setId("id2");
        summary2.setPriceModel(priceModel2);

        summaries.add(summary1);
        summaries.add(summary2);

        return summaries;
    }

    private String readXmlFromFile(File testFile) throws FileNotFoundException,
            InterruptedException, IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(testFile);
            String billingResult = Strings.toString(Streams.readFrom(is));
            return billingResult;
        } finally {
            Streams.close(is);
        }
    }

}
