/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.03.2011                                                      
 *                                                                              
 *  Completion Time: 14.03.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.auditlog.bean.AuditLogServiceBean;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPAccount;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Scenario;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;
import org.oscm.timerservice.bean.TimerServiceBean;
import org.oscm.triggerservice.local.TriggerServiceLocal;

/**
 * Tests for the operator service bean that use database access.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Ignore
public class OperatorServiceBeanWithDataServiceIT extends EJBTestBase {

    private OperatorService operatorService;
    private DataService dataService;
    private LocalizerServiceLocal localizer;

    private PlatformUser operatorAdmin;
    private Organization platformOp;
    private Organization supplier;
    private AccountServiceLocal accountServiceLocalMock;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.login(1);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(mock(BillingServiceLocal.class));
        container.addBean(mock(TimerServiceBean.class));
        container.addBean(new PaymentServiceStub());
        accountServiceLocalMock = Mockito.mock(AccountServiceLocal.class);
        container.addBean(accountServiceLocalMock);
        container.addBean(new IdentityServiceStub());
        container.addBean(mock(TriggerServiceLocal.class));
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }
        });
        localizer = container.get(LocalizerServiceLocal.class);
        container.addBean(new SearchServiceStub());
        container.addBean(new ImageResourceServiceStub());
        AuditLogServiceBean auditLogMock = mock(AuditLogServiceBean.class);
        when(
                auditLogMock.loadAuditLogs(Mockito.anyListOf(String.class),
                        Mockito.anyLong(), Mockito.anyLong())).thenReturn(
                new String("").getBytes());
        container.addBean(auditLogMock);
        container.addBean(new OperatorServiceBean());

        dataService = container.get(DataService.class);
        operatorService = container.get(OperatorService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final PSP psp = createPaymentTypes(dataService).get(0).getPsp();
                platformOp = Organizations.createOrganization(dataService,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                operatorAdmin = Organizations.createUserForOrg(dataService,
                        platformOp, true, "admin");

                supplier = Organizations.createOrganization(dataService,
                        OrganizationRoleType.SUPPLIER);
                final PSPAccount pspAccount = new PSPAccount();
                pspAccount.setPsp(psp);
                pspAccount.setPspIdentifier("someID");
                pspAccount.setOrganization(supplier);
                supplier.getPspAccounts().add(pspAccount);
                dataService.persist(pspAccount);
                return null;
            }
        });
        container.login(operatorAdmin.getKey(), ROLE_PLATFORM_OPERATOR);
    }

    @Test
    public void addAvailablePaymentTypes_FindsExistingEntry() throws Exception {
        OrganizationReference createdOrgRef = runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() throws Exception {
                OrganizationReference orgRef = new OrganizationReference(
                        platformOp, supplier,
                        OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
                dataService.persist(orgRef);
                dataService.flush();
                return orgRef;
            }
        });
        operatorService
                .addAvailablePaymentTypes(OrganizationAssembler
                        .toVOOrganization(supplier, false, new LocalizerFacade(
                                localizer, "en")), Collections
                        .singleton("CREDIT_CARD"));
        validate(createdOrgRef, true);
    }

    @Test
    public void testAddEnabledPaymentTypesMultipeTimes() throws Exception {
        final VOOrganization sup = OrganizationAssembler.toVOOrganization(
                supplier, false, new LocalizerFacade(localizer, "en"));
        operatorService.addAvailablePaymentTypes(sup,
                Collections.singleton("CREDIT_CARD"));
        operatorService.addAvailablePaymentTypes(sup,
                Collections.singleton("CREDIT_CARD"));
        runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() throws Exception {
                dataService.getReference(Organization.class, sup.getKey())
                        .getPaymentTypes(sup.getOrganizationId());
                return null;
            }
        });
    }

    @Test
    public void testAddPSPAccountMultipeTimes() throws Exception {
        final VOOrganization sup = OrganizationAssembler.toVOOrganization(
                supplier, false, new LocalizerFacade(localizer, "en"));
        VOPSP psp = new VOPSP();
        psp.setDistinguishedName("abc");
        psp.setId("CC");
        psp.setWsdlUrl("http://localhost:8180/example-service/services/ProvisioningService?wsdl");
        psp = operatorService.savePSP(psp);

        VOPSPAccount account = new VOPSPAccount();
        account.setPsp(psp);
        account.setPspIdentifier("123456789");

        // the test, try to save new PSPAccount twice
        operatorService.savePSPAccount(sup, account);
        try {
            operatorService.savePSPAccount(sup, account);
            Assert.fail("Duplicate PSP accounts are not allowed for one supplier!");
        } catch (ValidationException ex) {
            // expected
        }
    }

    @Test
    public void addAvailablePaymentTypes_CreatesNewEntry() throws Exception {
        operatorService
                .addAvailablePaymentTypes(OrganizationAssembler
                        .toVOOrganization(supplier, false, new LocalizerFacade(
                                localizer, "en")), Collections
                        .singleton("CREDIT_CARD"));
        validate(new OrganizationReference(platformOp, supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER), false);
    }

    @Test
    public void addOrganizationToRole_AlreadySupplier() throws Exception {
        final VOOrganization supplier = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                Scenario.setup(container, false);
                return OrganizationAssembler.toVOOrganization(Scenario
                        .getSupplier(), false, new LocalizerFacade(localizer,
                        "en"));
            }
        });
        PlatformUser opUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization op = Organizations
                        .createPlatformOperator(dataService);
                return Organizations.createUserForOrg(dataService, op, true,
                        "admin");
            }
        });
        container.login(opUser.getKey(), ROLE_PLATFORM_OPERATOR);
        Mockito.when(
                accountServiceLocalMock.addOrganizationToRole(
                        Matchers.anyString(),
                        Matchers.any(OrganizationRoleType.class))).thenAnswer(
                new Answer<Organization>() {
                    @Override
                    public Organization answer(InvocationOnMock invocation)
                            throws Throwable {
                        return dataService.getReference(Organization.class,
                                Scenario.getSupplier().getKey());
                    }
                });
        operatorService.addOrganizationToRole(supplier.getOrganizationId(),
                OrganizationRoleType.SUPPLIER);
        List<OrganizationRefToPaymentType> orgRefsPT = getAllPersistedObjectsOfType(OrganizationRefToPaymentType.class);
        int size = orgRefsPT.size();
        operatorService.addOrganizationToRole(supplier.getOrganizationId(),
                OrganizationRoleType.SUPPLIER);
        assertEquals(
                size,
                getAllPersistedObjectsOfType(OrganizationRefToPaymentType.class)
                        .size());
    }

    private void validate(OrganizationReference createdOrgRef,
            boolean validateKeys) throws Exception {
        List<OrganizationReference> allOrgRefs = getAllPersistedObjectsOfType(OrganizationReference.class);
        assertEquals(2, allOrgRefs.size());
        OrganizationReference organizationReference = allOrgRefs.get(0);
        for (OrganizationReference organizationRef : allOrgRefs) {
            if (!organizationRef.getTarget()
                    .equals(organizationRef.getSource())) {
                organizationReference = organizationRef;
                break;
            }
        }
        if (validateKeys) {
            assertEquals(createdOrgRef.getKey(), organizationReference.getKey());
        } else {
            assertTrue(organizationReference.getKey() > 0);
        }
        long orgRefKey = organizationReference.getKey();

        List<OrganizationRefToPaymentType> allOrgRefToPT = getAllPersistedObjectsOfType(OrganizationRefToPaymentType.class);
        assertEquals(1, allOrgRefToPT.size());

        OrganizationRefToPaymentType storedOrgRefToPT = allOrgRefToPT.get(0);
        for (OrganizationRefToPaymentType stOrgRefToPT : allOrgRefToPT) {
            if (orgRefKey == stOrgRefToPT.getOrganizationReference().getKey()) {
                storedOrgRefToPT = stOrgRefToPT;
                break;
            }
        }

        assertEquals(orgRefKey, storedOrgRefToPT.getOrganizationReference()
                .getKey());

        assertEquals(platformOp.getKey(), storedOrgRefToPT
                .getDefiningOrganization().getKey());
        assertEquals(supplier.getKey(), storedOrgRefToPT
                .getAffectedOrganization().getKey());
        assertEquals(OrganizationRoleType.SUPPLIER, storedOrgRefToPT
                .getOrganizationRole().getRoleName());
    }

    @Override
    protected <T> List<T> getAllPersistedObjectsOfType(final Class<T> clazz)
            throws Exception {
        return runTX(new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                String typeName = clazz.getSimpleName();
                String queryString = String.format("SELECT d FROM %s d",
                        typeName);
                Query query = container.get(DataService.class).createQuery(
                        queryString);
                List<T> result = ParameterizedTypes.list(query.getResultList(),
                        clazz);
                if (!result.isEmpty()
                        && result.get(0) instanceof OrganizationRefToPaymentType) {
                    OrganizationRefToPaymentType ref = (OrganizationRefToPaymentType) result
                            .get(0);
                    load(ref.getAffectedOrganization());
                    load(ref.getDefiningOrganization());
                    load(ref.getOrganizationReference());
                } else if (!result.isEmpty()
                        && result.get(0) instanceof OrganizationReference) {
                    for (T t : result) {
                        OrganizationReference orgRef = (OrganizationReference) t;
                        load(orgRef.getSource());
                        load(orgRef.getTarget());
                    }
                }
                return result;
            }
        });
    }
}
