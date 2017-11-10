/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 28.04.15 10:00
 *
 * ******************************************************************************
 */

package org.oscm.ui.dialog.mp.subscriptions;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.oscm.paginator.Pagination;
import org.oscm.paginator.PaginationFullTextFilter;
import org.richfaces.component.SortOrder;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.paginator.TableColumns;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.model.RichLazyDataModel;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.subscriptions.POSubscriptionForList;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

// Session, because we need to have sort order and filtering stored in session.
@SessionScoped
@ManagedBean(name = "subListsLazyModel")
public class SubscriptionListsLazyDataModel extends RichLazyDataModel<POSubscriptionForList> {

    private static final String PURCHASE_ORDER_NUMBER = "purchaseOrderNumber";
    private static final String SERVICE_NAME = "serviceName";
    private static final String SUBSCRIPTION_ID = "subscriptionId";
    private static final String UNIT = "unit";
    private static final String ACTIVATION = "activationDate";
    private static final String STATUS = "status";
    
    private POSubscriptionForList selectedSubscription;
    private String selectedSubscriptionId;
    private long selectedSubscriptionKey;
    private String fullTextSearchFilterValue;

    @EJB
    private SubscriptionsService subscriptionsService;
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SubscriptionListsLazyDataModel.class);
    private Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
            SubscriptionStatus.EXPIRED, SubscriptionStatus.PENDING,
            SubscriptionStatus.SUSPENDED, SubscriptionStatus.PENDING_UPD,
            SubscriptionStatus.SUSPENDED_UPD);

    public SubscriptionListsLazyDataModel() {
        super(false);
    }

    @PostConstruct
    public void init() {
        HashMap<String, TableColumns> columnNamesMapping = getColumnNamesMapping();
        columnNamesMapping.put(PURCHASE_ORDER_NUMBER, TableColumns.PURCHASE_ORDER_NUMBER);
        columnNamesMapping.put(SERVICE_NAME, TableColumns.SERVICE_NAME);
        columnNamesMapping.put(SUBSCRIPTION_ID, TableColumns.SUBSCRIPTION_ID);
        columnNamesMapping.put(UNIT, TableColumns.UNIT);
        columnNamesMapping.put(ACTIVATION, TableColumns.ACTIVATION_TIME);
        columnNamesMapping.put(STATUS, TableColumns.STATUS);

        getSortOrders().put(PURCHASE_ORDER_NUMBER, SortOrder.unsorted);
        getSortOrders().put(SERVICE_NAME, SortOrder.unsorted);
        getSortOrders().put(SUBSCRIPTION_ID, SortOrder.unsorted);
        getSortOrders().put(UNIT, SortOrder.unsorted);
        getSortOrders().put(ACTIVATION, SortOrder.descending);
        getSortOrders().put(STATUS, SortOrder.unsorted);
    }

    @Override
    public List<POSubscriptionForList> getDataList(int firstRow, int numRows, List<FilterField> filterFields, List<SortField> sortFields, Object refreshDataModel) {
        PaginationFullTextFilter pagination = new PaginationFullTextFilter(firstRow, numRows);
        applyFilters(getArrangeable().getFilterFields(), pagination);
        applySorting(getArrangeable().getSortFields(), pagination);
        decorateWithLocalizedStatuses(pagination);
        List<POSubscriptionForList> resultList = Collections.emptyList();
        fullTextSearchFilterValue = fullTextSearchFilterValue == null ? null : fullTextSearchFilterValue.trim();
        pagination.setFullTextFilterValue(fullTextSearchFilterValue);
        try {
            Response response = subscriptionsService.getSubscriptionsForOrgWithFiltering(states, pagination);
            resultList = response.getResultList(POSubscriptionForList.class);
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR);
        }
        return resultList;
    }

    @Override
    public Object getKey(POSubscriptionForList entry) {
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
            setTotalCount(subscriptionsService.getSubscriptionsForOrgSizeWithFiltering(
                    states, pagination).intValue());
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR);
        }
        return super.getTotalCount();
    }

    public void setSubscriptionsService(SubscriptionsService subscriptionsService) {
        this.subscriptionsService = subscriptionsService;
    }

    public List<POSubscriptionForList> getSubscriptions() {
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

    public String getUNIT() {
        return UNIT;
    }

    public String getACTIVATION() {
        return ACTIVATION;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public POSubscriptionForList getSelectedSubscription() {
        return selectedSubscription;
    }

    public void setSelectedSubscription(
            POSubscriptionForList selectedSubscription) {
        this.selectedSubscription = selectedSubscription;
    }

    public String getSelectedSubscriptionId() {
        return selectedSubscriptionId;
    }

    public void setSelectedSubscriptionId(String selectedSubscriptionId) {
        this.selectedSubscriptionId = selectedSubscriptionId;
    }
    
    public long getSelectedSubscriptionKey() {
        return selectedSubscriptionKey;
    }
    
    public void setSelectedSubscriptionKey(long selectedSubscriptionKey) {
        this.selectedSubscriptionKey = selectedSubscriptionKey;
    }

    public String getFullTextSearchFilterValue() {
        return fullTextSearchFilterValue;
    }

    public void setFullTextSearchFilterValue(String fullTextSearchFilterValue) {
        this.fullTextSearchFilterValue = fullTextSearchFilterValue;
    }
}
