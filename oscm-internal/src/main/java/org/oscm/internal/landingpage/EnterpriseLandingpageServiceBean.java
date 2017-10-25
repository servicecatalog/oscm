/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.landingpage;

import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.serviceprovisioningservice.local.ProductSearchResult;
import org.oscm.serviceprovisioningservice.local.SearchServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.internal.assembler.POLandingpageEntryAssembler;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOCategory;

/**
 * @author zankov
 * 
 */
@Stateless
@Remote(EnterpriseLandingpageService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class EnterpriseLandingpageServiceBean implements
        EnterpriseLandingpageService {

    @EJB
    SearchServiceLocal searchService;

    @EJB
    LocalizerServiceLocal localizer;

    @EJB
    CategorizationService categorizationService;

    @EJB
    SubscriptionServiceLocal subscriptionService;

    @EJB
    DataService dataManager;

    List<VOCategory> loadCategories(String marketplaceId, String locale) {
        List<VOCategory> categories = categorizationService.getCategories(
                marketplaceId, locale);
        if (categories.size() > 2) {
            categories = categories.subList(0, 3);
        }
        return categories;
    }

    @Override
    public Response loadLandingpageEntries(String marketplaceId, String locale) {
        List<VOCategory> categories = loadCategories(marketplaceId, locale);
        List<Subscription> subscriptions = loadSubscriptions();
        EnterpriseLandingpageData result = new EnterpriseLandingpageData();

        for (VOCategory category : categories) {
            List<Product> products;
            try {
                products = loadServicesByCategory(marketplaceId,
                        category.getCategoryId());

                LocalizerFacade facade = new LocalizerFacade(localizer, locale);
                List<POLandingpageEntry> entries = POLandingpageEntryAssembler
                        .toPOLandingpageEntries(products, subscriptions, facade);
                result.addEntriesForCategory(entries, category);
            } catch (ObjectNotFoundException e) {
                result.addEntriesForCategory(
                        Collections.<POLandingpageEntry> emptyList(), category);
            }
        }

        Response response = new Response();
        response.getResults().add(result);
        return response;
    }

    private List<Subscription> loadSubscriptions() {
        PlatformUser owner = dataManager.getCurrentUser();
        return subscriptionService.getSubscriptionsForUserInt(owner);
    }

    private List<Product> loadServicesByCategory(String marketplaceId,
            String categoryId) throws ObjectNotFoundException {
        ProductSearchResult result = searchService.getServicesByCategory(
                marketplaceId, categoryId);
        return result.getServices();
    }
}
