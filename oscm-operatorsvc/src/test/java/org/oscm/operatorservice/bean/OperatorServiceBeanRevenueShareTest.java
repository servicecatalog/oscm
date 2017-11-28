/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 26, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

public class OperatorServiceBeanRevenueShareTest {

    OperatorServiceBean opSrvBean;
    DataService ds;
    AccountServiceLocal as;
    SessionContext sctx;
    LocalizerServiceLocal lsl;

    PlatformUser currentUser;
    Organization createdOrg;

    @Before
    public void setup() throws Exception {
        opSrvBean = new OperatorServiceBean();
        createdOrg = null;

        currentUser = new PlatformUser();
        currentUser.setLocale("en");

        ds = mock(DataService.class);
        doReturn(currentUser).when(ds).getCurrentUser();
        opSrvBean.dm = ds;

        as = mock(AccountServiceLocal.class);
        opSrvBean.accMgmt = as;

        sctx = mock(SessionContext.class);
        opSrvBean.sessionCtx = sctx;

        lsl = mock(LocalizerServiceLocal.class);
        opSrvBean.localizer = lsl;
    }

    @Test
    public void createOrganization_Supplier() throws Exception {
        // given
        final String SUPPLIER_NAME = "SupplierOrg";
        final BigDecimal OPERATOR_REVENUE_SHARE = BigDecimal.valueOf(35);
        VOOrganization supplier = givenOrganization("a6b78813", SUPPLIER_NAME,
                OPERATOR_REVENUE_SHARE);
        VOUserDetails supplUser = new VOUserDetails();

        mockRegisterOrganization();

        // when
        VOOrganization resultOrg = opSrvBean.registerOrganization(supplier,
                null, supplUser, null, null, OrganizationRoleType.SUPPLIER);

        // then
        assertNotNull("Creation of organization failed", resultOrg);
        assertEquals("Wrong supplier name", SUPPLIER_NAME, resultOrg.getName());

        ArgumentCaptor<RevenueShareModel> argRevShareModel = ArgumentCaptor
                .forClass(RevenueShareModel.class);
        verify(ds).persist(argRevShareModel.capture());
        assertEquals("Wrong operator revenue share persisted",
                OPERATOR_REVENUE_SHARE, argRevShareModel.getValue()
                        .getRevenueShare());
        assertSame("Wrong operator price model in created organization",
                createdOrg.getOperatorPriceModel(), argRevShareModel.getValue());

        verify(as).registerOrganization(createdOrg, null, supplUser, null,
                supplier.getDomicileCountry(), null, supplier.getDescription(),
                OrganizationRoleType.SUPPLIER);
    }

    // TODO Thomas: Remove this when the UI works
    @Ignore
    @Test
    public void createOrganization_Supplier_MissingRevenueShare()
            throws Exception {
        // given
        final String SUPPLIER_NAME = "SupplierOrg";
        VOOrganization supplier = givenOrganization("a6b78813", SUPPLIER_NAME,
                null);
        VOUserDetails supplUser = new VOUserDetails();

        try {
            // when
            opSrvBean.registerOrganization(supplier, null, supplUser, null,
                    null, OrganizationRoleType.SUPPLIER);
            fail("ValidationException expected");
        } catch (ValidationException ve) {
            // then
            assertEquals(
                    "Cause must be invalid operator revenue share",
                    ValidationException.ReasonEnum.INVALID_OPERATOR_REVENUE_SHARE,
                    ve.getReason());
        }
    }

    @Test
    public void createOrganization_TechProvider_InvalidRevenueShare()
            throws Exception {
        // given
        final String ORG_NAME = "TechProvOrg";
        VOOrganization org = givenOrganization("a6b78813", ORG_NAME,
                BigDecimal.valueOf(18));

        try {
            // when
            opSrvBean.registerOrganization(org, null, new VOUserDetails(),
                    null, null, OrganizationRoleType.TECHNOLOGY_PROVIDER);
            fail("ValidationException expected");
        } catch (ValidationException ve) {
            // then
            assertEquals(
                    "Cause must be invalid operator revenue share",
                    ValidationException.ReasonEnum.INVALID_OPERATOR_REVENUE_SHARE,
                    ve.getReason());
        }
    }

