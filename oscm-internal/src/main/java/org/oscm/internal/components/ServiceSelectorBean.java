/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.components;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.domobjects.Product;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.internal.components.response.Response;

/**
 * @author barzu
 */
@Stateless
@Remote(ServiceSelector.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ServiceSelectorBean implements ServiceSelector {

    @EJB(beanInterface = ServiceProvisioningPartnerServiceLocal.class)
    ServiceProvisioningPartnerServiceLocal spPartnerServiceLocal;

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Response getTemplateServices() {

        List<Product> products = spPartnerServiceLocal.getTemplateProducts();
        List<POService> templateServices = new ArrayList<POService>();
        for (Product product : products) {
            templateServices.add(assembleServiceForPricing(product));
        }

        return new Response(templateServices);
    }

    static POService assembleServiceForPricing(Product product) {
        POService service = new POService();
        service.setKey(product.getKey());
        service.setServiceId(product.getProductId());
        service.setVendorOrganizationId(product.getVendor().getOrganizationId());
        return service;
    }

}
