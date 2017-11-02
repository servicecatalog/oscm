/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: May 5, 2011                                                      
 *                                                                              
 *  Completion Time: May 10, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.marketplace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.enumtypes.Sorting;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;

/**
 * Session scope bean that stores sorting, filtering and paging information for
 * service list retrieval in the marketplace context.
 * 
 * @author Dirk Bernsau
 * 
 */
@SessionScoped
@ManagedBean(name = "servicePagingBean")
public class ServicePagingBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -4386146284583103303L;

    private int selectedPage = 1;
    private int pageSize = 10;
    private int resultSize = 121;
    private String filterTag = "";
    private Sorting sorting = Sorting.ACTIVATION_DESCENDING;
    private final String[] availablePageSizes = { "5", "10", "20" };
    private int firstVisiblePage = 1;
    private int lastVisiblePage = 5;
    final private static int PAGE_DIRECT_SELECTION_SIZE = 5;
    Map<String, Locale> locales = new HashMap<String, Locale>();
    private List<String> sortingCriteriaList = null;
    private String searchPhrase = null;
    private boolean isNewSearchRequired = false;
    private final int MAX_SEARCH_RESULT_SIZE = 100;
    private final int MAX_SEARCH_INPUT_SIZE = 255;
    private boolean invalidSearchPhrase = false;

    private String filterCategoryForDisplay; // selected category to display in
                                             // title of result list

    @EJB
    IdentityService identityService;

    @EJB(name = "MarketplaceServiceBean")
    MarketplaceService marketplaceService;

    @ManagedProperty(value = "#{appBean}")
    private ApplicationBean applicationBean;

    public MarketplaceConfiguration getConfig() {
        return marketplaceService.getCachedMarketplaceConfiguration(BaseBean
                .getMarketplaceIdStatic());
    }

    /**
     * Defines the criteria which is used to determine the services which are
     * listed on the landing page.
     */
    private static ListCriteria landingPageCriteria = null;

    /**
     * @return the selectedPage
     */
    public int getSelectedPage() {
        return selectedPage;
    }

    /**
     * Select the page with the given index.
     * 
     * @param selectedPage
     *            the selectedPage to set
     */
    public void setSelectedPage(int selectedPage) {
        this.selectedPage = selectedPage;
        int nOfPages = getNumberOfPages();
        // The selected item should be displayed in the middle
        if (selectedPage > 3 && selectedPage < nOfPages - 3) {
            firstVisiblePage = selectedPage - 2;
            lastVisiblePage = selectedPage + 2;
        } else if (selectedPage <= 3) {
            firstVisiblePage = 1;
            lastVisiblePage = Math.min(PAGE_DIRECT_SELECTION_SIZE,
                    Math.max(nOfPages, 1));
        } else {
            // case if (selectedPage >= getNumberOfPages() - 3)
            firstVisiblePage = (nOfPages - Math.min(PAGE_DIRECT_SELECTION_SIZE,
                    nOfPages)) + 1;
            lastVisiblePage = nOfPages;
        }
        if (selectedPage > nOfPages) {
            // The selected page is no more available, maybe services have
            // been deleted. It is most likely that it's at the end of
            // the list what the user is looking for - so we take the last
            // one.
            this.selectedPage = Math.max(nOfPages, 1);
        }
    }

    /**
     * Return the number of pages according the current selected page size.
     */
    private int getNumberOfPages() {
        if ((resultSize % pageSize) > 0) {
            return (resultSize / pageSize) + 1;
        }
        return resultSize / pageSize;
    }

    /**
     * Return the index of the first service on the selected page.
     * 
     * @return the index of the first service on the selected page.
     */
    public int getFirstOnPage() {
        return ((selectedPage - 1) * pageSize) + 1;
    }

    /**
     * Return the index of the last service on the selected page.
     * 
     * @return the index of the last service on the selected page.
     */
    public int getLastOnPage() {
        return Math.min(selectedPage * pageSize, resultSize);
    }

    /**
     * Returns the currently set page size. A page size value smaller than 1 is
     * signaling an unlimited page size.
     * 
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the desired page size. A page size value smaller than 1 is treated
     * as unlimited page size.
     * 
     * @param pageSize
     *            the desired pageSize
     */
    public void setPageSize(int pageSize) {
        int oldPageSize = this.pageSize;
        this.pageSize = pageSize;
        if (oldPageSize != this.pageSize) {
            resetPaging();
        }
    }

    /**
     * Returns the size of the last handled list result. A negative size is
     * returned when no result is present.
     * 
     * @return the resultSize
     */
    public int getResultSize() {
        return resultSize;
    }

    /**
     * Sets the size of the current list result. A negative size signals that no
     * result is available.
     * 
     * @param resultSize
     *            the resultSize to set
     */
    public void setResultSize(int resultSize) {
        int oldResultSize = this.resultSize;
        this.resultSize = resultSize;
        if (oldResultSize != this.resultSize) {
            // Reselect current selection to calculate number of pages, first
            // and last page.
            setSelectedPage(selectedPage);
        }
    }

    /**
     * Returns the tag currently used for filtering or an empty string if no
     * filter is set.
     * <p>
     * Tags for a different locale than the currently defined one, are specified
     * by using the the locale as prefix separated with a comma (e.g.
     * <code>it,memoria</code>)
     * 
     * @return the filterTag
     */
    public String getFilterTag() {
        return (filterTag == null) ? "" : filterTag;
    }

    public boolean isFilterTagSelected() {
        if (this.filterTag == null || this.filterTag.trim().length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns the tag as displayed, which is currently used for filtering, or
     * an empty string if no filter is used.
     * <p>
     * 
     * In difference to {@link #getFilterTag()}, this method returns the
     * localized tag String only, without comma and locale information.
     * 
     * @return the filterTag for displaying
     */
    public String getFilterTagForDisplay() {
        String tag = getFilterTag();
        int idx = tag.indexOf(",");
        if (idx != -1 && tag.length() > idx + 1) {
            return tag.substring(idx + 1);
        }
        return tag;
    }

    /**
     * Returns the locale from the tag, which is currently used for filtering.
     * 
     * @return the local from the filter tag or <code>null</code> if no filter
     *         tag is used.
     */
    public Locale getLocaleOfFilterTag() {
        String tag = getFilterTag();
        int idx = tag.indexOf(",");
        if (idx != -1) {
            final String language = tag.substring(0, idx);
            Locale locale = locales.get(language);
            if (locale == null) {
                locale = new Locale(language);
                locales.put(language, locale);
            }
            return locale;
        }
        return null;
    }

    /**
     * Sets the tag to be used for filtering. An empty string or
     * <code>null</code> resets the filtering.
     * <p>
     * Tags for a different locale than the currently defined one, can be
     * specified by using the the locale as prefix separated with a comma (e.g.
     * <code>it,memoria</code>)
     * 
     * @param filterTag
     *            the filterTag to set
     */
    public void setFilterTag(String filterTag) {
        // ensure that oldFilterTag is not null
        String oldFilterTag = getFilterTag();
        this.filterTag = filterTag;
        if (!oldFilterTag.equals(this.filterTag)) {
            resetPaging();
        }
    }

    /**
     * Returns the sorting parameter currently used for sorting the services.
     * 
     * @return the sorting
     */
    public Sorting getSorting() {
        return sorting;
    }

    /**
     * Sets the parameter to be used for sorting. Only constants defined in
     * {@link ServiceSorting} are allowed.
     * 
     * @param sorting
     *            the sorting to set
     */
    public void setSorting(Sorting sorting) {
        if (sorting != null) {
            Sorting oldSorting = this.sorting;
            this.sorting = sorting;
            if (sorting != oldSorting)
                resetPaging();
        }
    }

    /**
     * Return list of visible pages.
     * 
     * @return list of visible pages.
     */
    public List<String> getVisiblePages() {
        List<String> result = new ArrayList<String>();
        for (int i = getFirstVisiblePage(); i <= getLastVisiblePage(); i++) {
            result.add(Integer.toString(i));
        }
        return result;
    }

    /**
     * Return the first page available in the page selection bar.
     * 
     * @return the first page available in the page selection bar.
     */
    public int getFirstVisiblePage() {
        return firstVisiblePage;
    }

    /**
     * Return the last page available in the page selection bar.
     * 
     * @return the last page available in the page selection bar.
     */
    public int getLastVisiblePage() {
        return lastVisiblePage;
    }

    /**
     * Returns the list of available sorting criteria.
     * 
     * @return the list of available sorting criteria.
     * @see ServiceSorting
     */
    public List<String> getSortingCriteria() {
        if (sortingCriteriaList == null) {
            sortingCriteriaList = new ArrayList<String>();
            sortingCriteriaList.add(Sorting.ACTIVATION_ASCENDING.name());
            sortingCriteriaList.add(Sorting.ACTIVATION_DESCENDING.name());
            sortingCriteriaList.add(Sorting.NAME_ASCENDING.name());
            sortingCriteriaList.add(Sorting.NAME_DESCENDING.name());
            if (getConfig().isReviewEnabled()) {
                sortingCriteriaList.add(Sorting.RATING_ASCENDING.name());
                sortingCriteriaList.add(Sorting.RATING_DESCENDING.name());
            }
        }
        return sortingCriteriaList;
    }

    /**
     * Get a list of available page sizes.
     * 
     * @return the list of available page sizes.
     */
    public List<String> getAvailablePagesSizes() {
        return Arrays.asList(availablePageSizes);
    }

    /**
     * Returns whether the previous link is available or not.
     * 
     * @return whether the previous link is available or not.
     */
    public boolean isPreviousAvailable() {
        return selectedPage > 1;
    }

    /**
     * Returns whether the next link is available or not.
     * 
     * @return whether the next link is available or not.
     */
    public boolean isNextAvailable() {
        return selectedPage < getNumberOfPages();
    }

    private void resetPaging() {
        setSelectedPage(1);
    }

    public ListCriteria getListCriteria() {
        ListCriteria crit = new ListCriteria();
        crit.setFilter(filterTag);
        crit.setSorting(sorting);
        crit.setLimit(pageSize);
        int offset = (selectedPage - 1) * pageSize;
        crit.setOffset(offset);
        return crit;
    }

    /**
     * Returns the static search criteria for getting the landing page services.
     * 
     * @return the search criteria for which the service on the landing page are
     *         defined.
     */
    public static ListCriteria getListCriteriaForLandingPage() {
        if (landingPageCriteria == null) {
            landingPageCriteria = new ListCriteria();
            landingPageCriteria.setFilter("");
            landingPageCriteria.setSorting(Sorting.NAME_ASCENDING);
            landingPageCriteria.setLimit(6);
            landingPageCriteria.setOffset(0);
        }
        return landingPageCriteria;
    }

    /**
     * The maximum result size limit.
     * 
     * @TODO configurable
     */
    public int getSearchResultSizeLimit() {
        return MAX_SEARCH_RESULT_SIZE;
    }

    /**
     * Get the maximum allowed characters for the search text field.
     * 
     * @TODO configurable
     */
    public int getMaxLengthSearchPhrase() {
        return MAX_SEARCH_INPUT_SIZE;
    }

    /**
     * Set the search phrase input.
     */
    public void setSearchPhrase(String phrase) {
        isNewSearchRequired = phrase != null || searchPhrase != null;
        searchPhrase = phrase;
        resultSize = 0;
    }

    public String getSearchPhrase() {
        return searchPhrase;
    }

    /**
     * Returns whether the search has been requested and should execute an
     * explicit new search request.
     */
    public boolean isNewSearchRequired() {
        return isNewSearchRequired;
    }

    /**
     * Resets the flag indicating that a new search request is necessary.
     */
    public void resetNewSearchRequired() {
        isNewSearchRequired = false;
    }

    /**
     * Returns whether the current request should return a search result.
     */
    public boolean isSearchRequested() {
        return getRequest().getAttribute(Constants.REQ_ATTR_SEARCH_REQUEST) != null;
    }

    /**
     * Sets whether the entered search phrase is valid.
     * 
     * @param isInvalidPhrase
     *            <code>true</code> if the entered search phrase was invalid,
     *            and <code>false</code> for a valid search phrase.
     */
    public void setInvalidSearchPhrase(boolean isInvalidPhrase) {
        invalidSearchPhrase = isInvalidPhrase;
    }

    /**
     * Returns whether the entered search phrase is valid or not.
     * 
     * @return isInvalidPhrase <code>true</code> if the entered search phrase
     *         was invalid, and <code>false</code> for a valid search phrase.
     */
    public boolean isInvalidSearchPhrase() {
        return invalidSearchPhrase;
    }

    public String getFilterCategoryForDisplay() {
        return (filterCategoryForDisplay == null) ? ""
                : filterCategoryForDisplay;
    }

    public void setFilterCategoryForDisplay(String filterCategoryForDisplay) {
        this.filterCategoryForDisplay = filterCategoryForDisplay;
    }

    public String getHeaderPrefixForSelectedFilter() {
        String filterTagForDisplay = getFilterTagForDisplay();
        String filterCategoryForDisplay = getFilterCategoryForDisplay();
        if (!(filterTagForDisplay.isEmpty() && filterCategoryForDisplay
                .isEmpty())) {
            return (filterTagForDisplay + filterCategoryForDisplay + " : ");
        } else {
            return "";
        }
    }

    public String getServiceListHeaderKey() {
        if (getResultSize() != 1) {
            return "marketplace.servicesList.headerMultipleServices";

        } else {
            return "marketplace.servicesList.headerSingleService";
        }
    }

    public String getServiceSearchResultHeaderKey() {
        if (getResultSize() != 1) {
            return "marketplace.searchresults.headerMultipleServices";

        } else {
            return "marketplace.searchresults.headerSingleService";
        }
    }

    public boolean isSearchAvailable() {
        boolean isRestricted = getConfig().isRestricted();

        if (!isRestricted) {
            return true;
        }
        VOUserDetails user = getUserFromSessionWithoutException();
        if (user != null) {
            String org = user.getOrganizationId();
            if (org != null) {
                return (getConfig().getAllowedOrganizations().contains(org));
            }
        }
        return true;
    }

    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }
}
