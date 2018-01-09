/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 25.04.2013                                                      
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
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;

/**
 * @author Zhou
 * 
 */
public class SubscriptionAuditLogCollector_DeassignUserRoleForServiceTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    private final static String TARGET_USER_ID = "target_user_id";
    private final static String USER_ROLE = "user_role";

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

    private static PlatformUser givenUser() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        return user;
    }

    @Test
    public void deassignUserRoleForService() {
        // given
        Subscription sub = givenSubscription();
        PlatformUser targetUser = givenTargetUser();
        RoleDefinition roleDef = givenRoleDef();

        // when
        deassignUserRoleForService(sub, targetUser, roleDef);

        // then
        verifyLogEntries();
    }

    @Test
    public void deassignUserRoleForService_null() {
        // given
        Subscription sub = givenSubscription();
        PlatformUser targetUser = givenTargetUser();

        // when
        deassignUserRoleForService(sub, targetUser, null);

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);
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
        assertEquals(TARGET_USER_ID,
                logParams.get(AuditLogParameter.TARGET_USER));
        assertEquals(USER_ROLE, logParams.get(AuditLogParameter.USER_ROLE));
    }

    private void deassignUserRoleForService(Subscription sub, PlatformUser usr,
            RoleDefinition roleDef) {
        AuditLogData.clear();
        logCollector.deassignUserRoleForService(dsMock, sub, usr, roleDef);
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

    private PlatformUser givenTargetUser() {
        PlatformUser user = new PlatformUser();
        user.setUserId(TARGET_USER_ID);
        return user;
    }

    private RoleDefinition givenRoleDef() {
        RoleDefinition roleDef = new RoleDefinition();
        roleDef.setRoleId(USER_ROLE);
        return roleDef;
    }

}
