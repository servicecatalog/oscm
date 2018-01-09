/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 16.06.2010
 *
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.TriggerProcessParameterType;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.TriggerProcessStatusException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.*;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.ServiceProvisioningServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.assembler.TriggerProcessAssembler;
import org.oscm.triggerservice.local.TriggerServiceLocal;

import com.google.common.collect.Lists;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

public class TriggerServiceBeanIT extends EJBTestBase {

    private static final int RESULT_OK = 0;
    private static final int RESULT_SAAS_APPLICATION_EXCEPTION = 1;
    private static final int RESULT_SAAS_SYSTEM_EXCEPTION = 2;
    private static final int RESULT_UNSUPPORTED_OPERATION = -1;

    protected DataService mgr;
    protected TriggerService triggerService;
    protected TriggerServiceLocal triggerServiceLocal;

    private long triggerProcessKey;
    private long triggerProcessKeySubScribeToService;
    private long result = RESULT_OK;
    private String adminKey;
    private String adminKey1;
    private String userKey;
    private String userKey2;
    private String userKey3;
    private String wrongUserKey;
    private LocalizerServiceLocal localizer;

    private List<Long> triggerPrcKeySuccess;
    private List<Long> triggerPrcKeyRollbackCheck;
    private TriggerProcess triggerProcessSubToService;
    private VOService serviceForTriggerProcess;

    private long longParamKey = 0;
    private long stringParamKey = 1;
    private long durationParamKey = 2;

