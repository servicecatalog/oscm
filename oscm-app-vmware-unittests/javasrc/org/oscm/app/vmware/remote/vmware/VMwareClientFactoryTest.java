/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.remote.vmware;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.vmware.exception.ValidationException;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.oscm.app.vmware.persistence.VMwareCredentials;

/**
 * @author Dirk Bernsau
 *
 */
public class VMwareClientFactoryTest {

    private VMwareClientFactory factory;

    @Before
    public void before() {
        factory = new VMwareClientFactory("locale");
        factory.das = mock(DataAccessService.class);
    }

    @Test(expected = ValidationException.class)
    public void validateState_missingUrl() throws Exception {
        // given
        String url = null;
        String vcenter = "vcenter";
        VMwareCredentials credentials = new VMwareCredentials(url, "userId",
                "password");

        // when
        factory.validateState(vcenter, credentials);

        // then expect validation error
    }

    @Test(expected = ValidationException.class)
    public void validateState_missingUserId() throws Exception {
        // given
        String url = "url";
        String vcenter = "vcenter";
        VMwareCredentials credentials = new VMwareCredentials(url, null,
                "password");

        // when
        factory.validateState(vcenter, credentials);

        // then expect validation error
    }

    @Test(expected = ValidationException.class)
    public void validateState_missingPassword() throws Exception {
        // given
        String url = "url";
        String vcenter = "vcenter";
        VMwareCredentials credentials = new VMwareCredentials(url, "userid",
                null);

        // when
        factory.validateState(vcenter, credentials);

        // then expect validation error
    }

}
