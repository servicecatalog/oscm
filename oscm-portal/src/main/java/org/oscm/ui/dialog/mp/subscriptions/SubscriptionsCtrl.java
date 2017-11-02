/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 5, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.subscriptions;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.subscriptiondetails.POSubscriptionDetails;
import org.oscm.internal.subscriptions.POSubscriptionForList;
import org.oscm.internal.triggerprocess.TriggerProcessesService;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.validator.SubscriptionActivationValidator;
import org.oscm.ui.validator.ValidationPerformer;

/**
 * @author tokoda
 * 
 */
@ManagedBean
@ViewScoped
public class SubscriptionsCtrl implements Serializable {

    private static final long serialVersionUID = -2397306735465909908L;

    @ManagedProperty(value = "#{subListsLazyModel}")
    private SubscriptionListsLazyDataModel model;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    @ManagedProperty(value = "#{triggerProcessesModel}")
    private TriggerProcessesModel triggerProcessesModel;
    private POSubscriptionDetails poSubscriptionDetails;

    @EJB
    TriggerProcessesService triggerProcessService;

    @EJB
    SubscriptionService subscriptionService;

    @PostConstruct
    public void initializeTriggerProcesses() {
    	triggerProcessesModel.setWaitingForApprovalSubs(triggerProcessService
                .getAllWaitingForApprovalSubscriptions().getResultList(
                        POSubscriptionForList.class));
    }

    public String showSubscriptionDetails(String subId, String subKey) {
        model.setSelectedSubscriptionId(subId);
        model.setSelectedSubscriptionKey(Long.valueOf(subKey));

        if(validateSubscriptionStatus(subId)) {
            sessionBean.setSelectedTab("tabUser");
            sessionBean.setSelectedSubscriptionId(subId);
            sessionBean.setSelectedSubscriptionKey(model.getSelectedSubscriptionKey());

            return BaseBean.OUTCOME_SHOW_DETAILS;
        } else {
            return BaseBean.OUTCOME_ERROR;
        }
    }
    
    public boolean validateSubscriptionStatus(String id) {
        if (!ValidationPerformer.validate(
                SubscriptionActivationValidator.class, subscriptionService, id)) {
            JSFUtils.addMessage(
                    null,
                    FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_SUBSCRIPTION_MODIFIED_OR_DELETED_CONCURRENTLY,
                    null);
            return false;
        }

        return true;
    }

    public SubscriptionListsLazyDataModel getModel() {
        return model;
    }

    public void setModel(SubscriptionListsLazyDataModel model) {
        this.model = model;
    }

    public POSubscriptionDetails getPOSubscriptionDetails() {
        return poSubscriptionDetails;
    }

    public void setPOSubscriptionDetails(
            POSubscriptionDetails poSubscriptionDetails) {
        this.poSubscriptionDetails = poSubscriptionDetails;
    }
    
    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

	public TriggerProcessesModel getTriggerProcessesModel() {
		return triggerProcessesModel;
	}

	public void setTriggerProcessesModel(TriggerProcessesModel triggerProcessesModel) {
		this.triggerProcessesModel = triggerProcessesModel;
	}
}
