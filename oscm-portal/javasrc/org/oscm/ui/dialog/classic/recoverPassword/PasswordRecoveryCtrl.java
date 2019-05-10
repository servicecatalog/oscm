/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2013-2-5
 *
 *******************************************************************************/

package org.oscm.ui.dialog.classic.recoverPassword;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;

/**
 * Handle the password recovery procedure
 *
 * @author Mao
 *
 */
@ViewScoped
@ManagedBean(name = "passwordRecoveryCtrl")
public class PasswordRecoveryCtrl extends BaseBean {

	@ManagedProperty(value = "#{passwordRecoveryModel}")
	private PasswordRecoveryModel model;

	/**
	 * Start password recovery procedure from marketplace
	 */
	public String startPasswordRecovery() {
		return handlePasswordRecovery(model.getUserId(), model.getMarketpalceId());
	}

	/**
	 * Start password recovery procedure for manager from administration portal
	 */
	public String startPasswordRecoveryForManager() {
		return handlePasswordRecovery(model.getUserId(), null);
	}

	private String handlePasswordRecovery(String user, String mId) {
		// call service to send the confirm mail
		getPasswordRecoveryService().startPasswordRecovery(user, mId);

		if (isMarketplaceSet(getRequest())) {
			return OUTCOME_MARKETPLACE_CONFIRMSTARTPWDRECOVERY;
		}
		// add message for blue portal
		addMessage(null, FacesMessage.SEVERITY_INFO, INFO_RECOVERPASSWORD_START);
		return OUTCOME_SUCCESS;
	}

	/**
	 * Returns an outcome that informs to get back to the previous page
	 *
	 * @return {@link BaseBean#OUTCOME_PREVIOUS}
	 */
	public String redirectToLogin() {
		return OUTCOME_PREVIOUS;
	}

	public PasswordRecoveryModel getModel() {
		return model;
	}

	public void setModel(PasswordRecoveryModel model) {
		this.model = model;
	}

	@PostConstruct
	protected void initialize() {
		if (this.model == null) {
			this.model = new PasswordRecoveryModel();
		}
		final String marketplaceId = getMarketplaceId();
		if (marketplaceId != null && (!marketplaceId.trim().equals(""))) {
			this.model.setMarketpalceId(marketplaceId);
		}
		this.setSessionAttribute(Constants.CAPTCHA_INPUT_STATUS, Boolean.FALSE);
	}

}
