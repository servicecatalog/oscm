/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceOperationException.Reason;

public class ServiceProvisioningPartnerServiceLocalBeanRevokeResalePermissionTest {

    private static final String SUPPLIER_ID = "MySupplier";
    private static final String GRANTEE_ID = "MyGrantee";
    private static final String TP_ID = "testTechnicalProduct";
    private static final String PRODUCT_ID = "testProduct";

    private DataService ds;
    private ServiceProvisioningServiceLocal spsl;
    private ServiceProvisioningPartnerServiceLocalBean sppslBean;

    private Organization supplier;
    private Organization grantee;
    private Product productTemplate;
    private Product resaleCopy = null;
    private ServiceAuditLogCollector audit;

    @Before
    public void setup() {
        ds = mock(DataService.class);
        spsl = mock(ServiceProvisioningServiceLocal.class);
        audit = mock(ServiceAuditLogCollector.class);

        sppslBean = spy(new ServiceProvisioningPartnerServiceLocalBean());
        sppslBean.dm = ds;
        sppslBean.spsl = spsl;
        sppslBean.audit = audit;
    }

    private void setup(OrganizationRoleType granteeType,
            ServiceStatus resaleCopyStatus) throws ObjectNotFoundException {
        createDomainObjects(granteeType);
        createResaleCopy(resaleCopyStatus);

        doAnswer(new Answer<DomainObject<?>>() {
            @Override
            public DomainObject<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] args = invocation.getArguments();
                if (args[0] instanceof Organization) {
                    Organization org = (Organization) args[0];
                    if (org.getOrganizationId().equals(
                            grantee.getOrganizationId())) {
                        return grantee;
                    } else {
                        return supplier;
                    }
                } else if (args[0] instanceof Product) {
                    return productTemplate;
                } else {
                    return null;
                }
            }
        }).when(ds).getReferenceByBusinessKey(any(DomainObject.class));

        doReturn(resaleCopy).when(sppslBean).loadProductCopyForVendor(
                any(Organization.class), any(Product.class));
    }

    private void createDomainObjects(OrganizationRoleType granteeType) {
        supplier = new Organization();
        supplier.setOrganizationId(SUPPLIER_ID);
        grantee = new Organization();
        grantee.setOrganizationId(GRANTEE_ID);
        addRole(grantee, granteeType);

        PlatformUser supplierManager = new PlatformUser();
        supplierManager.setOrganization(supplier);
        addRole(supplier, OrganizationRoleType.SUPPLIER);
        doReturn(supplierManager).when(ds).getCurrentUser();

        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setTechnicalProductId(TP_ID);
        productTemplate = new Product();
        productTemplate.setProductId(PRODUCT_ID);
        productTemplate.setStatus(ServiceStatus.ACTIVE);
        productTemplate.setVendor(supplier);
    }

    private void addRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }

    private void createResaleCopy(ServiceStatus resaleCopyStatus) {
        resaleCopy = productTemplate.copyForResale(grantee);
        resaleCopy.setStatus(resaleCopyStatus);
    }

    @Test(expected = IllegalArgumentException.class)
    public void revokeResalePermission_NullArguments() throws Exception {
        // when
        sppslBean.revokeResalePermission(null, null, null);
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void revokeResalePermission_invalidGrantee() throws Exception {
        // given
        setup(OrganizationRoleType.SUPPLIER, ServiceStatus.ACTIVE);

        // when
        sppslBean.revokeResalePermission(productTemplate.getProductId(),
                supplier.getOrganizationId(), grantee.getOrganizationId());
    }

    @Test
    public void revokeResalePermission_NoTemplate() throws Exception {
        // given
        setup(OrganizationRoleType.BROKER, ServiceStatus.ACTIVE);
        productTemplate.setTemplate(new Product());

        // when
        try {
            sppslBean.revokeResalePermission(productTemplate.getProductId(),
                    supplier.getOrganizationId(), grantee.getOrganizationId());
            fail("ServiceOperationException expected because passed service is not a template");
        } catch (ServiceOperationException soe) {
            // then
            soe.getMessageKey().contains(
                    Reason.SERVICE_IS_NOT_A_TEMPLATE.toString());
        }
    }

    @Test
    public void revokeResalePermission_activeProduct() throws Exception {
        // given
        setup(OrganizationRoleType.BROKER, ServiceStatus.ACTIVE);

        // when
        Product result = sppslBean.revokeResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId());

        // then
        assertTrue("Wrong result", result == resaleCopy);
        assertTrue("Wrong resale copy status",
                resaleCopy.getStatus() == ServiceStatus.DELETED);
    }

    @Test
    public void revokeResalePermission_deletedProduct() throws Exception {
        // given
        setup(OrganizationRoleType.RESELLER, ServiceStatus.DELETED);

        // when
        Product result = sppslBean.revokeResalePermission(
                productTemplate.getProductId(), supplier.getOrganizationId(),
                grantee.getOrganizationId());

        // then
        assertTrue("Wrong result", result == resaleCopy);
        assertTrue("Wrong resale copy status",
                resaleCopy.getStatus() == ServiceStatus.DELETED);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void revokeResalePermission_CalledByReseller() throws Exception {
        // given
        setup(OrganizationRoleType.RESELLER, ServiceStatus.DELETED);
        addRole(supplier, OrganizationRoleType.RESELLER);

        // when
        sppslBean.revokeResalePermission(productTemplate.getProductId(),
                supplier.getOrganizationId(), grantee.getOrganizationId());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void revokeResalePermission_CalledByBroker() throws Exception {
        // given
        setup(OrganizationRoleType.BROKER, ServiceStatus.DELETED);
        addRole(supplier, OrganizationRoleType.BROKER);

        // when
        sppslBean.revokeResalePermission(productTemplate.getProductId(),
                supplier.getOrganizationId(), grantee.getOrganizationId());
    }

}
