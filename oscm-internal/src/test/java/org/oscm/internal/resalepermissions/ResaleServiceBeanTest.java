/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.resalepermissions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.vo.VOService;

public class ResaleServiceBeanTest {

    private ServiceProvisioningPartnerServiceLocal sppslMock;
    private ResaleServiceBean resaleServiceBean;

    @Before
    public void setup() {
        sppslMock = mock(ServiceProvisioningPartnerServiceLocal.class);
        resaleServiceBean = new ResaleServiceBean();
        resaleServiceBean.spPartnerServiceLocal = sppslMock;
    }

    @Test
    public void getServicesForVendor_NoProduct() {
        // given
        List<Product> products = new ArrayList<Product>();
        setSpPartnerServiceLocalMock(products);

        // when
        Response response = resaleServiceBean.getServicesForVendor();

        // then
        assertEquals(0, response.getResultList(VOService.class).size());
    }

    @Test
    public void getServicesForVendor_MultipleProducts() {
        // given
        Organization supplier = createOrganization("MySupplier",
                OrganizationRoleType.SUPPLIER);
        Organization broker = createOrganization("MyBroker",
                OrganizationRoleType.BROKER);
        Organization reseller = createOrganization("MyReseller",
                OrganizationRoleType.RESELLER);
        Product product1 = createResaleProduct(11111, "productId1", supplier,
                broker);
        Product product2 = createResaleProduct(22222, "productId2", supplier,
                reseller);

        List<Product> products = new ArrayList<Product>();
        products.add(product1);
        products.add(product2);
        setSpPartnerServiceLocalMock(products);

        // when
        Response response = resaleServiceBean.getServicesForVendor();
        // then
        List<VOService> results = response.getResultList(VOService.class);
        assertEquals(2, results.size());
        assertEquals(11111, results.get(0).getKey());
        assertEquals(22222, results.get(1).getKey());

        assertEquals("productId1", results.get(0).getServiceId());
        assertEquals("productId2", results.get(1).getServiceId());
    }

    private Organization createOrganization(String orgId,
            OrganizationRoleType orgRole) {
        Organization org = new Organization();
        org.setOrganizationId(orgId);
        addRole(org, orgRole);
        return org;
    }

    private void addRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }

    private Product createResaleProduct(long key, String productId,
            Organization supplier, Organization vendor) {
        Product productTemplate = createProductTemplate(productId, supplier);
        Product resaleCopy = productTemplate.copyForResale(vendor);
        resaleCopy.setKey(key);
        return resaleCopy;
    }

    private Product createProductTemplate(String productId,
            Organization supplier) {
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setTechnicalProductId("TP");
        Product productTemplate = new Product();
        productTemplate.setProductId(productId);
        productTemplate.setStatus(ServiceStatus.ACTIVE);
        productTemplate.setVendor(supplier);
        productTemplate.setTechnicalProduct(techProduct);
        productTemplate.setAutoAssignUserEnabled(Boolean.FALSE);
        PriceModel pm = new PriceModel();
        pm.setProduct(productTemplate);
        productTemplate.setPriceModel(pm);
        return productTemplate;
    }

    private void setSpPartnerServiceLocalMock(List<Product> products) {
        when(sppslMock.getProductsForVendor()).thenReturn(products);

        DataService mockDS = mock(DataService.class);
        resaleServiceBean.dm = mockDS;

        Organization org = new Organization();
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        user.setLocale("en");
        doReturn(user).when(mockDS).getCurrentUser();

        LocalizerServiceLocal mockLocalizer = mock(LocalizerServiceLocal.class);
        resaleServiceBean.localizer = mockLocalizer;
    }

}
