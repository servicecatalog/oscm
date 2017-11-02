/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 08.07.15 09:52
 *
 * ******************************************************************************
 */
package org.oscm.ui.dialog.mp.userGroups;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.oscm.ui.common.Constants;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.VOUserDetails;

/**
 * This class provides a controller for assigning unit to the subscription.
 *
 */
@ViewScoped
@ManagedBean(name = "subscriptionUnitCtrl")
public class SubscriptionUnitCtrl implements Serializable {

    private static final long serialVersionUID = -5178199523298178136L;

    @Inject
    private SubscriptionUnitModel model;

    @EJB
    private UserGroupService unitService;

    private UiDelegate ui = new UiDelegate();

    /**
     * Method is used to set units list presented on the select unit popup.
     */
    public void initializeUnitListForCreateSubscription() {
        VOUserDetails voUserDetails = ui.getUserFromSessionWithoutException();
        List<POUserGroup> units = new ArrayList<POUserGroup>();
        if (!voUserDetails.hasAdminRole() && !voUserDetails.hasUnitAdminRole()) {
            return;
        }
        if (voUserDetails.hasUnitAdminRole() && !voUserDetails.hasAdminRole()) {
            units = getUnitService()
                    .getUserGroupsForUserWithRoleWithoutDefault(voUserDetails.getKey(),
                            UnitRoleType.ADMINISTRATOR.getKey());
            model.setUnits(units);
            if (!units.isEmpty() && model.getSelectedUnitId() == 0L) {
                assignToSelectedUnit(units.get(0).getKey());
                model.setAssignNoUnit(Constants.RADIO_UNSELECTED);
            }
        }
        if (voUserDetails.hasAdminRole()) {
            units = getUnitService().getGroupListForOrganization();
        }
        if (isUnitSelected()) {
            markUnitRadioAsSelected(units);
        }
        model.setUnits(units);
    }
    
    public void initializeUnitListForModifySubscription() {
        VOUserDetails voUserDetails = ui.getUserFromSessionWithoutException();
        List<POUserGroup> units = new ArrayList<POUserGroup>();
        if (!voUserDetails.hasAdminRole() && !voUserDetails.hasUnitAdminRole()) {
            return;
        }
        if (voUserDetails.hasAdminRole()) {
            units = getUnitService().getGroupListForOrganization();
        } else if (voUserDetails.hasUnitAdminRole()) {
            units = getUnitService()
                    .getUserGroupsForUserWithRoleWithoutDefault(voUserDetails.getKey(),
                            UnitRoleType.ADMINISTRATOR.getKey());
        }
        model.setUnits(units);
        if (isUnitSelected()) {
            markUnitRadioAsSelected(units);
        }
    }

    private void markUnitRadioAsSelected(List<POUserGroup> units) {
        for (POUserGroup unit : units) {
            if (unit.getKey() == model.getSelectedUnitId()) {
                unit.setUnitChecked(Constants.RADIO_SELECTED);
                unit.setUnitSelected(true);
                return;
            }
        }
    }

    /**
     * Method sets unit details to model.
     * @param unitId
     * @param unitName
     */
    public void setSelectedUnitToModel(long unitId, String unitName) {
        model.setSelectedUnitId(unitId);
        model.setSelectedUnitName(unitName);
        if (!isUnitSelected()) {
            model.setAssignNoUnit(Constants.RADIO_SELECTED);
        }
    }

    /**
     * This method is used to handle the value change event on radio buttons 
     * when using the 'Select unit for a subscription' popup.
     * @param event
     */
    public void changeSelectedUnit(ValueChangeEvent event) {
        UIComponent uiComponent = event.getComponent();
        if (!(uiComponent instanceof HtmlSelectOneRadio)) {
            return;
        }
        HtmlSelectOneRadio radioBtn = (HtmlSelectOneRadio) uiComponent;
        List<UIComponent> uiComponents = radioBtn.getChildren();
        for (UIComponent component : uiComponents) {
            if (!(component instanceof HtmlInputHidden)) {
                continue;
            }
            HtmlInputHidden hiddenInput = (HtmlInputHidden) component;
            long unitId = ((Long) hiddenInput.getValue()).longValue();
            if (unitId == 0L) { //radio 'The subscription is not assigned...' is selected
                deassignUnit();
                return;
            }
            if (Boolean.valueOf((String) event.getNewValue()).booleanValue()) {
                assignToSelectedUnit(unitId);
                return;
            }
            for (POUserGroup unit : model.getUnits()) {
                if (unit.getKey() == unitId) {
                    unit.setUnitSelected(false);
                    unit.setUnitChecked(null);
                }
            }

        }
    }
    

    private void deassignUnit() {
        for (POUserGroup unit : model.getUnits()) {
            unit.setUnitSelected(false);
            unit.setUnitChecked(null);
        }
        model.setSelectedUnitId(0);
        model.setSelectedUnitName("");
    }

    private void assignToSelectedUnit(long unitId) {
        for (POUserGroup unit : model.getUnits()) {
            if (unit.getKey() == unitId) {
                unit.setUnitSelected(true);
                model.setSelectedUnitName(unit.getGroupName());
                model.setSelectedUnitId(unitId);
            } else {
                unit.setUnitSelected(false);
            }
        }
    }

    public boolean isUnitSelected() {
        return model.getSelectedUnitId() != 0L;
    }

    public SubscriptionUnitModel getModel() {
        return model;
    }

    public void setModel(SubscriptionUnitModel model) {
        this.model = model;
    }

    public UserGroupService getUnitService() {
        return unitService;
    }

    public void setUnitService(UserGroupService unitService) {
        this.unitService = unitService;
    }

}
