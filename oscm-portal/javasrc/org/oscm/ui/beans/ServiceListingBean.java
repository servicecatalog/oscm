/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.oscm.converter.WhiteSpaceConverter;
import org.oscm.internal.types.enumtypes.LandingpageType;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.InvalidPhraseException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceListResult;
import org.oscm.ui.beans.marketplace.CategorySelectionBean;
import org.oscm.ui.beans.marketplace.ServicePagingBean;
import org.oscm.ui.beans.marketplace.TagCloudBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.converter.TrimConverter;
import org.oscm.ui.dialog.mp.landingpage.EnterpriseLandingpageModel;
import org.oscm.ui.model.Service;

/**
 * The ServicesListBean is responsible for serving serviceLists.
 * 
 * @author Enes Sejfi
 */
@ViewScoped
@ManagedBean(name="serviceListingBean")
public class ServiceListingBean extends BaseBean implements Serializable {

    /**
     * This list contains all selected services.
     */
    private List<Service> services;

    /**
     * This list contains all services selected by
     * SearchServiceBean.searchServices.
     */
    private List<Service> searchResult;

    /**
     * Contains the services which should be listed on the landing page.
     */
    private List<Service> landingPageServices;

    /**
     * Contains the services which should be listed on the enterprise landing
     * page.
     */
    EnterpriseLandingpageModel enterpriseLandingpageModel;

    /**
     * References the pagingBean which is responsible for paging the services.
     */
    @ManagedProperty(value="#{servicePagingBean}")
    private ServicePagingBean servicePagingBean;

    /**
     * Needed to reset the categories of a marketplace if a selected category
     * was deleted concurrently.
     */
    @ManagedProperty(value="#{categorySelectionBean}")
    private CategorySelectionBean categorySelectionBean;

    /**
     * Needed to reset the tags of a marketplace if a selected tag was deleted
     * concurrently.
     */
    @ManagedProperty(value="#{tagCloudBean}")
    private TagCloudBean tagCloudBean;

    LandingpageType landingpageType;

    private static final long serialVersionUID = 50587384050094871L;

    private boolean serviceListContainsChargeableResellerService;

    public static final Vo2ModelMapper<VOService, Service> DEFAULT_VOSERVICE_MAPPER = new Vo2ModelMapper<VOService, Service>() {
        @Override
        public Service createModel(final VOService vo) {
            return new Service(vo);
        }
    };

    void updateServiceListContainsChargeableResellerService(
            List<VOService> resultList) {
        serviceListContainsChargeableResellerService = false;
        if (resultList != null && resultList.size() > 0) {
            for (VOService svc : resultList) {
                if (OfferingType.RESELLER.equals(svc.getOfferingType())
                        && svc.getPriceModel().isChargeable()) {
                    serviceListContainsChargeableResellerService = true;
                    break;
                }
            }
        }
    }

    /**
     * @return the serviceListContainsChargeableResellerService
     */
    public boolean isServiceListContainsChargeableResellerService() {
        return serviceListContainsChargeableResellerService;
    }

    public void setServiceListContainsChargeableResellerService(
            @SuppressWarnings("unused") boolean listContainsResellerService) {
        // ignore
    }

    /**
     * Returns the list of services which should be listed on the landing page.
     * 
     * @return the services which should be listed on the landing page.
     */
    public List<Service> getServicesForLandingPage() {
        if (landingPageServices == null) {
            try {
                String locale = JSFUtils.getViewLocale().getLanguage();
                List<VOService> result = getShowLandingpage()
                        .servicesForLandingpage(getMarketplaceId(), locale);
                updateServiceListContainsChargeableResellerService(result);
                landingPageServices = DEFAULT_VOSERVICE_MAPPER.map(result);
            } catch (SaaSApplicationException e) {
                ExceptionHandler.execute(e);
            }
        }
        return landingPageServices;
    }

