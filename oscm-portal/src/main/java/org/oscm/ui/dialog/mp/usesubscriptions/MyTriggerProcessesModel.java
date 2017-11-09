/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 28.04.15 10:00
 *
 * ******************************************************************************
 */
package org.oscm.ui.dialog.mp.usesubscriptions;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseModel;
import org.oscm.internal.subscriptions.POSubscription;

@ViewScoped
@ManagedBean(name="myTriggerProcessesModel")
public class MyTriggerProcessesModel extends BaseModel {

	private static final long serialVersionUID = 3949463519102673924L;

	private List<POSubscription> waitingForApprovalSubs;

	public List<POSubscription> getWaitingForApprovalSubs() {
		return waitingForApprovalSubs;
	}

	public void setWaitingForApprovalSubs(List<POSubscription> waitingForApprovalSubs) {
		this.waitingForApprovalSubs = waitingForApprovalSubs;
	}
}
