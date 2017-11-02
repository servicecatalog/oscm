/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2014-8-6                                                      
 *
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import org.oscm.string.Strings;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.marketplace.ServicePagingBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.model.User;
import org.oscm.internal.intf.SearchServiceInternal;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.usergroupmgmt.POService;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceListResult;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author yuyin
 */
public abstract class UserGroupBaseCtrl {

    protected static final String GROUP_USER_DEASSIGN_MSG_KEY = "group.users.deassign.message";
    protected static final String REQUEST_PARAM_USER_TO_DEASSIGN = "userToDeassign";

    private UiDelegate ui;

    /**
     * EJB injected through setters.
     */
    private SearchServiceInternal searchServiceInternal;

    public UserGroupBaseCtrl() {
        ui = new UiDelegate();
    }

    abstract ManageGroupModel getManageGroupModel();

    List<POService> initServiceList() throws ObjectNotFoundException {
        VOServiceListResult voServiceListResult = getSearchServiceInternal()
                .getAccesibleServices(
                        BaseBean.getMarketplaceIdStatic(),
                        JSFUtils.getViewLocale().getLanguage(),
                        getInitListCriteria(),
                        PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
        return assemblePOService(voServiceListResult.getServices());
    }

    /**
     * @return criteria search parameter for service list
     */
    ListCriteria getInitListCriteria() {
        ServicePagingBean pagingBean = new ServicePagingBean();
        ListCriteria crit = pagingBean.getListCriteria();
        crit.setLimit(-1);
        return crit;
    }

    List<POService> assemblePOService(List<VOService> voServices) {
        String text = ui.getText("service.name.undefined");
        List<POService> pos = new ArrayList<POService>();
        for (VOService prod : voServices) {
            POService po = new POService(prod.getKey(), prod.getVersion());
            po.setProductId(prod.getServiceId());
            po.setProviderName(prod.getSellerName());
            if (Strings.isEmpty(prod.getNameToDisplay())
                    || checkServiceName(prod.getNameToDisplay())) {
                po.setServiceName(text);
            } else {
                po.setServiceName(prod.getNameToDisplay());
            }
            pos.add(po);
        }
        return pos;
    }

    boolean checkServiceName(String serviceName) {
        char[] chars = serviceName.toCharArray();
        for (char c : chars) {
            if (Character.isISOControl(c) && c != '\t' && c != ' ') {
                return true;
            }
        }
        return false;
    }

    List<User> getSelectedUsersFromList(List<User> users) {
        List<User> selectedUsers = new ArrayList<>();
        for (User user : users) {
            if (user.isSelected()) {
                selectedUsers.add(user);
            }
        }
        return selectedUsers;
    }

    protected void sortServiceRows(List<ServiceRow> rows) {
        Collections.sort(rows, serviceRowComparator);
    }

    /**
     * Comparator for service row names.
     */
    private final Comparator<ServiceRow> serviceRowComparator = new Comparator<ServiceRow>() {

        @Override
        public int compare(ServiceRow o1, ServiceRow o2) {
            String name1 = o1.getService().getServiceName();
            String name2 = o2.getService().getServiceName();

            if ((o1.isSelected() && o2.isSelected())
                    || (!o1.isSelected() && !o2.isSelected())) {
                int order = name1.compareToIgnoreCase(name2);
                if (order == 0) {
                    return name1.compareTo(name2);
                }
                return order;
            }
            if (o1.isSelected()) {
                return -1;
            }
            return 1;
        }
    };

    public abstract void setManageGroupModel(ManageGroupModel model);

    @EJB
    public void setSearchServiceInternal(
            SearchServiceInternal searchServiceInternal) {
        this.searchServiceInternal = searchServiceInternal;
    }

    public UiDelegate getUi() {
        return ui;
    }

    public void setUi(UiDelegate ui) {
        this.ui = ui;
    }

    public SearchServiceInternal getSearchServiceInternal() {
        if (searchServiceInternal == null) {
            searchServiceInternal = ui.findService(SearchServiceInternal.class);
        }
        return searchServiceInternal;
    }
}
