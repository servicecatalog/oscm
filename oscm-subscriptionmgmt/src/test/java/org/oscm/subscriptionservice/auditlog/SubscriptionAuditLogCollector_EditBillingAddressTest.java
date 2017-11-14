/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-4-24                                                      
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

import org.oscm.auditlog.util.AuditLogData;
import org.oscm.auditlog.util.AuditLogParameter;
import org.oscm.auditlog.util.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;

/**
 * @author Mao
 * 
 */
public class SubscriptionAuditLogCollector_EditBillingAddressTest {

    private final static long BILLING_CONTACT_KEY_1 = 10;
    private final static long BILLING_CONTACT_KEY_2 = 20;
    private final static String BILLING_CONTACT_ID_1 = "billing_contact_id_1";
    private final static String BILLING_CONTACT_ID_2 = "billing_contact_id_2";
    private final static String BILLING_CONTACT_ADDRESS_1 = "billing_contact_address_1";
    private final static String BILLING_CONTACT_ADDRESS_2 = "billing_contact_address_2";

    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static String ORGANIZATION_NAME = "organization_name";
    private final static String EMAIL = "test@bes.com";
    private static final long PRODUCT_KEY = 100;
    private static final String PRODUCT_ID = "product_id";
    private static final long SUBSCRIPTION_KEY = 1;
    private static final String SUBSCRIPTION_ID = "subscription_id";
    private static final String SEPARATOR = "|";

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
    public void editBillingAddress_InsertAction() {

        // given
        Subscription sub = givenSubscriptionWithBillingAddressNull();
        BillingContact bcNew = givenBillingContact1();

        // when
        editBillingAddress(sub, bcNew);

        // then
        verifyLogEntries();
    }

    @Test
    public void editBillingAddress_DeleteAction() {

        // given
        Subscription sub = givenSubscriptionWithBillingAddress1();

        // when
        editBillingAddress(sub, null);

        // then
        verifyLogEntries();
    }

    @Test
    public void editBillingAddress_NoneAction_NullBillingContact() {

        // given
        Subscription sub = givenSubscriptionWithBillingAddressNull();

        // when
        editBillingAddress(sub, null);

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);
    }

    @Test
    public void editBillingAddress_NoneAction_SameBillingContact() {

        // given
        Subscription sub = givenSubscriptionWithBillingAddress1();
        BillingContact bcNew = givenBillingContact1();

        // when
        editBillingAddress(sub, bcNew);

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);
    }

    @Test
    public void editBillingAddress_UpdateAction() {

        // given
        Subscription sub = givenSubscriptionWithBillingAddress2();
        BillingContact bcNew = givenBillingContact1();

        // when
        editBillingAddress(sub, bcNew);

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
        assertEquals(BILLING_CONTACT_ID_1,
                logParams.get(AuditLogParameter.ADDRESS_NAME));

        StringBuilder addressDetails = new StringBuilder();
        addressDetails.append(ORGANIZATION_NAME).append(SEPARATOR)
                .append(EMAIL).append(SEPARATOR)
                .append(BILLING_CONTACT_ADDRESS_1);

        assertEquals(addressDetails.toString(),
                logParams.get(AuditLogParameter.ADDRESS_DETAILS));

    }

    private Subscription givenSubscriptionWithBillingAddressNull() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setProduct(createProduct());
        return sub;
    }

    private Subscription givenSubscriptionWithBillingAddress1() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setProduct(createProduct());
        sub.setBillingContact(givenBillingContact1());
        return sub;
    }

    private Subscription givenSubscriptionWithBillingAddress2() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setProduct(createProduct());
        sub.setBillingContact(givenBillingContact2());
        return sub;
    }

    private Product createProduct() {
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setTemplate(prod);
        return prod;
    }

    private SubscriptionAuditLogCollector editBillingAddress(Subscription sub,
            BillingContact bcNew) {
        AuditLogData.clear();
        logCollector.editBillingAddress(dsMock, sub, bcNew);
        return logCollector;
    }

    private BillingContact givenBillingContact1() {
        BillingContact bc = new BillingContact();
        bc.setKey(BILLING_CONTACT_KEY_1);
        bc.setBillingContactId(BILLING_CONTACT_ID_1);
        bc.setAddress(BILLING_CONTACT_ADDRESS_1);
        bc.setCompanyName(ORGANIZATION_NAME);
        bc.setEmail(EMAIL);
        return bc;
    }

    private BillingContact givenBillingContact2() {
        BillingContact bc = new BillingContact();
        bc.setKey(BILLING_CONTACT_KEY_2);
        bc.setBillingContactId(BILLING_CONTACT_ID_2);
        bc.setAddress(BILLING_CONTACT_ADDRESS_2);
        bc.setCompanyName(ORGANIZATION_NAME);
        bc.setEmail(EMAIL);
        return bc;
    }
}
