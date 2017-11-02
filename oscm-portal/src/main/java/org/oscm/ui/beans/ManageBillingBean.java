/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-11-29                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.ui.common.SelectItemBuilder;
import org.oscm.internal.billingdataexport.POCutOffDay;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOOrganization;

/**
 * @author Yang Zou
 * 
 */
@ViewScoped
@ManagedBean(name="manageBillingBean")
public class ManageBillingBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -509974590674044659L;

    private static final int MIN_RANGE = 1;
    private static final int MAX_RANGE = 28;

    private POCutOffDay cutOffDay;
    private VOOrganization voOrganization;

    /**
     * initializer method called by <adm:initialize />
     * 
     * @return empty string (due to value jsf binding )
     * 
     *         workaround: to be refactored under jsf 2.0
     * 
     */
    public String getInitializeCutoffDay() {
        int currentCutOffDay = getAccountServiceManagement()
                .getCutOffDayOfOrganization();
        cutOffDay = new POCutOffDay(currentCutOffDay);
        voOrganization = getAccountServiceManagement().getOrganizationData();
        return "";
    }

    public List<SelectItem> getDayInMonthRange() {

        List<Integer> days = new ArrayList<Integer>();
        for (int i = MIN_RANGE; i <= MAX_RANGE; i++) {
            days.add(new Integer(i));
        }
        List<SelectItem> dayInMonthRange = SelectItemBuilder
                .buildSelectItems(days);

        return dayInMonthRange;
    }

    /**
     * @param cutOffDay
     *            the cutOffDay to set
     */
    public void setCutOffDay(int cutOffDay) {
        if (this.cutOffDay == null) {
            this.cutOffDay = new POCutOffDay(cutOffDay);
        } else {
            this.cutOffDay.setCutOffDay(cutOffDay);
        }
    }

    /**
     * @return the cutOffDay
     */
    public int getCutOffDay() {
        return cutOffDay.getCutOffDay();
    }

    /**
     * action method for save button
     * 
     * @return null: stay on same page
     */
    public String save() {
        try {
            getAccountServiceManagement().setCutOffDayOfOrganization(
                    cutOffDay.getCutOffDay(), voOrganization);
        } catch (ConcurrentModificationException e) {
            ui.handleException(e);
            return OUTCOME_ERROR;
        }
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_MANAGE_BILLING_SAVED);
        return OUTCOME_SUCCESS;
    }
}
