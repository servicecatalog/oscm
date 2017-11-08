/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-17                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.accountservice.bean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

/**
 * @author qiu
 * 
 */
public class AccountServiceBeanUserLicenseTest {

    private AccountServiceBean ab;

    @Before
    public void setup() throws Exception {
        ab = new AccountServiceBean();
        ab.userLicenseService = mock(UserLicenseServiceLocalBean.class);
    }

    @Test
    public void countRegisteredUsers() {
        // when
        ab.countRegisteredUsers();

        // then
        verify(ab.userLicenseService, times(1)).countRegisteredUsers();
    }

    @Test
    public void checkUserNum() throws Exception {
        // when
        ab.checkUserNum();

        // then
        verify(ab.userLicenseService, times(1)).checkUserNum();
    }
}