    /**
     * Get a list with all services.
     * 
     * @return a list with all services.
     */
    public List<Service> getServices() {
        if (services == null && isMarketplaceSet()) {
            if (isLoggedIn()) {
                services = DEFAULT_VOSERVICE_MAPPER
                        .map(getProvisioningServiceInternal()
                                .getServicesForMarketplace(
                                        getMarketplaceId(),
                                        PerformanceHint.ONLY_FIELDS_FOR_LISTINGS));
            } else if (isMarketplaceSet(getRequest())) {
                getPublicServices();
            }
        }
        return services;
    }

    /**
     * Returns a list of all public services that can be viewed in the public
     * catalog by a potential customer. The supplier ID or marketplace ID must
     * be given in the request URL.
     * 
     * @return List of services that can seen by anonymous users.
     */
    public List<Service> getPublicServices() {
        if (services == null) {
            String marketplaceId = getMarketplaceId();
            List<VOService> voList = Collections.emptyList();
            if (marketplaceId != null) {
                voList = getProvisioningServiceInternal()
                        .getServicesForMarketplace(marketplaceId,
                                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
                updateServiceListContainsChargeableResellerService(voList);
            }
            services = DEFAULT_VOSERVICE_MAPPER.map(voList);
        }
        return services;
    }

    /**
     * Executes the service for paging, sorting and filtering and updates the
     * result size if the service list is not initialized.
     * 
     * @return the paged, sorted and filtered services
     */
    public List<Service> searchWithCriteria() {
        ListCriteria criteria = servicePagingBean.getListCriteria();
        if (categorySelectionBean.isCategorySelected()) {
            criteria.setCategoryId(categorySelectionBean
                    .getSelectedCategoryId());
        }
        if (services == null) {
            try {
                VOServiceListResult result = getSearchServiceInternal()
                        .getServicesByCriteria(getMarketplaceId(),
                                JSFUtils.getViewLocale().getLanguage(),
                                criteria,
                                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
                getServicePagingBean().setResultSize(result.getResultSize());
                updateServiceListContainsChargeableResellerService(result
                        .getServices());
                services = DEFAULT_VOSERVICE_MAPPER.map(result.getServices());

            } catch (ObjectNotFoundException e) {
                if (e.getDomainObjectClassEnum() == ClassEnum.CATEGORY) {
                    refreshCategories();
                } else if (e.getDomainObjectClassEnum() == ClassEnum.TAG) {
                    refreshTags();
                } else {
                    ExceptionHandler.execute(e);
                }
            }
        }
        return services;
    }

    /**
     * Reset cached categories in case the category was not found
     */
    private void refreshCategories() {
        categorySelectionBean.resetCategoriesForMarketplace();
        services = new LinkedList<Service>();
        getServicePagingBean().setResultSize(0);
    }

    /**
     * Reset cached tags in case the tag was not found
     */
    private void refreshTags() {
        tagCloudBean.resetTagsForMarketplace();
        services = new LinkedList<Service>();
        getServicePagingBean().setResultSize(0);
    }

    /**
     * forces data to be loaded from backend
     * 
     * @param ae
     *            required by faces actionListener
     */
    public void reloadData(ActionEvent ae) {
        services = null;
    }

    /**
     * Return the service list for the marketplace portal. This is either the
     * complete, the filtered list a list resulting from the search execution.
     * <p>
     * The first one is also paged and sorted while the second is not (realized
     * in future).
     * 
     * @return the service list as described above.
     */
    public List<Service> getServiceList() {
        if (getServicePagingBean().isSearchRequested()) {
            return searchWithPhrase();
        } else {
            return searchWithCriteria();
        }
    }

    /**
     * Return the services representing the search results.
     * 
     * @return the services representing the search results.
     */
    public List<Service> searchWithPhrase() {
        String phrase = getServicePagingBean().getSearchPhrase();
        // TODO: Handle empty phrase condition in ServicePagingBean, eg.
        // combine logic with isInvalidSearchPhrase()

        String searchPhrase = WhiteSpaceConverter.replace(phrase);
        searchPhrase = searchPhrase.trim();

        if (searchPhrase != null
                && searchPhrase.trim().length() > 0
                && (searchResult == null || getServicePagingBean()
                        .isNewSearchRequired())) {
            getServicePagingBean().resetNewSearchRequired();
            try {
                VOServiceListResult result = getSearchServiceInternal()
                        .searchServices(getMarketplaceId(), getLanguage(),
                                searchPhrase.trim(),
                                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
                updateServiceListContainsChargeableResellerService(result
                        .getServices());
                searchResult = DEFAULT_VOSERVICE_MAPPER.map(result
                        .getServices());
            } catch (InvalidPhraseException e) {
                getServicePagingBean().setInvalidSearchPhrase(true);
                ExceptionHandler.execute(e);
                return Collections.emptyList();
            } catch (SaaSApplicationException e) {
                ExceptionHandler.execute(e);
            }
        }
        if (searchResult != null) {
            // Number of results must also be updated if results are cached,
            // because it might be changed by other queries.
            getServicePagingBean().setInvalidSearchPhrase(false);
            getServicePagingBean().setResultSize(searchResult.size());
        }
        categorySelectionBean.setSelectedCategoryId(null);
        return searchResult;
    }

    /**
     * Action for displaying the service list resulting from a search query.
     * 
     * @see org.oscm.ui.beans.marketplace.ServicePagingBean
     * @return OUTCOME_SHOW_SERVICE_LIST
     */
    public String showServiceListSearch() {
        String phrase = getServicePagingBean().getSearchPhrase();
        String tmp = TrimConverter.stripToNull(phrase);
        if (tmp == null) {
            return showServiceList();
        }
        getRequest().setAttribute(Constants.REQ_ATTR_SEARCH_REQUEST, phrase);
        getServicePagingBean().setFilterTag(null);
        return OUTCOME_SHOW_SERVICE_LIST;
    }

    protected String getLanguage() {
        return JSFUtils.getViewLocale().getLanguage();
    }

    /**
     * Sets the passed message key as error attribute in the current request.
     * 
     * @param errorMsgKey
     *            the error message key the be set.
     */
    private void setErrorAttribute(String errorMsgKey) {
        getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY, errorMsgKey);
    }

    /**
     * Action for displaying the service list.
     * <p>
     * All defined filter, sorting and paging criteria will be evaluated.
     * 
     * @see org.oscm.ui.beans.marketplace.ServicePagingBean
     * @return OUTCOME_SHOW_SERVICE_LIST
     */
    public String showServiceList() {
        services = null;
        return OUTCOME_SHOW_SERVICE_LIST;
    }

    /**
     * Sets the selected services
     * 
     * @param services
     *            selected services
     */
    public void setServices(List<Service> services) {
        this.services = services;
    }

    /**
     * Sets the paging bean which sorts, filters and pages the services
     * 
     * @param servicePagingBean
     *            paging bean instance
     */
    public void setServicePagingBean(ServicePagingBean servicePagingBean) {
        this.servicePagingBean = servicePagingBean;
    }

    /**
     * Returns the paging bean
     * 
     * @return current paging bean instance
     */
    public ServicePagingBean getServicePagingBean() {
        return servicePagingBean;
    }

    public void setCategorySelectionBean(
            CategorySelectionBean categorySelectionBean) {
        this.categorySelectionBean = categorySelectionBean;
    }

    public CategorySelectionBean getCategorySelectionBean() {
        return categorySelectionBean;
    }

    public TagCloudBean getTagCloudBean() {
        return tagCloudBean;
    }

    public void setTagCloudBean(TagCloudBean tagCloudBean) {
        this.tagCloudBean = tagCloudBean;
    }

    public boolean isPublicLandingpage() {
        return loadLandingpageType() == LandingpageType.PUBLIC;
    }

    LandingpageType loadLandingpageType() {
        if (landingpageType == null) {
            try {
                landingpageType = getLandingpageService().loadLandingpageType(
                        ui.getMarketplaceId());
            } catch (ObjectNotFoundException e) {
                ExceptionHandler.execute(e);
            }
        }

        return landingpageType;
    }
}
