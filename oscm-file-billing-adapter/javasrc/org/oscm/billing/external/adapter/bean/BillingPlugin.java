/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *******************************************************************************/

package org.oscm.billing.external.adapter.bean;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.client.WebTarget;

import org.oscm.billing.external.billing.service.BillingPluginService;
import org.oscm.billing.external.exception.BillingException;

/**
 * The implementation of the billing plugin interface
 *
 */
@Stateless
@Remote({ BillingPluginService.class })
public class BillingPlugin implements BillingPluginService {

    public static final String ID = "FILE_BILLING";
    public static final String TEST_CONNECTION_URL = "testConnectionURL";

    ConfigProperties properties = new ConfigProperties(ID);
    RestDAO restDao = new RestDAO();

    @Override
    public void testConnection() throws BillingException {

        String configProperty = properties
                .getConfigProperty(TEST_CONNECTION_URL);

        WebTarget webResource = restDao.createWebResource(configProperty);
        restDao.getTextResponse(webResource);
    }

}
