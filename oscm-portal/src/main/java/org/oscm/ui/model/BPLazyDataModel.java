/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 28.04.15 07:47
 *
 * ******************************************************************************
 */

package org.oscm.ui.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.oscm.paginator.Pagination;
import org.richfaces.component.SortOrder;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.paginator.TableColumns;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.subscriptions.POSubscriptionAndCustomer;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOSubscriptionDetails;

// Session, because we need to have sort order and filtering stored in session.
@SessionScoped
@ManagedBean
public class BPLazyDataModel extends RichLazyDataModel<POSubscriptionAndCustomer> {

	private static final String CUSTOMER_NAME = "customerName";
	private static final String CUSTOMER_ID = "customerId";
	private static final String SUBSCRIPTION_ID = "subscriptionId";
	private static final String ACTIVATION_TIME = "activationTimeInMillis";
	private static final String SERVICE_NAME = "serviceName";


    private POSubscriptionAndCustomer selectedSubscriptionAndCustomer;
    private VOSubscriptionDetails selectedSubscription;
    private String subscriptionId;
    private String customerId;
    private List<UdaRow> subscriptionUdaRows;

    @EJB
    private SubscriptionsService subscriptionsService;
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(BPLazyDataModel.class);

    public BPLazyDataModel() {
        super(false);
    }

    @PostConstruct
    public void init() {
    	Map<String, TableColumns> columnNamesMapping = getColumnNamesMapping();
    	columnNamesMapping.put(CUSTOMER_NAME, TableColumns.CUSTOMER_NAME);
    	columnNamesMapping.put(CUSTOMER_ID, TableColumns.CUSTOMER_ID);
    	columnNamesMapping.put(SUBSCRIPTION_ID, TableColumns.SUBSCRIPTION_ID);
    	columnNamesMapping.put(ACTIVATION_TIME, TableColumns.ACTIVATION_TIME);
    	columnNamesMapping.put(SERVICE_NAME, TableColumns.SERVICE_NAME);

    	getSortOrders().put(CUSTOMER_NAME, SortOrder.unsorted);
        getSortOrders().put(CUSTOMER_ID, SortOrder.unsorted);
        getSortOrders().put(SUBSCRIPTION_ID, SortOrder.unsorted);
        getSortOrders().put(ACTIVATION_TIME, SortOrder.descending);
        getSortOrders().put(SERVICE_NAME, SortOrder.unsorted);

    }

    @Override
    public List<POSubscriptionAndCustomer> getDataList(int firstRow,
            int numRows, List<FilterField> filterFields,
            List<SortField> sortFields, Object argument) {
        Pagination pagination = new Pagination(firstRow, numRows);
        applyFilters(getArrangeable().getFilterFields(), pagination);
        applySorting(getArrangeable().getSortFields(), pagination);
        List<POSubscriptionAndCustomer> resultList = Collections.emptyList();
        try {
            Response response = subscriptionsService.getSubscriptionsAndCustomersForManagers(pagination);
            resultList = response.getResultList(POSubscriptionAndCustomer.class);
        } catch (OrganizationAuthoritiesException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR);
        }
        return resultList;
    }

    @Override
    public Object getKey(POSubscriptionAndCustomer entry) {
        return Long.valueOf(entry.getTkey());
    }

    @Override
    public int getTotalCount() {
        try {
            Pagination pagination = new Pagination();
            applyFilters(getArrangeable().getFilterFields(), pagination);
            setTotalCount(subscriptionsService
                    .getSubscriptionsAndCustomersForManagersSize(pagination).intValue());
        } catch (OrganizationAuthoritiesException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR);
        }
        return super.getTotalCount();
    }

    public void setSubscriptionsService(SubscriptionsService subscriptionsService) {
        this.subscriptionsService = subscriptionsService;
    }

    public List<POSubscriptionAndCustomer> getSubscriptions() {
        return getCachedList();
    }

	public String getCUSTOMER_NAME() {
		return CUSTOMER_NAME;
	}

	public String getCUSTOMER_ID() {
		return CUSTOMER_ID;
	}

	public String getSUBSCRIPTION_ID() {
		return SUBSCRIPTION_ID;
	}

	public String getACTIVATION_TIME() {
		return ACTIVATION_TIME;
	}

	public String getSERVICE_ID() {
		return SERVICE_NAME;
	}

    public int getSubscriptionsListSize() {
        List<POSubscriptionAndCustomer> cachedList = getCachedList();
        if (cachedList != null) {
            return cachedList.size();
        }
        return 0;
    }

    public POSubscriptionAndCustomer getSelectedSubscriptionAndCustomer() {
        return selectedSubscriptionAndCustomer;
    }

    public void setSelectedSubscriptionAndCustomer(POSubscriptionAndCustomer selectedSubscriptionAndCustomer) {
        this.selectedSubscriptionAndCustomer = selectedSubscriptionAndCustomer;
    }

    public VOSubscriptionDetails getSelectedSubscription() {
        return selectedSubscription;
    }

    public void setSelectedSubscription(VOSubscriptionDetails selectedSubscription) {
        this.selectedSubscription = selectedSubscription;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getCurrentRowIndex() {
        int retVal = -1;
        if (selectedSubscriptionAndCustomer != null) {
            List<POSubscriptionAndCustomer> cachedList = getCachedList();
            for (int i = 0, cachedListSize = cachedList.size(); i < cachedListSize; i++) {
                POSubscriptionAndCustomer poSubscriptionAndCustomer = cachedList.get(i);
                String selectedSubAndCustSubId = selectedSubscriptionAndCustomer.getSubscriptionId();
                String poSubAndCustSubId = poSubscriptionAndCustomer.getSubscriptionId();
                String selectedCustId = selectedSubscriptionAndCustomer.getCustomerId();
                String poCustId = poSubscriptionAndCustomer.getCustomerId();
                if (selectedSubAndCustSubId.equals(poSubAndCustSubId) && selectedCustId.equals(poCustId)) {
                    retVal = i;
                    break;
                }
            }
        }
        return retVal;
    }

    public List<UdaRow> getSubscriptionUdaRows() {
        return subscriptionUdaRows;
    }

    public void setSubscriptionUdaRows(List<UdaRow> subscriptionUdaRows) {
        this.subscriptionUdaRows = subscriptionUdaRows;
    }
}
