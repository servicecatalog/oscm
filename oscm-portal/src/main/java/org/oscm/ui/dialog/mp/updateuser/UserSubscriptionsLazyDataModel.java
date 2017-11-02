/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2015                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.dialog.mp.updateuser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.usermanagement.POServiceRole;
import org.oscm.internal.usermanagement.POSubscription;
import org.oscm.internal.usermanagement.UserService;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.paginator.PaginationSubForUser;
import org.oscm.paginator.TableColumns;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.dialog.mp.createuser.Subscription;
import org.oscm.ui.model.RichLazyDataModel;
import org.richfaces.component.SortOrder;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

@ViewScoped
@ManagedBean(name = "userSubscriptionsLazyDataModel")
public class UserSubscriptionsLazyDataModel extends
        RichLazyDataModel<Subscription> {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UserSubscriptionsLazyDataModel.class);

    private static final String SUBSCRIPTION_ID = "id";
    private static final String ROLE_IN_SUB = "selectedRole";

    @EJB
    private UserService userService;

    @ManagedProperty(value = "#{updateUserModel}")
    private UpdateUserModel model;

    public UserSubscriptionsLazyDataModel() {
        super(false);
    }

    @PostConstruct
    public void init() {
        getColumnNamesMapping().put(SUBSCRIPTION_ID,
                TableColumns.SUBSCRIPTION_ID);
        getColumnNamesMapping().put(ROLE_IN_SUB, TableColumns.ROLE_IN_SUB);

        getSortOrders().put(SUBSCRIPTION_ID, SortOrder.unsorted);
        getSortOrders().put(ROLE_IN_SUB, SortOrder.unsorted);
    }

    @Override
    public List<Subscription> getDataList(int firstRow, int numRows,
            List<FilterField> filterFields, List<SortField> sortFields,
            Object refreshDataModel) {

        PaginationSubForUser pagination = new PaginationSubForUser(firstRow,
                numRows);

        applyFilters(getArrangeable().getFilterFields(), pagination);
        applySorting(getArrangeable().getSortFields(), pagination);
        decorateWithChangedData(pagination);

        // TODO MULTITENANT
        List<Subscription> resultList = Collections.emptyList();
        String userId = model.getUser().getUserId();

        if (refreshDataModel != null) {
            try {
                List<POSubscription> userSubscriptions = userService
                        .getUserAssignableSubscriptions(pagination, userId);

                resultList = toSubscriptionList(userSubscriptions);

                if (!resultList.isEmpty()) {

                    for (Subscription sub : resultList) {
                        model.getAllSubscriptions().put(sub.getId(), sub);
                    }
                }

            } catch (SaaSApplicationException e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR);
            }
            model.setSubscriptions(resultList);

        } else {
            resultList = model.getSubscriptions();
        }

        return resultList;
    }

    List<Subscription> toSubscriptionList(List<POSubscription> poSubs) {

        ArrayList<Subscription> list = new ArrayList<>();

        for (POSubscription poSub : poSubs) {

            String subscriptionId = poSub.getId();

            Subscription sub = new Subscription();
            sub.setId(subscriptionId);
            List<POServiceRole> roles = poSub.getRoles();
            sub.setRolesRendered(!roles.isEmpty());
            List<SelectItem> items = new ArrayList<>();

            for (POServiceRole r : roles) {
                SelectItem si = new SelectItem(String.format("%s:%s",
                        Long.valueOf(r.getKey()), r.getId()), r.getName());
                if (r.getName().equalsIgnoreCase(UnitRoleType.USER.name())) {
                    items.add(0, si);
                } else {
                    items.add(si);
                }
            }

            sub.setRoles(items);

            if (model.getSelectedSubsIds().containsKey(subscriptionId)) {
                Boolean selected = model.getSelectedSubsIds().get(
                        subscriptionId);
                sub.setSelected(selected);
            } else {
                sub.setSelected(poSub.isAssigned());
            }

            String selectedRole = null;

            if (poSub.getUsageLicense() != null
                    && poSub.getUsageLicense().getPoServieRole() != null) {

                POServiceRole poServiceRole = poSub.getUsageLicense()
                        .getPoServieRole();

                long roleKey = poServiceRole.getKey();
                String roleId = poServiceRole.getId();

                selectedRole = String.format("%s:%s", Long.valueOf(roleKey),
                        roleId);

                sub.setLicKey(poSub.getUsageLicense().getKey());
                sub.setLicVersion(poSub.getUsageLicense().getVersion());
            }

            if (model.getChangedRoles().containsKey(subscriptionId)) {
                String displayedRole = model.getChangedRoles().get(
                        subscriptionId);
                for (SelectItem item : items) {
                    if (item.getLabel().equals(displayedRole)) {
                        selectedRole = (String) item.getValue();
                    }
                }
            }

            sub.setSelectedRole(selectedRole);
            list.add(sub);
        }

        return list;
    }

    @Override
    public Object getKey(Subscription t) {

        return t.getId();
    }

    @Override
    public int getTotalCount() {

        String userId = model.getUser().getUserId();

        try {
            PaginationSubForUser pagination = new PaginationSubForUser();
            applyFilters(getArrangeable().getFilterFields(), pagination);
            decorateWithChangedData(pagination);

            Long totalCount = userService.getUserAssignableSubscriptionsNumber(
                    pagination, userId, model.getTenantId());

            setTotalCount(totalCount.intValue());

            if (totalCount.intValue() == 0) {
                List<Subscription> emptyList = Collections.emptyList();
                model.setSubscriptions(emptyList);
            }

        } catch (SaaSApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return super.getTotalCount();
    }

    private void decorateWithChangedData(PaginationSubForUser pagination) {
        pagination.setChangedRoles(model.getChangedRoles());
        pagination.setSelectedUsersIds(model.getSelectedSubsIds());
    }

    public String getSUBSCRIPTION_ID() {
        return SUBSCRIPTION_ID;
    }

    public String getROLE_IN_SUB() {
        return ROLE_IN_SUB;
    }

    public UpdateUserModel getModel() {
        return model;
    }

    public void setModel(UpdateUserModel model) {
        this.model = model;
    }

}
