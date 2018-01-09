/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.HashSet;
import java.util.Set;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Task for setting the default payment for customer and service.
 * 
 * @author weiser
 * 
 */
public class PaymentDefaultConfigurationTask extends WebtestTask {

    private String customerTypes;
    private String serviceTypes;

    @Override
    public void executeInternal() throws Exception {
        Set<VOPaymentType> customerDefault = new HashSet<VOPaymentType>();
        if (!isEmpty(customerTypes)) {
            String[] split = customerTypes.split(",");
            for (String string : split) {
                VOPaymentType pt = new VOPaymentType();
                pt.setPaymentTypeId(string.trim().toUpperCase());
                customerDefault.add(pt);
            }
        }
        log(String.format("Enabling %s as customer default", customerDefault));
        Set<VOPaymentType> serviceDefault = new HashSet<VOPaymentType>();
        if (!isEmpty(serviceTypes)) {
            String[] split = serviceTypes.split(",");
            for (String string : split) {
                VOPaymentType pt = new VOPaymentType();
                pt.setPaymentTypeId(string.trim().toUpperCase());
                serviceDefault.add(pt);
            }
        }
        log(String.format("Enabling %s as service default", serviceDefault));
        AccountService service = getServiceInterface(AccountService.class);
        service.savePaymentConfiguration(customerDefault, null, serviceDefault,
                null);
    }

    public void setCustomerTypes(String customerTypes) {
        this.customerTypes = customerTypes;
    }

    public void setServiceTypes(String serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

}
