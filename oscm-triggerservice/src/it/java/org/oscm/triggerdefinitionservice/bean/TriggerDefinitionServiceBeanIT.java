/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.triggerdefinitionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.internal.intf.TriggerDefinitionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TriggerDefinitionDataException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.TriggerDefinitions;
import org.oscm.test.data.TriggerProcesses;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.ServiceProvisioningServiceStub;
import org.oscm.triggerservice.bean.TriggerDefinitionServiceBean;

@SuppressWarnings("boxing")
public class TriggerDefinitionServiceBeanIT extends EJBTestBase {

    private TriggerType[] allowedTriggersForSupplier = {
            TriggerType.ACTIVATE_SERVICE, TriggerType.DEACTIVATE_SERVICE,
            TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
            TriggerType.SAVE_PAYMENT_CONFIGURATION,
            TriggerType.START_BILLING_RUN, TriggerType.SUBSCRIPTION_CREATION,
            TriggerType.SUBSCRIPTION_MODIFICATION,
            TriggerType.SUBSCRIPTION_TERMINATION,
            TriggerType.REGISTER_OWN_USER };

    private TriggerType[] allowedTriggersForCustomer = {
            TriggerType.ADD_REVOKE_USER, TriggerType.MODIFY_SUBSCRIPTION,
            TriggerType.SUBSCRIBE_TO_SERVICE,
            TriggerType.UNSUBSCRIBE_FROM_SERVICE,
            TriggerType.UPGRADE_SUBSCRIPTION, TriggerType.START_BILLING_RUN,
            TriggerType.REGISTER_OWN_USER };

    private Organization supp1;
    private Organization techProv;
    private Organization supp3;
    private Organization platformOperatorOrg;
    private Organization customerOrg;

    PlatformUser platformOperatorUser;

    private DataService mgr;

    private TriggerDefinitionService triggerDefinitionService;

    private long supplier1Key;
    private long techProvKey;
    private long supplier3Key;
    private long platformOperatorUserKey;
    private long customerKey;
    private long nonAdminKey;
    PlatformUser createUserForOrg;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        mgr = container.get(DataService.class);

