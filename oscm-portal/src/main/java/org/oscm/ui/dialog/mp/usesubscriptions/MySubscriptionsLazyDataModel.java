/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 29.04.15 08:11                                                      
 *
 *******************************************************************************/

package org.oscm.ui.dialog.mp.usesubscriptions;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.subscriptions.OperationModel;
import org.oscm.internal.subscriptions.POSubscription;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.paginator.PaginationFullTextFilter;
import org.oscm.paginator.TableColumns;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;
import org.oscm.ui.model.RichLazyDataModel;
import org.oscm.validator.ADMValidator;

import org.richfaces.component.SortOrder;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

// Session, because we need to have sort order and filtering stored in session.
@SessionScoped
@ManagedBean(name = "mySubscriptionsLazyDataModel")
public class MySubscriptionsLazyDataModel extends RichLazyDataModel<POSubscription> {

    private static final String PURCHASE_ORDER_NUMBER = "purchaseOrderNumber";
    private static final String SERVICE_NAME = "serviceName";
    private static final String SUBSCRIPTION_ID = "subscriptionId";
    private static final String ACTIVATION = "activationDate";
    private static final String STATUS = "status";
    private String subscriptionIdForOperation;
    private POSubscription selectedSubscription;
    private String selectedSubscriptionId;
    private String fullTextSearchFilterValue;


    @ManagedProperty(value = "#{appBean}")
    private ApplicationBean applicationBean;

    @EJB
    private SubscriptionsService subscriptionsService;
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(MySubscriptionsLazyDataModel.class);
    private List<POSubscription> resultList;

    public MySubscriptionsLazyDataModel() {
        super(false);
    }

    @PostConstruct
    public void init() {
        getColumnNamesMapping().put(PURCHASE_ORDER_NUMBER, TableColumns.PURCHASE_ORDER_NUMBER);
        getColumnNamesMapping().put(SERVICE_NAME, TableColumns.SERVICE_NAME);
        getColumnNamesMapping().put(SUBSCRIPTION_ID, TableColumns.SUBSCRIPTION_ID);
        getColumnNamesMapping().put(ACTIVATION, TableColumns.ACTIVATION_TIME);
        getColumnNamesMapping().put(STATUS, TableColumns.STATUS);

        getSortOrders().put(PURCHASE_ORDER_NUMBER, SortOrder.unsorted);
        getSortOrders().put(SERVICE_NAME, SortOrder.unsorted);
        getSortOrders().put(SUBSCRIPTION_ID, SortOrder.unsorted);
        getSortOrders().put(ACTIVATION, SortOrder.descending);
        getSortOrders().put(STATUS, SortOrder.unsorted);
    }

