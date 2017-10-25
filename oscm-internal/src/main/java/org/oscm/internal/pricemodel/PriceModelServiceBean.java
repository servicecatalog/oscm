/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 12.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricemodel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.validation.VersionAndKeyValidator;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOOrganization;

/**
 * @author weiser
 * 
 */
@Remote(PriceModelService.class)
@Stateless
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class PriceModelServiceBean implements PriceModelService {

    @EJB(beanInterface = AccountService.class)
    AccountService accountService;

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @EJB(beanInterface = ServiceProvisioningServiceLocal.class)
    ServiceProvisioningServiceLocal spsl;

    @Resource
    SessionContext sc;

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<POCustomer> getCustomers() throws SaaSApplicationException {

        List<VOOrganization> customers = new ArrayList<VOOrganization>();
        customers = accountService.getMyCustomersOptimization();
        List<POCustomer> result = new ArrayList<POCustomer>();
        for (VOOrganization o : customers) {
            POCustomer c = new POCustomer();
            c.setKey(o.getKey());
            c.setVersion(o.getVersion());
            c.setId(o.getOrganizationId());
            c.setName(o.getName());
            result.add(c);
        }

        return result;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public List<POCustomerService> getCustomerSpecificServices(String customerId)
            throws SaaSApplicationException {

        Organization cust = new Organization();
        cust.setOrganizationId(customerId);
        cust = (Organization) ds.getReferenceByBusinessKey(cust);
        List<Product> prods = spsl.getCustomerSpecificProducts(cust, ds
                .getCurrentUser().getOrganization());
        List<POCustomerService> result = new ArrayList<POCustomerService>();
        for (Product p : prods) {
            POCustomerService s = new POCustomerService();
            s.setKey(p.getKey());
            s.setVersion(p.getVersion());
            s.setId(p.getCleanProductId());
            result.add(s);
        }

        return result;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public Response deleteCustomerSpecificServices(
            List<POCustomerService> services) throws SaaSApplicationException {

        try {
            for (POCustomerService pocs : services) {
                Product p = ds.find(Product.class, pocs.getKey());
                if (p == null) {
                    // no need to delete as it doesn't exist
                    continue;
                }
                VersionAndKeyValidator.verify(p, pocs.getKey(),
                        pocs.getVersion());
                spsl.deleteProduct(ds.getCurrentUser().getOrganization(), p);
            }
        } catch (SaaSApplicationException e) {
            sc.setRollbackOnly();
            throw e;
        }

        return new Response();
    }
}