        container.addBean(new CommunicationServiceStub());
        container.addBean(new AccountServiceStub());
        container.addBean(new ServiceProvisioningServiceStub());
        container.addBean(Mockito.mock(SubscriptionServiceLocal.class));
        container.addBean(new LocalizerServiceStub());
        TriggerDefinitionServiceBean tsb = new TriggerDefinitionServiceBean();
        container.addBean(tsb);
        triggerDefinitionService = container
                .get(TriggerDefinitionService.class);
        // create supplier
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supp1 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);

                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, supp1, false, "admin");
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.ORGANIZATION_ADMIN);
                supplier1Key = createUserForOrg.getKey();

                // create a second user for that org with roles=SERVICE_MANAGER,
                // TECHNOLOGY_MANAGER, MARKETPLACE_OWNER;
                PlatformUser nonAdmin = PlatformUsers.createUser(mgr,
                        "nonAdmin", supp1);
                PlatformUsers.grantRoles(mgr, nonAdmin,
                        UserRoleType.SERVICE_MANAGER,
                        UserRoleType.TECHNOLOGY_MANAGER,
                        UserRoleType.MARKETPLACE_OWNER);
                nonAdminKey = nonAdmin.getKey();

                // technologyProvider
                techProv = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                createUserForOrg = Organizations.createUserForOrg(mgr, techProv,
                        false, "userS2");
                techProvKey = createUserForOrg.getKey();
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.ORGANIZATION_ADMIN);

                supp3 = Organizations.createOrganization(mgr);

                createUserForOrg = Organizations.createUserForOrg(mgr, supp3,
                        false, "userS3");
                supplier3Key = createUserForOrg.getKey();
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.ORGANIZATION_ADMIN);

                customerOrg = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                createUserForOrg = Organizations.createUserForOrg(mgr,
                        customerOrg, false, "userS4");
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.ORGANIZATION_ADMIN);
                customerKey = createUserForOrg.getKey();

                return null;
            }
        });
        createPlatformOperator();
    }

    private void createPlatformOperator() throws Exception {
        if (platformOperatorOrg == null) {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    platformOperatorOrg = Organizations.createOrganization(mgr,
                            OrganizationRoleType.PLATFORM_OPERATOR);

                    platformOperatorUser = Organizations.createUserForOrg(mgr,
                            platformOperatorOrg, true, "Administrator");
                    PlatformUsers.grantRoles(mgr, platformOperatorUser,
                            UserRoleType.PLATFORM_OPERATOR);

                    platformOperatorUserKey = platformOperatorUser.getKey();
                    return null;
                }
            });
        }
    }

    private VOTriggerDefinition buildTriggerVO(boolean suspending) {
        VOTriggerDefinition vo = new VOTriggerDefinition();
        vo.setType(TriggerType.ACTIVATE_SERVICE);
        vo.setTargetType(TriggerTargetType.WEB_SERVICE);
        vo.setTarget("http://localhost");
        vo.setSuspendProcess(suspending);
        vo.setName("NAME");
        return vo;
    }

    private VOTriggerDefinition createTriggerForSupp1() throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);
        VOTriggerDefinition def1 = buildTriggerVO(true);
        createTriggerDefinition(def1);
        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(1, list.size());
        VOTriggerDefinition trigger = list.get(0);
        container.logout();
        return trigger;
    }

    private void createTriggerDefinition(VOTriggerDefinition vo)
            throws Exception {
        try {
            triggerDefinitionService.createTriggerDefinition(vo);
        } catch (EJBException ex) {
            throw ex.getCausedByException();
        }
    }

    private void updateTriggerDefinition(VOTriggerDefinition vo)
            throws Exception {
        try {
            triggerDefinitionService.updateTriggerDefinition(vo);
        } catch (EJBException ex) {
            throw ex.getCausedByException();
        }
    }

    private void deleteTriggerDefinition(long key) throws Exception {
        try {
            triggerDefinitionService.deleteTriggerDefinition(key);
        } catch (EJBException ex) {
            throw ex.getCausedByException();
        }
    }

    @Test
    public void testCreateTriggerDefinition() throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition vo = buildTriggerVO(true);

        createTriggerDefinition(vo);

        List<VOTriggerDefinition> triggerDefinitions = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertNotNull(triggerDefinitions);
        Assert.assertEquals(1, triggerDefinitions.size());
        VOTriggerDefinition triggerDef = triggerDefinitions.get(0);
        Assert.assertNotNull(triggerDef);

        Assert.assertEquals(vo.getType(), triggerDef.getType());
        Assert.assertEquals(vo.getTargetType(), triggerDef.getTargetType());
        Assert.assertEquals(vo.getTarget(), triggerDef.getTarget());
        Assert.assertEquals(vo.isSuspendProcess(),
                triggerDef.isSuspendProcess());
        Assert.assertEquals(vo.getName(), triggerDef.getName());
    }

    @Test(expected = TriggerDefinitionDataException.class)
    public void testCreateTriggerDefinitionDuplicateSuspendProcess()
            throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);
        VOTriggerDefinition vo = buildTriggerVO(true);
        createTriggerDefinition(vo);
        List<VOTriggerDefinition> triggerDefinitions = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertNotNull(triggerDefinitions);
        createTriggerDefinition(vo);
    }

    @Test
    public void testUpdateTriggerDefinition() throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);
        VOTriggerDefinition vo = buildTriggerVO(true);

        createTriggerDefinition(vo);
        VOTriggerDefinition triggerDefinition = triggerDefinitionService
                .getTriggerDefinitions().get(0);

        triggerDefinition.setType(TriggerType.DEACTIVATE_SERVICE);
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setTarget("http://localhost:8080");
        triggerDefinition.setSuspendProcess(false);
        triggerDefinition.setName("NAME_NEU");
        updateTriggerDefinition(triggerDefinition);

        VOTriggerDefinition updated = triggerDefinitionService
                .getTriggerDefinitions().get(0);

        Assert.assertEquals(triggerDefinition.getType(), updated.getType());
        Assert.assertEquals(triggerDefinition.getTargetType(),
                updated.getTargetType());
        Assert.assertEquals(triggerDefinition.getTarget(), updated.getTarget());
        Assert.assertEquals(triggerDefinition.getName(), updated.getName());
        Assert.assertEquals(triggerDefinition.isSuspendProcess(),
                updated.isSuspendProcess());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateTriggerDefinition_wrong_authorization()
            throws Exception {

        // given
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);
        VOTriggerDefinition vo = buildTriggerVO(true);
        createTriggerDefinition(vo);
        VOTriggerDefinition triggerDefinition = triggerDefinitionService
                .getTriggerDefinitions().get(0);
        triggerDefinition.setType(TriggerType.SUBSCRIBE_TO_SERVICE);
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setTarget("http://localhost:8080");
        triggerDefinition.setSuspendProcess(false);
        triggerDefinition.setName("NAME_NEU");

        // when
        container.login(supplier3Key, ROLE_ORGANIZATION_ADMIN);
        updateTriggerDefinition(triggerDefinition);

        // then
        // OperationNotPermittedException
    }

    @Test(expected = TriggerDefinitionDataException.class)
    public void testUpdateTriggerDefinitionDuplicateSuspendProcess()
            throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition vo = buildTriggerVO(true);
        createTriggerDefinition(vo);

        vo.setSuspendProcess(false);
        createTriggerDefinition(vo);
        List<VOTriggerDefinition> triggerDefinitions = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertNotNull(triggerDefinitions);
        VOTriggerDefinition trigger1 = triggerDefinitions.get(0);
        VOTriggerDefinition trigger2 = triggerDefinitions.get(1);
        if (trigger1.isSuspendProcess()) {
            vo = trigger2;
        } else {
            vo = trigger1;
        }

        vo.setSuspendProcess(true);
        updateTriggerDefinition(vo);
    }

    @Test
    public void testDeleteTriggerDefinition() throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);
        VOTriggerDefinition def1 = buildTriggerVO(true);

        createTriggerDefinition(def1);
        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(1, list.size());
        VOTriggerDefinition trigger = list.get(0);
        deleteTriggerDefinition(trigger.getKey());
        List<VOTriggerDefinition> list2 = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(0, list2.size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testDeleteTriggerDefinition_wrong_authorization()
            throws Exception {

        // given
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);
        VOTriggerDefinition def1 = buildTriggerVO(true);
        createTriggerDefinition(def1);
        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(1, list.size());
        VOTriggerDefinition trigger = list.get(0);

        // when
        container.login(supplier3Key, ROLE_ORGANIZATION_ADMIN);
        deleteTriggerDefinition(trigger.getKey());

        // then
        // OperationNotPermittedException
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeleteTriggerDefinition_NonExisting() throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        deleteTriggerDefinition(0);
        List<VOTriggerDefinition> list2 = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(0, list2.size());
    }

    @Test
    public void testGetTriggerDefinitionsForOrganization() throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);
        VOTriggerDefinition def1 = buildTriggerVO(true);
        VOTriggerDefinition def2 = buildTriggerVO(false);

        createTriggerDefinition(def1);
        createTriggerDefinition(def2);

        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();

        Assert.assertEquals(2, list.size());
        Assert.assertEquals(def1.getType(), list.get(0).getType());
        Assert.assertEquals(def1.getTargetType(), list.get(0).getTargetType());
        Assert.assertEquals(def1.getTarget(), list.get(0).getTarget());
        Assert.assertEquals(def1.isSuspendProcess(),
                list.get(0).isSuspendProcess());
    }

    @Test
    public void testUpdateTriggerDefinition_OnlyNameChanged() throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition definition = TriggerDefinitions
                        .createSuspendingTriggerDefinition(mgr, supp1,
                                TriggerType.ACTIVATE_SERVICE);

                TriggerProcesses.createPendingTriggerProcess(mgr,
                        createUserForOrg, definition);
                return null;
            }

        });

        VOTriggerDefinition triggerDefinition = triggerDefinitionService
                .getTriggerDefinitions().get(0);

        triggerDefinition.setType(TriggerType.ACTIVATE_SERVICE);
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setTarget(
                "http://estbesdev1:8680/oscm-integrationtests-mockproduct/NotificationService?wsdl");
        triggerDefinition.setSuspendProcess(true);
        triggerDefinition.setName("NAME_NEU");
        updateTriggerDefinition(triggerDefinition);

        VOTriggerDefinition updated = triggerDefinitionService
                .getTriggerDefinitions().get(0);

        Assert.assertEquals(triggerDefinition.getType(), updated.getType());
        Assert.assertEquals(triggerDefinition.getTargetType(),
                updated.getTargetType());
        Assert.assertEquals(triggerDefinition.getTarget(), updated.getTarget());
        Assert.assertEquals(triggerDefinition.getName(), updated.getName());
        Assert.assertEquals(triggerDefinition.isSuspendProcess(),
                updated.isSuspendProcess());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateTriggerDefinition_NotOnlyNameChanged()
            throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition definition = TriggerDefinitions
                        .createSuspendingTriggerDefinition(mgr, supp1,
                                TriggerType.ACTIVATE_SERVICE);

                TriggerProcesses.createPendingTriggerProcess(mgr,
                        createUserForOrg, definition);
                return null;
            }

        });

        VOTriggerDefinition triggerDefinition = triggerDefinitionService
                .getTriggerDefinitions().get(0);

        triggerDefinition.setType(TriggerType.DEACTIVATE_SERVICE);
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setTarget(
                "http://estbesdev1:8680/oscm-integrationtests-mockproduct/NotificationService?wsdl");
        triggerDefinition.setSuspendProcess(true);
        triggerDefinition.setName("NAME_NEU");
        updateTriggerDefinition(triggerDefinition);
    }

    @Test
    public void testGetTriggerDefinitionsForOrganization_PendingStatus()
            throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition definition = TriggerDefinitions
                        .createSuspendingTriggerDefinition(mgr, supp1,
                                TriggerType.ACTIVATE_SERVICE);

                TriggerProcesses.createPendingTriggerProcess(mgr,
                        createUserForOrg, definition);
                return null;
            }

        });

        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();

        Assert.assertEquals(1, list.size());
        Assert.assertEquals(TriggerTargetType.WEB_SERVICE,
                list.get(0).getTargetType());
        Assert.assertEquals(TriggerType.ACTIVATE_SERVICE,
                list.get(0).getType());
        Assert.assertEquals(true, list.get(0).isHasTriggerProcess());
    }

    // create
    @Test(expected = EJBAccessException.class)
    public void testCreateTrigger_WrongUserRole_TECHPROV() throws Throwable {
        container.login(techProvKey, ROLE_TECHNOLOGY_MANAGER);

        VOTriggerDefinition vo = buildTriggerVO(true);

        createTriggerDefinition(vo);
        Assert.fail();
    }

    @Test(expected = EJBAccessException.class)
    public void testCreateTrigger_WrongUserRole_MARKETPLACE_OWNER()
            throws Exception {
        container.login(supplier3Key, UserRoleType.MARKETPLACE_OWNER.name());

        VOTriggerDefinition vo = buildTriggerVO(true);

        createTriggerDefinition(vo);
        Assert.fail();
    }

    @Test(expected = ValidationException.class)
    public void testCreateTrigger_WrongUserRole_PLATFORM_OPERATOR()
            throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);

        VOTriggerDefinition vo = buildTriggerVO(true);

        createTriggerDefinition(vo);
        Assert.fail();
    }

    // wrong roles for updateTriggerDefinition
    @Test(expected = EJBAccessException.class)
    public void testUpdateTrigger_WrongUserRole() throws Exception {

        VOTriggerDefinition trigger = createTriggerForSupp1();

        container.login(nonAdminKey, UserRoleType.MARKETPLACE_OWNER.name());
        updateTriggerDefinition(trigger);
        Assert.fail();
    }

    // wrong role for delete
    @Test(expected = EJBAccessException.class)
    public void testDeleteTriggerDefinition_WrongRole()
            throws Exception, DeletionConstraintException {
        VOTriggerDefinition trigger = createTriggerForSupp1();

        container.login(nonAdminKey, UserRoleType.MARKETPLACE_OWNER.name());
        deleteTriggerDefinition(trigger.getKey());
        Assert.fail();
    }

    // supplier - allowed trigger types
    @Test
    public void testCreateAllowedTriggerTypes_Supplier_ACTIVATE_SERVICE()
            throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition def1 = buildTriggerVO(true);

        def1.setType(TriggerType.ACTIVATE_SERVICE);
        def1.setSuspendProcess(TriggerType.ACTIVATE_SERVICE.isSuspendProcess());
        createTriggerDefinition(def1);

        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testCreateAllowedTriggerTypes_Supplier_DEACTIVATE_SERVICE()
            throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition def1 = buildTriggerVO(true);

        def1.setType(TriggerType.DEACTIVATE_SERVICE);
        def1.setSuspendProcess(
                TriggerType.DEACTIVATE_SERVICE.isSuspendProcess());
        createTriggerDefinition(def1);

        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testCreateAllowedTriggerTypes_Supplier_REGISTER_CUSTOMER_FOR_SUPPLIER()
            throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition def1 = buildTriggerVO(true);

        def1.setType(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER);
        def1.setSuspendProcess(
                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER.isSuspendProcess());
        createTriggerDefinition(def1);

        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testCreateAllowedTriggerTypes_Supplier_SAVE_PAYMENT_CONFIGURATION()
            throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition def1 = buildTriggerVO(true);

        def1.setType(TriggerType.SAVE_PAYMENT_CONFIGURATION);
        def1.setSuspendProcess(
                TriggerType.SAVE_PAYMENT_CONFIGURATION.isSuspendProcess());
        createTriggerDefinition(def1);

        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testCreateAllowedTriggerTypes_Supplier_START_BILLING_RUN()
            throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition def1 = buildTriggerVO(true);

        def1.setType(TriggerType.START_BILLING_RUN);
        def1.setSuspendProcess(
                TriggerType.START_BILLING_RUN.isSuspendProcess());
        createTriggerDefinition(def1);

        List<VOTriggerDefinition> list = triggerDefinitionService
                .getTriggerDefinitions();
        Assert.assertEquals(1, list.size());
    }

    // customer - un-allowed trigger types
    @Test(expected = ValidationException.class)
    public void testCreateTriggerTypesNotAllowedFor_Customer_ACTIVATE_SERVICE()
            throws Exception {
        container.login(customerKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.ACTIVATE_SERVICE);

        createTriggerDefinition(def1);
        Assert.fail();
    }

    @Test(expected = ValidationException.class)
    public void testCreateTriggerTypesNotAllowedFor_Customer_DEACTIVATE_SERVICE()
            throws Exception {
        container.login(customerKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.DEACTIVATE_SERVICE);

        createTriggerDefinition(def1);
        Assert.fail();
    }

    @Test(expected = ValidationException.class)
    public void testCreateTriggerTypesNotAllowedFor_Customer_REGISTER_CUSTOMER_FOR_SUPPLIER()
            throws Exception {
        container.login(customerKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER);

        createTriggerDefinition(def1);
        Assert.fail();
    }

    @Test(expected = ValidationException.class)
    public void testCreateTriggerTypesNotAllowedFor_Customer_SAVE_PAYMENT_CONFIGURATION()
            throws Exception {
        container.login(customerKey, ROLE_ORGANIZATION_ADMIN);

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.SAVE_PAYMENT_CONFIGURATION);

        createTriggerDefinition(def1);
        Assert.fail();
    }

    // customer - allowed trigger types
    @Test
    public void testCreateAllowedTriggerTypes_Customer_ADD_REVOKE_USER()
            throws Exception {
        container.login(customerKey, "ORGANIZATION_ADMIN");

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.ADD_REVOKE_USER);
        def1.setSuspendProcess(TriggerType.ADD_REVOKE_USER.isSuspendProcess());
        createTriggerDefinition(def1);
    }

    @Test
    public void testCreateAllowedTriggerTypes_Customer_MODIFY_SUBSCRIPTION()
            throws Exception {
        container.login(customerKey, "ORGANIZATION_ADMIN");

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.MODIFY_SUBSCRIPTION);
        def1.setSuspendProcess(
                TriggerType.MODIFY_SUBSCRIPTION.isSuspendProcess());
        createTriggerDefinition(def1);
    }

    @Test
    public void testCreateAllowedTriggerTypes_Customer_SUBSCRIBE_TO_SERVICE()
            throws Exception {
        container.login(customerKey, "ORGANIZATION_ADMIN");

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.SUBSCRIBE_TO_SERVICE);
        def1.setSuspendProcess(
                TriggerType.SUBSCRIBE_TO_SERVICE.isSuspendProcess());
        createTriggerDefinition(def1);
    }

    @Test
    public void testCreateAllowedTriggerTypes_Customer_UNSUBSCRIBE_FROM_SERVICE()
            throws Exception {
        container.login(customerKey, "ORGANIZATION_ADMIN");

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.UNSUBSCRIBE_FROM_SERVICE);
        def1.setSuspendProcess(
                TriggerType.UNSUBSCRIBE_FROM_SERVICE.isSuspendProcess());
        createTriggerDefinition(def1);
    }

    @Test
    public void testCreateAllowedTriggerTypes_Customer_UPGRADE_SUBSCRIPTION()
            throws Exception {
        container.login(customerKey, "ORGANIZATION_ADMIN");

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.UPGRADE_SUBSCRIPTION);
        def1.setSuspendProcess(
                TriggerType.UPGRADE_SUBSCRIPTION.isSuspendProcess());
        createTriggerDefinition(def1);
    }

    @Test
    public void testCreateAllowedTriggerTypes_Customer_START_BILLING_RUN()
            throws Exception {
        container.login(customerKey, "ORGANIZATION_ADMIN");

        VOTriggerDefinition def1 = buildTriggerVO(true);
        def1.setType(TriggerType.START_BILLING_RUN);
        def1.setSuspendProcess(
                TriggerType.START_BILLING_RUN.isSuspendProcess());
        createTriggerDefinition(def1);
    }

    @Test
    public void getTriggerTypes_customerAdmin() throws Exception {
        // given
        container.login(customerKey, "ORGANIZATION_ADMIN");

        // when
        List<TriggerType> triggerTypes = triggerDefinitionService
                .getTriggerTypes();

        // then
        assertEquals(allowedTriggersForCustomer.length, triggerTypes.size());
        assertTrue(triggerTypes
                .containsAll(Arrays.asList(allowedTriggersForCustomer)));
    }

    @Test
    public void getTriggerTypes_supplierAdmin() throws Exception {
        // given
        container.login(supplier1Key, "ORGANIZATION_ADMIN");

        // when
        List<TriggerType> triggerTypes = triggerDefinitionService
                .getTriggerTypes();

        // then
        assertTrue(triggerTypes
                .containsAll(Arrays.asList(allowedTriggersForSupplier)));
        assertTrue(triggerTypes
                .containsAll(Arrays.asList(allowedTriggersForCustomer)));
    }

    @Test(expected = EJBAccessException.class)
    public void getTriggerTypes_nonAdmin() throws Exception {
        // given
        container.login(nonAdminKey, UserRoleType.MARKETPLACE_OWNER.name());

        // when
        try {
            triggerDefinitionService.getTriggerTypes();
        } catch (EJBException ex) {
            throw ex.getCausedByException();
        }

        // then
        // exception
    }
}
