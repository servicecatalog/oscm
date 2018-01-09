/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 28.03.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.vo.VOBillingContact;

/**
 * @author weiser
 * 
 */
public class BillingContactCreateTask extends WebtestTask {

    private String address;
    private String companyName;
    private String email;
    private String bcId;

    @Override
    public void executeInternal() throws Exception {
        AccountService as = getServiceInterface(AccountService.class);
        VOBillingContact bc = new VOBillingContact();
        bc.setAddress(getAddress());
        bc.setCompanyName(getCompanyName());
        bc.setEmail(getEmail());
        bc.setId(bcId);
        as.saveBillingContact(bc);
    }

    public String getAddress() {
        if (isEmpty(address)) {
            return getProject().getProperty(TEST_ORGANIZATION_ADDRESS);
        }
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyName() {
        if (isEmpty(companyName)) {
            return getProject().getProperty(COMMON_ORG_NAME);
        }
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        if (isEmpty(email)) {
            return getProject().getProperty(COMMON_EMAIL);
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBcId(String bcId) {
        this.bcId = bcId;
    }

}
