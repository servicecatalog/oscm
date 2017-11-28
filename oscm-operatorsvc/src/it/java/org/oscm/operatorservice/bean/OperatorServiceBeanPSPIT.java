/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 11.10.2011                                                      
 *                                                                              
 *  Completion Time: 11.10.2011  
 *                                           
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.auditlog.bean.AuditLogServiceBean;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PSP;
import org.oscm.domobjects.PSPAccount;
import org.oscm.domobjects.PSPSetting;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.paymentservice.assembler.PSPAccountAssembler;
import org.oscm.paymentservice.assembler.PSPAssembler;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.serviceprovisioningservice.local.SearchServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;
import org.oscm.timerservice.bean.TimerServiceBean;
import org.oscm.triggerservice.local.TriggerServiceLocal;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPAccount;
import org.oscm.internal.vo.VOPSPSetting;
import org.oscm.internal.vo.VOPaymentType;

public class OperatorServiceBeanPSPIT extends EJBTestBase {

    private OperatorService opService;
    private DataService ds;
    private LocalizerServiceLocal localizer;

    private PSP psp1;
    private VOPSP voPsp1;
    private PSPSetting pspSetting1;
    private PSP psp2;
    private VOPSP voPsp2;

    private PaymentType pt;

    private PlatformUser user;
    private Organization org;
    private Organization supplier;
    private Organization reseller;
    private Organization broker;
    private PSPAccount pspAccount;
    private VOPSPAccount voPspAccount;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(Mockito.mock(ImageResourceServiceLocal.class));
        container.addBean(Mockito.mock(SearchServiceLocal.class));
        container.addBean(Mockito.mock(TriggerServiceLocal.class));
        container.addBean(Mockito.mock(ConfigurationServiceLocal.class));
        container.addBean(Mockito.mock(BillingServiceLocal.class));
        container.addBean(Mockito.mock(TimerServiceBean.class));
        container.addBean(Mockito.mock(PaymentServiceLocal.class));
        container.addBean(Mockito.mock(LocalizerServiceLocal.class));
        container.addBean(Mockito.mock(AccountServiceLocal.class));
        container.addBean(Mockito.mock(IdentityServiceLocal.class));
        container.addBean(new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return user;
            }
        });
        AuditLogServiceBean auditLogMock = mock(AuditLogServiceBean.class);
        when(
                auditLogMock.loadAuditLogs(Mockito.anyListOf(String.class),
                        Mockito.anyLong(), Mockito.anyLong())).thenReturn(
                new String("").getBytes());
        container.addBean(auditLogMock);
        container.addBean(new OperatorServiceBean());

        user = new PlatformUser();
        user.setOrganization(new Organization());
        opService = container.get(OperatorService.class);
        ds = container.get(DataService.class);
        localizer = container.get(LocalizerServiceLocal.class);

        initData();
    }

    @Test(expected = EJBException.class)
    public void getPSPs_Unauthorized() throws Exception {
        opService.getPSPs();
    }

    @Test
    public void getPSPs_NoEntries() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        deletePSPs();
        List<VOPSP> psps = opService.getPSPs();
        assertNotNull(psps);
        assertTrue(psps.isEmpty());
    }

    @Test
    public void getPSPs_TwoEntries() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        List<VOPSP> psps = opService.getPSPs();
        assertNotNull(psps);
        assertEquals(2, psps.size());
        VOPSP psp = psps.get(0);
        assertEquals(psp1.getIdentifier(), psp.getId());
        assertEquals(psp1.getWsdlUrl(), psp.getWsdlUrl());
        psp = psps.get(1);
        assertEquals(psp2.getIdentifier(), psp.getId());
        assertEquals(psp2.getWsdlUrl(), psp.getWsdlUrl());
    }

    @Test(expected = EJBException.class)
    public void savePSP_Unauthorized() throws Exception {
        VOPSP vopsp = new VOPSP();
        opService.savePSP(vopsp);
    }

    @Test
    public void savePSP_CreateNoSettingsInvalidId() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = initVOPSP();
        vopsp.setId("");
        vopsp.getPspSettings().clear();
        try {
            opService.savePSP(vopsp);
            fail();
        } catch (ValidationException e) {
            assertPSPCount(0);
        }
    }

    @Test
    public void savePSP_CreateNoSettingsInvalidUrl() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = initVOPSP();
        vopsp.setWsdlUrl("");
        vopsp.getPspSettings().clear();
        try {
            opService.savePSP(vopsp);
            fail();
        } catch (ValidationException e) {
            assertPSPCount(0);
        }
    }

    @Test(expected = EJBException.class)
    public void savePSP_NullInput() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.savePSP(null);
    }

    @Test
    public void savePSP_CreateNoSettingsDuplicateId() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = initVOPSP();
        vopsp.getPspSettings().clear();
        opService.savePSP(vopsp);
        try {
            opService.savePSP(vopsp);
            fail();
        } catch (NonUniqueBusinessKeyException e) {
            assertPSPCount(1);
        }
    }

    @Test
    public void savePSP_CreateNoSettingsKeyGreater0() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = initVOPSP();
        vopsp.getPspSettings().clear();
        vopsp.setKey(1);
        try {
            opService.savePSP(vopsp);
            fail();
        } catch (ObjectNotFoundException e) {
            assertPSPCount(0);
        }
    }

    @Test
    public void savePSP_CreateNoSettingsVersionSet() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = initVOPSP();
        vopsp.getPspSettings().clear();
        vopsp.setVersion(-11);
        VOPSP savedPSP = opService.savePSP(vopsp);
        assertNotNull(savedPSP);
        assertTrue(savedPSP.getKey() > 0);
        assertEquals(0, savedPSP.getVersion());
        assertEquals(vopsp.getId(), savedPSP.getId());
        assertEquals(vopsp.getWsdlUrl(), savedPSP.getWsdlUrl());
    }

    @Test
    public void savePSP_CreateWithSettings() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = initVOPSP();
        VOPSP savedPSP = opService.savePSP(vopsp);
        validateSettings(vopsp, savedPSP);
    }

    @Test
    public void savePSP_CreateWithSettingsKeyGreater0() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = initVOPSP();
        vopsp.getPspSettings().get(0).setKey(5);
        try {
            opService.savePSP(vopsp);
            fail();
        } catch (ObjectNotFoundException e) {
            assertPSPCount(0);
        }
    }

    @Test
    public void savePSP_CreateWithSettingsDifferentVersion() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = initVOPSP();
        vopsp.getPspSettings().get(0).setVersion(-5);
        VOPSP savedPSP = opService.savePSP(vopsp);
        validateSettings(vopsp, savedPSP);
    }

    @Test
    public void savePSP_CreateWithSettingsDuplicateKey() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = initVOPSP();
        vopsp.getPspSettings().get(0)
                .setSettingKey(vopsp.getPspSettings().get(1).getSettingKey());
        try {
            opService.savePSP(vopsp);
            fail();
        } catch (ValidationException e) {
            assertPSPCount(0);
        }
    }

    @Test
    public void savePSP_UpdateWithNewSetting() throws Exception {
        // given
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = opService.getPSPs().get(0);
        assertEquals(1, vopsp.getPspSettings().size());
        VOPSPSetting newSetting = new VOPSPSetting();
        newSetting.setSettingKey("newKey");
        newSetting.setSettingValue("newValue");
        vopsp.getPspSettings().add(newSetting);

        // when
        VOPSP savedPSP = opService.savePSP(vopsp);

        // then
        assertEquals(2, savedPSP.getPspSettings().size());
        VOPSPSetting vopspSetting = savedPSP.getPspSettings().get(1);
        assertEquals(0, vopspSetting.getVersion());
        assertTrue(vopspSetting.getKey() > 0);
        assertEquals("newKey", vopspSetting.getSettingKey());
        assertEquals("newValue", vopspSetting.getSettingValue());
    }

    @Test
    public void savePSP_UpdateWithModifiedSetting() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = opService.getPSPs().get(0);
        assertEquals(1, vopsp.getPspSettings().size());
        VOPSPSetting oldSetting = vopsp.getPspSettings().get(0);
        oldSetting.setSettingKey("newKey");
        oldSetting.setSettingValue("newValue");
        VOPSP savedPSP = opService.savePSP(vopsp);
        assertEquals(1, savedPSP.getPspSettings().size());
        VOPSPSetting vopspSetting = savedPSP.getPspSettings().get(0);
        assertEquals(1, vopspSetting.getVersion());
        assertEquals(oldSetting.getKey(), vopspSetting.getKey());
        assertEquals("newKey", vopspSetting.getSettingKey());
        assertEquals("newValue", vopspSetting.getSettingValue());
    }

    @Test
    public void savePSP_UpdateWithRemovedSetting() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = opService.getPSPs().get(0);
        assertEquals(1, vopsp.getPspSettings().size());
        VOPSPSetting newSetting = new VOPSPSetting();
        newSetting.setSettingKey("newKey");
        newSetting.setSettingValue("newValue");
        vopsp.setPspSettings(Arrays.asList(newSetting));
        VOPSP savedPSP = opService.savePSP(vopsp);
        assertEquals(1, savedPSP.getPspSettings().size());
        VOPSPSetting vopspSetting = savedPSP.getPspSettings().get(0);
        assertEquals(0, vopspSetting.getVersion());
        assertTrue(vopspSetting.getKey() > 0);
        assertEquals("newKey", vopspSetting.getSettingKey());
        assertEquals("newValue", vopspSetting.getSettingValue());
    }

    @Test(expected = ValidationException.class)
    public void savePSP_UpdateWithInvalidId() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = opService.getPSPs().get(0);
        vopsp.setId("");
        opService.savePSP(vopsp);
    }

    @Test(expected = ValidationException.class)
    public void savePSP_UpdateWithInvalidURL() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = opService.getPSPs().get(0);
        vopsp.setWsdlUrl("");
        opService.savePSP(vopsp);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void savePSP_UpdateNonExisting() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = opService.getPSPs().get(0);
        vopsp.setKey(-1);
        opService.savePSP(vopsp);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void savePSP_UpdateWrongVersion() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = opService.getPSPs().get(0);
        vopsp.setVersion(-1);
        opService.savePSP(vopsp);
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void savePSP_UpdateDuplicateIdForAddition() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPSP vopsp = opService.getPSPs().get(0);
        vopsp.setId("psp2");
        opService.savePSP(vopsp);
    }

    @Test(expected = EJBException.class)
    public void getPSPAccounts_Unauthorized() throws Exception {
        opService.getPSPAccounts(new VOOrganization());
    }

    @Test(expected = EJBException.class)
    public void getPSPAccounts_NullArgument() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.getPSPAccounts(null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getPSPAccounts_NonExistingOrg() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.getPSPAccounts(new VOOrganization());
    }

    @Test
    public void getPSPAccounts_NoResult() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(org.getKey());
        List<VOPSPAccount> pspAccounts = opService.getPSPAccounts(organization);
        assertNotNull(pspAccounts);
        assertTrue(pspAccounts.isEmpty());
    }

    @Test
    public void getPSPAccounts() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(org.getKey());
        createPSPAccount(org, psp1);
        List<VOPSPAccount> pspAccounts = opService.getPSPAccounts(organization);
        assertNotNull(pspAccounts);
        assertEquals(1, pspAccounts.size());
        VOPSPAccount acc = pspAccounts.get(0);
        assertEquals("pspIdentifier", acc.getPspIdentifier());
    }

    @Test(expected = EJBException.class)
    public void savePSPAccount_Unauthorized() throws Exception {
        opService.savePSPAccount(new VOOrganization(), new VOPSPAccount());
    }

    @Test(expected = EJBException.class)
    public void savePSPAccount_NullOrg() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.savePSPAccount(null, new VOPSPAccount());
    }

    @Test(expected = EJBException.class)
    public void savePSPAccount_NullAccount() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.savePSPAccount(new VOOrganization(), null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void savePSPAccount_NonExistingOrg() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.savePSPAccount(new VOOrganization(), voPspAccount);
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void savePSPAccount_NonSupplierOrg() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(org.getKey());
        voPspAccount.setKey(-1);
        opService.savePSPAccount(organization, voPspAccount);
    }

    @Test(expected = EJBException.class)
    public void savePSPAccount_NullPSP() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(supplier.getKey());
        VOPSPAccount account = new VOPSPAccount();
        account.setPspIdentifier("pspIdentifier");
        account.setPsp(null);
        opService.savePSPAccount(organization, account);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void savePSPAccount_NonExistingPSP() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(supplier.getKey());
        VOPSPAccount account = new VOPSPAccount();
        account.setPspIdentifier("pspIdentifier");
        account.setPsp(new VOPSP());
        opService.savePSPAccount(organization, account);
    }

    @Test(expected = ValidationException.class)
    public void savePSPAccount_InvalidPSPId() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(supplier.getKey());
        VOPSPAccount account = new VOPSPAccount();
        account.setPspIdentifier("");
        VOPSP psp = new VOPSP();
        psp.setKey(psp1.getKey());
        account.setPsp(psp);
        opService.savePSPAccount(organization, account);
    }

    @Test
    public void savePSPAccount_Create() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(supplier.getKey());
        VOPSPAccount account = new VOPSPAccount();
        account.setPspIdentifier("23456");
        account.setVersion(-4); // use negative version to check reset to 0
        VOPSP psp = new VOPSP();
        psp.setKey(psp1.getKey());
        account.setPsp(psp);
        VOPSPAccount savedAccount = opService.savePSPAccount(organization,
                account);
        assertNotNull(savedAccount);
        assertTrue(savedAccount.getKey() > 0);
        assertEquals(0, savedAccount.getVersion());
        assertEquals("psp1", savedAccount.getPsp().getId());
        assertEquals("23456", savedAccount.getPspIdentifier());
    }

    @Test
    public void savePSPAccount_CreateAsReseller() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(reseller.getKey());
        organization.setOrganizationId(reseller.getOrganizationId());
        VOPSPAccount account = new VOPSPAccount();
        account.setPspIdentifier("23456");
        account.setVersion(-4); // use negative version to check reset to 0
        VOPSP psp = new VOPSP();
        psp.setKey(psp1.getKey());
        account.setPsp(psp);
        VOPSPAccount savedAccount = opService.savePSPAccount(organization,
                account);
        assertNotNull(savedAccount);
        assertTrue(savedAccount.getKey() > 0);
        assertEquals(0, savedAccount.getVersion());
        assertEquals("psp1", savedAccount.getPsp().getId());
        assertEquals("23456", savedAccount.getPspIdentifier());
        Set<String> types = new HashSet<String>();
        types.add("psp2_pt1");
        types.add("psp2_pt2");
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void savePSPAccount_CreateAsBroker() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(broker.getKey());
        VOPSPAccount account = new VOPSPAccount();
        account.setPspIdentifier("23456");
        account.setVersion(-4); // use negative version to check reset to 0
        VOPSP psp = new VOPSP();
        psp.setKey(psp1.getKey());
        account.setPsp(psp);
        opService.savePSPAccount(organization, account);
    }

    @Test(expected = ValidationException.class)
    public void savePSPAccount_CreateDuplicate() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(supplier.getKey());
        VOPSPAccount account = new VOPSPAccount();
        account.setPspIdentifier("23456");
        VOPSP psp = new VOPSP();
        psp.setKey(psp1.getKey());
        account.setPsp(psp);
        VOPSPAccount savedAccount = opService.savePSPAccount(organization,
                account);
        savedAccount.setKey(0);
        opService.savePSPAccount(organization, savedAccount);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void savePSPAccount_UpdateWrongKey() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(supplier.getKey());
        VOPSPAccount account = new VOPSPAccount();
        account.setKey(-1);
        VOPSP psp = new VOPSP();
        psp.setKey(psp1.getKey());
        account.setPsp(psp);
        opService.savePSPAccount(organization, account);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void savePSPAccount_UpdateWrongVersion() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(supplier.getKey());
        VOPSPAccount account = voPspAccount;
        account.setVersion(-1);
        opService.savePSPAccount(organization, account);
    }

    @Test
    public void savePSPAccount_Update() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOOrganization organization = new VOOrganization();
        organization.setKey(supplier.getKey());
        VOPSPAccount account = voPspAccount;
        account.setPspIdentifier("modId");
        account.getPsp().setKey(psp1.getKey());
        VOPSPAccount savedAccount = opService.savePSPAccount(organization,
                account);
        assertEquals("modId", savedAccount.getPspIdentifier());
        assertEquals(1, savedAccount.getVersion());
        assertEquals("psp1", savedAccount.getPsp().getId());
    }

    @Test(expected = EJBException.class)
    public void getPaymentTypes_Unauthorized() throws Exception {
        opService.getPaymentTypes(new VOPSP());
    }

    @Test(expected = EJBException.class)
    public void getPaymentTypes_NullArg() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.getPaymentTypes(null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getPaymentTypes_NonExistingPSP() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.getPaymentTypes(new VOPSP());
    }

    @Test
    public void getPaymentTypes_PSPWithoutPaymentTypes() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        List<VOPaymentType> paymentTypes = opService.getPaymentTypes(voPsp1);
        assertNotNull(paymentTypes);
        assertTrue(paymentTypes.isEmpty());
    }

    @Test
    public void getPaymentTypes_PSPWithPaymentTypes() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        List<VOPaymentType> paymentTypes = opService.getPaymentTypes(voPsp2);
        assertNotNull(paymentTypes);
        assertEquals(2, paymentTypes.size());

        for (VOPaymentType voPaymentType : paymentTypes) {
            if (voPaymentType.getPaymentTypeId().equals("psp2_pt1")) {
                assertEquals(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                        voPaymentType.getCollectionType());
                assertTrue(voPaymentType.getKey() > 0);
                assertEquals(0, voPaymentType.getVersion());

            } else

            if (voPaymentType.getPaymentTypeId().equals("psp2_pt2")) {
                assertEquals(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                        voPaymentType.getCollectionType());
                assertTrue(voPaymentType.getKey() > 0);
                assertEquals(0, voPaymentType.getVersion());
            } else {
                fail();
            }
        }
    }

    @Test(expected = EJBException.class)
    public void createPaymentType_Unauthorized() throws Exception {
        opService.savePaymentType(voPsp1, new VOPaymentType());
    }

    @Test(expected = EJBException.class)
    public void createPaymentType_NullPSP() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.savePaymentType(null, new VOPaymentType());
    }

    @Test(expected = EJBException.class)
    public void createPaymentType_NullPT() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.savePaymentType(voPsp1, null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void createPaymentType_NonExistingPSP() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.savePaymentType(new VOPSP(), new VOPaymentType());
    }

    @Test(expected = ValidationException.class)
    public void createPaymentType_EmptyPtId() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        opService.savePaymentType(voPsp1, new VOPaymentType());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void createPaymentType_DuplicateId() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPaymentType paymentType = new VOPaymentType();
        paymentType.setPaymentTypeId(pt.getPaymentTypeId());
        opService.savePaymentType(voPsp1, paymentType);
    }

    @Test
    public void createPaymentType_Create() throws Exception {
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPaymentType paymentType = new VOPaymentType();
        paymentType.setPaymentTypeId("newPaymentTypeId");
        paymentType
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        VOPaymentType savedPt = opService.savePaymentType(voPsp1, paymentType);
        assertTrue(savedPt.getKey() > 0);
        assertEquals(0, savedPt.getVersion());
        assertEquals("newPaymentTypeId", savedPt.getPaymentTypeId());
        assertEquals(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                savedPt.getCollectionType());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void createPaymentType_CreatePtKeyGreater0() throws Exception {
        // has to be discussed with Mike, who is on holiday until 24.10.
        // in my opinion (Mani) it should throw an exception at this point, what
        // is what actually happens correctly.
        container.login(1, ROLE_PLATFORM_OPERATOR);
        VOPaymentType paymentType = new VOPaymentType();
        paymentType.setPaymentTypeId("newPaymentTypeId");
        paymentType
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        paymentType.setKey(1);
        VOPaymentType savedPt = opService.savePaymentType(voPsp1, paymentType);
        assertTrue(savedPt.getKey() > 0);
        assertEquals(0, savedPt.getVersion());
        assertEquals("newPaymentTypeId", savedPt.getPaymentTypeId());
        assertEquals(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                savedPt.getCollectionType());
    }

    private void createPSPAccount(final Organization org, final PSP psp)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PSPAccount account = new PSPAccount();
                account.setOrganization(org);
                account.setPsp(psp);
                account.setPspIdentifier("pspIdentifier");
                ds.persist(account);
                return null;
            }
        });
    }

    private void deletePSPs() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = ds
                        .createQuery("DELETE FROM PSPSetting pspsetting");
                query.executeUpdate();
                query = ds.createQuery("DELETE FROM PSPAccount account");
                query.executeUpdate();
                query = ds.createQuery("DELETE FROM PaymentType pt");
                query.executeUpdate();
                query = ds.createQuery("DELETE FROM PSP psp");
                query.executeUpdate();
                return null;
            }
        });
    }

    private void validateSettings(VOPSP vopsp, VOPSP savedPSP) {
        assertNotNull(savedPSP);
        assertTrue(savedPSP.getKey() > 0);
        assertEquals(0, savedPSP.getVersion());
        assertEquals(vopsp.getId(), savedPSP.getId());
        assertEquals(vopsp.getWsdlUrl(), savedPSP.getWsdlUrl());
        List<VOPSPSetting> pspSettings = savedPSP.getPspSettings();
        assertNotNull(pspSettings);
        assertEquals(2, pspSettings.size());
        VOPSPSetting setting = pspSettings.get(0);
        assertEquals("setting1", setting.getSettingKey());
        assertEquals("value1", setting.getSettingValue());
        assertTrue(setting.getKey() > 0);
        assertEquals(0, setting.getVersion());
        setting = pspSettings.get(1);
        assertEquals("setting2", setting.getSettingKey());
        assertEquals("value2", setting.getSettingValue());
        assertTrue(setting.getKey() > 0);
        assertEquals(0, setting.getVersion());
    }

    /**
     * Creates two PSPs.
     * 
     * @throws Exception
     */
    private void initData() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                psp1 = new PSP();
                psp1.setIdentifier("psp1");
                psp1.setWsdlUrl("http://www.google.de");
                pspSetting1 = new PSPSetting();
                pspSetting1.setSettingKey("psp1_key");
                pspSetting1.setSettingValue("psp1_value");
                psp1.addPSPSetting(pspSetting1);
                ds.persist(psp1);
                psp2 = new PSP();
                psp2.setIdentifier("psp2");
                psp2.setWsdlUrl("http://www.chessbase.de");
                ds.persist(psp2);

                org = new Organization();
                org.setOrganizationId("orgId");
                org.setCutOffDay(1);
                ds.persist(org);

                createOrganizationRoles(ds);
                supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER);
                broker = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);

                pspAccount = new PSPAccount();
                pspAccount.setOrganization(supplier);
                pspAccount.setPsp(psp2);
                pspAccount.setPspIdentifier("the external identifier");
                ds.persist(pspAccount);

                pt = new PaymentType();
                pt.setPaymentTypeId("psp2_pt1");
                pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
                pt.setPsp(psp2);
                ds.persist(pt);
                PaymentType pt2 = new PaymentType();
                pt2.setPaymentTypeId("psp2_pt2");
                pt2.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
                pt2.setPsp(psp2);
                ds.persist(pt2);

                LocalizerFacade lf = new LocalizerFacade(localizer, ds
                        .getCurrentUser().getLocale());
                voPspAccount = PSPAccountAssembler.toVo(pspAccount, lf);
                voPsp1 = PSPAssembler.toVo(psp1, lf);
                voPsp2 = PSPAssembler.toVo(psp2, lf);
                return null;
            }
        });
    }

    private VOPSP initVOPSP() {
        VOPSP vopsp = new VOPSP();
        vopsp.setId("identifier");
        vopsp.setWsdlUrl("http://www.google.de");

        VOPSPSetting setting1 = new VOPSPSetting();
        setting1.setSettingKey("setting1");
        setting1.setSettingValue("value1");

        VOPSPSetting setting2 = new VOPSPSetting();
        setting2.setSettingKey("setting2");
        setting2.setSettingValue("value2");

        List<VOPSPSetting> settings = new ArrayList<VOPSPSetting>();
        settings.add(setting1);
        settings.add(setting2);
        vopsp.setPspSettings(settings);
        return vopsp;
    }

    private void assertPSPCount(final int count) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = ds
                        .createQuery("SELECT p FROM PSP p WHERE p.dataContainer.identifier NOT IN (:ids)");
                query.setParameter("ids", Arrays.asList("psp1", "psp2"));
                List<?> resultList = query.getResultList();
                assertEquals(count, resultList.size());
                return null;
            }
        });
    }

}
