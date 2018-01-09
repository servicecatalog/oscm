/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 24.04.2013                                                      
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.auditlog.AuditLogData;
import org.oscm.auditlog.AuditLogParameter;
import org.oscm.auditlog.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * @author Iversen
 * 
 */
public class SubscriptionAuditLogCollector_EditSubscriptionParameterConfigurationTest {

    private final static long SUBSCRIPTION_KEY = 1;
    private final static String SUBSCRIPTION_ID = "subscription_id";
    private final static String USER_ID = "user_id";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static long PRODUCT_KEY = 100;
    private final static String PRODUCT_ID = "product_id";

    private final static String PARAMETER_VALUE_1 = "true";
    private final static String PARAMETER_ID_1 = "Parameter1";
    private final static long PARAMETER_KEY_1 = 10;
    private final static String PARAMETER_VALUE_2 = "false";
    private final static String PARAMETER_ID_2 = "Parameter2";
    private final static long PARAMETER_KEY_2 = 20;
    private final static String PARAMETER_VALUE_3 = "3";
    private final static String PARAMETER_ID_3 = "Parameter3";
    private final static long PARAMETER_KEY_3 = 30;
    private final static String PARAMETER_VALUE_4 = "3";
    private final static String PARAMETER_ID_4 = "MEMORY_STORAGE";
    private final static long PARAMETER_KEY_4 = 40;
    private final static long PARAMETER_OPTION_KEY4 = 20671;
    private final static String PARAMETER_OPTION_LOCALIZED_RESOURCE_4 = "TEST";
    private final static String LOCALIZED_RESOURCE = "TEST";

    private final static String EXPECTED_PARAMETER_VALUE_1 = "ON";
    private final static String EXPECTED_PARAMETER_VALUE_2 = "OFF";
    private static SubscriptionAuditLogCollector logCollector = new SubscriptionAuditLogCollector();
    private static LocalizerServiceLocal localizerMock;
    private static DataService dsMock;

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

