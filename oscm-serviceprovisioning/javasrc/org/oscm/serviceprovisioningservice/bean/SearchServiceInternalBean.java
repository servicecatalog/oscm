/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                   
 *                                                                              
 *  Creation Date: 19.10.2011                                                      
 *                                                                              
 *  Completion Time: 19.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.SearchService;
import org.oscm.internal.intf.SearchServiceInternal;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.InvalidPhraseException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceListResult;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.local.ProductSearchResult;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.validation.ArgumentValidator;

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
@Remote(SearchServiceInternal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class SearchServiceInternalBean implements SearchServiceInternal {

    @EJB
    private SearchService searchServiceBean;
    @EJB
    private UserGroupServiceLocalBean userGroupService;
    @EJB(beanInterface = DataService.class)
    private DataService dm;
    private static String DEFAULT_LOCALE = "en";

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    //TODO: fix performance hits and code duplications.
    @Override
    public VOServiceListResult getServicesByCriteria(String marketplaceId,
            String locale, ListCriteria listCriteria,
            PerformanceHint performanceHint) throws ObjectNotFoundException {
        return searchServiceBean.getServicesByCriteria(marketplaceId, locale,
                listCriteria);
    }

    @Override
    public VOServiceListResult getAccesibleServices(String marketplaceId,
            String locale, ListCriteria listCriteria,
            PerformanceHint performanceHint) throws ObjectNotFoundException {
        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
        ArgumentValidator.notEmptyString("locale", locale);
        ArgumentValidator.notNull("listCriteria", listCriteria);

        PlatformUser user = dm.getCurrentUserIfPresent();
        // temporary solution to get all services for initializing the accesible
        // service list for unit admin
        Set<Long> invisibleKeys = null;
        if ((user != null) && !user.isOrganizationAdmin()
                && !user.isUnitAdmin()) {
            List<Long> invisibleKeyList = userGroupService
                    .getInvisibleProductKeysForUser(user.getKey());
            invisibleKeys = new HashSet<Long>(invisibleKeyList);
        }

        ProductSearch search = new ProductSearch(dm, marketplaceId,
                listCriteria, DEFAULT_LOCALE, locale, invisibleKeys);

        return convertToVoServiceList(search.execute(), locale,
                performanceHint);
    }

    /**
     * Converts the product list to transfer objects.
     *
     * @param productList
     *            List of domain products
     * @return found VO services
     */
    private List<VOService> convertToVoServices(List<Product> productList,
                                                LocalizerFacade facade, PerformanceHint performanceHint) {
        List<VOService> resultList = new ArrayList<VOService>();
        ProductAssembler.prefetchData(productList, facade, performanceHint);
        for (Product product : productList) {
            resultList.add(ProductAssembler.toVOProduct(product, facade,
                    performanceHint));
        }
        return resultList;
    }

    VOServiceListResult convertToVoServiceList(ProductSearchResult services,
                                               String locale, PerformanceHint performanceHint) {
        VOServiceListResult result = new VOServiceListResult();
        LocalizerFacade facade = new LocalizerFacade(localizer, locale);
        result.setResultSize(services.getResultSize());
        result.setServices(convertToVoServices(services.getServices(), facade,
                performanceHint));
        return result;
    }

    @Override
    public VOServiceListResult searchServices(String marketplaceId,
            String locale, String searchPhrase, PerformanceHint performanceHint)
            throws InvalidPhraseException, ObjectNotFoundException {
        return searchServiceBean.searchServices(marketplaceId, locale,
                searchPhrase);
    }

}
