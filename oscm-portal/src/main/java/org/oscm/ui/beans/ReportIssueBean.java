/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-4-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.mp.subscriptionDetails.ManageSubscriptionModel;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Backing bean for handling of sending of issue e-mails to the suppliers
 * support email contact.
 * 
 * @author yuyin
 * 
 */
@ManagedBean
@RequestScoped
public class ReportIssueBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -577075617302574650L;
    private static final String ERROR_SUBSCRIPTION_NOT_ACCESSIBLE = "error.subscription.notAccessible";
    private static final String OUTCOME_SUBSCRIPTION_NOT_AVAILABLE = "subscriptionNotAccessible";
    private String supportEmailTitle;
    private String supportEmailContent;
    private static final String SUBSCRIPTIONDETAILS_MODEL = "manageSubscriptionModel";
    UiDelegate ui = new UiDelegate();
    public String init() {
        supportEmailTitle = null;
        supportEmailContent = null;
        // reset the values of all UIInput children
        resetUIInputChildren();
        return null;
    }

    /**
     * Maximum allowed character length of the subject field.
     */
    public int getSubjectLen() {
        return ADMValidator.LENGTH_EMAIL_SUBJECT;
    }

    /**
     * Maximum allowed character length of the content text area.
     */
    public int getContentLen() {
        return ADMValidator.LENGTH_EMAIL_CONTENT;
    }

    /**
     * @param supportemailTitle
     *            the supportemailTitle to set
     */
    public void setSupportEmailTitle(String supportemailTitle) {
        this.supportEmailTitle = supportemailTitle;
    }

    /**
     * @return the supportemailTitle
     */
    public String getSupportEmailTitle() {
        return supportEmailTitle;
    }

    /**
     * @param supportemailContent
     *            the supportemailContent to set
     */
    public void setSupportEmailContent(String supportemailContent) {
        this.supportEmailContent = supportemailContent;
    }

    /**
     * @return the supportemailContent
     */
    public String getSupportEmailContent() {
        return supportEmailContent;
    }

    /**
     * Invoke sending of the support email.
     * 
     * @return the navigation outcome string according the result of the
     *         operation.
     */
    public String reportIssue(String selectedSubscriptionId) {

        try {
            if (selectedSubscriptionId == null
                    || ADMStringUtils.isBlank(selectedSubscriptionId))
                throw new ValidationException();

            if (validateSubscriptionStatus()) {
                ui.handleError(null, ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                        selectedSubscriptionId);
                return OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
            }

            getSubscriptionService().reportIssue(selectedSubscriptionId,
                    getSupportEmailTitle(), getSupportEmailContent());
        } catch (SaaSApplicationException e) {
            handleException(e);
            return OUTCOME_ERROR;
        } finally {
            this.supportEmailContent = null;
            this.supportEmailTitle = null;
        }
        addMessage(null, FacesMessage.SEVERITY_INFO,
                INFO_ORGANIZATION_SUPPORTMAIL_SENT);

        return OUTCOME_SUCCESS;
    }

    protected void handleException(SaaSApplicationException e) {
        ExceptionHandler.execute(e);
    }

    /**
     * This method add the "save successful" message to the faces context. The
     * method is used in the context of modal window handling.
     */
    public String refreshSendSuccessMessage() {
        addMessage(null, FacesMessage.SEVERITY_INFO,
                INFO_ORGANIZATION_SUPPORTMAIL_SENT);
        return OUTCOME_SUCCESS;
    }

    boolean validateSubscriptionStatus() {
        try {
            SubscriptionStatus status = getSubscriptionDetailsService()
                    .loadSubscriptionStatus(getSelectedSubscriptionKey())
                    .getResult(SubscriptionStatus.class);
            return status.isInvalidOrDeactive();
        } catch (ObjectNotFoundException e) {
            return true;
        }
    }

    long getSelectedSubscriptionKey() {
    	ManageSubscriptionModel model = ui.findBean(SUBSCRIPTIONDETAILS_MODEL);
        return model.getSubscription().getKey();
    }

}
