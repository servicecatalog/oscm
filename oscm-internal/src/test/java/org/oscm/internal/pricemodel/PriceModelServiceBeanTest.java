/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 15.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricemodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOOrganization;

/**
 * @author weiser
 * 
 */
public class PriceModelServiceBeanTest {

    private static final String CUSTOMER_ID = "customerId";

    private PriceModelServiceBean bean;

    @Before
    public void setup() throws Exception {
        bean = new PriceModelServiceBean();

        bean.accountService = mock(AccountService.class);
        bean.ds = mock(DataService.class);
        bean.spsl = mock(ServiceProvisioningServiceLocal.class);
        bean.sc = mock(SessionContext.class);

        PlatformUser u = new PlatformUser();
        u.setOrganization(new Organization());

        List<Product> prods = createProducts();
        when(bean.ds.getCurrentUser()).thenReturn(u);
        when(bean.ds.getReferenceByBusinessKey(any(Organization.class)))
                .thenAnswer(new Answer<DomainObject<?>>() {

                    @Override
                    public DomainObject<?> answer(InvocationOnMock invocation)
                            throws Throwable {
                        Organization o = (Organization) invocation
                                .getArguments()[0];
                        if (CUSTOMER_ID.equals(o.getOrganizationId())) {
                            return o;
                        }
                        throw new ObjectNotFoundException();
                    }
                });
        for (Product p : prods) {
            when(bean.ds.find(eq(Product.class), eq(p.getKey()))).thenReturn(p);
        }

        when(bean.accountService.getMyCustomersOptimization()).thenReturn(
                createCustomers());

        when(
                bean.spsl.getCustomerSpecificProducts(any(Organization.class),
                        any(Organization.class))).thenReturn(prods);
    }

    @Test
    public void getCustomers() throws SaaSApplicationException {
        List<POCustomer> list = bean.getCustomers();

        assertEquals(2, list.size());
        assertEquals("org1", list.get(0).getId());
        assertEquals("org1 name", list.get(0).getName());
        assertEquals(1, list.get(0).getKey());
        assertEquals("org2", list.get(1).getId());
        assertEquals("org2 name", list.get(1).getName());
        assertEquals(2, list.get(1).getKey());
    }

    @Test
    public void getCustomerSpecificServices() throws Exception {
        List<POCustomerService> list = bean
                .getCustomerSpecificServices(CUSTOMER_ID);

        assertEquals(2, list.size());
        assertEquals("p1", list.get(0).getId());
        assertEquals(1, list.get(0).getKey());
        assertEquals("p2", list.get(1).getId());
        assertEquals(2, list.get(1).getKey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getCustomerSpecificServices_NotFound() throws Exception {
        bean.getCustomerSpecificServices("notfound");
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getCustomerSpecificServices_NotPermitted() throws Exception {
        when(
                bean.spsl.getCustomerSpecificProducts(any(Organization.class),
                        any(Organization.class))).thenThrow(
                new OperationNotPermittedException());

        bean.getCustomerSpecificServices(CUSTOMER_ID);
    }

    @Test
    public void deleteCustomerSpecificServices() throws Exception {
        List<POCustomerService> list = createCustomerServices();

        Response r = bean.deleteCustomerSpecificServices(list);

        assertNotNull(r);
        assertTrue(r.getReturnCodes().isEmpty());
        verify(bean.spsl, times(2)).deleteProduct(any(Organization.class),
                any(Product.class));
    }

    @Test
    public void deleteCustomerSpecificServices_NotFound() throws Exception {
        when(bean.ds.find(eq(Product.class), anyLong())).thenReturn(null);

        Response r = bean
                .deleteCustomerSpecificServices(createCustomerServices());

        assertNotNull(r);
        assertTrue(r.getReturnCodes().isEmpty());
        verifyZeroInteractions(bean.spsl, bean.sc);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteCustomerSpecificServices_NotPermitted() throws Exception {
        doThrow(new OperationNotPermittedException()).when(bean.spsl)
                .deleteProduct(any(Organization.class), any(Product.class));

        try {
            bean.deleteCustomerSpecificServices(createCustomerServices());
        } finally {
            verify(bean.sc).setRollbackOnly();
        }
    }

    private static List<VOOrganization> createCustomers() {
        List<VOOrganization> result = new ArrayList<VOOrganization>();
        VOOrganization o = new VOOrganization();
        o.setOrganizationId("org1");
        o.setName("org1 name");
        o.setKey(1);
        result.add(o);

        o = new VOOrganization();
        o.setOrganizationId("org2");
        o.setName("org2 name");
        o.setKey(2);
        result.add(o);
        return result;
    }

    private static List<Product> createProducts() {
        List<Product> result = new ArrayList<Product>();
        Product p = new Product();
        // customer specific copies have the time stamp added to the id
        p.setProductId("p1#12345");
        p.setKey(1);
        result.add(p);

        p = new Product();
        p.setProductId("p2#54321");
        p.setKey(2);
        result.add(p);
        return result;
    }

    private static List<POCustomerService> createCustomerServices() {
        List<Product> list = createProducts();
        List<POCustomerService> result = new ArrayList<POCustomerService>();
        for (Product p : list) {
            POCustomerService cs = new POCustomerService();
            cs.setId(p.getCleanProductId());
            cs.setKey(p.getKey());
            result.add(cs);
        }
        return result;
    }
}
