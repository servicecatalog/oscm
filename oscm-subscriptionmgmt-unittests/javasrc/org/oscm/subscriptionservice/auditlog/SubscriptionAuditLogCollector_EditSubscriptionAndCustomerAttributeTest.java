/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.04.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.subscriptionservice.auditlog;

import static org.junit.Assert.assertEquals;
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
 * 
 * @author Mao
 * 
 */
public class SubscriptionAuditLogCollector_EditSubscriptionAndCustomerAttributeTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String CUSTOMER_UDA = "CUSTOMER";
    private final static String CUSTOMER_SUBSCRIPTION_UDA = "CUSTOMER_SUBSCRIPTION";
    private final static String EDIT_SUBSCRIPTION_ATTRIBUTE_CONFIGURATION = "EDIT_SUBSCRIPTION_ATTRIBUTE_BY_CUSTOMER";
    private final static String EDIT_CUSTOMER_ATTRIBUTE_CONFIGURATION = "EDIT_CUSTOMER_ATTRIBUTE_BY_CUSTOMER";
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static String ORGANIZATION_NAME = "organization_name";
    private final static String ATTRIBUTE_NAME = "name";
    private final static String ATTRIBUTE_VALUE = "value";

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
    public void editAttribute_CustomerSubscriptionUda() {
        // given
        Subscription sub = givenSubscription();

        // when
        editSubscriptionAndCustomerAttributeConfiguration(null, sub,
                ATTRIBUTE_NAME, ATTRIBUTE_VALUE, CUSTOMER_SUBSCRIPTION_UDA);

        // then
        verifySubscriptionLogEntries(EDIT_SUBSCRIPTION_ATTRIBUTE_CONFIGURATION);
    }

    @Test
    public void editAttribute_CustomerUda() {
        // given
        Subscription sub = givenSubscription();
        Organization customer = givenOrganization();

        // when
        editSubscriptionAndCustomerAttributeConfiguration(customer, sub,
                ATTRIBUTE_NAME, ATTRIBUTE_VALUE, CUSTOMER_UDA);

        // then
        verifyCustomerLogEntries(EDIT_CUSTOMER_ATTRIBUTE_CONFIGURATION);
    }

    private static PlatformUser givenUser() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        return user;
    }

    private Subscription givenSubscription() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setProduct(createProduct());
        return sub;
    }

    private Organization givenOrganization() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        org.setName(ORGANIZATION_NAME);
        return org;
    }

    private Product createProduct() {
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setTemplate(prod);
        return prod;
    }

    private SubscriptionAuditLogCollector editSubscriptionAndCustomerAttributeConfiguration(
            Organization customer, Subscription subscription,
            String attributeName, String attributeValue, String operation) {
        AuditLogData.clear();
        logCollector.editSubscriptionAndCustomerAttributeByCustomer(dsMock,
                customer, subscription, attributeName, attributeValue,
                operation);
        return logCollector;
    }

    private void verifySubscriptionLogEntries(String operationName) {
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
        assertEquals(ATTRIBUTE_NAME,
                logParams.get(AuditLogParameter.ATTRIBUTE_NAME));
        assertEquals(ATTRIBUTE_VALUE,
                logParams.get(AuditLogParameter.ATTRIBUTE_VALUE));
        assertEquals(operationName, logEntry.getOperationName());
    }

    private void verifyCustomerLogEntries(String operationName) {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(1, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(ORGANIZATION_ID,
                logParams.get(AuditLogParameter.CUSTOMER_ID));
        assertEquals(ORGANIZATION_NAME,
                logParams.get(AuditLogParameter.CUSTOMER_NAME));
        assertEquals(ATTRIBUTE_NAME,
                logParams.get(AuditLogParameter.ATTRIBUTE_NAME));
        assertEquals(ATTRIBUTE_VALUE,
                logParams.get(AuditLogParameter.ATTRIBUTE_VALUE));
        assertEquals(operationName, logEntry.getOperationName());
    }
}
