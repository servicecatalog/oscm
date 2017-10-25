/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                    
 *                                                                              
 *  Creation Date: 17.11.2011                                                      
 *                                                                              
 *  Completion Time: 17.11.2011                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Subscription;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.intf.SubscriptionServiceInternal;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserSubscription;
import org.oscm.subscriptionservice.assembler.SubscriptionAssembler;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;

/**
 * 
 * Implementation of performance optimized search service functionality. This
 * class contains methods that provide performance optimized access. It is
 * intended only as a temporary optimization and will be replaced by other
 * means. Only for internal usage.
 * 
 * @author cheld
 * 
 */
@Stateless
@Remote(SubscriptionServiceInternal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class SubscriptionServiceInternalBean
        implements SubscriptionServiceInternal {

    @EJB
    private SubscriptionService subscriptionServiceBean;

    @EJB
    private SubscriptionServiceLocal subscriptionServiceBeanLocal;

    @EJB(beanInterface = DataService.class)
    private DataService dm;

    @EJB(beanInterface = SubscriptionListServiceLocal.class)
    SubscriptionListServiceLocal subscriptionListService;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;
//TODO: fix performance hints and code duplications
    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER", "UNIT_ADMINISTRATOR" })
    public List<VOSubscription> getSubscriptionsForOrganization(
            PerformanceHint performanceHint) {
        return subscriptionServiceBean.getSubscriptionsForOrganization();
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER", "UNIT_ADMINISTRATOR" })
    public List<VOSubscription> getAllSubscriptionsForOrganization(
            PerformanceHint performanceHint) {
        ArrayList<VOSubscription> result = new ArrayList<>();
        List<Subscription> subscriptions = subscriptionListService
                .getAllSubscriptionsForOrganization();
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        for (Subscription sub : subscriptions) {
            VOSubscription voSub = SubscriptionAssembler.toVOSubscription(sub,
                    facade, performanceHint);
            result.add(voSub);
        }
        return result;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER", "UNIT_ADMINISTRATOR" })
    public List<VOSubscription> getSubscriptionsForOrganizationWithFilter(
            Set<SubscriptionStatus> requiredStatus,
            PerformanceHint performanceHint) {
        return subscriptionServiceBean.getSubscriptionsForOrganizationWithFilter(requiredStatus);
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public List<VOUserSubscription> getSubscriptionsForUser(VOUser user,
            PerformanceHint performanceHint) throws ObjectNotFoundException,
            OperationNotPermittedException {
        return subscriptionServiceBean.getSubscriptionsForUser(user);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER", "UNIT_ADMINISTRATOR" })
    public boolean validateSubscriptionIdForOrganization(String subscriptionId) {
        return subscriptionServiceBeanLocal.validateSubscriptionIdForOrganization(subscriptionId);
    }
}
