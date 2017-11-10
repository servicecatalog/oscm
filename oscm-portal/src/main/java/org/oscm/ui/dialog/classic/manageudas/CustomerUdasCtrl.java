/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import java.util.ArrayList;
import java.util.List;

import org.oscm.ui.beans.UdaBean;
import org.oscm.ui.model.UdaRow;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author yuyin
 * 
 */
public class CustomerUdasCtrl {

    private AccountService ac;

    public CustomerUdasCtrl(AccountService delegate) {
        ac = delegate;
        customerUdas = new CustomerUdas();
    }

    private CustomerUdas customerUdas;

    /**
     * @param customerUdas
     *            the customerUdas to set
     */
    public void setCustomerUdas(CustomerUdas customerUdas) {
        this.customerUdas = customerUdas;
    }

    /**
     * @return the customerUdas
     */
    public CustomerUdas getCustomerUdas() {

        return customerUdas;
    }

    public CustomerUdas getUdasForNewSubscription(String supplierId,
            long organizationKey) throws ObjectNotFoundException,
            ValidationException, OrganizationAuthoritiesException,
            OperationNotPermittedException {

        List<VOUdaDefinition> definitions = ac
                .getUdaDefinitionsForCustomer(supplierId);
        // create model and assign organization and subscription related UDAs
        // remember the model and return it

        List<VOUdaDefinition> subUdaDefinitions = new ArrayList<VOUdaDefinition>();
        List<VOUdaDefinition> orgUdaDefinitions = new ArrayList<VOUdaDefinition>();
        for (VOUdaDefinition def : definitions) {
            if (def.getTargetType().equals(UdaBean.CUSTOMER_SUBSCRIPTION)) {
                subUdaDefinitions.add(def);
            } else if (def.getTargetType().equals(UdaBean.CUSTOMER)) {
                orgUdaDefinitions.add(def);
            }
        }
        // get UDA definitions from supplier and existing UDAs for the
        // organization convert to UdaRow instances;
        List<VOUda> orgUdas = ac.getUdasForCustomer(UdaBean.CUSTOMER,
                organizationKey, supplierId);
        List<UdaRow> orgCustomerUdas = UdaRow.getUdaRows(orgUdaDefinitions,
                orgUdas);
        customerUdas.setOrganizationUdaRows(orgCustomerUdas);

        // create new VOUda instances objects for definitions without yet saved
        // UDAs
        List<UdaRow> subCustomerUdas = UdaRow.getUdaRows(subUdaDefinitions,
                new ArrayList<VOUda>());
        customerUdas.setSubscriptionUdaRows(subCustomerUdas);
        return customerUdas;
    }

    public CustomerUdas getUdasForExistingSubscription(String supplierId,
            long organizationKey, long subscriptionKey)
            throws ObjectNotFoundException, ValidationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        List<VOUdaDefinition> definitions = ac
                .getUdaDefinitionsForCustomer(supplierId);
        // create model and assign organization and subscription related UDAs
        // remember the model and return it

        List<VOUdaDefinition> subUdaDefinitions = new ArrayList<VOUdaDefinition>();
        List<VOUdaDefinition> orgUdaDefinitions = new ArrayList<VOUdaDefinition>();
        for (VOUdaDefinition def : definitions) {
            if (def.getTargetType().equals(UdaBean.CUSTOMER_SUBSCRIPTION)) {
                subUdaDefinitions.add(def);
            } else if (def.getTargetType().equals(UdaBean.CUSTOMER)) {
                orgUdaDefinitions.add(def);
            }
        }
        // get UDA definitions from supplier and existing UDAs for the
        // organization convert to UdaRow instances; create new VOUda instances
        // objects for definitions without yet saved UDAs
        List<VOUda> orgUdas = ac.getUdasForCustomer(UdaBean.CUSTOMER,
                organizationKey, supplierId);
        List<UdaRow> orgCustomerUdas = UdaRow.getUdaRows(orgUdaDefinitions,
                orgUdas);
        customerUdas.setOrganizationUdaRows(orgCustomerUdas);

        // get UDA definitions from supplier and existing UDAs for the
        // subscription convert to UdaRow instances; create new VOUda instances
        // objects for definitions without yet saved UDAs
        List<VOUda> subUdas = ac.getUdasForCustomer(
                UdaBean.CUSTOMER_SUBSCRIPTION, subscriptionKey, supplierId);
        List<UdaRow> subCustomerUdas = UdaRow.getUdaRows(subUdaDefinitions,
                subUdas);
        customerUdas.setSubscriptionUdaRows(subCustomerUdas);

        return customerUdas;
    }

}