    @Override
    public void setup(final TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new AccountServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new ServiceProvisioningServiceStub());
        SubscriptionServiceLocal subServiceMock = Mockito
                .mock(SubscriptionServiceLocal.class);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation)
                    throws ObjectNotFoundException {
                if (result == RESULT_SAAS_APPLICATION_EXCEPTION) {
                    throw new ObjectNotFoundException();
                } else if (result == RESULT_SAAS_SYSTEM_EXCEPTION) {
                    throw new SaaSSystemException("test");
                } else if (result == RESULT_OK) {
                    return null;
                }
                throw new UnsupportedOperationException();
            }
        }).when(subServiceMock)
                .addRevokeUserInt(Mockito.any(TriggerProcess.class));

        Mockito.doThrow(new UnsupportedOperationException())
                .when(subServiceMock)
                .modifySubscriptionInt(Mockito.any(TriggerProcess.class));

        Mockito.doThrow(new UnsupportedOperationException())
                .when(subServiceMock)
                .subscribeToServiceInt(Mockito.any(TriggerProcess.class));
        Mockito.doThrow(new UnsupportedOperationException())
                .when(subServiceMock)
                .unsubscribeFromServiceInt(Mockito.any(TriggerProcess.class));

        Mockito.doThrow(new UnsupportedOperationException())
                .when(subServiceMock)
                .upgradeSubscriptionInt(Mockito.any(TriggerProcess.class));

        container.addBean(subServiceMock);

        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new IdentityServiceStub());
        container.addBean(new TriggerServiceBean());

        mgr = container.get(DataService.class);

        triggerService = container.get(TriggerService.class);
        triggerServiceLocal = container.get(TriggerServiceLocal.class);

        localizer = container.get(LocalizerServiceLocal.class);

        container.login("setup", ROLE_ORGANIZATION_ADMIN);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BaseAdmUmTest.createPaymentTypes(mgr);
                Organization org = Organizations.createOrganization(mgr);
                PlatformUser admin = Organizations.createUserForOrg(mgr, org,
                        true, "admin");
                adminKey = String.valueOf(admin.getKey());
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        false, "user");
                userKey = String.valueOf(user.getKey());

                // For delete trigger definition tests
                Organization org2 = Organizations.createOrganization(mgr);
                PlatformUser user2 = Organizations.createUserForOrg(mgr, org2,
                        false, "user2");
                userKey2 = String.valueOf(user2.getKey());

                admin = Organizations.createUserForOrg(mgr,
                        Organizations.createOrganization(mgr), true, "admin");
                wrongUserKey = String.valueOf(admin.getKey());

                // For get all actions for organization by subscriptionId tests
                Organization org3 = Organizations.createOrganization(mgr);
                PlatformUser user3 = Organizations.createUserForOrg(mgr, org3,
                        false, "user3");
                userKey3 = String.valueOf(user3.getKey());

                Organization org1 = Organizations.createOrganization(mgr);
                PlatformUser admin1 = Organizations.createUserForOrg(mgr, org1,
                    true, "admin1");
                adminKey1 = String.valueOf(admin1.getKey());
                return null;
            }
        });

        container.login(userKey, ROLE_ORGANIZATION_ADMIN);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.ADD_REVOKE_USER);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName(def.getType().name());
                mgr.persist(def);

                triggerProcessSubToService = new TriggerProcess();
                triggerProcessSubToService.setTriggerDefinition(def);
                triggerProcessSubToService
                        .setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
                triggerProcessSubToService.setUser(mgr.getCurrentUser());
                triggerProcessSubToService
                        .setActivationDate(System.currentTimeMillis());
                mgr.persist(triggerProcessSubToService);
                mgr.flush();

                triggerProcessKey = triggerProcessSubToService.getKey();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.SUBSCRIBE_TO_SERVICE);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName(def.getType().name());
                mgr.persist(def);

                serviceForTriggerProcess = new VOService();
                List<VOParameter> paramList = TriggerServiceBeanTestHelper
                        .getVOParameters(ParameterValueType.LONG, "",
                                Long.valueOf(0L), Long.valueOf(100L));
                paramList.get(0).getParameterDefinition().setKey(longParamKey);
                paramList.addAll(TriggerServiceBeanTestHelper
                        .getVOParameters(ParameterValueType.DURATION, ""));
                paramList.get(1).getParameterDefinition()
                        .setKey(durationParamKey);
                paramList.addAll(TriggerServiceBeanTestHelper
                        .getVOParameters(ParameterValueType.STRING, ""));
                paramList.get(2).getParameterDefinition()
                        .setKey(stringParamKey);
                serviceForTriggerProcess.setParameters(paramList);

                TriggerProcess triggerProcess = new TriggerProcess();
                triggerProcess.setTriggerDefinition(def);
                triggerProcess
                        .setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
                triggerProcess.setUser(mgr.getCurrentUser());
                triggerProcess.setActivationDate(System.currentTimeMillis());
                triggerProcess.addTriggerProcessParameter(
                        org.oscm.types.enumtypes.TriggerProcessParameterName.SUBSCRIPTION,
                        new VOSubscriptionDetails());
                triggerProcess.addTriggerProcessParameter(
                        org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT,
                        serviceForTriggerProcess);
                mgr.persist(triggerProcess);
                mgr.flush();

                triggerProcessKeySubScribeToService = triggerProcess.getKey();

                return null;
            }
        });

        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess triggerProcess = mgr
                        .getReference(TriggerProcess.class, triggerProcessKey);
                TriggerDefinition def = triggerProcess.getTriggerDefinition();

                triggerProcess = new TriggerProcess();
                triggerProcess.setTriggerDefinition(def);
                triggerProcess
                        .setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
                triggerProcess.setUser(mgr.getCurrentUser());
                triggerProcess
                        .setActivationDate(System.currentTimeMillis() - 1000);
                mgr.persist(triggerProcess);
                mgr.flush();

                return null;
            }
        });

        // create the test data for the test deleting the trigger definition
        container.login(userKey2, ROLE_ORGANIZATION_ADMIN);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.ADD_REVOKE_USER);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName("Test");
                mgr.persist(def);

                TriggerProcess prcApproved = new TriggerProcess();
                prcApproved.setTriggerDefinition(def);
                prcApproved.setState(TriggerProcessStatus.APPROVED);
                prcApproved.setUser(mgr.getCurrentUser());
                prcApproved.setActivationDate(System.currentTimeMillis());
                mgr.persist(prcApproved);

                TriggerProcess prcCancelled = new TriggerProcess();
                prcCancelled.setTriggerDefinition(def);
                prcCancelled.setState(TriggerProcessStatus.CANCELLED);
                prcCancelled.setUser(mgr.getCurrentUser());
                prcCancelled.setActivationDate(System.currentTimeMillis());
                mgr.persist(prcCancelled);

                TriggerProcess prcError = new TriggerProcess();
                prcError.setTriggerDefinition(def);
                prcError.setState(TriggerProcessStatus.ERROR);
                prcError.setUser(mgr.getCurrentUser());
                prcError.setActivationDate(System.currentTimeMillis());
                mgr.persist(prcError);

                TriggerProcess prcFailed = new TriggerProcess();
                prcFailed.setTriggerDefinition(def);
                prcFailed.setState(TriggerProcessStatus.FAILED);
                prcFailed.setUser(mgr.getCurrentUser());
                prcFailed.setActivationDate(System.currentTimeMillis());
                mgr.persist(prcFailed);

                TriggerProcess prcNotified = new TriggerProcess();
                prcNotified.setTriggerDefinition(def);
                prcNotified.setState(TriggerProcessStatus.NOTIFIED);
                prcNotified.setUser(mgr.getCurrentUser());
                prcNotified.setActivationDate(System.currentTimeMillis());
                mgr.persist(prcNotified);

                TriggerProcess prcRejected = new TriggerProcess();
                prcRejected.setTriggerDefinition(def);
                prcRejected.setState(TriggerProcessStatus.REJECTED);
                prcRejected.setUser(mgr.getCurrentUser());
                prcRejected.setActivationDate(System.currentTimeMillis());
                mgr.persist(prcRejected);
                mgr.flush();

                triggerPrcKeySuccess = new ArrayList<>();
                triggerPrcKeySuccess.add(Long.valueOf(prcApproved.getKey()));
                triggerPrcKeySuccess.add(Long.valueOf(prcCancelled.getKey()));
                triggerPrcKeySuccess.add(Long.valueOf(prcError.getKey()));
                triggerPrcKeySuccess.add(Long.valueOf(prcFailed.getKey()));
                triggerPrcKeySuccess.add(Long.valueOf(prcNotified.getKey()));
                triggerPrcKeySuccess.add(Long.valueOf(prcRejected.getKey()));

                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.ADD_REVOKE_USER);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName("Test");
                mgr.persist(def);

                TriggerProcess prc = new TriggerProcess();
                prc.setTriggerDefinition(def);
                prc.setState(TriggerProcessStatus.INITIAL);
                prc.setUser(mgr.getCurrentUser());
                prc.setActivationDate(System.currentTimeMillis());
                mgr.persist(prc);
                mgr.flush();

                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.ADD_REVOKE_USER);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName("Test");
                mgr.persist(def);

                TriggerProcess prc = new TriggerProcess();
                prc.setTriggerDefinition(def);
                prc.setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
                prc.setUser(mgr.getCurrentUser());
                prc.setActivationDate(System.currentTimeMillis());
                mgr.persist(prc);
                mgr.flush();

                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.ADD_REVOKE_USER);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName("Test");
                mgr.persist(def);

                TriggerProcess prcApproved = new TriggerProcess();
                prcApproved.setTriggerDefinition(def);
                prcApproved.setState(TriggerProcessStatus.APPROVED);
                prcApproved.setUser(mgr.getCurrentUser());
                prcApproved.setActivationDate(System.currentTimeMillis());
                mgr.persist(prcApproved);

                TriggerProcess prcWaiting = new TriggerProcess();
                prcWaiting.setTriggerDefinition(def);
                prcWaiting.setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
                prcWaiting.setUser(mgr.getCurrentUser());
                prcWaiting.setActivationDate(System.currentTimeMillis() + 1);
                mgr.persist(prcWaiting);
                mgr.flush();

                triggerPrcKeyRollbackCheck = new ArrayList<>();
                triggerPrcKeyRollbackCheck
                        .add(Long.valueOf(prcApproved.getKey()));
                triggerPrcKeyRollbackCheck
                        .add(Long.valueOf(prcWaiting.getKey()));

                return null;
            }
        });

        container.login(userKey3, ROLE_ORGANIZATION_ADMIN);
        // create a "add revoke user" trigger
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.ADD_REVOKE_USER);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName("TestAdd");
                mgr.persist(def);

                TriggerProcess prc = new TriggerProcess();
                prc.setTriggerDefinition(def);
                prc.setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
                prc.setUser(mgr.getCurrentUser());
                prc.setActivationDate(System.currentTimeMillis());
                prc.addTriggerProcessParameter(
                        org.oscm.types.enumtypes.TriggerProcessParameterName.SUBSCRIPTION,
                        "subId");
                mgr.persist(prc);
                mgr.flush();

                return null;
            }
        });

        // create a "terminate subscription" trigger
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.UNSUBSCRIBE_FROM_SERVICE);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName("TestTerminate");
                mgr.persist(def);

                TriggerProcess prc = new TriggerProcess();
                prc.setTriggerDefinition(def);
                prc.setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
                prc.setUser(mgr.getCurrentUser());
                prc.setActivationDate(System.currentTimeMillis());
                prc.addTriggerProcessParameter(
                        org.oscm.types.enumtypes.TriggerProcessParameterName.SUBSCRIPTION,
                        "subId");
                mgr.persist(prc);
                mgr.flush();

                return null;
            }
        });

    }

    @Test
    public void testApproveAction() throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);

        result = RESULT_OK;
        triggerService.approveAction(triggerProcessKey);

        validateTriggerProcessStatus(TriggerProcessStatus.APPROVED);
    }

    @Test
    public void testApproveActionAppEx() throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);

        result = RESULT_SAAS_APPLICATION_EXCEPTION;
        try {
            triggerService.approveAction(triggerProcessKey);
            Assert.fail();
        } catch (SaaSApplicationException e) {
            // expected
        }
        validateTriggerProcessStatus(TriggerProcessStatus.FAILED);
    }

    @Test
    public void testApproveActionSysEx() throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);

        result = RESULT_SAAS_SYSTEM_EXCEPTION;
        try {
            triggerService.approveAction(triggerProcessKey);
            Assert.fail();
        } catch (EJBException e) {
            // expected
        }
        validateTriggerProcessStatus(TriggerProcessStatus.FAILED);
        validateReasonInternalError();
    }

    private void validateReasonInternalError() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess tp = mgr.getReference(TriggerProcess.class,
                        triggerProcessKey);
                VOTriggerProcess voTp = TriggerProcessAssembler
                        .toVOTriggerProcess(tp,
                                new LocalizerFacade(localizer, "en"));
                Assert.assertEquals(
                        "Internal server error. Please see log file for details.",
                        voTp.getReason());
                voTp = TriggerProcessAssembler.toVOTriggerProcess(tp,
                        new LocalizerFacade(localizer, "de"));
                Assert.assertEquals(
                        "Internal server error. Please see log file for details.",
                        voTp.getReason());
                return null;
            }
        });
    }

    @Test(expected = TriggerProcessStatusException.class)
    public void testApproveActionWrongStatus() throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);

        triggerService.cancelActions(
                Collections.singletonList(Long.valueOf(triggerProcessKey)),
                null);
        triggerService.approveAction(triggerProcessKey);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testApproveActionWrongOrganization() throws Exception {
        container.login(wrongUserKey, ROLE_ORGANIZATION_ADMIN);

        triggerService.approveAction(triggerProcessKey);
    }

    private long approveAction(TriggerType type) throws Exception {
        long key = createTriggerProcess(type);
        try {
            triggerService.approveAction(key);
            validateTriggerProcessStatus(key, TriggerProcessStatus.APPROVED);
            Assert.fail();
        } catch (EJBException e) {
            Assert.assertNotNull(
                    "TriggerServiceBean.execute(TriggerProcess) implement case "
                            + type + ":",
                    e.getCausedByException().getCause());
            Assert.assertEquals(type.toString(),
                    UnsupportedOperationException.class,
                    e.getCausedByException().getCause().getClass());
        }
        return key;
    }

    @Test
    public void testApproveActionExecute() throws Exception {
        result = RESULT_UNSUPPORTED_OPERATION;
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);

        List<Long> keys = new ArrayList<>();
        for (TriggerType type : TriggerType.values()) {
            if (type.isSuspendProcess()) {
                keys.add(Long.valueOf(approveAction(type)));
            }
        }
        triggerService.deleteActions(keys);

        // the two trigger processes from the setup
        Assert.assertEquals(3,
                triggerService.getAllActionsForOrganization().size());
    }

    @Test(expected = SaaSSystemException.class)
    public void testApproveActionWrongType() throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);

        try {
            long key = createTriggerProcess(TriggerType.START_BILLING_RUN);
            triggerService.approveAction(key);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testRejectAction() throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);
        List<VOLocalizedText> reason = initializeReason();
        triggerService.rejectAction(triggerProcessKey, reason);
        validateTriggerProcessStatus(TriggerProcessStatus.REJECTED);
        validateReason();
    }

    @Test
    public void testCancelActions() throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);
        List<VOLocalizedText> reason = initializeReason();
        triggerService.cancelActions(
                Collections.singletonList(Long.valueOf(triggerProcessKey)),
                reason);
        validateTriggerProcessStatus(TriggerProcessStatus.CANCELLED);
        validateReason();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeleteAction() throws Exception {
        container.login(userKey);

        triggerService.cancelActions(
                Collections.singletonList(Long.valueOf(triggerProcessKey)),
                null);
        validateTriggerProcessStatus(TriggerProcessStatus.CANCELLED);
        triggerService.deleteActions(
                Collections.singletonList(Long.valueOf(triggerProcessKey)));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mgr.getReference(TriggerProcess.class, triggerProcessKey);
                return null;
            }
        });
    }

    @Test
    public void testGetAllActions() throws Exception {
        container.login(userKey);

        List<VOTriggerProcess> list = triggerService.getAllActions();
        Assert.assertEquals(2, list.size());

        String orgId = list.get(1).getUser().getOrganizationId();
        Assert.assertEquals("user_" + orgId, list.get(1).getUser().getUserId());
        Assert.assertEquals(TriggerType.ADD_REVOKE_USER,
                list.get(1).getTriggerDefinition().getType());
        Assert.assertEquals("", list.get(1).getReason());
        Assert.assertEquals(TriggerProcessStatus.WAITING_FOR_APPROVAL,
                list.get(1).getStatus());
    }

    @Test
    public void testGetAllActionsForOrganization() throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN);

        List<VOTriggerProcess> list = triggerService
                .getAllActionsForOrganization();
        Assert.assertEquals(3, list.size());

        String orgId = list.get(1).getUser().getOrganizationId();
        Assert.assertEquals("user_" + orgId, list.get(0).getUser().getUserId());
        Assert.assertEquals("admin_" + orgId,
                list.get(2).getUser().getUserId());
    }

    @Test
    public void testGetAllActionsForOrganizationNonAdmin() throws Exception {
        container.login(userKey);

        List<VOTriggerProcess> list = triggerService
                .getAllActionsForOrganization();
        Assert.assertEquals(3, list.size());
    }

    @Test
    public void testGetAllActionsForOrganizationRelatedSubscription()
            throws Exception {
        container.login(adminKey1, ROLE_ORGANIZATION_ADMIN);
        // create a "add revoke user" trigger
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.ADD_REVOKE_USER);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName("TestAdd");
                mgr.persist(def);

                TriggerProcess prc = new TriggerProcess();
                prc.setTriggerDefinition(def);
                prc.setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
                prc.setUser(mgr.getCurrentUser());
                prc.setActivationDate(System.currentTimeMillis());

                VOSubscription subscription = new VOSubscription();
                subscription.setSubscriptionId("subscriptionId");
                prc.addTriggerProcessParameter(
                    TriggerProcessParameterName.SUBSCRIPTION, subscription);
                VOService service = new VOService();
                service.setServiceId("serviceId");
                prc.addTriggerProcessParameter(
                    TriggerProcessParameterName.PRODUCT, service);
                mgr.persist(prc);
                mgr.flush();

                return null;
            }
        });

        List<VOTriggerProcess> list = triggerService
                .getAllActionsForOrganizationRelatedSubscription();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(list.get(0).getSubscription().getSubscriptionId(), "subscriptionId");
        Assert.assertEquals(list.get(0).getService().getServiceId(), "serviceId");
    }

    @Test
    public void testGetAllSubscriptionActionsForOrganizationBySubId()
            throws Exception {
        container.login(userKey3, ROLE_ORGANIZATION_ADMIN);

        List<VOTriggerProcess> list = triggerServiceLocal
                .getAllActionsForSubscription("subId");
        Assert.assertEquals(2, list.size());
    }

    @Test(expected = EJBException.class)
    public void testUpdateActionForNonAdmin() throws Exception {
        // given
        container.login(userKey);

        // when
        triggerService.updateActionParameters(
                triggerProcessKeySubScribeToService,
                Lists.<VOTriggerProcessParameter> newArrayList());
    }

    @Test
    public void testUpdateParamActionForAdmin() throws Exception {
        // given
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerProcessParameter triggerProcessParameter = new VOTriggerProcessParameter();

        VOTriggerProcess voTriggerProcess = getTriggerProcess(
                triggerProcessKeySubScribeToService);

        final String PARAM_VALUE = "123";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.STRING, PARAM_VALUE, true);
        parameterList.get(0).getParameterDefinition().setKey(stringParamKey);
        voTriggerProcess.getService().setParameters(parameterList);

        triggerProcessParameter.setValue(voTriggerProcess.getService());
        triggerProcessParameter.setTriggerProcessKey(
                Long.valueOf(triggerProcessKeySubScribeToService));
        triggerProcessParameter.setType(TriggerProcessParameterType.PRODUCT);

        List<VOTriggerProcessParameter> parameters = Lists
                .newArrayList(triggerProcessParameter);

        // when
        triggerService.updateActionParameters(
                triggerProcessKeySubScribeToService, parameters);
        final List<VOParameter> updatedPI = new ArrayList<>();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess reference = mgr.getReference(
                        TriggerProcess.class,
                        triggerProcessKeySubScribeToService);
                VOService service = reference
                        .getParamValueForName(
                                org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT)
                        .getValue(VOService.class);
                updatedPI.addAll(service.getParameters());
                return null;
            }
        });

        // then
        Assert.assertEquals(3, updatedPI.size());
        Assert.assertEquals(parameterList.get(0).getValue(),
                updatedPI.get(0).getValue());
    }

    @Test(expected = ValidationException.class)
    public void testUpdateParamActionExceptionForAdmin() throws Exception {
        // given
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerProcess voTriggerProcess = getTriggerProcess(
                triggerProcessKeySubScribeToService);

        VOTriggerProcessParameter triggerProcessParameter = new VOTriggerProcessParameter();

        final String PARAM_VALUE = "123";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.LONG, PARAM_VALUE,
                        Long.valueOf(1L), Long.valueOf(100L));
        parameterList.get(0).getParameterDefinition().setKey(longParamKey);
        voTriggerProcess.getService().setParameters(parameterList);

        triggerProcessParameter.setValue(voTriggerProcess.getService());
        triggerProcessParameter
                .setTriggerProcessKey(Long.valueOf(voTriggerProcess.getKey()));
        triggerProcessParameter.setType(TriggerProcessParameterType.valueOf(
                org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT
                        .name()));

        List<VOTriggerProcessParameter> parameters = Lists
                .newArrayList(triggerProcessParameter);

        // when
        triggerService.updateActionParameters(voTriggerProcess.getKey(),
                parameters);
    }

    /**
     * Test will check bugfix for 11739 and also 11731
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateActionParameterBugfix11739() throws Exception {
        // given
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerProcess updatedProcess = getTriggerProcess(
                triggerProcessKeySubScribeToService);
        VOTriggerProcessParameter triggerProcessParameter = new VOTriggerProcessParameter();

        final String PARAM_VALUE = "123";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.STRING, PARAM_VALUE, true);
        parameterList.get(0).getParameterDefinition().setKey(stringParamKey);
        updatedProcess.getService().setParameters(parameterList);

        triggerProcessParameter.setValue(updatedProcess.getService());
        triggerProcessParameter
                .setTriggerProcessKey(Long.valueOf(updatedProcess.getKey()));
        triggerProcessParameter.setType(TriggerProcessParameterType.valueOf(
                org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT
                        .name()));

        List<VOTriggerProcessParameter> parameters = Lists
                .newArrayList(triggerProcessParameter);

        // when
        triggerService.updateActionParameters(updatedProcess.getKey(),
                parameters);
    }

    @Test
    public void testUpdateActionDurationParamBugfix11779() throws Exception {
        // given
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerProcess updatedProcess = getTriggerProcess(
                triggerProcessKeySubScribeToService);
        VOTriggerProcessParameter triggerProcessParameter = new VOTriggerProcessParameter();

        final String PARAM_VALUE = "518400000";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.DURATION, PARAM_VALUE,
                        true);
        parameterList.get(0).getParameterDefinition().setKey(durationParamKey);
        updatedProcess.getService().setParameters(parameterList);

        triggerProcessParameter.setValue(updatedProcess.getService());
        triggerProcessParameter
                .setTriggerProcessKey(Long.valueOf(updatedProcess.getKey()));
        triggerProcessParameter.setType(TriggerProcessParameterType.valueOf(
                org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT
                        .name()));

        List<VOTriggerProcessParameter> parameters = Lists
                .newArrayList(triggerProcessParameter);

        // when
        triggerService.updateActionParameters(updatedProcess.getKey(),
                parameters);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateActionDurationParamExceptionBugfix11779()
            throws Exception {
        // given
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerProcess updatedProcess = getTriggerProcess(
                triggerProcessKeySubScribeToService);
        VOTriggerProcessParameter triggerProcessParameter = new VOTriggerProcessParameter();

        final String PARAM_VALUE = "518400000.0";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.DURATION, PARAM_VALUE,
                        true);
        parameterList.get(0).getParameterDefinition().setKey(durationParamKey);
        updatedProcess.getService().setParameters(parameterList);

        triggerProcessParameter.setValue(updatedProcess.getService());
        triggerProcessParameter
                .setTriggerProcessKey(Long.valueOf(updatedProcess.getKey()));
        triggerProcessParameter.setType(TriggerProcessParameterType.valueOf(
                org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT
                        .name()));

        List<VOTriggerProcessParameter> parameters = Lists
                .newArrayList(triggerProcessParameter);

        // when
        triggerService.updateActionParameters(updatedProcess.getKey(),
                parameters);
    }

    /**
     * Default long range is set to 0-100 so this should result in exception
     * even so parameter definition for updated param is set to range 200-600.
     * Current definition should be retrieved from DB anyway.
     * 
     * @throws Exception
     */
    @Test(expected = ValidationException.class)
    public void testUpdateActionLongExceptionBugfix11782() throws Exception {
        // given
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerProcess updatedProcess = getTriggerProcess(
                triggerProcessKeySubScribeToService);
        VOTriggerProcessParameter triggerProcessParameter = new VOTriggerProcessParameter();

        final String PARAM_VALUE = "515";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.LONG, PARAM_VALUE,
                        Long.valueOf(200L), Long.valueOf(600L));
        parameterList.get(0).getParameterDefinition().setKey(longParamKey);
        updatedProcess.getService().setParameters(parameterList);

        triggerProcessParameter.setValue(updatedProcess.getService());
        triggerProcessParameter
                .setTriggerProcessKey(Long.valueOf(updatedProcess.getKey()));
        triggerProcessParameter.setType(TriggerProcessParameterType.valueOf(
                org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT
                        .name()));

        List<VOTriggerProcessParameter> parameters = Lists
                .newArrayList(triggerProcessParameter);

        // when
        triggerService.updateActionParameters(updatedProcess.getKey(),
                parameters);
    }

    @Test
    public void testUpdateActionLongBugfix11782() throws Exception {
        // given
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerProcess updatedProcess = getTriggerProcess(
                triggerProcessKeySubScribeToService);
        VOTriggerProcessParameter triggerProcessParameter = new VOTriggerProcessParameter();

        final String PARAM_VALUE = "50";
        List<VOParameter> parameterList = TriggerServiceBeanTestHelper
                .getVOParameters(ParameterValueType.LONG, PARAM_VALUE,
                        Long.valueOf(200L), Long.valueOf(600L));
        parameterList.get(0).getParameterDefinition().setKey(longParamKey);
        updatedProcess.getService().setParameters(parameterList);

        triggerProcessParameter.setValue(updatedProcess.getService());
        triggerProcessParameter
                .setTriggerProcessKey(Long.valueOf(updatedProcess.getKey()));
        triggerProcessParameter.setType(TriggerProcessParameterType.valueOf(
                org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT
                        .name()));

        List<VOTriggerProcessParameter> parameters = Lists
                .newArrayList(triggerProcessParameter);

        // when
        triggerService.updateActionParameters(updatedProcess.getKey(),
                parameters);
    }

    @Test
    public void testGetActionParameterForNonAdmin() throws Exception {
        // given
        container.login(userKey);

        // when
        VOTriggerProcessParameter result = triggerService.getActionParameter(
                triggerProcessKeySubScribeToService,
                TriggerProcessParameterType.PRODUCT);

        // then
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetActionParameterForAdmin() throws Exception {
        // given
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        // when
        VOTriggerProcessParameter result = triggerService.getActionParameter(
                triggerProcessKeySubScribeToService,
                TriggerProcessParameterType.PRODUCT);

        // then
        Assert.assertNotNull(result);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetActionParameterNotExist() throws Exception {
        // given
        container.login(userKey, ROLE_ORGANIZATION_ADMIN);

        // when
        triggerService.getActionParameter(0,
                TriggerProcessParameterType.PRODUCT);

        // then exception
    }

    @Test(expected = EJBException.class)
    public void testGetActionParameterNullParamName() throws Exception {
        // given
        container.login(userKey);

        // when
        triggerService.getActionParameter(triggerProcessKeySubScribeToService,
                null);

        // then EJBException caused by IllegalArgumentException
    }

    /**
     * Creates a localized reason information.
     * 
     * @return A list of localized texts.
     */
    private List<VOLocalizedText> initializeReason() {
        VOLocalizedText enText = new VOLocalizedText("en", "enReason");
        VOLocalizedText deText = new VOLocalizedText("de", "deReason");
        List<VOLocalizedText> reason = new ArrayList<>();
        reason.add(enText);
        reason.add(deText);
        return reason;
    }

    /**
     * VAlidates that the values initialized in {@link #initializeReason()} are
     * provided with the value object.
     */
    private void validateReason() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess tp = mgr.getReference(TriggerProcess.class,
                        triggerProcessKey);
                VOTriggerProcess voTp = TriggerProcessAssembler
                        .toVOTriggerProcess(tp,
                                new LocalizerFacade(localizer, "en"));
                Assert.assertEquals("enReason", voTp.getReason());
                voTp = TriggerProcessAssembler.toVOTriggerProcess(tp,
                        new LocalizerFacade(localizer, "de"));
                Assert.assertEquals("deReason", voTp.getReason());
                return null;
            }
        });
    }

    private long createTriggerProcess(final TriggerType type) throws Exception {
        return runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(type);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(false);
                def.setName("Test");
                mgr.persist(def);

                TriggerProcess triggerProcess = new TriggerProcess();
                triggerProcess.setTriggerDefinition(def);
                triggerProcess
                        .setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
                triggerProcess.setUser(mgr.getCurrentUser());
                mgr.persist(triggerProcess);
                def.setName("Test");
                mgr.flush();

                return Long.valueOf(triggerProcess.getKey());
            }
        }).longValue();
    }

    private VOTriggerProcess getTriggerProcess(final long key)
            throws Exception {
        return runTX(new Callable<VOTriggerProcess>() {
            @Override
            public VOTriggerProcess call() throws ObjectNotFoundException {
                TriggerProcess tp = mgr.getReference(TriggerProcess.class, key);
                return TriggerProcessAssembler.toVOTriggerProcess(tp,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }

    private void validateTriggerProcessStatus(final TriggerProcessStatus status)
            throws Exception {
        validateTriggerProcessStatus(triggerProcessKey, status);
    }

    private void validateTriggerProcessStatus(final long key,
            final TriggerProcessStatus status) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess triggerProcess = mgr
                        .getReference(TriggerProcess.class, key);
                Assert.assertEquals(status, triggerProcess.getStatus());
                return null;
            }
        });
    }

}
