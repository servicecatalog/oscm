/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 17.06.2010                                                      
 *                                                                              
 *  Completion Time: 17.06.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.api.VOConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.notification.vo.VONotification;
import org.oscm.notification.vo.VOProperty;
import org.oscm.test.stubs.DataServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.ObjectMessageStub;
import org.oscm.triggerservice.adapter.INotificationServiceAdapter;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

public class TriggerProcessListenerTest {

    private TriggerProcessListener listener;
    private DataServiceStub dm;
    private TriggerProcess storedTriggerProcess;
    private BillingResult storedBillingResult;
    private boolean throwsCommunicationException;
    private PlatformUser globalUser;
    private final boolean rollbackCredentialTxn = false;
    private String locale;
    private final VOPaymentType pt = new VOPaymentType();

    /** Parameters of the web service call */
    private final Map<String, Object> wsParams = new HashMap<>();

    @Before
    public void setUp() {
        AESEncrypter.generateKey();
        pt.setKey(5);
        pt.setPaymentTypeId(PaymentType.CREDIT_CARD);

        listener = new TriggerProcessListener() {
            @Override
            protected INotificationServiceAdapter getServiceClient(
                    TriggerDefinition td) throws MalformedURLException {
                if (throwsCommunicationException) {
                    throw new MalformedURLException();
                }
                return new INotificationServiceAdapter() {
                    @Override
                    public void billingPerformed(String xmlBillingData) {
                        wsParams.put("xmlBillingData", xmlBillingData);
                    }

                    @Override
                    public void onActivateProduct(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            org.oscm.vo.VOService product) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("product", product);
                    }

                    @Override
                    public void onAddRevokeUser(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            String subscriptionId,
                            List<org.oscm.vo.VOUsageLicense> usersToBeAdded,
                            List<org.oscm.vo.VOUser> usersToBeRevoked) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("subscriptionId", subscriptionId);
                        wsParams.put("usersToBeAdded", usersToBeAdded);
                        wsParams.put("usersToBeRevoked", usersToBeRevoked);
                    }

                    @Override
                    public void onAddSupplier(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            String supplierId) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("supplierId", supplierId);
                    }

                    @Override
                    public void onDeactivateProduct(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            org.oscm.vo.VOService product) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("product", product);
                    }