    @Override
    public List<POSubscription> getDataList(int firstRow, int numRows,
            List<FilterField> filterFields, List<SortField> sortFields,
            Object argument) {
        PaginationFullTextFilter pagination = new PaginationFullTextFilter(firstRow, numRows);
        applyFilters(getArrangeable().getFilterFields(), pagination);
        applySorting(getArrangeable().getSortFields(), pagination);
        decorateWithLocalizedStatuses(pagination);
        resultList = Collections.emptyList();

        fullTextSearchFilterValue = fullTextSearchFilterValue == null ? null : fullTextSearchFilterValue.trim();
        pagination.setFullTextFilterValue(fullTextSearchFilterValue);
        try {
            Response response = subscriptionsService.getMySubscriptionsWithFiltering(pagination);
            resultList = response.getResultList(POSubscription.class);
            for (POSubscription subscription : resultList) {
                subscription.setAccessUrl(getAccessUrl(subscription));
                subscription.setTarget(isOpenNewTab(subscription) ? "_blank" : "");
            }
            refreshSelectedSubscription();
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR);
        }
        return resultList;
    }

    private void refreshSelectedSubscription() {
        if (selectedSubscription != null) {
            if (resultList.contains(selectedSubscription)) {
                OperationModel selectedOperation = selectedSubscription.getSelectedOperation();
                selectedSubscription = subscriptionsService.getMySubscriptionDetails(selectedSubscription.getKey());
                if (selectedSubscription == null) {
                    selectedSubscriptionId = null;
                } else {
                    selectedSubscription.setAccessUrl(getAccessUrl(selectedSubscription));
                    selectedSubscription.setTarget(isOpenNewTab(selectedSubscription) ? "_blank" : "");
                    operationChanged(selectedOperation);
                }
            } else {
                selectedSubscription = null;
                selectedSubscriptionId = null;
            }
        }
    }

    private void operationChanged(OperationModel selectedOperation) {
        if (selectedOperation == null) {
            selectedSubscription.setSelectedOperation(null);
            selectedSubscription.setSelectedOperationId(null);
            selectedSubscription.setExecuteDisabled(true);
        } else {
            String operationId = selectedOperation.getOperation().getOperationId();
            selectedSubscription.setSelectedOperation(selectedOperation);
            selectedSubscription.setSelectedOperationId(operationId);
            selectedSubscription.setExecuteDisabled(false);
        }
    }

    @Override
    public Object getKey(POSubscription entry) {
        return entry.getSubscriptionId();
    }

    @Override
    public int getTotalCount() {
        try {
            PaginationFullTextFilter pagination = new PaginationFullTextFilter();
            applyFilters(getArrangeable().getFilterFields(), pagination);
            decorateWithLocalizedStatuses(pagination);
            fullTextSearchFilterValue = fullTextSearchFilterValue == null ? null : fullTextSearchFilterValue.trim();
            pagination.setFullTextFilterValue(fullTextSearchFilterValue);
            setTotalCount(subscriptionsService
                    .getMySubscriptionsSizeWithFiltering(pagination).intValue());
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR);
        }
        int totalCount = super.getTotalCount();
        if (totalCount == 0) {
            selectedSubscription = null;
            selectedSubscriptionId = null;
        }
        return totalCount;
    }

    public void setSubscriptionsService(SubscriptionsService subscriptionsService) {
        this.subscriptionsService = subscriptionsService;
    }

    public List<POSubscription> getMySubscriptions() {
        return getCachedList();
    }

    public String getPURCHASE_ORDER_NUMBER() {
        return PURCHASE_ORDER_NUMBER;
    }

    public String getSERVICE_NAME() {
        return SERVICE_NAME;
    }

    public String getSUBSCRIPTION_ID() {
        return SUBSCRIPTION_ID;
    }

    public String getACTIVATION() {
        return ACTIVATION;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public String getSubscriptionIdForOperation() {
        return subscriptionIdForOperation;
    }

    public void setSubscriptionIdForOperation(String subscriptionIdForOperation) {
        this.subscriptionIdForOperation = subscriptionIdForOperation;
    }

    public POSubscription getSelectedSubscription() {
        return selectedSubscription;
    }

    public void setSelectedSubscription(POSubscription selectedSubscription) {
        this.selectedSubscription = selectedSubscription;
    }

    private boolean isOpenNewTab(POSubscription subscription) {
        if (subscription.getAccessUrl() != null) {
            if (!isInternalURL(subscription.getServiceBaseURL())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given URL is external URL. External URL's are all URL which
     * do not contain the BES-Base-URL
     *
     * @param serviceAccessURL
     * @return true if URL doesn't contain the BES-Base-URL
     */
    boolean isInternalURL(String serviceAccessURL) {
        if (serviceAccessURL == null || serviceAccessURL.length() == 0) {
            return false;
        }

        return isMatch(serviceAccessURL, applicationBean
                .getServerBaseUrl())
                || isMatch(serviceAccessURL, applicationBean
                .getServerBaseUrlHttps());
    }

    /**
     * Gets the URL to access a subscribed service.
     *
     * @param subscription
     * @return
     */
    protected String getAccessUrl(POSubscription subscription) {
        if (subscription.getServiceAccessType() == ServiceAccessType.USER
                || subscription.getServiceAccessType() == ServiceAccessType.DIRECT) {
            if (subscription.getServiceBaseURL() == null) {
                return "";
            }

            return subscription.getServiceBaseURL();
        } else {
            String serviceBaseUrl = subscription.getServiceBaseURL();
            String serverBaseUrl;
            if (ADMValidator.isHttpsScheme(serviceBaseUrl)) {
                serverBaseUrl = applicationBean.getServerBaseUrlHttps();
            } else {
                serverBaseUrl = applicationBean.getServerBaseUrl();
            }

            return ADMStringUtils.removeEndingSlash(serverBaseUrl)
                    + Constants.SERVICE_BASE_URI + "/"
                    + subscription.getHexKey() + "/";
        }
    }

    private static boolean isMatch(String s, String pattern) {
        Pattern patt = Pattern.compile("\\b" + pattern + ".*");
        Matcher matcher = patt.matcher(s);
        return matcher.matches();
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

	public String getSelectedSubscriptionId() {
		return selectedSubscriptionId;
	}

	public void setSelectedSubscriptionId(String selectedSubscriptionId) {
		this.selectedSubscriptionId = selectedSubscriptionId;
	}

    public String getFullTextSearchFilterValue() {
        return fullTextSearchFilterValue;
    }

    public void setFullTextSearchFilterValue(String fullTextSearchFilterValue) {
        this.fullTextSearchFilterValue = fullTextSearchFilterValue;
    }
}
