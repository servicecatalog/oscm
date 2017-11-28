/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-30                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PriceModelException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author cmin
 * 
 */
public class ServiceProvisioningServiceBeanSavePriceModelForCustomerTest {

    private ServiceProvisioningServiceBean bean;
    private DataService dm;
    private PlatformUser currentUser;
    private Organization supp;
    private Product product;

    @Before
    public void before() {
        bean = spy(new ServiceProvisioningServiceBean());
        dm = mock(DataService.class);
        bean.dm = dm;

    }

    @Test(expected = ServiceStateException.class)
    public void savePriceModelForCustomer_deleted() throws Exception {
        // given
        prepareProduct(ServiceStatus.DELETED);
        setUp();

        // when
        bean.savePriceModelForCustomer(new VOServiceDetails(),
                new VOPriceModel(), new VOOrganization());
    }

    @Test
    public void savePriceModelForCustomer_OK() throws Exception {
        // given
        prepareProduct(ServiceStatus.INACTIVE);
        setUp();

        doReturn(product).when(bean).prepareProductWithPriceModel(
                any(VOServiceDetails.class), any(VOPriceModel.class),
                any(Organization.class), any(ServiceType.class),
                any(Subscription.class));

        doNothing().when(dm).flush();
        doNothing().when(dm).refresh(any(Product.class));

        doReturn(new VOServiceDetails()).when(bean).getServiceDetails(
                any(Product.class), any(LocalizerFacade.class));

        // when
        bean.savePriceModelForCustomer(new VOServiceDetails(),
                new VOPriceModel(), new VOOrganization());
    }

    private void setUp() throws OperationNotPermittedException,
            ObjectNotFoundException, PriceModelException {
        prepareCurrentUser();
        doNothing().when(bean).validateCustomersRole(any(Organization.class),
                any(PlatformUser.class));
        doReturn(supp).when(dm).getReference(eq(Organization.class), anyLong());

        doNothing().when(bean).validateCurrencyUniqunessOfMigrationPath(
                any(VOPriceModel.class), any(VOService.class));
    }

    private void prepareCurrentUser() {
        currentUser = new PlatformUser();
        currentUser.setKey(11);
        doReturn(currentUser).when(dm).getCurrentUser();
        
        supp = new Organization();        
        List<OrganizationReference> targets = new ArrayList<OrganizationReference>();
        OrganizationReference ref = new OrganizationReference(supp, supp,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        targets.add(ref);
        supp.setTargets(targets);
        
        currentUser.setOrganization(supp);
    }

    private void prepareProduct(ServiceStatus status)
            throws ObjectNotFoundException {
        Product refProduct = new Product();
        refProduct.setStatus(status);
        refProduct.setProductId("test");
        refProduct.setTemplate(new Product());
        doReturn(refProduct).when(dm)
                .getReference(eq(Product.class), anyLong());
    }
}
