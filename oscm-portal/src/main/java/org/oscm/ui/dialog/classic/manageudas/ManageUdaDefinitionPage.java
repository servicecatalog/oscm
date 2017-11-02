/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import java.util.List;

import org.oscm.ui.beans.UdaBean;

/**
 * @author yuyin
 * 
 */
public class ManageUdaDefinitionPage {

    private List<UdaDefinitionRowModel> subscriptionUdas;
    private List<UdaDefinitionRowModel> customerUdas;
    private UdaDefinitionDetails currentUdaDefinition;
    private UdaDefinitionDetails newUdaDefinition;
    private String udaType;

    /**
     * @return the subscriptionUdas
     */
    public List<UdaDefinitionRowModel> getSubscriptionUdas() {
        return subscriptionUdas;
    }

    /**
     * @param subscriptionUdas
     *            the subscriptionUdas to set
     */
    public void setSubscriptionUdas(List<UdaDefinitionRowModel> subscriptionUdas) {
        this.subscriptionUdas = subscriptionUdas;
    }

    /**
     * @return the customerUdas
     */
    public List<UdaDefinitionRowModel> getCustomerUdas() {
        return customerUdas;
    }

    /**
     * @param customerUdas
     *            the customerUdas to set
     */
    public void setCustomerUdas(List<UdaDefinitionRowModel> customerUdas) {
        this.customerUdas = customerUdas;
    }

    /**
     * @return the currentUdaDefinition
     */
    public UdaDefinitionDetails getCurrentUdaDefinition() {
        if (null == currentUdaDefinition) {
            currentUdaDefinition = new UdaDefinitionDetails();
        }
        return currentUdaDefinition;
    }

    /**
     * @param currentUdaDefinition
     *            the currentUdaDefinition to set
     */
    public void setCurrentUda(UdaDefinitionDetails currentUdaDefinition) {
        this.currentUdaDefinition = currentUdaDefinition;
    }

    /**
     * @return the newUdaDefinition
     */
    public UdaDefinitionDetails getNewUdaDefinition() {
        if (null == newUdaDefinition) {
            newUdaDefinition = new UdaDefinitionDetails();
        }
        return newUdaDefinition;
    }

    /**
     * @param newUdaDefinition
     *            the newUdaDefinition to set
     */
    public void setNewUdaDefinition(UdaDefinitionDetails newUdaDefinition) {
        this.newUdaDefinition = newUdaDefinition;
    }

    /**
     * @param udaType
     *            the udaType to set
     */
    public void setUdaType(String udaType) {
        // refresh newUdaDefinition while open create customer attributes dialog
        this.newUdaDefinition = null;
        this.udaType = udaType;
    }

    /**
     * @return the udaType
     */
    public String getUdaType() {
        return udaType;
    }

    /**
     * set current selected Uda to currentUdaDefinition for
     * <code>customerUdas</code> or <code>subscriptionUdas</code>
     * 
     * @param index
     */
    public void setCurrentUdaIndex(int index) {
        if (udaType.equals(UdaBean.CUSTOMER)) {
            currentUdaDefinition = UdaModelConverter
                    .convertUdaDefinitionRowModelToUdaDefDetails(customerUdas
                            .get(index));
        } else {
            currentUdaDefinition = UdaModelConverter
                    .convertUdaDefinitionRowModelToUdaDefDetails(subscriptionUdas
                            .get(index));

        }

    }
}