    @Test
    public void createOrganization_Supplier_InvalidRevenueShareRange()
            throws Exception {
        // given
        final String SUPPLIER_NAME = "SupplierOrg";
        VOOrganization supplier = givenOrganization("a6b78813", SUPPLIER_NAME,
                BigDecimal.valueOf(185));
        VOUserDetails supplUser = new VOUserDetails();

        try {
            // when
            opSrvBean.registerOrganization(supplier, null, supplUser, null,
                    null, OrganizationRoleType.SUPPLIER);
            fail("ValidationException expected");
        } catch (ValidationException ve) {
            // then
            assertEquals("Cause must be invalid value range",
                    ValidationException.ReasonEnum.VALUE_NOT_IN_RANGE,
                    ve.getReason());
        }
    }

    @Test
    public void updateOrganizationIntern_TechProviderToSupplier()
            throws Exception {
        // given
        final String ORG_NAME = "MyOrg";
        final BigDecimal OPERATOR_REVENUE_SHARE = BigDecimal.valueOf(35);
        VOOperatorOrganization org = givenOrganizationWithRoles("deadbeef",
                ORG_NAME, OPERATOR_REVENUE_SHARE, OrganizationRoleType.SUPPLIER);

        final Organization updatedOrg = givenOrgDomainObj("deadbeef", ORG_NAME,
                null, OrganizationRoleType.TECHNOLOGY_PROVIDER);
        addPaymentTypeInvoice(updatedOrg,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER,
                OrganizationRoleType.SUPPLIER);

        SupportedCountry sc = new SupportedCountry();
        doReturn(updatedOrg).doReturn(sc).when(ds)
                .getReferenceByBusinessKey(any(Organization.class));
        mockAddOrganizationToRole(updatedOrg);

        // when
        VOOperatorOrganization resultOrg = opSrvBean.updateOrganizationIntern(
                org, null);

        // then
        assertTrue("Supplier role missing", resultOrg.getOrganizationRoles()
                .contains(OrganizationRoleType.SUPPLIER));
        assertEquals("Wrong operator revenue share", OPERATOR_REVENUE_SHARE,
                resultOrg.getOperatorRevenueShare());

        ArgumentCaptor<RevenueShareModel> argRevShareModel = ArgumentCaptor
                .forClass(RevenueShareModel.class);
        verify(ds).persist(argRevShareModel.capture());
        assertEquals("Wrong operator revenue share persisted",
                OPERATOR_REVENUE_SHARE, argRevShareModel.getValue()
                        .getRevenueShare());
        assertSame("Wrong operator price model in updated organization",
                updatedOrg.getOperatorPriceModel(), argRevShareModel.getValue());
    }

