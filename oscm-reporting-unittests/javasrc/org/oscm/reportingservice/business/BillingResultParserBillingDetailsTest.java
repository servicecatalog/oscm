/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Aug 29, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
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
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * @author zhou
 * 
 */
public class BillingResultParserBillingDetailsTest {

    private BillingDao dao;
    private BillingResultParser parser;
    private PriceConverter formatter;
    private PlatformUser user;

    private final File XML_FILE_BILLING_DETAILS = new File(
            "javares/BillingResultForBillingDetailsTest.xml");

    @Before
    public void setup() {
        dao = mock(BillingDao.class);
        parser = new BillingResultParser(dao);
        formatter = mock(PriceConverter.class);
        when(formatter.getActiveLocale()).thenReturn(Locale.ENGLISH);
        doReturn("0.00").when(formatter).getValueToDisplay(eq(BigDecimal.ZERO),
                eq(true));

    }

    private void givenUser(OrganizationRoleType... roles) {
        Organization o = new Organization();
        o.setKey(1L);
        Set<OrganizationToRole> grantedRoles = new HashSet<>();
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
    public void evaluateBillingResultForBillingDetails_SubscriptionSuspendedAndActived()
            throws Exception {
        // given
        givenUser(OrganizationRoleType.CUSTOMER);
        RDOSummary summaryTemplate = new RDOSummary();
        Document document = XMLConverter.convertToDocument(
                readXmlFromFile(XML_FILE_BILLING_DETAILS), true);

        // when
        List<RDOSummary> result = parser.evaluateBillingResultForBillingDetails(
                summaryTemplate, document, user, formatter);

        // then
        assertEquals(4, result.size());
        RDOSummary summary1 = result.get(0);
        assertEquals(
                DateConverter.convertLongToDateTimeFormat(1377510545074L,
                        Calendar.getInstance().getTimeZone(),
                        DateConverter.DTP_WITHOUT_MILLIS),
                summary1.getPriceModelStartDate());
        assertEquals(
                DateConverter.convertLongToDateTimeFormat(1377510569900L,
                        Calendar.getInstance().getTimeZone(),
                        DateConverter.DTP_WITHOUT_MILLIS),
                summary1.getPriceModelEndDate());

        assertEquals("0.0", summary1.getDiscount());
        assertEquals("0.00", summary1.getDiscountAmount());

        RDOSummary summary4 = result.get(3);
        assertEquals(
                DateConverter.convertLongToDateTimeFormat(1377676047123L,
                        Calendar.getInstance().getTimeZone(),
                        DateConverter.DTP_WITHOUT_MILLIS),
                summary4.getPriceModelStartDate());
        assertEquals(
                DateConverter.convertLongToDateTimeFormat(1377964800000L,
                        Calendar.getInstance().getTimeZone(),
                        DateConverter.DTP_WITHOUT_MILLIS),
                summary4.getPriceModelEndDate());

    }

    private String readXmlFromFile(File testFile)
            throws InterruptedException, IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(testFile);
            return Strings.toString(Streams.readFrom(is));
        } finally {
            Streams.close(is);
        }
    }

}
