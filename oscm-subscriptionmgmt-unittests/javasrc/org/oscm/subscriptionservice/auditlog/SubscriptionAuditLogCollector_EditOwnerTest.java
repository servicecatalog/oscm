/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 26.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.auditlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.auditlog.AuditLogData;
import org.oscm.auditlog.AuditLogParameter;
import org.oscm.auditlog.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;

/**
 * @author Stavreva
 * 
 */
public class SubscriptionAuditLogCollector_EditOwnerTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    private final static String OWNER_ID = "owner_id";
    private final static String NEW_OWNER_ID = "new_owner_id";

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
    public void ownerNotChanged_Null() {

        // given
        Subscription sub = givenSubscription();

        // when
        ownerNotChanged_Null(sub);

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);
    }

    @Test
    public void ownerNotChanged_NotNull() {

        // given
        Subscription sub = givenSubscription();

        // when
        ownerNotChanged(sub);

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);
    }

    @Test
    public void ownerChanged() {

        // given
        Subscription sub = givenSubscription();

        // when
        ownerChanged(sub);

        // then
        verifyLogEntries(NEW_OWNER_ID);
    }

    @Test
    public void ownerSet() {

        // given
        Subscription sub = givenSubscription();

        // when
        ownerSet(sub);

        // then
        verifyLogEntries(NEW_OWNER_ID);
    }

    @Test
    public void ownerReset() {

        // given
        Subscription sub = givenSubscription();

        // when
        ownerReset(sub);

        // then
        verifyLogEntries("NO_OWNER");
    }

    private void verifyLogEntries(String expectedOwnerId) {
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
        assertEquals(expectedOwnerId,
                logParams.get(AuditLogParameter.SUBSCRIPTION_OWNER));
    }

    private static PlatformUser givenUser() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        return user;
    }

    private SubscriptionAuditLogCollector ownerChanged(Subscription sub) {
        AuditLogData.clear();
        PlatformUser oldOwner = new PlatformUser();
        oldOwner.setUserId(OWNER_ID);
        PlatformUser newOwner = new PlatformUser();
        newOwner.setUserId(NEW_OWNER_ID);
        sub.setOwner(newOwner);
        logCollector.editSubscriptionOwner(dsMock, sub, oldOwner);
        return logCollector;
    }

    private SubscriptionAuditLogCollector ownerReset(Subscription sub) {
        AuditLogData.clear();
        PlatformUser oldOwner = new PlatformUser();
        oldOwner.setUserId(OWNER_ID);
        sub.setOwner(null);
        logCollector.editSubscriptionOwner(dsMock, sub, oldOwner);
        return logCollector;
    }

    private SubscriptionAuditLogCollector ownerSet(Subscription sub) {
        AuditLogData.clear();
        PlatformUser newOwner = new PlatformUser();
        newOwner.setUserId(NEW_OWNER_ID);
        sub.setOwner(newOwner);
        logCollector.editSubscriptionOwner(dsMock, sub, null);
        return logCollector;
    }

    private SubscriptionAuditLogCollector ownerNotChanged(Subscription sub) {
        AuditLogData.clear();
        PlatformUser oldOwner = new PlatformUser();
        oldOwner.setUserId(OWNER_ID);
        sub.setOwner(oldOwner);
        logCollector.editSubscriptionOwner(dsMock, sub, oldOwner);
        return logCollector;
    }

    private SubscriptionAuditLogCollector ownerNotChanged_Null(Subscription sub) {
        AuditLogData.clear();
        sub.setOwner(null);
        logCollector.editSubscriptionOwner(dsMock, sub, null);
        return logCollector;
    }

    private Subscription givenSubscription() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setProduct(createProduct());
        PlatformUser owner = new PlatformUser();
        owner.setUserId(OWNER_ID);
        sub.setOwner(owner);
        return sub;
    }

    private Product createProduct() {
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setTemplate(prod);
        return prod;
    }

}