    @Test
    public void updateOrganizationIntern_ChangeRevenueShare() throws Exception {
        // given
        final String ORG_NAME = "MyOrg";
        final BigDecimal NEW_OPERATOR_REVENUE_SHARE = BigDecimal.valueOf(25);
        VOOperatorOrganization org = givenOrganizationWithRoles("deadbeef",
                ORG_NAME, NEW_OPERATOR_REVENUE_SHARE,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        final Organization updatedOrg = givenOrgDomainObj("deadbeef", ORG_NAME,
                BigDecimal.valueOf(15),
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);

        SupportedCountry sc = new SupportedCountry();
        doReturn(updatedOrg).doReturn(sc).when(ds)
                .getReferenceByBusinessKey(any(Organization.class));

        // when
        VOOperatorOrganization resultOrg = opSrvBean.updateOrganizationIntern(
                org, null);

        // then
        assertEquals("Wrong operator revenue share",
                NEW_OPERATOR_REVENUE_SHARE, resultOrg.getOperatorRevenueShare());

        verify(ds, never()).persist(any(DomainObject.class));

        assertSame("Wrong operator revenue share in updated organization",
                NEW_OPERATOR_REVENUE_SHARE, updatedOrg.getOperatorPriceModel()
                        .getRevenueShare());
    }

    @Test
    public void updateOrganizationIntern_TechProviderToSupplier_InvalidRevenueShareRange()
            throws Exception {
        // given
        final String ORG_NAME = "MyOrg";
        VOOperatorOrganization org = givenOrganizationWithRoles("deadbeef",
                ORG_NAME, BigDecimal.valueOf(228),
                OrganizationRoleType.SUPPLIER);

        final Organization updatedOrg = givenOrgDomainObj("deadbeef", ORG_NAME,
                null, OrganizationRoleType.TECHNOLOGY_PROVIDER);

        SupportedCountry sc = new SupportedCountry();
        doReturn(updatedOrg).doReturn(sc).when(ds)
                .getReferenceByBusinessKey(any(Organization.class));

        // when
        try {
            opSrvBean.updateOrganizationIntern(org, null);
            fail("ValidationException expected");
        } catch (ValidationException ve) {
            // then
            assertEquals("Cause must be invalid value range",
                    ValidationException.ReasonEnum.VALUE_NOT_IN_RANGE,
                    ve.getReason());
        }
    }

    @Test
    public void updateOrganizationIntern_TechProviderToSupplier_MissingRevenueShare()
            throws Exception {
        // given
        final String ORG_NAME = "MyOrg";
        VOOperatorOrganization org = givenOrganizationWithRoles("deadbeef",
                ORG_NAME, null, OrganizationRoleType.SUPPLIER);

        final Organization updatedOrg = givenOrgDomainObj("deadbeef", ORG_NAME,
                null, OrganizationRoleType.TECHNOLOGY_PROVIDER);
        addPaymentTypeInvoice(updatedOrg,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER,
                OrganizationRoleType.SUPPLIER);

        SupportedCountry sc = new SupportedCountry();
        doReturn(updatedOrg).doReturn(sc).when(ds)
                .getReferenceByBusinessKey(any(Organization.class));
        mockAddOrganizationToRole(updatedOrg);

        // when
        try {
            opSrvBean.updateOrganizationIntern(org, null);
            fail("ValidationException expected");
        } catch (ValidationException ve) {
            // then
            assertEquals(
                    "Cause must be invalid operator revenue share",
                    ValidationException.ReasonEnum.INVALID_OPERATOR_REVENUE_SHARE,
                    ve.getReason());
        }
    }

    @Test
    public void updateOrganizationIntern_TechProvider_InvalidRevenueShare()
            throws Exception {
        // given
        final String ORG_NAME = "MyOrg";
        final BigDecimal OPERATOR_REVENUE_SHARE = BigDecimal.valueOf(25);
        VOOperatorOrganization org = givenOrganizationWithRoles("deadbeef",
                ORG_NAME, OPERATOR_REVENUE_SHARE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        final Organization updatedOrg = givenOrgDomainObj("deadbeef", ORG_NAME,
                null, OrganizationRoleType.TECHNOLOGY_PROVIDER);

        SupportedCountry sc = new SupportedCountry();
        doReturn(updatedOrg).doReturn(sc).when(ds)
                .getReferenceByBusinessKey(any(Organization.class));

        try {
            // when
            opSrvBean.updateOrganizationIntern(org, null);
            fail("ValidationException expected");
        } catch (ValidationException ve) {
            // then
            assertEquals(
                    "Cause must be invalid operator revenue share",
                    ValidationException.ReasonEnum.INVALID_OPERATOR_REVENUE_SHARE,
                    ve.getReason());
        }
    }

    private VOOrganization givenOrganization(String orgId, String orgName,
            BigDecimal operatorRevenueShare) {
        return fillVOOrganization(new VOOperatorOrganization(), orgId, orgName,
                operatorRevenueShare);
    }

    private VOOrganization fillVOOrganization(VOOrganization org, String orgId,
            String orgName, BigDecimal operatorRevenueShare) {
        org.setKey(1L);
        org.setVersion(0);
        org.setOrganizationId(orgId);

        org.setEmail("email@email.de");
        org.setPhone("phone");
        org.setUrl("http://www.tom.com");
        org.setName(orgName);
        org.setAddress("address");
        org.setLocale("en");
        org.setOperatorRevenueShare(operatorRevenueShare);

        return org;
    }

    private VOOperatorOrganization givenOrganizationWithRoles(String orgId,
            String orgName, BigDecimal operatorRevenueShare,
            OrganizationRoleType... roleType) {
        VOOperatorOrganization org = new VOOperatorOrganization();
        fillVOOrganization(org, orgId, orgName, operatorRevenueShare);
        org.setOrganizationRoles(Arrays.asList(roleType));

        return org;
    }

    private Organization givenOrgDomainObj(String orgId, String orgName,
            BigDecimal operatorRevenueShare, OrganizationRoleType... roleType) {
        Organization org = new Organization();
        org.setKey(1L);
        org.setOrganizationId(orgId);
        org.setName(orgName);

        Set<OrganizationToRole> grantedRoles = new HashSet<OrganizationToRole>();
        org.setGrantedRoles(grantedRoles);
        for (OrganizationRoleType role : roleType) {
            addOrgToRole(org, role);
        }

        if (operatorRevenueShare != null) {
            RevenueShareModel operatorPriceModel = new RevenueShareModel();
            operatorPriceModel
                    .setRevenueShareModelType(RevenueShareModelType.OPERATOR_REVENUE_SHARE);
            operatorPriceModel.setRevenueShare(operatorRevenueShare);
            org.setOperatorPriceModel(operatorPriceModel);
        }

        return org;
    }

    private void addOrgToRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole orgRole = new OrganizationRole(roleType);
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganizationRole(orgRole);
        orgToRole.setOrganization(org);
        org.getGrantedRoles().add(orgToRole);
    }

    private void addPaymentTypeInvoice(Organization org,
            OrganizationReferenceType orgRefType, OrganizationRoleType roleType) {
        Organization po = new Organization();
        po.setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR.name());

        OrganizationReference ref = new OrganizationReference(po, org,
                orgRefType);
        po.getTargets().add(ref);
        org.getSources().add(ref);

        PaymentType pt = new PaymentType();
        pt.setKey(1);
        pt.setPaymentTypeId(PaymentType.INVOICE);

        OrganizationRefToPaymentType ortpt = new OrganizationRefToPaymentType();
        ortpt.setOrganizationReference(ref);
        ortpt.setPaymentType(pt);
        ortpt.setUsedAsDefault(true);
        ortpt.setUsedAsServiceDefault(true);
        ortpt.setOrganizationRole(new OrganizationRole(roleType));
        ref.getPaymentTypes().add(ortpt);
    }

    private void mockRegisterOrganization() throws Exception {
        doAnswer(new Answer<Organization>() {
            public Organization answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] args = invocation.getArguments();
                if (args[0] instanceof Organization) {
                    createdOrg = (Organization) args[0];
                    return createdOrg;
                } else {
                    return null;
                }
            }
        }).when(as).registerOrganization(any(Organization.class),
                any(ImageResource.class), any(VOUserDetails.class),
                any(Properties.class), any(String.class), any(String.class),
                any(String.class), any(OrganizationRoleType[].class));
    }

    private void mockAddOrganizationToRole(final Organization org)
            throws Exception {
        doAnswer(new Answer<Organization>() {
            public Organization answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] args = invocation.getArguments();
                if (args[0] instanceof String
                        && ((String) args[0]).equals(org.getOrganizationId())
                        && args[1] instanceof OrganizationRoleType) {
                    OrganizationRoleType newRole = (OrganizationRoleType) args[1];
                    addOrgToRole(org, newRole);
                    return org;
                } else {
                    return null;
                }
            }
        }).when(as).addOrganizationToRole(any(String.class),
                any(OrganizationRoleType.class));
    }

}
