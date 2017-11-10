/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 16.06.2010                                                      
 *                                                                              
 *  Completion Time: 16.06.2010                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.SessionContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.test.stubs.DataServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.stubs.ConnectionFactoryStub;
import org.oscm.triggerservice.stubs.QueueStub;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

public class TriggerQueueServiceLocalTest {

    private TriggerQueueServiceBean tqs;
    private QueueStub queue;
    private ConnectionFactoryStub connFact;
    private PlatformUser user;
    private Organization organization;
    private final List<Object> storedObjects = new ArrayList<>();
    private final List<Organization> organizations = new ArrayList<>();
    private boolean throwsSaasNonUniqueBusinessKeyException;
    private SessionContext sessionMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        AESEncrypter.generateKey();
        tqs = new TriggerQueueServiceBean();

        sessionMock = mock(SessionContext.class);
        doReturn(tqs).when(sessionMock).getBusinessObject(any(Class.class));

        tqs.sessionCtx = sessionMock;

        queue = new QueueStub();
        connFact = new ConnectionFactoryStub();
        tqs.qFactory = connFact;
        tqs.queue = queue;

        user = new PlatformUser();
        organization = new Organization();
        user.setOrganization(organization);
        organizations.add(organization);

        tqs.dm = new DataServiceStub() {
            @Override
            public void flush() {
            }

            @Override
            public void persist(DomainObject<?> obj)
                    throws NonUniqueBusinessKeyException {
                if (throwsSaasNonUniqueBusinessKeyException) {
                    throw new NonUniqueBusinessKeyException(ClassEnum.EVENT,
                            "");
                }
                storedObjects.add(obj);
            }

            @Override
            public PlatformUser getCurrentUser() {
                return user;
            }
        };
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendSuspendingMessageNoFactoryAndNoQueue()
            throws Exception {
        tqs.qFactory = null;
        tqs.queue = null;
        tqs.sendSuspendingMessages(null);
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendSuspendingMessageNoFactory() throws Exception {
        tqs.qFactory = null;
        tqs.sendSuspendingMessages(null);
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendSuspendingMessageNoQueue() throws Exception {
        tqs.queue = null;
        tqs.sendSuspendingMessages(null);
    }

    @Test
    public void testSendSuspendingMessageForExistingTriggerDefinitionNonSuspending()
            throws Exception {
        organization.setTriggerDefinitions(
                Collections.singletonList(new TriggerDefinition()));
        TriggerMessage messageData = new TriggerMessage(
                TriggerType.ACTIVATE_SERVICE);
        List<TriggerMessage> listOfMsgs = new ArrayList<>();
        int msgsCount = 2543;
        for (int i = 0; i < msgsCount; i++) {
            listOfMsgs.add(messageData);
        }
        List<TriggerProcessMessageData> list = tqs
                .sendSuspendingMessages(listOfMsgs);
        Assert.assertNotNull(list.get(0).getTrigger());
        Assert.assertNull(list.get(0).getTrigger().getTriggerDefinition());
        Assert.assertTrue(list.size() == msgsCount);
    }

    @Test
    public void testSendSuspendingMessageForExistingTriggerDefinitionSuspending()
            throws Exception {
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ACTIVATE_SERVICE);
        td.setSuspendProcess(true);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        TriggerMessage messageData = new TriggerMessage(
                TriggerType.ACTIVATE_SERVICE);
        List<TriggerProcessMessageData> list = tqs
                .sendSuspendingMessages(Collections.singletonList(messageData));
        TriggerProcess tp = list.get(0).getTrigger();
        Assert.assertNotNull(tp);
        Assert.assertFalse(storedObjects.isEmpty());
        Assert.assertTrue(storedObjects.get(0) instanceof TriggerProcess);
        Assert.assertEquals(
                TriggerProcess.class.cast(storedObjects.get(0)).getKey(),
                tp.getKey());
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendSuspendingMessageForExistingTriggerDefinitionSuspendingPersistFails()
            throws Exception {
        throwsSaasNonUniqueBusinessKeyException = true;
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ACTIVATE_SERVICE);
        td.setSuspendProcess(true);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        TriggerMessage messageData = new TriggerMessage(
                TriggerType.ACTIVATE_SERVICE);
        tqs.sendSuspendingMessages(Collections.singletonList(messageData));
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendSuspendingMessageForExistingTriggerDefinitionSuspendingJMSException()
            throws Exception {
        connFact.setThrowsJMSException(true);
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ACTIVATE_SERVICE);
        td.setSuspendProcess(true);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        TriggerMessage messageData = new TriggerMessage(
                TriggerType.ACTIVATE_SERVICE);
        tqs.sendSuspendingMessages(Collections.singletonList(messageData));
    }

    @Test
    public void testSendSuspendingMessageForNonExistingTriggerDefinition()
            throws Exception {
        TriggerMessage messageData = new TriggerMessage(
                TriggerType.ACTIVATE_SERVICE);
        List<TriggerProcessMessageData> list = tqs
                .sendSuspendingMessages(Collections.singletonList(messageData));
        TriggerProcess tp = list.get(0).getTrigger();
        Assert.assertNotNull(tp);
        Assert.assertNull(tp.getTriggerDefinition());
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesNoFactoryAndNoQueue()
            throws Exception {
        tqs.qFactory = null;
        tqs.queue = null;
        tqs.sendAllNonSuspendingMessages(createMessages(null, null));
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesNoFactory() throws Exception {
        tqs.qFactory = null;
        tqs.sendAllNonSuspendingMessages(createMessages(null, null));
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesNoQueue() throws Exception {
        tqs.queue = null;
        tqs.sendAllNonSuspendingMessages(createMessages(null, null));
    }

    @Test
    public void testSendAllNonSuspendingMessagesNoTriggerDef()
            throws Exception {
        tqs.sendAllNonSuspendingMessages(
                createMessages(TriggerType.ACTIVATE_SERVICE, null));
        Assert.assertTrue(storedObjects.isEmpty());
    }

    @Test
    public void testSendAllNonSuspendingMessagesOnlySuspendingTriggerDef()
            throws Exception {
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ACTIVATE_SERVICE);
        td.setSuspendProcess(true);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        List<TriggerProcessParameter> list = new ArrayList<>();
        TriggerProcessParameter tpp = new TriggerProcessParameter();
        tpp.setName(TriggerProcessParameterName.OBJECT_ID);
        tpp.setValue("test");
        list.add(tpp);
        tqs.sendAllNonSuspendingMessages(
                createMessages(TriggerType.ACTIVATE_SERVICE, list));
        Assert.assertTrue(storedObjects.isEmpty());
    }

    @Test
    public void testSendAllNonSuspendingMessages() throws Exception {
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(false);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        tqs.sendAllNonSuspendingMessages(
                createMessages(TriggerType.ADD_REVOKE_USER, null));
        Assert.assertFalse(storedObjects.isEmpty());
    }

    private List<TriggerMessage> createMessages(TriggerType addRevokeUser,
            List<TriggerProcessParameter> params) {
        return TriggerMessage.create(addRevokeUser, params, organization);
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesPersistFails()
            throws Exception {
        throwsSaasNonUniqueBusinessKeyException = true;
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(false);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        tqs.sendAllNonSuspendingMessages(
                createMessages(TriggerType.ADD_REVOKE_USER, null));
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesJMSException()
            throws Exception {
        connFact.setThrowsJMSException(true);

        List<TriggerDefinition> triggerDefs = new ArrayList<>();

        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ACTIVATE_SERVICE);
        td.setSuspendProcess(false);
        triggerDefs.add(td);

        td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(true);
        triggerDefs.add(td);

        organization.setTriggerDefinitions(triggerDefs);
        tqs.sendAllNonSuspendingMessages(
                createMessages(TriggerType.ACTIVATE_SERVICE, null));
    }

    @Test
    public void testSendAllNonSuspendingMessagesBillingTypeNullReference()
            throws Exception {
        List<TriggerDefinition> triggerDefs = new ArrayList<>();
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.START_BILLING_RUN);
        td.setSuspendProcess(false);
        triggerDefs.add(td);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        List<TriggerProcessParameter> list = new ArrayList<>();
        TriggerProcessParameter tpp = new TriggerProcessParameter();
        tpp.setName(TriggerProcessParameterName.XML_BILLING_DATA);
        tpp.setValue("test");
        list.add(tpp);
        tqs.sendAllNonSuspendingMessages(
                createMessages(TriggerType.START_BILLING_RUN, list));
        Assert.assertFalse(storedObjects.isEmpty());
        Assert.assertTrue(storedObjects.get(0) instanceof TriggerProcess);
        TriggerProcess storedProcess = (TriggerProcess) storedObjects.get(0);
        Assert.assertEquals(TriggerProcessStatus.INITIAL,
                storedProcess.getStatus());
    }

    @Test
    public void testSendAllNonSuspendingMessagesBillingType() throws Exception {
        List<TriggerDefinition> triggerDefs = new ArrayList<>();
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.START_BILLING_RUN);
        td.setSuspendProcess(false);
        triggerDefs.add(td);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        tqs.sendAllNonSuspendingMessages(
                createMessages(TriggerType.START_BILLING_RUN, null));
        Assert.assertFalse(storedObjects.isEmpty());
        Assert.assertTrue(storedObjects.get(0) instanceof TriggerProcess);
        TriggerProcess storedProcess = (TriggerProcess) storedObjects.get(0);
        Assert.assertEquals(TriggerProcessStatus.INITIAL,
                storedProcess.getStatus());
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesWithOrgsNoFactoryAndNoQueue()
            throws Exception {
        tqs.qFactory = null;
        tqs.queue = null;
        TriggerMessage tm = new TriggerMessage(null, null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesWithOrgsNoFactory()
            throws Exception {
        tqs.qFactory = null;
        TriggerMessage tm = new TriggerMessage(null, null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesWithOrgsNoQueue()
            throws Exception {
        tqs.queue = null;
        TriggerMessage tm = new TriggerMessage(null, null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
    }

    @Test
    public void testSendAllNonSuspendingMessagesWithOrgsNoTriggerDef()
            throws Exception {
        TriggerMessage tm = new TriggerMessage(TriggerType.ACTIVATE_SERVICE,
                null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
        Assert.assertTrue(storedObjects.isEmpty());
    }

    @Test
    public void testSendAllNonSuspendingMessagesWithOrgsOnlySuspendingTriggerDef()
            throws Exception {
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ACTIVATE_SERVICE);
        td.setSuspendProcess(true);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        List<TriggerProcessParameter> list = new ArrayList<>();
        TriggerProcessParameter tpp = new TriggerProcessParameter();
        tpp.setName(TriggerProcessParameterName.OBJECT_ID);
        tpp.setValue("test");
        list.add(tpp);
        TriggerMessage tm = new TriggerMessage(TriggerType.ACTIVATE_SERVICE,
                list, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
        Assert.assertTrue(storedObjects.isEmpty());
    }

    @Test
    public void testSendAllNonSuspendingMessagesWithOrgs() throws Exception {
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(false);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        TriggerMessage tm = new TriggerMessage(TriggerType.ADD_REVOKE_USER,
                null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
        Assert.assertFalse(storedObjects.isEmpty());
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesWithOrgsPersistFails()
            throws Exception {
        throwsSaasNonUniqueBusinessKeyException = true;
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(false);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        TriggerMessage tm = new TriggerMessage(TriggerType.ADD_REVOKE_USER,
                null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessagesWithOrgsJMSException()
            throws Exception {
        connFact.setThrowsJMSException(true);
        List<TriggerDefinition> triggerDefs = new ArrayList<>();

        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ACTIVATE_SERVICE);
        td.setSuspendProcess(false);
        triggerDefs.add(td);

        td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(true);
        organization.setTriggerDefinitions(triggerDefs);

        TriggerMessage tm = new TriggerMessage(TriggerType.ACTIVATE_SERVICE,
                null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
    }

    @Test
    public void testSendAllNonSuspendingMessagesWithOrgsBillingTypeNullReference()
            throws Exception {
        List<TriggerDefinition> triggerDefs = new ArrayList<>();
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.START_BILLING_RUN);
        td.setSuspendProcess(false);
        triggerDefs.add(td);
        organization.setTriggerDefinitions(triggerDefs);
        List<TriggerProcessParameter> list = new ArrayList<>();
        TriggerProcessParameter tpp = new TriggerProcessParameter();
        tpp.setName(TriggerProcessParameterName.XML_BILLING_DATA);
        tpp.setValue("test");
        list.add(tpp);
        TriggerMessage tm = new TriggerMessage(TriggerType.START_BILLING_RUN,
                list, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
        Assert.assertFalse(storedObjects.isEmpty());
        Assert.assertTrue(storedObjects.get(0) instanceof TriggerProcess);
        TriggerProcess storedProcess = (TriggerProcess) storedObjects.get(0);
        Assert.assertEquals(TriggerProcessStatus.INITIAL,
                storedProcess.getStatus());
    }

    @Test
    public void testSendAllNonSuspendingMessagesWithOrgsBillingType()
            throws Exception {
        List<TriggerDefinition> triggerDefs = new ArrayList<>();
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.START_BILLING_RUN);
        td.setSuspendProcess(false);
        triggerDefs.add(td);
        organization.setTriggerDefinitions(triggerDefs);
        TriggerMessage tm = new TriggerMessage(TriggerType.START_BILLING_RUN,
                null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
        Assert.assertFalse(storedObjects.isEmpty());
        Assert.assertTrue(storedObjects.get(0) instanceof TriggerProcess);
        TriggerProcess storedProcess = (TriggerProcess) storedObjects.get(0);
        Assert.assertEquals(TriggerProcessStatus.INITIAL,
                storedProcess.getStatus());
    }

    @Test
    public void testSendAllNonSuspendingMessagesWithOrgsNoReceivers()
            throws Exception {
        List<TriggerDefinition> triggerDefs = new ArrayList<>();
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.START_BILLING_RUN);
        td.setSuspendProcess(false);
        triggerDefs.add(td);
        organization.setTriggerDefinitions(triggerDefs);
        organizations.clear();
        TriggerMessage tm = new TriggerMessage(TriggerType.START_BILLING_RUN,
                null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
        assertTrue(storedObjects.isEmpty());
    }

    @Test
    public void testSendAllNonSuspendingMessagesWithOrgsSeveralReceivers()
            throws Exception {
        List<TriggerDefinition> triggerDefs = new ArrayList<>();
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.START_BILLING_RUN);
        td.setSuspendProcess(false);
        triggerDefs.add(td);
        organization.setTriggerDefinitions(triggerDefs);

        Organization org2 = new Organization();
        org2.setKey(organization.getKey() + 1);
        List<TriggerDefinition> triggerDefs2 = new ArrayList<>();
        TriggerDefinition td2 = new TriggerDefinition();
        td2.setKey(td.getKey() + 1);
        td2.setType(TriggerType.START_BILLING_RUN);
        td2.setSuspendProcess(false);
        triggerDefs2.add(td2);
        org2.setTriggerDefinitions(triggerDefs2);

        organizations.add(org2);
        TriggerMessage tm = new TriggerMessage(TriggerType.START_BILLING_RUN,
                null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
        assertEquals(2, storedObjects.size());
        List<TriggerProcess> list = ParameterizedTypes.list(storedObjects,
                TriggerProcess.class);
        Set<Long> tpKeys = new HashSet<>();
        for (TriggerProcess triggerProcess : list) {
            tpKeys.add(Long
                    .valueOf(triggerProcess.getTriggerDefinition().getKey()));
        }
        assertTrue(tpKeys.contains(Long.valueOf(td.getKey())));
        assertTrue(tpKeys.contains(Long.valueOf(td2.getKey())));
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessages_ListNoFactoryAndNoQueue()
            throws Exception {
        tqs.qFactory = null;
        tqs.queue = null;
        tqs.sendAllNonSuspendingMessages(null);
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessages_ListNoFactory()
            throws Exception {
        tqs.qFactory = null;
        tqs.sendAllNonSuspendingMessages(null);
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessages_ListNoQueue()
            throws Exception {
        tqs.queue = null;
        tqs.sendAllNonSuspendingMessages(null);
    }

    @Test
    public void testSendAllNonSuspendingMessages_ListNoTriggerDef()
            throws Exception {
        List<Organization> orgs = Collections.emptyList();
        TriggerMessage triggerMessage = new TriggerMessage(
                TriggerType.ACTIVATE_SERVICE, null, orgs);
        tqs.sendAllNonSuspendingMessages(
                Collections.singletonList(triggerMessage));
        Assert.assertTrue(storedObjects.isEmpty());
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessages_ListNonUBK() throws Exception {
        throwsSaasNonUniqueBusinessKeyException = true;
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(false);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        List<Organization> orgs = Collections.singletonList(organization);
        TriggerMessage triggerMessage = new TriggerMessage(
                TriggerType.ADD_REVOKE_USER, null, orgs);
        tqs.sendAllNonSuspendingMessages(
                Collections.singletonList(triggerMessage));
        Assert.assertTrue(storedObjects.isEmpty());
    }

    @Test(expected = SaaSSystemException.class)
    public void testSendAllNonSuspendingMessages_ListJMSException()
            throws Exception {
        connFact.setThrowsJMSException(true);
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(false);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        List<Organization> orgs = Collections.singletonList(organization);
        TriggerMessage triggerMessage = new TriggerMessage(
                TriggerType.ADD_REVOKE_USER, null, orgs);
        tqs.sendAllNonSuspendingMessages(
                Collections.singletonList(triggerMessage));
        Assert.assertTrue(storedObjects.isEmpty());
    }

    @Test
    public void testSendAllNonSuspendingMessages_ListTwoEntries()
            throws Exception {
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(false);
        organization.setTriggerDefinitions(Collections.singletonList(td));
        List<Organization> orgs = Collections.singletonList(organization);
        TriggerMessage triggerMessage = new TriggerMessage(
                TriggerType.ADD_REVOKE_USER, null, orgs);
        List<TriggerMessage> messages = new ArrayList<>();
        messages.add(triggerMessage);
        messages.add(triggerMessage);
        tqs.sendAllNonSuspendingMessages(messages);
        assertEquals(2, storedObjects.size());
    }

    @Test
    public void sendAllNonSuspendingMessages_Bug10275() throws Exception {
        List<TriggerDefinition> triggerDefs = new ArrayList<>();
        TriggerDefinition td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        td.setSuspendProcess(false);
        triggerDefs.add(td);
        organization.setTriggerDefinitions(triggerDefs);
        TriggerMessage tm = new TriggerMessage(TriggerType.ADD_REVOKE_USER,
                null, organizations);
        tqs.sendAllNonSuspendingMessages(Collections.singletonList(tm));
        Assert.assertFalse(storedObjects.isEmpty());
        Assert.assertTrue(storedObjects.get(0) instanceof TriggerProcess);
        TriggerProcess storedProcess = (TriggerProcess) storedObjects.get(0);
        Assert.assertEquals(TriggerProcessStatus.INITIAL,
                storedProcess.getStatus());
    }

}
