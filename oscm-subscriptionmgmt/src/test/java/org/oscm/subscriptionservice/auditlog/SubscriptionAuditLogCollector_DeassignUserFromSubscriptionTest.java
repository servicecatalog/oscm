/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 26.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.auditlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.auditlog.util.AuditLogData;
import org.oscm.auditlog.util.AuditLogParameter;
import org.oscm.auditlog.util.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;

/**
 * @author Qiu
 * 
 */
public class SubscriptionAuditLogCollector_DeassignUserFromSubscriptionTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static String USER_ID = "user_id";
    private final static String USER_ID1 = "user_id1";
    private final static String USER_ID2 = "user_id2";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";

    private static DataService dsMock;

    private static LocalizerServiceLocal localizerMock;
    private final static String LOCALIZED_RESOURCE = "TEST";
    private static SubscriptionAuditLogCollector logCollector = new SubscriptionAuditLogCollector();

    @BeforeClass
    public static void setup() {
        dsMock = mock(DataService.class);
        when(dsMock.getCurrentUser()).thenReturn(givenUser());

        localizerMock = mock(LocalizerServiceLocal.class);
        when(
                localizerMock.getLocalizedTextFromDatabase(Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.any(LocalizedObjectTypes.class))).thenReturn(
                                LOCALIZED_RESOURCE);
        logCollector.localizer = localizerMock;
    }

    @Test
    public void deassignUserFromSubscription_Null() {

        // given
        Subscription sub = givenSubscription();

        // when
        deassignUserFromSubscription(sub, null);

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);

    }

    @Test
    public void deassignUserFromSubscription_Empty() {

        // given
        Subscription sub = givenSubscription();

        // when
        deassignUserFromSubscription(sub, new ArrayList<UsageLicense>());

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);

    }

    @Test
    public void deassignUserFromSubscription() {

        // given
        Subscription sub = givenSubscription();
        List<UsageLicense> usageLicense = givenUsageLicenses();

        // when
        deassignUserFromSubscription(sub, usageLicense);

        // then
        verifyLogEntries();
    }

    private void verifyLogEntries() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(PRODUCT_ID, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(SUBSCRIPTION_ID,
                logParams.get(AuditLogParameter.SUBSCRIPTION_NAME));
        assertEquals(USER_ID1 + "," + USER_ID2,
                logParams.get(AuditLogParameter.TARGET_USER));
    }

    private static PlatformUser givenUser() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        return user;
    }

    private PlatformUser givenUser1() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID1);
        user.setOrganization(org);
        return user;
    }

    private PlatformUser givenUser2() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID2);
        user.setOrganization(org);
        return user;
    }

    private SubscriptionAuditLogCollector deassignUserFromSubscription(
            Subscription sub, List<UsageLicense> usageLicenses) {
        AuditLogData.clear();
        logCollector.deassignUserFromSubscription(dsMock, sub, usageLicenses);
        return logCollector;
    }

    private Subscription givenSubscription() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setProduct(createProduct());
        return sub;
    }

    private Product createProduct() {
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setTemplate(prod);
        return prod;
    }

    private List<UsageLicense> givenUsageLicenses() {
        List<UsageLicense> usageLicenses = new ArrayList<UsageLicense>();
        UsageLicense usageLicense1 = new UsageLicense();
        usageLicense1.setUser(givenUser1());
        UsageLicense usageLicense2 = new UsageLicense();
        usageLicense2.setUser(givenUser2());
        usageLicenses.add(usageLicense1);
        usageLicenses.add(usageLicense2);
        return usageLicenses;
    }
}
