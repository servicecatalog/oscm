/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptiondetails;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.DiscountService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.ServiceProvisioningServiceInternal;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.intf.SubscriptionServiceInternal;
import org.oscm.internal.partnerservice.PartnerService;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUserDetails;

@Stateless
@Remote(SubscriptionDetailsService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class SubscriptionDetailsServiceBean implements
        SubscriptionDetailsService {

    @EJB(beanInterface = SubscriptionService.class)
    SubscriptionService subscriptionService;

    @EJB(beanInterface = SubscriptionServiceInternal.class)
    SubscriptionServiceInternal subscriptionServiceInternal;

    @EJB(beanInterface = IdentityService.class)
    IdentityService identityService;

    @EJB(beanInterface = AccountService.class)
    AccountService accountService;

    @EJB(beanInterface = DiscountService.class)
    DiscountService discountService;

    @EJB(beanInterface = ServiceProvisioningService.class)
    ServiceProvisioningService serviceProvisioningService;

    @EJB(beanInterface = ServiceProvisioningServiceInternal.class)
    ServiceProvisioningServiceInternal serviceProvisioningServiceInternal;

    @EJB(beanInterface = SessionServiceLocal.class)
    SessionServiceLocal sessionServiceLocal;

    @EJB(beanInterface = PartnerService.class)
    PartnerService partnerService;

    @EJB(beanInterface = SessionService.class)
    SessionService sessionService;

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @Resource
    SessionContext sessionCtx;

    public Response getSubscriptionDetails(String id, String language)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, OrganizationAuthoritiesException {
        final VOSubscriptionDetails sd = subscriptionService
                .getSubscriptionDetails(id);

        final List<VOService> services = subscriptionService
                .getUpgradeOptions(id);

        final List<VORoleDefinition> serviceRoles = subscriptionService
                .getServiceRolesForSubscription(id);

        POSubscriptionDetails subscriptionDetails = getSubscriptionDetails(sd,
                services, serviceRoles, language);

        return new Response(subscriptionDetails);
    }
    
    @Override
    public Response getSubscriptionDetails(long key, String language)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, OrganizationAuthoritiesException {
        final VOSubscriptionDetails sd = subscriptionService
                .getSubscriptionDetails(key);
    
        final List<VOService> services = subscriptionService
                .getUpgradeOptions(key);
    
        final List<VORoleDefinition> serviceRoles = subscriptionService
                .getServiceRolesForSubscription(key);
    
        POSubscriptionDetails subscriptionDetails = getSubscriptionDetails(sd,
                services, serviceRoles, language);
    
        return new Response(subscriptionDetails);
    }
    
    POSubscriptionDetails getSubscriptionDetails(VOSubscriptionDetails sd,
            List<VOService> services, List<VORoleDefinition> serviceRoles,
            String language)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationAuthoritiesException, ValidationException {
        final POSubscriptionDetails subscriptionDetails = new POSubscriptionDetails();
        
        subscriptionDetails.setSubscription(sd);
        Subscription sub = ds.getReference(Subscription.class, sd.getKey());
        String vendorId = sub.getProduct().getSupplierOrResellerTemplate()
                .getVendor().getOrganizationId();
    
        List<VOUserDetails> usersForOrganization = identityService
                .getUsersForOrganization();
    
        subscriptionDetails.setUsersForOrganization(usersForOrganization);
    
        subscriptionDetails.setAllUsers(usersForOrganization);
        subscriptionDetails.setCustomer(accountService.getOrganizationData());
        subscriptionDetails.setUpgradeOptions(services);
        subscriptionDetails.setSeller(serviceProvisioningService
                .getServiceSellerFallback(sd.getServiceKey(), language));
        subscriptionDetails.setPartner(serviceProvisioningServiceInternal
                .getPartnerForService(sd.getServiceKey(), language));
    
        subscriptionDetails.setUdasDefinitions(accountService
                .getUdaDefinitionsForCustomer(vendorId));
        subscriptionDetails.setUdasOrganisation(accountService
                .getUdasForCustomer("CUSTOMER", subscriptionDetails
                        .getCustomer().getKey(), vendorId));
        subscriptionDetails.setUdasSubscription(accountService
                .getUdasForCustomer("CUSTOMER_SUBSCRIPTION", sd.getKey(),
                        vendorId));
        subscriptionDetails.setStatus(sub.getStatus());

        subscriptionDetails.setServiceRoles(serviceRoles);
    
        subscriptionDetails.setDiscount(discountService
                .getDiscountForService(sd.getServiceKey()));
        subscriptionDetails.setNumberOfSessions(sessionService
                .getNumberOfServiceSessions(sd.getKey()));
        
        return subscriptionDetails;
    }
    
    public Response loadSubscriptionStatus(long selectedSubscriptionKey)
            throws ObjectNotFoundException {
        Subscription sub = ds.getReference(Subscription.class,
                selectedSubscriptionKey);
        return new Response(sub.getStatus());
    }

    public Response getServiceForSubscription(long serviceKey, String language)
            throws ObjectNotFoundException, ServiceStateException,
            OperationNotPermittedException, ValidationException,
            OrganizationAuthoritiesException {
        final POServiceForSubscription service = new POServiceForSubscription();
        service.setService(partnerService.getServiceForMarketplace(serviceKey,
                language).getResult(VOServiceEntry.class));
        String vendorId = ds.getReference(Product.class, serviceKey)
                .getSupplierOrResellerTemplate().getVendor()
                .getOrganizationId();
        service.setSubscriptions(subscriptionServiceInternal
                .getAllSubscriptionsForOrganization(PerformanceHint.ONLY_IDENTIFYING_FIELDS));
        service.setDefinitions(accountService
                .getUdaDefinitionsForCustomer(vendorId));
        service.setOrganizationUdas(accountService.getUdasForCustomer(
                "CUSTOMER", ds.getCurrentUser().getOrganization().getKey(),
                vendorId));
        service.setDiscount(discountService.getDiscountForService(serviceKey));
        return new Response(service);
    }

    public boolean isUserAssignedToTheSubscription(long userKey,
            long subscriptionKey) throws ObjectNotFoundException {

        PlatformUser user = ds.getReference(PlatformUser.class, userKey);
        Subscription sub = ds.getReference(Subscription.class, subscriptionKey);
        UsageLicense result = sessionServiceLocal.findUsageLicense(sub,
                user.getUserId());
        return (result != null);
    }

}
