/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.auditlog;

import static org.oscm.auditlog.util.AuditLogParameter.SERVICE_ID;
import static org.oscm.auditlog.util.AuditLogParameter.SERVICE_NAME;
import static org.oscm.auditlog.util.AuditLogParameter.SUBSCRIPTION_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.auditlog.util.AuditLogData;
import org.oscm.auditlog.util.AuditLogParameter;
import org.oscm.auditlog.util.BESAuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;

@SuppressWarnings("boxing")
public class SubscriptionAuditLogCollector_UnsubscribeFromService_Test {

    private final static Long SUBSCRIPTION_KEY = 1l;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static Long PRODUCT_KEY = 100l;
    private final static String PRODUCT_ID = "product_id";

    private static DataService dsMock;

    private static LocalizerServiceLocal localizerMock;
    private final static String LOCALIZED_RESOURCE = "TEST";
    private static SubscriptionAuditLogCollector logCollector = new SubscriptionAuditLogCollector();

    @BeforeClass
    public static void setup() {
        dsMock = mockDataService();

        localizerMock = mock(LocalizerServiceLocal.class);
        when(
                localizerMock.getLocalizedTextFromDatabase(Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.any(LocalizedObjectTypes.class))).thenReturn(
                                LOCALIZED_RESOURCE);
        logCollector.localizer = localizerMock;
    }

    private static DataService mockDataService() {
        DataService ds = mock(DataService.class);
        PlatformUser user = new PlatformUser();
        user.setUserId("userId");
        Organization organization = new Organization();
        organization.setOrganizationId("orgId");
        organization.setName("organizationName");
        user.setOrganization(organization);
        doReturn(user).when(ds).getCurrentUser();
        return ds;
    }

    @Before
    public void before() {
        AuditLogData.clear();
    }

    @Test
    public void unsubscribeFromService() {
        // given
        Subscription subscription = givenSubscription();
        // when
        logCollector.unsubscribeFromService(dsMock, subscription);

        // then
        assertLogEntries();
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
        return prod;
    }

    private void assertLogEntries() {
        assertEquals(1, AuditLogData.get().size());

        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(PRODUCT_ID, logParams.get(SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(SERVICE_NAME));
        assertEquals(SUBSCRIPTION_ID, logParams.get(SUBSCRIPTION_NAME));
    }
}
