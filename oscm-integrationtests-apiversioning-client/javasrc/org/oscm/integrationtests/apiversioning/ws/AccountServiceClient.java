/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015年1月23日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationtests.apiversioning.ws;

import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.oscm.integrationtests.apiversioning.client.CTMGClient;
import org.oscm.integrationtests.apiversioning.factory.ServiceFactory;
import org.oscm.intf.AccountService;
import org.oscm.vo.VOOrganization;

/**
 * Client of AccountService
 */
public class AccountServiceClient {

    private static final String wsdlFile = "/AccountService.wsdl";
    private AccountService accountServ;

    public AccountServiceClient(String userKey, String password) {
        URL wsdlURL = CTMGClient.class.getResource(wsdlFile);
        ServiceFactory factory = new ServiceFactory(userKey, password, wsdlURL);
        try {
            accountServ = factory.getBESWebService(AccountService.class);
        } catch (ParserConfigurationException e) {
            System.err.println("Connect to OSCM failed, reason: "
                    + e.getCause());
        }
    }

    public VOOrganization getOrganization() {
        return accountServ.getOrganizationData();
    }
}
