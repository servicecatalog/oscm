/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 07.07.15 09:52
 *
 * ******************************************************************************
 */
package org.oscm.ui.dialog.mp.userGroups;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseModel;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.internal.usergroupmgmt.POUserGroup;

/**
 * This class provides a model for assigning unit to the subscription.
 *
 */
@ViewScoped
@ManagedBean(name="subscriptionUnitModel")
public class SubscriptionUnitModel extends BaseModel {

    private static final long serialVersionUID = -3975618449717535062L;
    private List<POUserGroup> units = new ArrayList<POUserGroup>();
    private String assignNoUnit;
    private long selectedUnitId;
    private String selectedUnitName;

    public List<POUserGroup> getUnits() {
        return units;
    }

    public void setUnits(List<POUserGroup> units) {
        this.units = units;
    }

    public String getAssignNoUnit() {
        if (selectedUnitId == 0L) {
            assignNoUnit = Constants.RADIO_SELECTED;
        }
        return assignNoUnit;
    }

    public void setAssignNoUnit(String assignNoUnit) {
        this.assignNoUnit = assignNoUnit;
    }

    public String getSelectedUnitName() {
        if (selectedUnitName == null || selectedUnitName.trim().isEmpty()) {
            return JSFUtils.getText("unit.notAssigned", new Object[]{""});
        }
        return selectedUnitName;
    }

    public void setSelectedUnitName(String selectedUnitName) {
        this.selectedUnitName = selectedUnitName;
    }

    public long getSelectedUnitId() {
        return selectedUnitId;
    }

    public void setSelectedUnitId(long selectedUnitId) {
        this.selectedUnitId = selectedUnitId;
    }

}
