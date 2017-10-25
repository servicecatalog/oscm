/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                  
 *                                                                              
 *  Creation Date: 20.10.2011                                                      
 *                                                                              
 *  Completion Time: 20.10.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Product;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.ServiceProvisioningServiceInternal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.validation.ArgumentValidator;

/**
 * Implementation of performance optimized listing of services. This class is
 * intended only as a temporary optimization and will be replaced by other
 * means. Only for internal usage.
 * 
 * @author cheld
 * 
 */
@Stateless
@Remote(ServiceProvisioningServiceInternal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ServiceProvisioningServiceInternalBean
        implements
        ServiceProvisioningServiceInternal {

    @EJB
    private ServiceProvisioningService serviceProvisioningServiceBean;

    @EJB(beanInterface = DataService.class)
    private DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    private LocalizerServiceLocal localizer;
//TODO: fix performance hits.
    public List<VOService> getServicesForMarketplace(String marketplaceId,
            PerformanceHint performanceHint) {
        return getServiceProvisioningServiceBean().getServicesForMarketplace(marketplaceId);
    }

    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<VOService> getSuppliedServices(PerformanceHint performanceHint) {
        return getServiceProvisioningServiceBean().getSuppliedServices();
    }

    @RolesAllowed({ "SERVICE_MANAGER", "TECHNOLOGY_MANAGER" })
    public List<VOTechnicalService> getTechnicalServices(
            OrganizationRoleType role, PerformanceHint performanceHint) throws OrganizationAuthoritiesException {
        return getServiceProvisioningServiceBean().getTechnicalServices(role);
    }

    @Override
    public VOOrganization getPartnerForService(long serviceKey, String locale) throws ObjectNotFoundException {

        ArgumentValidator.notNull("locale", locale);

        Product product = dm.getReference(Product.class, serviceKey);
        return OrganizationAssembler.toVOOrganization(product.getVendor(),
                false, new LocalizerFacade(localizer, locale));
    }

    public ServiceProvisioningService getServiceProvisioningServiceBean() {
        return serviceProvisioningServiceBean;
    }
}
