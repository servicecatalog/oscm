/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOPriceModelLocalization;

public class ServiceProvisioningServiceLocalizationBeanTest {

    private ServiceProvisioningServiceLocalizationBean localizerBean;
    private LocalizerServiceLocal localizer;
    private DataService ds;

    private PlatformUser user;
    private Organization organization;
    private Product product;
    private Product templateProduct;
    private PriceModel priceModel;

    @Before
    public void setup() {
        ds = mock(DataService.class);
        localizer = mock(LocalizerServiceLocal.class);
        localizerBean = spy(new ServiceProvisioningServiceLocalizationBean());
        localizerBean.ds = ds;
        localizerBean.localizer = localizer;
        user = new PlatformUser();
        user.setKey(1L);
    }

    void mockIsRightsForLocalizedResources(Boolean b)
            throws ObjectNotFoundException {
        doReturn(b).when(localizerBean).checkIsAllowedForLocalizingService(
                anyLong());
    }

    public void givenSupplierManager() {
        UserRoleType roleType = UserRoleType.SERVICE_MANAGER;
        user.setAssignedRoles(newRoleAssignment(roleType));
    }

    private Set<RoleAssignment> newRoleAssignment(UserRoleType roleType) {
        Set<RoleAssignment> roles = new HashSet<RoleAssignment>();
        RoleAssignment ra = new RoleAssignment();
        ra.setKey(1L);
        ra.setUser(user);
        ra.setRole(new UserRole(roleType));
        roles.add(ra);
        return roles;
    }

    private void newProductAndOrganization(long prodKey, OrganizationRoleType or)
            throws ObjectNotFoundException {
        organization = new Organization();
        organization.setKey(10L);
        organization.setOrganizationId("organizationId");
        Set<OrganizationToRole> otrs = newOrganizationRoles(or);
        organization.getGrantedRoles().addAll(otrs);
        user.setOrganization(organization);

        product = new Product();
        product.setProductId("productId");
        product.setKey(prodKey);
        product.setVendor(organization);

        doReturn(product).when(ds).getReference(Product.class, prodKey);
    }

    /**
     * Creates a supplier organization and a template service with a price
     * model.
     * 
     * @return service key
     * @throws ObjectNotFoundException
     */
    private long givenSupplierService() throws ObjectNotFoundException {
        long srvKey = 100L;
        newProductAndOrganization(srvKey, OrganizationRoleType.SUPPLIER);
        newPriceModel(product);
        return srvKey;
    }

    /**
     * Creates a broker organization and a service copy of a template service.
     * The template service has a price model whereas the service copy does not
     * has a price model.
     * 
     * @return
     * @throws ObjectNotFoundException
     */
    private long givenBrokerService() throws ObjectNotFoundException {
        long srvKey = 110L;
        newProductAndOrganization(srvKey, OrganizationRoleType.BROKER);
        newTemplateProduct(120L);
        product.setTemplate(templateProduct);
        newPriceModel(templateProduct);
        return srvKey;
    }

    private long givenResellerService() throws ObjectNotFoundException {
        long srvKey = 140L;
        newProductAndOrganization(srvKey, OrganizationRoleType.RESELLER);
        newTemplateProduct(150L);
        product.setTemplate(templateProduct);
        newPriceModel(templateProduct);
        return srvKey;
    }

    private void newTemplateProduct(long templateKey)
            throws ObjectNotFoundException {
        templateProduct = new Product();
        templateProduct.setProductId("templateproductId");
        templateProduct.setKey(templateKey);
        templateProduct.setVendor(null);
        doReturn(templateProduct).when(ds).getReference(Product.class,
                templateKey);
    }

    private Set<OrganizationToRole> newOrganizationRoles(OrganizationRoleType or) {
        Set<OrganizationToRole> otrs = new HashSet<OrganizationToRole>();
        OrganizationToRole otr = new OrganizationToRole();
        otr.setKey(1L);
        otr.setOrganization(organization);
        OrganizationRole role = new OrganizationRole(or);
        otr.setOrganizationRole(role);
        otrs.add(otr);
        return otrs;
    }

    private void newPriceModel(Product p) throws ObjectNotFoundException {
        long pmKey = 1000L;
        priceModel = new PriceModel();
        priceModel.setKey(pmKey);
        priceModel.setProduct(p);
        p.setPriceModel(priceModel);
        doReturn(priceModel).when(ds).getReference(PriceModel.class, pmKey);

        VOLocalizedText pmDesc = new VOLocalizedText("en",
                "pricemodel description");
        VOLocalizedText rLic = new VOLocalizedText("en", "reseller license");
        VOLocalizedText pmLic = new VOLocalizedText("en", "pricemodel license");

        when(
                localizer.getLocalizedValues(eq(pmKey),
                        eq(LocalizedObjectTypes.PRICEMODEL_DESCRIPTION)))
                .thenReturn(Arrays.asList(pmDesc));
        when(
                localizer.getLocalizedValues(eq(product.getKey()),
                        eq(LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE)))
                .thenReturn(Arrays.asList(rLic));
        when(
                localizer.getLocalizedValues(eq(pmKey),
                        eq(LocalizedObjectTypes.PRICEMODEL_LICENSE)))
                .thenReturn(Arrays.asList(pmLic));
    }

    @Test
    public void getPriceModelLocalization_supplier() throws Exception {
        // given
        mockIsRightsForLocalizedResources(Boolean.TRUE);
        long srvKey = givenSupplierService();

        // when
        VOPriceModelLocalization localization = localizerBean
                .getPriceModelLocalization(srvKey);

        // then
        assertEquals(1, localization.getDescriptions().size());
        assertEquals("pricemodel description", localization.getDescriptions()
                .get(0).getText());
        assertEquals(1, localization.getLicenses().size());
        assertEquals("pricemodel license", localization.getLicenses().get(0)
                .getText());
    }

    @Test
    public void getPriceModelLocalization_broker() throws Exception {
        // given
        mockIsRightsForLocalizedResources(Boolean.TRUE);
        long srvKey = givenBrokerService();

        // when
        VOPriceModelLocalization localization = localizerBean
                .getPriceModelLocalization(srvKey);

        // then
        assertEquals(1, localization.getDescriptions().size());
        assertEquals("pricemodel description", localization.getDescriptions()
                .get(0).getText());
        assertEquals(1, localization.getLicenses().size());
        assertEquals("pricemodel license", localization.getLicenses().get(0)
                .getText());
    }

    @Test
    public void getPriceModelLocalization_reseller() throws Exception {
        // given
        mockIsRightsForLocalizedResources(Boolean.TRUE);
        long srvKey = givenResellerService();

        // when
        VOPriceModelLocalization localization = localizerBean
                .getPriceModelLocalization(srvKey);

        // then
        assertEquals(1, localization.getDescriptions().size());
        assertEquals("pricemodel description", localization.getDescriptions()
                .get(0).getText());
        assertEquals(1, localization.getLicenses().size());
        assertEquals("reseller license", localization.getLicenses().get(0)
                .getText());
    }

}
