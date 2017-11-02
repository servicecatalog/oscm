/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.01.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.richfaces.event.DataScrollEvent;
import org.richfaces.component.SortOrder;

import org.oscm.ui.common.UiDelegate;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * @author weiser
 * 
 */
@ManagedBean
@SessionScoped
//TODO: Think if it really needs to be session scoped.
public class TableState implements Serializable {

    private static final long serialVersionUID = 5486103325088513168L;

    public static final String BEAN_NAME = "tableState";

    /**
     * @author weiser
     * 
     */
    static final class ActivePageMap extends HashMap<String, Integer> {

        private static final long serialVersionUID = 1536933267407630785L;

        static final String ADD_GROUPSERVICE_SUFFIX = "addGroupServiceListPager";
        static final String EDIT_GROUPSERVICE_SUFFIX = "editGroupServiceListPager";
        static final String EDIT_UNIT_USERS_SUFFIX = "usersInGroupPager";
        static final String EDIT_USER_GROUPS_SUFFIX = "userGroupsPager";
        static final String EDIT_USER_SUBSCRIPTIONS_SUFFIX = "userSubscriptions";

        @Override
        public Integer get(Object key) {
            Integer value = super.get(key);
            if (value == null) {
                value = Integer.valueOf(1);
            }
            return value;
        }

        public void resetGroupServicePage(String page) {
            for (String key : super.keySet()) {
                if (key.contains(page)) {
                    super.put(key, Integer.valueOf(1));
                    return;
                }
            }
        }
    }

    /**
     * @author weiser
     * 
     */
    static final class ColumnSortingMap extends HashMap<String, SortOrder> {

        private static final long serialVersionUID = 8199033955118983409L;

        static final String DEFAULT_COLUM_SUFFIX = "UserId";

        @Override
        public SortOrder get(Object key) {
            SortOrder value = super.get(key);
            if (value == null && key.toString().endsWith(DEFAULT_COLUM_SUFFIX)) {
                value = SortOrder.ascending;
            }
            return value;
        }
    }

    UiDelegate ui = new UiDelegate();

    private ActivePageMap activePages = new ActivePageMap();
    private ColumnSortingMap sortOrders = new ColumnSortingMap();
    private Map<String, String> filterValues = new HashMap<String, String>();

    public void actionListener(DataScrollEvent dse) {
        int page = dse.getPage();
        activePages.put(ui.getClientId(dse.getComponent()),
                Integer.valueOf(page));
    }

    public ActivePageMap getActivePages() {
        return activePages;
    }

    public void setActivePages(ActivePageMap activePages) {
        this.activePages = activePages;
    }

    public Map<String, String> getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(Map<String, String> filterValues) {
        this.filterValues = filterValues;
    }

    public ColumnSortingMap getSortOrders() {
        return sortOrders;
    }

    public void setSortOrders(ColumnSortingMap sortOrders) {
        this.sortOrders = sortOrders;
    }

    public void resetActivePages() {
        activePages.clear();
    }

    public void resetActiveAddPage() {
        activePages
                .resetGroupServicePage(ActivePageMap.ADD_GROUPSERVICE_SUFFIX);
    }

    public void resetActiveEditPage() {
        activePages
                .resetGroupServicePage(ActivePageMap.EDIT_GROUPSERVICE_SUFFIX);
    }

    public void resetActiveUnitPage() {
        activePages.resetGroupServicePage(ActivePageMap.EDIT_UNIT_USERS_SUFFIX);
    }

    public void resetActiveUserGroupsAndSubscriptionsPage() {
        activePages.resetGroupServicePage(ActivePageMap.EDIT_USER_GROUPS_SUFFIX);
        activePages.resetGroupServicePage(ActivePageMap.EDIT_USER_SUBSCRIPTIONS_SUFFIX);
    }
}
