/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 21.04.2013                                                      
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
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;

/**
 * @author stavreva
 * 
 */
public class SubscriptionAuditLogCollector_EditPaymentTypeTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static long PAYMENT_KEY = 10;
    private final static long PAYMENT_KEY_2 = 20;
    private final static String PAYMENT_NAME = "payment_name";
    private final static String PAYMENT_NAME_2 = "payment_name_2";
    private static final String PAYMENT_TYPE = "payment_type";
    private static final String PAYMENT_TYPE_2 = "payment_type_2";
    private final static String USER_ID = "user_id";
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

    private static PlatformUser givenUser() {
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        PlatformUser user = new PlatformUser();
        user.setUserId(USER_ID);
        user.setOrganization(org);
        return user;
    }

    @Test
    public void editPaymentType_InsertAction() {

        // given
        Subscription sub = givenSubscriptionWithPaymentInfoNull();
        PaymentInfo piNew = givenPaymentInfo1();

        // when
        editPaymentType(sub, piNew);

        // then
        verifyLogEntries();
    }

    @Test
    public void editPaymentType_DeleteAction() {

        // given
        Subscription sub = givenSubscriptionWithPaymentInfo1();

        // when
        editPaymentType(sub, null);

        // then
        verifyLogEntries();
    }

    @Test
    public void editPaymentType_NoneAction_NullPaymentTypes() {

        // given
        Subscription sub = givenSubscriptionWithPaymentInfoNull();

        // when
        editPaymentType(sub, null);

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);
    }

    @Test
    public void editPaymentType_NoneAction_SamePaymentTypes() {

        // given
        Subscription sub = givenSubscriptionWithPaymentInfo1();
        PaymentInfo piNew = givenPaymentInfo1();

        // when
        editPaymentType(sub, piNew);

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);
    }

    @Test
    public void editPaymentType_UpdateAction() {

        // given
        Subscription sub = givenSubscriptionWithPaymentInfo2();
        PaymentInfo piNew = givenPaymentInfo1();

        // when
        editPaymentType(sub, piNew);

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
        assertEquals(PAYMENT_TYPE,
                logParams.get(AuditLogParameter.PAYMENT_TYPE));
        assertEquals(PAYMENT_NAME,
                logParams.get(AuditLogParameter.PAYMENT_NAME));
    }

    private SubscriptionAuditLogCollector editPaymentType(Subscription sub,
            PaymentInfo piNew) {
        AuditLogData.clear();
        logCollector.editPaymentType(dsMock, sub, piNew);
        return logCollector;
    }

    private Subscription givenSubscriptionWithPaymentInfo1() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setPaymentInfo(givenPaymentInfo1());
        sub.setProduct(createProduct());
        return sub;
    }

    private Subscription givenSubscriptionWithPaymentInfo2() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setPaymentInfo(givenPaymentInfo2());
        sub.setProduct(createProduct());
        return sub;
    }

    private PaymentInfo givenPaymentInfo1() {
        PaymentInfo pi = new PaymentInfo();
        pi.setKey(PAYMENT_KEY);
        pi.setPaymentInfoId(PAYMENT_NAME);
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PAYMENT_TYPE);
        pi.setPaymentType(pt);
        return pi;
    }

    private PaymentInfo givenPaymentInfo2() {
        PaymentInfo pi = new PaymentInfo();
        pi.setKey(PAYMENT_KEY_2);
        pi.setPaymentInfoId(PAYMENT_NAME_2);
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PAYMENT_TYPE_2);
        pi.setPaymentType(pt);
        return pi;
    }

    private Subscription givenSubscriptionWithPaymentInfoNull() {
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

}
