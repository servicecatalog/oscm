/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 24.04.15 16:21
 *
 * ******************************************************************************
 */

package org.oscm.ui.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.oscm.internal.tables.Pagination;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.paginator.Filter;
import org.oscm.paginator.PaginationInt;
import org.oscm.paginator.SortOrder;
import org.oscm.paginator.Sorting;
import org.oscm.paginator.TableColumns;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.richfaces.model.Arrangeable;
import org.richfaces.model.ArrangeableState;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

/**
 * Extended data model
 */
public abstract class RichLazyDataModel<T> extends ExtendedDataModel<T>
        implements Arrangeable {

    private final boolean CACHE_ENABLED;
    private int totalCount = -1;
    private SequenceRange cachedRange;
    private Integer cachedRowCount;
    private List<T> cachedList;
    private Object rowKey;
    private ArrangeableState arrangeable;

    // Sorting and filtering stuff
    private HashMap<String, TableColumns> columnNamesMapping = new HashMap<>();
    private Map<String, org.richfaces.component.SortOrder> sortOrders = new HashMap<>();
    private Map<String, String> filterValues = new HashMap<>();
    private String sortProperty;

    public abstract List<T> getDataList(int firstRow, int numRows,
            List<FilterField> filterFields, List<SortField> sortFields, Object argument);

    protected void decorateWithLocalizedStatuses(Pagination pagination) {
        Map<SubscriptionStatus, String> localizedStatusesMap = pagination
                .getLocalizedStatusesMap();
        fillStatusesMap(localizedStatusesMap);
    }

    protected void decorateWithLocalizedStatuses(org.oscm.paginator.Pagination pagination) {
        Map<SubscriptionStatus, String> localizedStatusesMap = pagination
                .getLocalizedStatusesMap();
        fillStatusesMap(localizedStatusesMap);
    }

    private void fillStatusesMap(Map<SubscriptionStatus, String> localizedStatusesMap) {
        for (SubscriptionStatus subscriptionStatus : SubscriptionStatus
                .values()) {
            localizedStatusesMap.put(
                    subscriptionStatus,
                    JSFUtils.getText(SubscriptionStatus.class.getSimpleName()
                            + "." + subscriptionStatus.name(), null));
        }
    }

    public abstract Object getKey(T t);

    public RichLazyDataModel(boolean cacheEnabled) {
        this.CACHE_ENABLED = cacheEnabled;
    }

    @Override
    public void walk(FacesContext ctx, DataVisitor dv, Range range,
            Object argument) {

        SequenceRange sr = (SequenceRange) range;

        if (!CACHE_ENABLED
                || (cachedList == null || !equalRanges(cachedRange, sr))) {
            cachedList = getDataList(sr.getFirstRow(), sr.getRows(),
                    arrangeable.getFilterFields(), arrangeable.getSortFields(), argument);
            cachedRange = sr;
        }

        for (T t : cachedList) {
            if (getKey(t) == null) {
                /*
                 * the 2nd param is used to build the client id of the table
                 * row, i.e. mytable:234:inputname, so don't let it be null.
                 */
                throw new IllegalStateException("found null key");
            }
            dv.process(ctx, getKey(t), argument);
        }

    }

    /*
     * The rowKey is the id from getKey, presumably obtained from
     * dv.process(...).
     */
    @Override
    public void setRowKey(Object rowKey) {
        this.rowKey = rowKey;
    }

    @Override
    public Object getRowKey() {
        return rowKey;
    }

    @Override
    public boolean isRowAvailable() {
        return (getRowData() != null);
    }

    @Override
    public int getRowCount() {
        if (!CACHE_ENABLED || cachedRowCount == null) {
            cachedRowCount = Integer.valueOf(getTotalCount());
        }
        return cachedRowCount.intValue();
    }

    @Override
    public T getRowData() {
        for (T t : cachedList) {
            if (getKey(t).equals(this.getRowKey())) {
                return t;
            }
        }
        return null;
    }

    protected static boolean equalRanges(SequenceRange range1,
            SequenceRange range2) {
        if (range1 == null || range2 == null) {
            return range1 == null && range2 == null;
        } else {
            return range1.getFirstRow() == range2.getFirstRow()
                    && range1.getRows() == range2.getRows();
        }
    }

    /*
     * get/setRowIndex are used when doing multiple select in an
     * extendedDataTable, apparently. Not tested. Actually, the get method is
     * used when using iterationStatusVar="it" & #{it.index}.
     */
    @Override
    public int getRowIndex() {
        if (cachedList != null) {
            ListIterator<T> it = cachedList.listIterator();
            while (it.hasNext()) {
                T t = it.next();
                if (getKey(t).equals(this.getRowKey())) {
                    return it.previousIndex() + cachedRange.getFirstRow();
                }
            }
        }
        return -1;
    }

    @Override
    public void setRowIndex(int rowIndex) {
        int upperBound = cachedRange.getFirstRow() + cachedRange.getRows();
        if (rowIndex >= cachedRange.getFirstRow() && rowIndex < upperBound) {
            int index = rowIndex % cachedRange.getRows();
            T t = cachedList.get(index);
            setRowKey(getKey(t));
        }
    }

    @Override
    public Object getWrappedData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setWrappedData(Object data) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    public List<T> getCachedList() {
        return cachedList;
    }

    @Override
    public void arrange(FacesContext facesContext,
            ArrangeableState arrangeableState) {
        this.arrangeable = arrangeableState;
    }

    public ArrangeableState getArrangeable() {
        return arrangeable;
    }

    protected void applyFilters(List<FilterField> filterFields,
            Pagination pagination) {
        Set<Filter> filters = new HashSet<>();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        for (FilterField filterField : filterFields) {
            String propertyName = (String) filterField.getFilterExpression()
                    .getValue(facesContext.getELContext());
            Object filterValue = filterField.getFilterValue();

            if (filterValue == null || filterValue.equals("")) {
                continue;
            }
            Filter filter = new Filter(columnNamesMapping.get(propertyName),
                    filterValue.toString());
            filters.add(filter);
        }
        ResourceBundle bundle = facesContext.getApplication()
                .getResourceBundle(facesContext, Constants.BUNDLE_NAME);
        pagination.setDateFormat(bundle
                .getString(ApplicationBean.DatePatternEnum.DATE_INPUT_PATTERN
                        .getMessageKey()));
        pagination.setFilterSet(filters);
    }

    protected void applySorting(List<SortField> sortFields,
            Pagination pagination) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Sorting sorting = null;
        for (SortField sortField : sortFields) {
            String propertyName = (String) sortField.getSortBy().getValue(
                    facesContext.getELContext());
            SortOrder order;
            if (sortField.getSortOrder() == org.richfaces.component.SortOrder.ascending) {
                order = SortOrder.ASC;
            } else if (sortField.getSortOrder() == org.richfaces.component.SortOrder.descending) {
                order = SortOrder.DESC;
            } else {
                order = SortOrder.UNSORTED;
            }
            sorting = new Sorting(columnNamesMapping.get(propertyName), order);
        }
        if (sorting != null) {
            pagination.setSorting(sorting);
        }
    }
    
    protected void applyFilters(List<FilterField> filterFields,
            PaginationInt pagination) {
        Set<Filter> filters = new HashSet<>();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        for (FilterField filterField : filterFields) {
            String propertyName = (String) filterField.getFilterExpression()
                    .getValue(facesContext.getELContext());
            Object filterValue = filterField.getFilterValue();

            if (filterValue == null || filterValue.equals("")) {
                continue;
            }
            Filter filter = new Filter(columnNamesMapping.get(propertyName),
                    filterValue.toString());
            filters.add(filter);
        }
        ResourceBundle bundle = facesContext.getApplication()
                .getResourceBundle(facesContext, Constants.BUNDLE_NAME);
        pagination.setDateFormat(bundle
                .getString(ApplicationBean.DatePatternEnum.DATE_INPUT_PATTERN
                        .getMessageKey()));
        pagination.setFilterSet(filters);
    }

    protected void applySorting(List<SortField> sortFields,
            PaginationInt pagination) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Sorting sorting = null;
        for (SortField sortField : sortFields) {
            String propertyName = (String) sortField.getSortBy().getValue(
                    facesContext.getELContext());
            SortOrder order;
            if (sortField.getSortOrder() == org.richfaces.component.SortOrder.ascending) {
                order = SortOrder.ASC;
            } else if (sortField.getSortOrder() == org.richfaces.component.SortOrder.descending) {
                order = SortOrder.DESC;
            } else {
                order = SortOrder.UNSORTED;
            }
            sorting = new Sorting(columnNamesMapping.get(propertyName), order);
        }
        if (sorting != null) {
            pagination.setSorting(sorting);
        }
    }

    public void toggleSort() {
        for (Map.Entry<String, org.richfaces.component.SortOrder> entry : sortOrders
                .entrySet()) {
            org.richfaces.component.SortOrder newOrder;

            if (entry.getKey().equals(sortProperty)) {
                if (entry.getValue() == org.richfaces.component.SortOrder.ascending) {
                    newOrder = org.richfaces.component.SortOrder.descending;
                } else {
                    newOrder = org.richfaces.component.SortOrder.ascending;
                }
            } else {
                newOrder = org.richfaces.component.SortOrder.unsorted;
            }

            entry.setValue(newOrder);
        }
    }

    public HashMap<String, TableColumns> getColumnNamesMapping() {
        return columnNamesMapping;
    }

    public Map<String, String> getFilterValues() {
        return filterValues;
    }

    public Map<String, org.richfaces.component.SortOrder> getSortOrders() {
        return sortOrders;
    }

    public void setSortOrders(
            Map<String, org.richfaces.component.SortOrder> sortOrders) {
        this.sortOrders = sortOrders;
    }

    public String getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(String sortProperty) {
        this.sortProperty = sortProperty;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    };
}
