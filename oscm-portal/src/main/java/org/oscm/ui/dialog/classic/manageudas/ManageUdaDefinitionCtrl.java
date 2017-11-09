/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.ui.beans.UdaBean;
import org.oscm.validation.ArgumentValidator;

/**
 * @author yuyin
 * 
 */
public class ManageUdaDefinitionCtrl {

    private AccountService as;
    private ManageUdaDefinitionPage model;

    /**
     * @param model
     *            the model to set
     */
    public void setModel(ManageUdaDefinitionPage model) {
        this.model = model;
    }

    ManageUdaDefinitionCtrl(AccountService delegate) {
        as = delegate;
    }

    /**
     * @return all UdaDefinitionRow for CUSTOMER type
     */
    private List<VOUdaDefinition> getCustomerUdaDefinitions() {
        List<VOUdaDefinition> result = new ArrayList<>();
        List<VOUdaDefinition> udaDefinitions = as.getUdaDefinitions();
        for (VOUdaDefinition def : udaDefinitions) {
            if (def.getTargetType().equals(UdaBean.CUSTOMER)) {
                result.add(def);
            }
        }
        return result;
    }

    /**
     * @return all UdaDefinitionRow for CUSTOMER_SUBSCRIPTION type
     */
    private List<VOUdaDefinition> getSubscriptionUdaDefinitions() {
        List<VOUdaDefinition> result = new ArrayList<>();
        List<VOUdaDefinition> udaDefinitions = as.getUdaDefinitions();
        for (VOUdaDefinition def : udaDefinitions) {
            if (def.getTargetType().equals(UdaBean.CUSTOMER_SUBSCRIPTION)) {
                result.add(def);
            }
        }
        return result;
    }

    /**
     * @return if model is not null then return it, otherwise initialize the
     *         model then return
     */
    public ManageUdaDefinitionPage getModel() {
        if (model == null) {
            refreshModel();
        }
        return model;
    }

    /**
     * convert the UdaDefinitionDetails saved in model.selectedUdaDefinition,
     * then save the VOUdaDefinition
     * 
     * @throws SaaSApplicationException
     */
    public void createUdaDefinition() throws SaaSApplicationException {
        ArgumentValidator.notNull("UdaDefinitionDetails", model);
        VOUdaDefinition udaDefitionDetails = UdaModelConverter
                .convertUdaDefDetailsToVoUdaDefinition(
                        model.getNewUdaDefinition());

        // the TargetType value kept by model
        if (model.getUdaType().equals(UdaBean.CUSTOMER)) {
            udaDefitionDetails.setTargetType(UdaBean.CUSTOMER);
        } else {
            udaDefitionDetails.setTargetType(UdaBean.CUSTOMER_SUBSCRIPTION);
        }
        persistUda(udaDefitionDetails, true);
    }

    /**
     * convert the UdaDefinitionDetails saved in model.selectedUdaDefinition,
     * then save the VOUdaDefinition
     * 
     * @throws SaaSApplicationException
     * @throws GeneralSecurityException
     */
    public void updateUdaDefinition()
            throws SaaSApplicationException, GeneralSecurityException {
        ArgumentValidator.notNull("UdaDefinitionDetails", model);
        VOUdaDefinition udaDefitionDetails = UdaModelConverter
                .convertUdaDefDetailsToVoUdaDefinition(
                        model.getCurrentUdaDefinition());

        // the TargetType value kept by model
        if (model.getUdaType().equals(UdaBean.CUSTOMER)) {
            udaDefitionDetails.setTargetType(UdaBean.CUSTOMER);
        } else {
            udaDefitionDetails.setTargetType(UdaBean.CUSTOMER_SUBSCRIPTION);
        }
        persistUda(udaDefitionDetails, true);
    }

    /**
     * convert the UdaDefinitionDetails saved in model.selectedUdaDefinition,
     * then delete
     * 
     * @throws SaaSApplicationException
     */
    public void deleteUdaDefinition() throws SaaSApplicationException {
        // convert and delete model.selectedUdaDefinition
        ArgumentValidator.notNull("UdaDefinitionDetails", model);
        VOUdaDefinition udaDefitionDetails = UdaModelConverter
                .convertUdaDefDetailsToVoUdaDefinition(
                        model.getCurrentUdaDefinition());
        // the TargetType value kept by model
        if (model.getUdaType().equals(UdaBean.CUSTOMER)) {
            udaDefitionDetails.setTargetType(UdaBean.CUSTOMER);
        } else {
            udaDefitionDetails.setTargetType(UdaBean.CUSTOMER_SUBSCRIPTION);
        }
        persistUda(udaDefitionDetails, false);
    }

    /**
     * Saves or deletes definitions of custom attributes. New attribute
     * definitions in the <code>udaDefitionDetails</code> list are created,
     * existing ones are updated. <code>isSave</code> determines the action is
     * saving or deleting.
     * 
     * @param udaDefitionDetails
     * @param isSave
     * @throws SaaSApplicationException
     */
    private void persistUda(VOUdaDefinition udaDefitionDetails, boolean isSave)
            throws SaaSApplicationException {
        List<VOUdaDefinition> toSave = new ArrayList<>();
        List<VOUdaDefinition> toDelete = new ArrayList<>();
        if (isSave) {
            toSave.add(udaDefitionDetails);
        } else {
            toDelete.add(udaDefitionDetails);
        }
        as.saveUdaDefinitions(toSave, toDelete);
    }

    /**
     * initialize the model when model is null
     */
    void refreshModel() {
        // initialize the model
        model = new ManageUdaDefinitionPage();

        // get the uda DefinitionRow list For Customer and save to
        // model.customerUdas
        List<VOUdaDefinition> voUdaDefinitionsForCustomer = getCustomerUdaDefinitions();
        List<UdaDefinitionRowModel> customerUdas = new ArrayList<>();
        for (VOUdaDefinition voUdaDef : voUdaDefinitionsForCustomer) {
            customerUdas.add(UdaModelConverter
                    .convertVoUdaDefinitionToRowModel(voUdaDef));
        }
        model.setCustomerUdas(customerUdas);

        // get the uda DefinitionRow list For Subscription and save to
        // model.subscriptionUdas
        List<VOUdaDefinition> voUdaDefinitionForSubscription = getSubscriptionUdaDefinitions();
        List<UdaDefinitionRowModel> subscriptionUdas = new ArrayList<>();
        for (VOUdaDefinition voUdaDef : voUdaDefinitionForSubscription) {
            subscriptionUdas.add(UdaModelConverter
                    .convertVoUdaDefinitionToRowModel(voUdaDef));
        }
        model.setSubscriptionUdas(subscriptionUdas);
    }

    public String getLocalizedAttributeName(long key, String locale) {
        return as.getLocalizedAttributeName(key, locale);
    }
}
