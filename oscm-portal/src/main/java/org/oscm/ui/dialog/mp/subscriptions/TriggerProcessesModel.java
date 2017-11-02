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

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseModel;
import org.oscm.internal.subscriptions.POSubscriptionForList;

@ViewScoped
@ManagedBean(name="triggerProcessesModel")
public class TriggerProcessesModel extends BaseModel {

	private static final long serialVersionUID = 3515619287200750896L;

	private List<POSubscriptionForList> waitingForApprovalSubs;

	public List<POSubscriptionForList> getWaitingForApprovalSubs() {
		return waitingForApprovalSubs;
	}

	public void setWaitingForApprovalSubs(List<POSubscriptionForList> waitingForApprovalSubs) {
		this.waitingForApprovalSubs = waitingForApprovalSubs;
	}
}
