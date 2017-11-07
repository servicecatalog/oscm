/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                  
 *                                                                              
 *  Creation Date: 16.07.2012                                                      
 *                                                                                                                         
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessIdentifier;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TriggerDefinitions;
import org.oscm.test.data.TriggerProcesses;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

public class IdentityServiceBeanTriggerIdIT extends EJBTestBase {

    private VOUserDetails user;
    private DataService ds;
    private Organization supplier;
    private PlatformUser supplierUser;
    private TriggerProcessMessageData triggerProcessData;
    private TriggerProcess tp;

    private final String marketplaceId = "FUJITSU";

    private IdentityService identityService;
    private TriggerQueueServiceLocal triggerQueueServiceLocal;
    private ConfigurationServiceLocal cs;

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.enableInterfaceMocking(true);
        triggerQueueServiceLocal = mock(TriggerQueueServiceLocal.class);
        cs = Mockito.spy(new ConfigurationServiceStub());
        container.addBean(cs);
        container.addBean(triggerQueueServiceLocal);
        container.addBean(new DataServiceBean());
        IdentityServiceLocal identityServiceBean = new IdentityServiceBean();
        container.addBean(identityServiceBean);
        identityService = container.get(IdentityService.class);

        ConfigurationSetting setting = new ConfigurationSetting(
                ConfigurationKey.AUTH_MODE, Configuration.GLOBAL_CONTEXT,
                "INTERNAL");

        doReturn(setting).when(cs).getConfigurationSetting(
                any(ConfigurationKey.class), anyString());

        ds = container.get(DataService.class);
        doAnswer(new Answer<List<TriggerProcessMessageData>>() {
            @Override
            public List<TriggerProcessMessageData> answer(
                    InvocationOnMock invocation) throws Throwable {
                return Collections.singletonList(triggerProcessData);
            }
        }).when(triggerQueueServiceLocal)
                .sendSuspendingMessages(anyListOf(TriggerMessage.class));

    }

    @Test
    public void createUser_NonConflicting() throws Exception {

        // given no trigger definition
        initData(false, TriggerType.REGISTER_OWN_USER);
        tp.setTriggerDefinition(null);
        container.login(supplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);

        // when
        identityService.createUser(user,
                Collections.singletonList(UserRoleType.SERVICE_MANAGER),
                marketplaceId);

        // then no identifier is created
        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertTrue(processIdentifiers.isEmpty());
    }

    @Test
    public void createUser_NonConflictingValidateIdentifierGeneration()
            throws Exception {

        // given a trigger definition
        initData(false, TriggerType.REGISTER_OWN_USER);
        container.login(supplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);

        // when
        identityService.createUser(user,
                Collections.singletonList(UserRoleType.SERVICE_MANAGER),
                marketplaceId);

        // then identifiers are created
        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertEquals(2, processIdentifiers.size());
    }

    @Test(expected = OperationPendingException.class)
    public void createUser_Conflicting() throws Exception {

        // given existing identifiers
        initData(true, TriggerType.REGISTER_OWN_USER);
        container.login(supplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);

        // then exception is thrown
        identityService.createUser(user,
                Collections.singletonList(UserRoleType.SERVICE_MANAGER),
                marketplaceId);

    }

    @Test
    public void createUser_nonSuspending() throws Exception {

        // given no suspending trigger
        initData(false, TriggerType.REGISTER_OWN_USER);
        tp.setTriggerDefinition(null);
        container.login(supplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);

        // when
        identityService.createUser(user,
                Collections.singletonList(UserRoleType.SERVICE_MANAGER),
                marketplaceId);

        // then always a non-suspending notification is sent
        verify(triggerQueueServiceLocal).sendAllNonSuspendingMessages(
                ParameterizedTypes.list(anyList(), TriggerMessage.class));

    }

    /**
     * Initializes the test setup, creates the org roles, currencies, countries,
     * a supplier organization and, if required, some trigger data.
     * 
     * @param createTriggerProcessIds
     *            Indicates whether the trigger process identifier should be
     *            generated or not.
     * @param triggerType
     *            The type of the trigger
     * @throws Exception
     */
    private void initData(final boolean createTriggerProcessIds,
            final TriggerType triggerType) throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createUserRoles(ds);
                supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                user = new VOUserDetails();
                user.setUserId("testUser");
                user.setEMail("user@server.com");
                user.setLocale("en");
                user.setOrganizationId(supplier.getOrganizationId());

                supplierUser = Organizations.createUserForOrg(ds, supplier,
                        true, "supplierAdmin");

                createTriggerData(createTriggerProcessIds, triggerType);
                return null;
            }

        });
    }

    /**
     * Creates trigger definition and process.
     * 
     * @param createTriggerProcessIds
     *            Indicates if trigger process identifiers have to be set.
     * @param assignTDToSecondSupplier
     *            Indicates if the trigger definition is created for the second
     *            supplier.
     * @param triggerType
     *            The type of the trigger to be created.
     * @throws Exception
     */
    private void createTriggerData(final boolean createTriggerProcessIds,
            final TriggerType triggerType) throws Exception {
        PlatformUser triggerUser = supplierUser;
        Organization triggerOrg = supplier;

        TriggerDefinition td = TriggerDefinitions
                .createSuspendingTriggerDefinition(ds, triggerOrg, triggerType);
        tp = TriggerProcesses.createPendingTriggerProcess(ds, triggerUser, td);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USER, user);
        triggerProcessData = new TriggerProcessMessageData(tp,
                new TriggerMessage());
        if (createTriggerProcessIds) {
            createTriggerIds(tp);
        }
    }

    /**
     * Creates the trigger process identifiers for the registration of a
     * customer organization for a supplier and initializes the trigger process
     * meta-data accordingly.
     * 
     * @param tp
     *            The trigger process the identifiers belong to.
     */
    private void createTriggerIds(TriggerProcess tp) {
        List<TriggerProcessParameter> params = Collections.emptyList();
        TriggerType triggerType = tp.getTriggerDefinition().getType();
        TriggerMessage tm = new TriggerMessage(triggerType, params,
                Collections.singletonList(supplier));
        TriggerProcessIdentifier tpi1 = new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(tp.getUser().getOrganization().getKey()));
        tpi1.setTriggerProcess(tp);
        tp.setTriggerProcessIdentifiers(Arrays.asList(tpi1));
        if (triggerType == TriggerType.REGISTER_OWN_USER) {
            TriggerProcessIdentifier tpi2 = new TriggerProcessIdentifier(
                    TriggerProcessIdentifierName.USER_ID, user.getUserId());
            tpi2.setTriggerProcess(tp);
            tp.setTriggerProcessIdentifiers(Arrays.asList(tpi1, tpi2));
        }
        triggerProcessData = new TriggerProcessMessageData(tp, tm);
    }

    private List<TriggerProcessIdentifier> getProcessIdentifiers(
            final Long... tpKeys) throws Exception {
        return runTX(new Callable<List<TriggerProcessIdentifier>>() {
            @Override
            public List<TriggerProcessIdentifier> call() throws Exception {
                return TriggerProcesses.getProcessIdentifiers(ds, tpKeys);
            }

        });
    }

}
