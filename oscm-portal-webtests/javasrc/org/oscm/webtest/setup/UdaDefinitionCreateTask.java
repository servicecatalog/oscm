/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 16.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;
import java.util.List;

import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author weiser
 * 
 */
public class UdaDefinitionCreateTask extends WebtestTask {

    String udaName = "uda";
    int numOfUdas = 1;
    String defaultValue = "";
    boolean forCustomer = true;
    boolean userOption = false;
    boolean mandatory = false;

    @Override
    public void executeInternal() throws Exception {
        List<VOUdaDefinition> toCreate = new ArrayList<VOUdaDefinition>();
        for (int i = 0; i < numOfUdas; i++) {
            VOUdaDefinition def = new VOUdaDefinition();
            def.setDefaultValue(defaultValue);
            def.setUdaId(udaName + i);
            def.setTargetType(forCustomer ? UdaTargetType.CUSTOMER.name()
                    : UdaTargetType.CUSTOMER_SUBSCRIPTION.name());
            def.setConfigurationType(determineConfigurationtype());
            toCreate.add(def);
        }

        AccountService as = getServiceInterface(AccountService.class);
        as.saveUdaDefinitions(toCreate, new ArrayList<VOUdaDefinition>());
    }

    UdaConfigurationType determineConfigurationtype() {
        UdaConfigurationType type = UdaConfigurationType.SUPPLIER;
        if (userOption) {
            type = UdaConfigurationType.USER_OPTION_OPTIONAL;
            if (mandatory) {
                type = UdaConfigurationType.USER_OPTION_MANDATORY;
            }
        }
        return type;
    }

    public void setUdaName(String udaName) {
        this.udaName = udaName;
    }

    public void setNumOfUdas(int numOfUdas) {
        this.numOfUdas = numOfUdas;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setForCustomer(boolean forCustomer) {
        this.forCustomer = forCustomer;
    }

    public void setUserOption(boolean userOption) {
        this.userOption = userOption;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

}