                    @Override
                    public void onModifySubscription(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            org.oscm.vo.VOSubscription subscription,
                            List<org.oscm.vo.VOParameter> modifiedParameters) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("subscription", subscription);
                        wsParams.put("modifiedParameters", modifiedParameters);
                    }

                    @Override
                    public void onRegisterCustomer(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            org.oscm.vo.VOOrganization organization,
                            org.oscm.vo.VOUserDetails user,
                            Properties organizationProperties) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("user", user);
                        wsParams.put("organization", organization);
                        wsParams.put("organizationProperties",
                                organizationProperties);
                    }

                    @Override
                    public void onRemoveSupplier(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            String supplierId) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("supplierId", supplierId);
                    }

                    @Override
                    public void onSaveDefaultPaymentConfiguration(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            Set<org.oscm.vo.VOPaymentType> defaultConfiguration) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("defaultCustomerConfiguration",
                                defaultConfiguration);
                    }

                    @Override
                    public void onSaveCustomerPaymentConfiguration(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            org.oscm.vo.VOOrganizationPaymentConfiguration customerConfiguration) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("customerConfiguration",
                                customerConfiguration);
                    }

                    @Override
                    public void onSubscribeToProduct(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            org.oscm.vo.VOSubscription subscription,
                            org.oscm.vo.VOService product,
                            List<org.oscm.vo.VOUsageLicense> users) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("subscription", subscription);
                        wsParams.put("product", product);
                        wsParams.put("users", users);
                    }

                    @Override
                    public void onUnsubscribeFromProduct(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            String subId) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("subId", subId);
                    }

                    @Override
                    public void onUpgradeSubscription(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            org.oscm.vo.VOSubscription current,
                            org.oscm.vo.VOService newProduct) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("current", current);
                        wsParams.put("newProduct", newProduct);
                    }

                    @Override
                    public void onSaveServicePaymentConfiguration(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            org.oscm.vo.VOServicePaymentConfiguration serviceConfiguration) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("serviceConfiguration",
                                serviceConfiguration);
                    }

                    @Override
                    public void onSaveServiceDefaultPaymentConfiguration(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            Set<org.oscm.vo.VOPaymentType> defaultConfiguration) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("defaultServiceConfiguration",
                                defaultConfiguration);
                    }

                    @Override
                    public void setNotificationService(
                            Object notificationService) {
                    }

                    @Override
                    public void setConfigurationService(
                            ConfigurationServiceLocal configurationService) {
                    }

                    @Override
                    public void setDataService(DataService dataService) {
                    }

                    @Override
                    public void onRegisterUserInOwnOrganization(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            org.oscm.vo.VOUserDetails user,
                            List<org.oscm.types.enumtypes.UserRoleType> roles,
                            String marketplaceId) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("user", user);
                        wsParams.put("roles", roles);
                        wsParams.put("marketplaceId", marketplaceId);
                    }

                    @Override
                    public void onSubscriptionCreation(
                            org.oscm.vo.VOTriggerProcess arg0,
                            org.oscm.vo.VOService service,
                            List<org.oscm.vo.VOUsageLicense> users,
                            VONotification notification) {
                        wsParams.put(TriggerProcessParameterName.PRODUCT.name(),
                                service);
                        wsParams.put(TriggerProcessParameterName.USERS.name(),
                                users);
                        wsParams.put(
                                TriggerProcessParameterName.NOTIFICATION.name(),
                                notification);
                    }

                    @Override
                    public void onSubscriptionModification(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            List<org.oscm.vo.VOParameter> parameters,
                            VONotification notification) {
                        wsParams.put("triggerProcess", triggerProcess);
                        wsParams.put("parameters", parameters);
                        wsParams.put("notification", notification);
                    }

                    @Override
                    public void onSubscriptionTermination(
                            org.oscm.vo.VOTriggerProcess triggerProcess,
                            VONotification notification) {
                        wsParams.put(
                                TriggerProcessParameterName.SUBSCRIPTION.name(),
                                triggerProcess);
                        wsParams.put(
                                TriggerProcessParameterName.NOTIFICATION.name(),
                                notification);
                    }

                    @Override
                    public void onCancelAction(long actionKey) {
                        wsParams.put("actionKey", Long.valueOf(actionKey));
                    }

                };
            }
        };
        dm = new DataServiceStub() {
            @Override
            public <T extends DomainObject<?>> T getReference(Class<T> objclass,
                    long key) throws ObjectNotFoundException {
                if (storedTriggerProcess == null) {
                    throw new ObjectNotFoundException(ClassEnum.EVENT, "bk");
                }
                if (TriggerProcess.class == objclass) {
                    if (rollbackCredentialTxn) {
                        storedTriggerProcess
                                .setState(TriggerProcessStatus.REJECTED);
                    }
                    return objclass.cast(storedTriggerProcess);
                } else if (BillingResult.class == objclass) {
                    return objclass.cast(storedBillingResult);
                }
                return null;
            }

            @Override
            public void setCurrentUserKey(Long key) {
            }
        };

        listener.dm = dm;
        listener.localizer = new LocalizerServiceStub() {
            @Override
            public boolean storeLocalizedResource(String localeString,
                    long objectKey, LocalizedObjectTypes objectType,
                    String value) {
                return false;
            }

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                locale = localeString;
                return "dbText";
            }
        };
    }

    @Test
    public void testOnMessageNullObjectMessage() throws Exception {
        listener.onMessage(null);
    }

    @Test
    public void testOnMessageObjectMessageNoValue() throws Exception {
        listener.onMessage(new ObjectMessageStub());
    }

    @Test
    public void testOnMessageObjectMessageNoMatchingTriggerProcess()
            throws Exception {
        ObjectMessageStub message = initObjectMessage();
        listener.onMessage(message);
    }

    @Test
    public void testOnMessageObjectMessageMatchingTriggerProcess()
            throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.ACTIVATE_SERVICE,
                false);
        storedTriggerProcess = tp;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());
        assertEquals("de", locale);
    }

    @Test
    public void testOnMessageObjectMessageNoOrganizationLocale()
            throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.ACTIVATE_SERVICE,
                false);
        tp.getUser().getOrganization().setLocale(null);
        storedTriggerProcess = tp;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.NOTIFIED,
                dm.getReference(TriggerProcess.class,
                        storedTriggerProcess.getKey()).getStatus());
        assertEquals("en", locale);
    }

    @Test
    public void testOnMessageDoNotSendNotificationForCancelledProcess()
            throws Exception {
        // given
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcessAlreadyCancelled();

        storedTriggerProcess = tp;

        // when
        listener.onMessage(message);

        // then
        assertEquals(TriggerProcessStatus.CANCELLED,
                dm.getReference(TriggerProcess.class,
                        storedTriggerProcess.getKey()).getStatus());
        assertNull(wsParams.get("actionKey"));
    }

    @Test
    public void testOnMessageSendNotificationForCancelledProcess()
            throws Exception {
        // given
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE,
                true);

        tp.setState(TriggerProcessStatus.CANCELLED);
        storedTriggerProcess = tp;

        // when
        listener.onMessage(message);

        // then
        assertEquals(TriggerProcessStatus.CANCELLED,
                dm.getReference(TriggerProcess.class,
                        storedTriggerProcess.getKey()).getStatus());
        assertEquals(Long.valueOf(storedTriggerProcess.getKey()),
                wsParams.get("actionKey"));
    }

    private TriggerProcess initTriggerProcessAlreadyCancelled() {
        TriggerProcess tp = new TriggerProcess() {

            private static final long serialVersionUID = 5749421953534109743L;

            @Override
            public int getVersion() {
                return 2;
            }
        };

        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.SUBSCRIBE_TO_SERVICE);

        td.setSuspendProcess(true);

        tp.setTriggerDefinition(td);
        tp.setState(TriggerProcessStatus.CANCELLED);
        return tp;
    }

    /**
     * Initializes a trigger process and a parent trigger definition.
     * 
     * @param type
     *            The type of the trigger.
     * @param suspend
     *            <code>true</code> if the trigger shall be a suspending one
     * @return The initialized trigger process.
     */
    private TriggerProcess initTriggerProcess(TriggerType type,
            boolean suspend) {
        TriggerProcess tp = new TriggerProcess();
        tp.setState(TriggerProcessStatus.INITIAL);

        globalUser = new PlatformUser();
        globalUser.setUserId("admin");

        Organization org = new Organization();
        org.setOrganizationId("orgId");
        org.setLocale("de");
        globalUser.setOrganization(org);
        globalUser.setLocale("de");

        TriggerDefinition td = new TriggerDefinition();
        td.setType(type);
        td.setTarget("http://localhost/service?wsdl");
        td.setSuspendProcess(suspend);
        td.setOrganization(org);

        tp.setTriggerDefinition(td);
        tp.setUser(globalUser);
        return tp;
    }

    @Test
    public void testOnMessageObjectMessageMatchingTriggerProcessCommFailure()
            throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.ACTIVATE_SERVICE,
                false);
        storedTriggerProcess = tp;
        throwsCommunicationException = true;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.ERROR,
                storedTriggerProcess.getStatus());
    }

    @Test
    public void testOnMessageObjectMessageMatchingTriggerProcessUsingWSNonSuspending()
            throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.ACTIVATE_SERVICE,
                false);
        storedTriggerProcess = tp;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());
    }

    @Test
    public void testOnMessageObjectMessageMatchingTriggerProcessUsingWSSuspending()
            throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE,
                true);
        storedTriggerProcess = tp;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
    }

    @Test
    public void testOnMessageNoTriggerDef() throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE,
                true);
        tp.setTriggerDefinition(null);
        storedTriggerProcess = tp;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.ERROR,
                storedTriggerProcess.getStatus());
    }

    @Test
    public void testOnMessageNoProcessUser() throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.START_BILLING_RUN,
                false);
        tp.setUser(null);
        storedTriggerProcess = tp;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());
    }

    public void testOnMessageObjectMessageMatchingTriggerProcessBillingProcessWrongObjectTypeSetForTD()
            throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.START_BILLING_RUN,
                false);
        storedTriggerProcess = tp;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.ERROR,
                storedTriggerProcess.getStatus());
    }

    @Test
    public void testBillingStartedNullParams() throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.START_BILLING_RUN,
                false);
        storedTriggerProcess = tp;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());
    }

    @Test
    public void testBillingStarted() throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.START_BILLING_RUN,
                false);
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.XML_BILLING_DATA, "<xmlData/>");
        storedTriggerProcess = tp;
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());
        assertEquals("<xmlData/>", wsParams.get("xmlBillingData").toString());
    }

    @Test
    public void testOnMessageObjectMessageMatchingTriggerProcessBillingProcess()
            throws Exception {
        ObjectMessageStub message = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.START_BILLING_RUN,
                false);
        storedTriggerProcess = tp;
        storedBillingResult = new BillingResult();
        listener.onMessage(message);
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());
    }

    /**
     * Inits an object message.
     * 
     * @return The created object message.
     */
    private ObjectMessageStub initObjectMessage() throws JMSException {
        ObjectMessageStub message = new ObjectMessageStub();
        message.setObject(new Long(1));
        return message;
    }

    @Test
    public void testRegisterCustomerForSupplierNullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(
                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER, true);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
    }

    @Test
    public void testRegisterCustomerForSupplier() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(
                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER, true);
        VOOrganization org = new VOOrganization();
        org.setOrganizationId("bla");
        VOUserDetails user = new VOUserDetails();
        user.setUserId("userId");
        Properties props = new Properties();
        props.put("key", "value");

        tp.addTriggerProcessParameter(TriggerProcessParameterName.ORGANIZATION,
                org);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USER, user);
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.ORGANIZATION_PROPERTIES, props);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        assertEquals(user.getUserId(), org.oscm.vo.VOUserDetails.class
                .cast(wsParams.get("user")).getUserId());
        assertEquals(org.getOrganizationId(), org.oscm.vo.VOOrganization.class
                .cast(wsParams.get("organization")).getOrganizationId());
        assertEquals(props.getProperty("key"),
                Properties.class.cast(wsParams.get("organizationProperties"))
                        .getProperty("key"));
    }

    @Test
    public void testSavePaymentConfigurationNullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(
                TriggerType.SAVE_PAYMENT_CONFIGURATION, true);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertNull(wsParams.get("triggerProcess"));

    }

    @Test
    public void testSavePaymentConfiguration_CustomerDefault()
            throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(
                TriggerType.SAVE_PAYMENT_CONFIGURATION, true);
        Set<VOPaymentType> pts = new HashSet<>(Arrays.asList(pt));
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.DEFAULT_CONFIGURATION, pts);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());

        @SuppressWarnings("unchecked")
        Set<org.oscm.vo.VOPaymentType> paymentTypes = (Set<org.oscm.vo.VOPaymentType>) wsParams
                .get("defaultCustomerConfiguration");
        for (org.oscm.vo.VOPaymentType pt : paymentTypes) {
            assertTrue(pts.contains(VOConverter.convertToUp(pt)));
        }
    }

    @Test
    public void testSavePaymentConfiguration_ServiceDefault() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(
                TriggerType.SAVE_PAYMENT_CONFIGURATION, true);
        Set<VOPaymentType> pts = new HashSet<>(Arrays.asList(pt));
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION,
                pts);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        @SuppressWarnings("unchecked")
        Set<org.oscm.vo.VOPaymentType> paymentTypes = (Set<org.oscm.vo.VOPaymentType>) wsParams
                .get("defaultServiceConfiguration");
        for (org.oscm.vo.VOPaymentType pt : paymentTypes) {
            assertTrue(pts.contains(VOConverter.convertToUp(pt)));
        }
    }

    @Test
    public void testSavePaymentConfiguration_CustomerSpecific()
            throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(
                TriggerType.SAVE_PAYMENT_CONFIGURATION, true);

        VOOrganizationPaymentConfiguration config = new VOOrganizationPaymentConfiguration();
        Set<VOPaymentType> types = new HashSet<>(
                Arrays.asList(pt));
        config.setEnabledPaymentTypes(types);
        VOOrganization org = new VOOrganization();
        org.setKey(1234);
        config.setOrganization(org);

        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.CUSTOMER_CONFIGURATION, config);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        org.oscm.vo.VOOrganizationPaymentConfiguration conf = (org.oscm.vo.VOOrganizationPaymentConfiguration) wsParams
                .get("customerConfiguration");
        for (org.oscm.vo.VOPaymentType pt : conf.getEnabledPaymentTypes()) {
            assertTrue(types.contains(VOConverter.convertToUp(pt)));
        }
        assertEquals(org.getKey(), conf.getOrganization().getKey());
    }

    @Test
    public void testSavePaymentConfiguration_ServiceSpecific()
            throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(
                TriggerType.SAVE_PAYMENT_CONFIGURATION, true);

        VOServicePaymentConfiguration config = new VOServicePaymentConfiguration();
        Set<VOPaymentType> types = new HashSet<>(
                Arrays.asList(pt));
        config.setEnabledPaymentTypes(types);
        VOService svc = new VOService();
        svc.setKey(1234);
        config.setService(svc);

        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                config);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        org.oscm.vo.VOServicePaymentConfiguration conf = (org.oscm.vo.VOServicePaymentConfiguration) wsParams
                .get("serviceConfiguration");
        for (org.oscm.vo.VOPaymentType pt : conf.getEnabledPaymentTypes()) {
            assertTrue(types.contains(VOConverter.convertToUp(pt)));
        }
        assertEquals(svc.getKey(), conf.getService().getKey());
    }

    @Test
    public void testActivateProductNullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.ACTIVATE_SERVICE,
                true);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
    }

    @Test
    public void testActivateProduct() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.ACTIVATE_SERVICE,
                true);
        VOService prod = new VOService();
        prod.setServiceId("productId");
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                prod);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        assertEquals(prod.getServiceId(), org.oscm.vo.VOService.class
                .cast(wsParams.get("product")).getServiceId());
    }

    @Test
    public void testDeactivateProductNullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.DEACTIVATE_SERVICE,
                true);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
    }

    @Test
    public void testDeactivateProduct() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.DEACTIVATE_SERVICE,
                true);
        VOService prod = new VOService();
        prod.setServiceId("productId");
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                prod);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        assertEquals(prod.getServiceId(), org.oscm.vo.VOService.class
                .cast(wsParams.get("product")).getServiceId());
    }

    @Test
    public void testSubscribeNullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE,
                true);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
    }

    @Test
    public void testSubscribe() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE,
                true);

        VOSubscription sub = new VOSubscription();
        sub.setSubscriptionId("sub");
        VOService prod = new VOService();
        prod.setServiceId("prod");
        List<VOUsageLicense> users = new ArrayList<>();

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                sub);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                prod);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USERS, users);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        assertEquals("sub", org.oscm.vo.VOSubscription.class
                .cast(wsParams.get("subscription")).getSubscriptionId());
        assertEquals("prod", org.oscm.vo.VOService.class
                .cast(wsParams.get("product")).getServiceId());
    }

    @Test
    public void testUnsubscribeNullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(
                TriggerType.UNSUBSCRIBE_FROM_SERVICE, true);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
    }

    @Test
    public void testUnsubscribe() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(
                TriggerType.UNSUBSCRIBE_FROM_SERVICE, true);
        String subId = "subId";
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                subId);
        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        assertEquals("subId", wsParams.get("subId"));
    }

    @Test
    public void testModifySubscriptionNullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION,
                true);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
    }

    @Test
    public void testModifySubscription() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION,
                true);
        VOSubscription sub = new VOSubscription();
        sub.setSubscriptionId("someId");
        List<VOParameter> params = new ArrayList<>();

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                sub);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PARAMETERS,
                params);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        assertEquals("someId", org.oscm.vo.VOSubscription.class
                .cast(wsParams.get("subscription")).getSubscriptionId());
        assertEquals(0,
                List.class.cast(wsParams.get("modifiedParameters")).size());
    }

    @Test
    public void testUpgradeSubscriptionNullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION,
                true);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
    }

    @Test
    public void testUpgradeSubscription() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION,
                true);

        VOSubscription current = new VOSubscription();
        current.setSubscriptionId("anotherSubId");
        VOService product = new VOService();
        product.setServiceId("product Identifier");

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                current);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                product);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        assertEquals("anotherSubId", org.oscm.vo.VOSubscription.class
                .cast(wsParams.get("current")).getSubscriptionId());
        assertEquals("product Identifier", org.oscm.vo.VOService.class
                .cast(wsParams.get("newProduct")).getServiceId());
    }

    @Test
    public void testAddRevokeUserNullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.ADD_REVOKE_USER,
                true);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
    }

    @Test
    public void testAddRevokeUser() throws Exception {
        ObjectMessageStub om = initObjectMessage();
        TriggerProcess tp = initTriggerProcess(TriggerType.ADD_REVOKE_USER,
                true);

        String subId = "subscription identifier";
        List<VOUser> users = new ArrayList<>();

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                subId);
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.USERS_TO_REVOKE, users);

        storedTriggerProcess = tp;

        listener.onMessage(om);
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        assertEquals("subscription identifier", wsParams.get("subscriptionId"));
        assertEquals(0,
                List.class.cast(wsParams.get("usersToBeRevoked")).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void registerOwnUser() throws Exception {
        ObjectMessageStub om = initObjectMessage();

        // given trigger with parameters
        storedTriggerProcess = initTriggerProcess(TriggerType.REGISTER_OWN_USER,
                true);
        addUserDetailsToTriggerProcess("stored user id");
        List<UserRoleType> roles = Collections
                .singletonList(UserRoleType.BROKER_MANAGER);
        storedTriggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.USER_ROLE_TYPE, roles);
        String marketplaceId = "FUJITSU";
        storedTriggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.MARKETPLACE_ID, marketplaceId);

        // when processed
        listener.onMessage(om);

        // then process is updated and parameters are passed to external system
        assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                storedTriggerProcess.getStatus());
        assertEquals(globalUser.getUserId(), org.oscm.vo.VOTriggerProcess.class
                .cast(wsParams.get("triggerProcess")).getUser().getUserId());
        org.oscm.vo.VOUserDetails sentUserDetais = (org.oscm.vo.VOUserDetails) wsParams
                .get("user");
        assertEquals("stored user id", sentUserDetais.getUserId());
        for (org.oscm.types.enumtypes.UserRoleType r : (List<org.oscm.types.enumtypes.UserRoleType>) wsParams
                .get("roles")) {
            assertTrue(roles.contains(UserRoleType.valueOf(r.name())));

        }
        assertEquals(marketplaceId, wsParams.get("marketplaceId"));
    }

    @Test
    public void registerOwnUser_nullParams() throws Exception {
        ObjectMessageStub om = initObjectMessage();

        // given trigger with parameters
        storedTriggerProcess = initTriggerProcess(TriggerType.REGISTER_OWN_USER,
                true);

        // when processed
        listener.onMessage(om);

        // then process is updated and only trigger process data is passed to
        // external system
        assertEquals(TriggerProcessStatus.ERROR,
                storedTriggerProcess.getStatus());
    }

    @Test
    public void subscriptionCreation_chargeableService() throws Exception {
        // given
        ObjectMessageStub om = initObjectMessage();

        storedTriggerProcess = initTriggerProcess(
                TriggerType.SUBSCRIPTION_CREATION, false);

        String serviceId = "serviceId1";
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        addServiceTriggerParameter(storedTriggerProcess, serviceId, priceModel);

        String userId = "userId1";
        String roleDefinitionId = "roleDefinitionId1";
        addUsageLicenseTriggerParameter(storedTriggerProcess, userId,
                roleDefinitionId);

        String subscriptionId = "subscriptionId1";
        String serviceInstanceId = "serviceInstanceId";
        addSubscriptionTriggerParameter(storedTriggerProcess, subscriptionId,
                serviceInstanceId);

        String email = "test@test.com";
        String address = "abc";
        String companyName = "def";
        addBillingContactTriggerParameter(storedTriggerProcess, email, address,
                companyName);

        // when
        listener.onMessage(om);

        // then process is updated and parameters are passed to external system
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());

        assertEquals(serviceId,
                ((org.oscm.vo.VOService) wsParams
                        .get(TriggerProcessParameterName.PRODUCT.name()))
                                .getServiceId());

        @SuppressWarnings("unchecked")
        List<org.oscm.vo.VOUsageLicense> usageLicenses = (List<org.oscm.vo.VOUsageLicense>) wsParams
                .get(TriggerProcessParameterName.USERS.name());
        assertNotNull(usageLicenses);
        assertEquals(1, usageLicenses.size());
        assertEquals(userId, usageLicenses.get(0).getUser().getUserId());
        assertEquals(roleDefinitionId,
                usageLicenses.get(0).getRoleDefinition().getRoleId());

        VONotification notification = (VONotification) wsParams
                .get(TriggerProcessParameterName.NOTIFICATION.name());
        assertNotNull(notification);
        assertEquals(5, notification.getProperties().size());
        assertEquals(subscriptionId, getPropertyValue(notification,
                VOProperty.SUBSCRIPTION_SUBSCRIPTION_ID));
        assertEquals(serviceInstanceId, getPropertyValue(notification,
                VOProperty.SUBSCRIPTION_SERVICE_INSTANCE_ID));
        assertEquals(email, getPropertyValue(notification,
                VOProperty.BILLING_CONTACT_EMAIL));
        assertEquals(address, getPropertyValue(notification,
                VOProperty.BILLING_CONTACT_ADDRESS));
        assertEquals(companyName, getPropertyValue(notification,
                VOProperty.BILLING_CONTACT_COMPANYNAME));
    }

    @Test
    public void subscriptionCreation_freeService() throws Exception {
        // given
        ObjectMessageStub om = initObjectMessage();

        storedTriggerProcess = initTriggerProcess(
                TriggerType.SUBSCRIPTION_CREATION, false);

        String serviceId = "serviceId1";
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        addServiceTriggerParameter(storedTriggerProcess, serviceId, priceModel);

        String userId = "userId1";
        String roleDefinitionId = "roleDefinitionId1";
        addUsageLicenseTriggerParameter(storedTriggerProcess, userId,
                roleDefinitionId);

        String subscriptionId = "subscriptionId1";
        String serviceInstanceId = "serviceInstanceId";
        addSubscriptionTriggerParameter(storedTriggerProcess, subscriptionId,
                serviceInstanceId);

        // when
        listener.onMessage(om);

        // then process is updated and parameters are passed to external system
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());

        assertEquals(serviceId,
                ((org.oscm.vo.VOService) wsParams
                        .get(TriggerProcessParameterName.PRODUCT.name()))
                                .getServiceId());

        @SuppressWarnings("unchecked")
        List<org.oscm.vo.VOUsageLicense> usageLicenses = (List<org.oscm.vo.VOUsageLicense>) wsParams
                .get(TriggerProcessParameterName.USERS.name());
        assertNotNull(usageLicenses);
        assertEquals(1, usageLicenses.size());
        assertEquals(userId, usageLicenses.get(0).getUser().getUserId());
        assertEquals(roleDefinitionId,
                usageLicenses.get(0).getRoleDefinition().getRoleId());

        VONotification notification = (VONotification) wsParams
                .get(TriggerProcessParameterName.NOTIFICATION.name());
        assertNotNull(notification);
        assertEquals(2, notification.getProperties().size());
        assertEquals(subscriptionId, getPropertyValue(notification,
                VOProperty.SUBSCRIPTION_SUBSCRIPTION_ID));
        assertEquals(serviceInstanceId, getPropertyValue(notification,
                VOProperty.SUBSCRIPTION_SERVICE_INSTANCE_ID));
    }

    @Test
    public void subscriptionCreation_nullParams() throws Exception {
        // given trigger with parameters
        ObjectMessageStub om = initObjectMessage();
        storedTriggerProcess = initTriggerProcess(
                TriggerType.SUBSCRIPTION_CREATION, false);

        // when processed
        listener.onMessage(om);

        // then
        // necessary input parameters are missing
        assertEquals(TriggerProcessStatus.ERROR,
                storedTriggerProcess.getStatus());
    }

    @Test
    public void subscriptionTermination() throws Exception {
        // given
        ObjectMessageStub om = initObjectMessage();
        String subId = "subscriptionId";
        storedTriggerProcess = initTriggerProcess(
                TriggerType.SUBSCRIPTION_TERMINATION, false);
        storedTriggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, subId);

        // when
        listener.onMessage(om);

        // then
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());

        VONotification notification = (VONotification) wsParams
                .get(TriggerProcessParameterName.NOTIFICATION.name());
        assertNotNull(notification);
        assertEquals(1, notification.getProperties().size());
        assertEquals(subId, getPropertyValue(notification,
                VOProperty.SUBSCRIPTION_SUBSCRIPTION_ID));
    }

    @Test
    public void subscriptionTermination_nullParams() throws Exception {
        // given
        ObjectMessageStub om = initObjectMessage();
        storedTriggerProcess = initTriggerProcess(
                TriggerType.SUBSCRIPTION_TERMINATION, false);

        // when
        listener.onMessage(om);

        // then
        assertEquals(TriggerProcessStatus.ERROR,
                storedTriggerProcess.getStatus());
    }

    private String getPropertyValue(VONotification notification,
            String propertyName) {
        for (VOProperty property : notification.getProperties()) {
            if (property.getName().equals(propertyName)) {
                return property.getValue();
            }
        }
        return null;
    }

    private VOService addServiceTriggerParameter(TriggerProcess tp,
            String serviceId, VOPriceModel priceModel) {
        VOService service = new VOService();
        service.setServiceId(serviceId);
        service.setPriceModel(priceModel);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                service);
        return service;
    }

    private void addUsageLicenseTriggerParameter(TriggerProcess tp,
            String userId, String roleDefinitionId) {
        VOUsageLicense license = new VOUsageLicense();
        VOUser user = new VOUser();
        user.setUserId(userId);
        license.setUser(user);

        VORoleDefinition roleDefinition = new VORoleDefinition();
        roleDefinition.setRoleId(roleDefinitionId);
        license.setRoleDefinition(roleDefinition);

        List<VOUsageLicense> users = new ArrayList<>();
        users.add(license);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USERS, users);
    }

    private void addSubscriptionTriggerParameter(TriggerProcess tp,
            String subscriptionId, String serviceInstanceId) {
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(subscriptionId);
        subscription.setServiceInstanceId(serviceInstanceId);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                subscription);
    }

    private void addBillingContactTriggerParameter(TriggerProcess tp,
            String email, String address, String companyName) {
        VOBillingContact billingContact = new VOBillingContact();
        billingContact.setEmail(email);
        billingContact.setAddress(address);
        billingContact.setCompanyName(companyName);
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, billingContact);
    }

    @Test
    public void subscriptionModification() throws Exception {
        ObjectMessageStub om = initObjectMessage();

        // given trigger with parameters
        storedTriggerProcess = initTriggerProcess(
                TriggerType.SUBSCRIPTION_MODIFICATION, false);
        addUserDetailsToTriggerProcess("userId");
        addParameterToTriggerProcess("param value");
        addSubscriptionToTriggerProcess("stored subId",
                "stored serv-instance-id", "stored service-Id");

        // when processed
        listener.onMessage(om);

        // then process is updated and parameters are passed to external system
        assertEquals(TriggerProcessStatus.NOTIFIED,
                storedTriggerProcess.getStatus());
        org.oscm.vo.VOTriggerProcess sentProcess = (org.oscm.vo.VOTriggerProcess) wsParams
                .get("triggerProcess");
        assertEquals(globalUser.getUserId(), sentProcess.getUser().getUserId());
        @SuppressWarnings("unchecked")
        org.oscm.vo.VOParameter sentParameter = ((List<org.oscm.vo.VOParameter>) wsParams
                .get("parameters")).get(0);
        assertEquals("param value", sentParameter.getValue());
        VONotification sentNotification = (VONotification) wsParams
                .get("notification");
        assertEquals("stored subId", getPropertyValue(sentNotification,
                VOProperty.SUBSCRIPTION_SUBSCRIPTION_ID));
        assertEquals("stored serv-instance-id", getPropertyValue(
                sentNotification, VOProperty.SUBSCRIPTION_SERVICE_INSTANCE_ID));
        assertEquals("stored service-Id", getPropertyValue(sentNotification,
                VOProperty.SUBSCRIPTION_SERVICE_ID));
        assertEquals("1", getPropertyValue(sentNotification,
                VOProperty.SUBSCRIPTION_SERVICE_KEY));

    }

    public void addSubscriptionToTriggerProcess(String subscriptionId,
            String serverInstanceId, String serviceId) {
        VOSubscription storedSubscription = new VOSubscription();
        storedSubscription.setSubscriptionId(subscriptionId);
        storedSubscription.setServiceInstanceId(serverInstanceId);
        storedSubscription.setServiceId(serviceId);
        long storedServiceKey = 1;
        storedSubscription.setServiceKey(storedServiceKey);
        storedTriggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, storedSubscription);
    }

    public VOParameter addParameterToTriggerProcess(String paramValue) {
        VOParameter storedParameter = new VOParameter();
        storedParameter.setValue(paramValue);
        List<VOParameter> storedParameters = Collections
                .singletonList(storedParameter);
        storedTriggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PARAMETERS, storedParameters);
        return storedParameter;
    }

    public void addUserDetailsToTriggerProcess(String userId) {
        VOUserDetails user = new VOUserDetails();
        user.setUserId(userId);
        storedTriggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.USER, user);
    }

}