    @Before
    public void clearData() {
        AuditLogData.clear();
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
    public void editSubscriptionParameterConfiguration_NoAction_NoChanges() {
        // given
        Subscription subscription = givenSubscription();

        // when
        addEditSubscriptionParameterConfiguration(subscription,
                new ArrayList<Parameter>());

        // then
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertNull(logEntries);

    }

    @Test
    public void editSubscriptionParameterConfiguration_Insert_Boolean_ON() {
        // given
        Subscription subscription = givenSubscription();

        // when
        addEditSubscriptionParameterConfiguration(subscription, subscription
                .getParameterSet().getParameters());

        // then
        verifyLogEntries1();

    }

    @Test
    public void editSubscriptionParameterConfiguration_Insert_Bollean_OFF() {
        // given
        Subscription subscription = givenSubscription();

        // when
        addEditSubscriptionParameterConfiguration(subscription, subscription
                .getParameterSet().getParameters());

        // then
        verifyLogEntries2();

    }

    @Test
    public void editSubscriptionParameterConfiguration_Insert_AnyString() {
        // given
        Subscription subscription = givenSubscription();

        // when
        addEditSubscriptionParameterConfiguration(subscription, subscription
                .getParameterSet().getParameters());

        // then
        verifyLogEntries3();

    }

    @Test
    public void editSubscriptionParameterConfiguration_Insert_Enumeration() {
        // given
        Subscription subscription = givenSubscription();

        // when
        addEditSubscriptionParameterConfiguration(subscription, subscription
                .getParameterSet().getParameters());

        // then
        verifyLogEntries4();

    }

    private void verifyLogEntries1() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(4, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(0);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(PRODUCT_ID, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(SUBSCRIPTION_ID,
                logParams.get(AuditLogParameter.SUBSCRIPTION_NAME));
        assertEquals(PARAMETER_ID_1,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(EXPECTED_PARAMETER_VALUE_1,
                logParams.get(AuditLogParameter.PARAMETER_VALUE));

    }

    private void verifyLogEntries2() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(4, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(1);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(PRODUCT_ID, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(SUBSCRIPTION_ID,
                logParams.get(AuditLogParameter.SUBSCRIPTION_NAME));
        assertEquals(PARAMETER_ID_2,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(EXPECTED_PARAMETER_VALUE_2,
                logParams.get(AuditLogParameter.PARAMETER_VALUE));

    }

    private void verifyLogEntries3() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(4, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(2);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(PRODUCT_ID, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(SUBSCRIPTION_ID,
                logParams.get(AuditLogParameter.SUBSCRIPTION_NAME));
        assertEquals(PARAMETER_ID_3,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(PARAMETER_VALUE_3,
                logParams.get(AuditLogParameter.PARAMETER_VALUE));

    }

    private void verifyLogEntries4() {
        List<AuditLogEntry> logEntries = AuditLogData.get();
        assertEquals(4, logEntries.size());
        BESAuditLogEntry logEntry = (BESAuditLogEntry) AuditLogData.get()
                .get(3);
        Map<AuditLogParameter, String> logParams = logEntry.getLogParameters();
        assertEquals(PRODUCT_ID, logParams.get(AuditLogParameter.SERVICE_ID));
        assertEquals(LOCALIZED_RESOURCE,
                logParams.get(AuditLogParameter.SERVICE_NAME));
        assertEquals(SUBSCRIPTION_ID,
                logParams.get(AuditLogParameter.SUBSCRIPTION_NAME));
        assertEquals(PARAMETER_ID_4,
                logParams.get(AuditLogParameter.PARAMETER_NAME));
        assertEquals(PARAMETER_OPTION_LOCALIZED_RESOURCE_4,
                logParams.get(AuditLogParameter.PARAMETER_VALUE));

    }

    private SubscriptionAuditLogCollector addEditSubscriptionParameterConfiguration(
            Subscription sub, List<Parameter> modifiedParameters) {

        logCollector.editSubscriptionParameterConfiguration(dsMock,
                sub.getProduct(), modifiedParameters);
        return logCollector;
    }

    private Subscription givenSubscription() {
        Subscription sub = new Subscription();
        sub.setKey(SUBSCRIPTION_KEY);
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setProduct(createProduct(sub));

        return sub;
    }

    private Product createProduct(Subscription sub) {
        Product prod = new Product();
        prod.setKey(PRODUCT_KEY);
        prod.setProductId(PRODUCT_ID);
        prod.setParameterSet(createParameterSet());
        prod.setOwningSubscription(sub);
        return prod;
    }

    private ParameterSet createParameterSet() {
        ParameterSet params = new ParameterSet();
        List<Parameter> paramList = new ArrayList<>();

        Parameter p1 = new Parameter();
        ParameterDefinition pd1 = new ParameterDefinition();
        pd1.setParameterId(PARAMETER_ID_1);
        pd1.setValueType(ParameterValueType.BOOLEAN);
        p1.setParameterDefinition(pd1);
        p1.setValue(PARAMETER_VALUE_1);
        p1.setKey(PARAMETER_KEY_1);
        p1.setParameterSet(params);
        paramList.add(p1);

        Parameter p2 = new Parameter();
        ParameterDefinition pd2 = new ParameterDefinition();
        pd2.setParameterId(PARAMETER_ID_2);
        pd2.setValueType(ParameterValueType.BOOLEAN);
        p2.setParameterDefinition(pd2);
        p2.setValue(PARAMETER_VALUE_2);
        p2.setKey(PARAMETER_KEY_2);
        p2.setParameterSet(params);
        paramList.add(p2);

        Parameter p3 = new Parameter();
        ParameterDefinition pd3 = new ParameterDefinition();
        pd3.setParameterId(PARAMETER_ID_3);
        pd3.setValueType(ParameterValueType.INTEGER);
        p3.setParameterDefinition(pd3);
        p3.setValue(PARAMETER_VALUE_3);
        p3.setKey(PARAMETER_KEY_3);
        p3.setParameterSet(params);
        paramList.add(p3);

        Parameter p4 = new Parameter();
        ParameterDefinition pd4 = new ParameterDefinition();
        ParameterOption po4 = new ParameterOption();
        ArrayList<ParameterOption> poList = new ArrayList<ParameterOption>();
        po4.setKey(PARAMETER_OPTION_KEY4);
        po4.setOptionId(PARAMETER_VALUE_4);
        poList.add(po4);
        pd4.setParameterId(PARAMETER_ID_4);
        pd4.setValueType(ParameterValueType.ENUMERATION);
        pd4.setOptionList(poList);
        p4.setParameterDefinition(pd4);
        p4.setValue(PARAMETER_VALUE_4);
        p4.setKey(PARAMETER_KEY_4);

        p4.setParameterSet(params);

        paramList.add(p4);

        params.setParameters(paramList);

        return params;
    }

}
