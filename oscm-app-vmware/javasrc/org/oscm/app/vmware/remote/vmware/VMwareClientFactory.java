/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.remote.vmware;

import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.exception.ValidationException;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.oscm.app.vmware.persistence.VMwareCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create a VMware client.
 *
 * @author Dirk Bernsau
 *
 */
public class VMwareClientFactory {

    private static final Logger LOG = LoggerFactory
            .getLogger(VMwareClientFactory.class);

    DataAccessService das;

    private String locale;

    public VMwareClientFactory(String locale) {
        this.locale = locale;
        das = new DataAccessService(locale);
    }

    public VMwareClient getInstance(VMPropertyHandler paramHandler)
            throws Exception {

        String vcenter = paramHandler.getTargetVCenterServer();
        VMwareCredentials credentials = das.getCredentials(vcenter);

        validateState(vcenter, credentials);

        return new VMwareClient(credentials);
    }

    void validateState(String vcenter, VMwareCredentials credentials)
            throws Exception {

        if (credentials.getURL() == null || credentials.getUserId() == null
                || credentials.getPassword() == null) {
            String message = Messages.get(locale, "error_db_vsphere_api_info",
                    new Object[] { vcenter, credentials.getURL(),
                            credentials.getUserId() });
            LOG.error(message);
            throw new ValidationException(message);
        }
    }

    public VMwareClient getInstance(String vcenter) throws Exception {
        VMwareCredentials credentials = das.getCredentials(vcenter);
        validateState(vcenter, credentials);
        return new VMwareClient(credentials);
    }

}
