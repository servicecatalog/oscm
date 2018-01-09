/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.remote.bes;

import org.oscm.app.v2_0.BSSWebServiceFactory;
import org.oscm.app.vmware.persistence.APPDataAccessService;

public class BesClient {

    public <T> T getWebServiceAsOrganizationAdmin(Class<T> webService,
            String orgId) throws Exception {
        APPDataAccessService das = new APPDataAccessService();
        Credentials cred = das.getCredentials(orgId);
        return getWebService(webService, cred);
    }

    public <T> T getWebService(Class<T> serviceClass, Credentials credentials)
            throws Exception {
        return BSSWebServiceFactory.getBSSWebService(serviceClass,
                credentials.toPasswordAuthentication());
    }

}
