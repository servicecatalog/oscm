/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2012-6-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.ui.beans.BaseBean;

/**
 * @author yuyin
 * 
 */
@ViewScoped
@ManagedBean(name = "manageUdaDefinitionBean")
public class ManageUdaDefinitionBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -2833012635378419493L;
    private ManageUdaDefinitionCtrl controller;
    ObjectNotFoundException onfe;

    /**
     * @return the controller
     */
    public ManageUdaDefinitionCtrl getController() {
        return controller;
    }

    /**
     * @param controller
     *            the controller to set
     */
    public void setController(ManageUdaDefinitionCtrl controller) {
        this.controller = controller;
    }

    public ManageUdaDefinitionBean() {
        AccountService ac = getAccountingService();
        controller = new ManageUdaDefinitionCtrl(ac);
    }

    ManageUdaDefinitionBean(ManageUdaDefinitionCtrl controller) {
        this.controller = controller;
    }

    /**
     * @return OUTCOME_SUCCESS if successfully create selected Uda;
     *         OUTCOME_ERROR if encounter some error when creating
     * @throws SaaSApplicationException
     */
    public String create() throws SaaSApplicationException {
        // delegate to controller
        controller.createUdaDefinition();
        // evaluate result (e.g. add message on success)
        addMessage(null, FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_UDADEFINITIONS_SAVED);
        return OUTCOME_SUCCESS;
    }

    /**
     * @return OUTCOME_SUCCESS if successfully update selected Uda;
     *         OUTCOME_ERROR if encounter some error when updating
     * @throws SaaSApplicationException
     */
    public String update()
            throws SaaSApplicationException, GeneralSecurityException {
        // delegate to controller
        try {
            controller.updateUdaDefinition();
            addMessage(null, FacesMessage.SEVERITY_INFO,
                    BaseBean.INFO_UDADEFINITIONS_SAVED);
        } catch (ObjectNotFoundException e) {
            onfe = e;
        }
        // evaluate result
        return OUTCOME_SUCCESS;
    }

    /**
     * @return OUTCOME_SUCCESS if successfully delete selected Uda;
     *         OUTCOME_ERROR if encounter some error when deleting
     * @throws SaaSApplicationException
     */
    public String delete() throws SaaSApplicationException {
        // delegate to controller
        controller.deleteUdaDefinition();
        // evaluate result
        addMessage(null, FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_UDADEFINITIONS_DELETED);
        return OUTCOME_SUCCESS;
    }

    /**
     * @return customerUdas in model
     */
    public List<UdaDefinitionRowModel> getCustomerUdas() {
        return controller.getModel().getCustomerUdas();
    }

    /**
     * @return subscriptionUdas in model
     */
    public List<UdaDefinitionRowModel> getSubscriptionUdas() {
        return controller.getModel().getSubscriptionUdas();
    }

    /**
     * @return new UdaDefinitionDetails model in ManageUdaDefinitionPage
     */
    public UdaDefinitionDetails getNewUda() {
        return controller.getModel().getNewUdaDefinition();
    }

    /**
     * set new UdaDefinitionDetails model
     * 
     * @param currentUda
     */
    public void setNewUda(UdaDefinitionDetails currentUda) {
        controller.getModel().setNewUdaDefinition(currentUda);
    }

    /**
     * @return Current UdaDefinitionDetails model in ManageUdaDefinitionPage
     */
    public UdaDefinitionDetails getCurrentUda() {
        return controller.getModel().getCurrentUdaDefinition();
    }

    /**
     * set current UdaDefinitionDetails model
     * 
     * @param currentUda
     */
    public void setCurrentUda(UdaDefinitionRowModel currentUda) {
        controller.getModel().setCurrentUda(UdaModelConverter
                .convertUdaDefinitionRowModelToUdaDefDetails(currentUda));
    }

    /**
     * set the Uda target type for current action
     * 
     * @param type
     */
    public void setUdaType(String type) {
        controller.getModel().setUdaType(type);
    }

    /**
     * @return the Uda target type for current action
     */
    public String getUdaType() {
        return controller.getModel().getUdaType();
    }

    /**
     * set current selected Uda
     * 
     * @param index
     * @throws NumberFormatException
     */
    public void setCurrentUdaIndex(int index) throws NumberFormatException {
        controller.getModel().setCurrentUdaIndex(index);
    }

    /**
     * This method add the "save successful" message to the faces context. The
     * method is used in the context of modal window handling. In contrast to
     * the error message, the "save success" message for a
     * manageUdaDefinitionBean should be shown on the parent page (not in the
     * modal dialog), so it necessary to read the message before executing a
     * submit.
     */
    public String refreshSaveSuccessMessage() {

        if (onfe != null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR, onfe.getMessageKey(),
                    onfe.getMessageParams());
            onfe = null;
        } else {
            addMessage(null, FacesMessage.SEVERITY_INFO,
                    INFO_UDADEFINITIONS_SAVED);
        }
        controller.setModel(null);

        return OUTCOME_SUCCESS;
    }

    /**
     * Same as {@link #refreshSaveSuccessMessage()} but for deletion.
     */
    public String refreshDeleteSuccessMessage() {

        addMessage(null, FacesMessage.SEVERITY_INFO,
                INFO_UDADEFINITIONS_DELETED);
        controller.setModel(null);

        return OUTCOME_SUCCESS;
    }

    public String reset() {

        controller.setModel(null);

        return OUTCOME_SUCCESS;
    }

    public void processValueChange(ValueChangeEvent event) {
        String attrName = controller.getLocalizedAttributeName(
                controller.getModel().getCurrentUdaDefinition().getKey(),
                event.getNewValue().toString());
        controller.getModel().getCurrentUdaDefinition().setName(attrName);
    }

}
