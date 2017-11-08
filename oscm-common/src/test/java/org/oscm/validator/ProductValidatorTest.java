/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 31.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.validator;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.ServiceStateException;

/**
 * @author barzu
 */
public class ProductValidatorTest {

    @Test(expected = NullPointerException.class)
    public void validateInactiveOrSuspended_Null() throws Exception {
        ProductValidator.validateInactiveOrSuspended("P", null);
    }

    @Test(expected = ServiceStateException.class)
    public void validateInactiveOrSuspended_ACTIVE() throws Exception {
        ProductValidator.validateInactiveOrSuspended("P", ServiceStatus.ACTIVE);
    }

    @Test(expected = ServiceStateException.class)
    public void validateInactiveOrSuspended_DELETED() throws Exception {
        ProductValidator
                .validateInactiveOrSuspended("P", ServiceStatus.DELETED);
    }

    @Test(expected = ServiceStateException.class)
    public void validateInactiveOrSuspended_OBSOLETE() throws Exception {
        ProductValidator.validateInactiveOrSuspended("P",
                ServiceStatus.OBSOLETE);
    }

    @Test
    public void validateInactiveOrSuspended_INACTIVE() throws Exception {
        ProductValidator.validateInactiveOrSuspended("P",
                ServiceStatus.INACTIVE);
    }

    @Test
    public void validateInactiveOrSuspended_SUSPENDED() throws Exception {
        ProductValidator.validateInactiveOrSuspended("P",
                ServiceStatus.SUSPENDED);
    }

    @Test(expected = NullPointerException.class)
    public void validateActiveOrInactiveOrSuspended_Null() throws Exception {
        ProductValidator.validateActiveOrInactiveOrSuspended("P", null);
    }

    @Test
    public void validateActiveOrInactiveOrSuspended_ACTIVE() throws Exception {
        ProductValidator.validateActiveOrInactiveOrSuspended("P",
                ServiceStatus.ACTIVE);
    }

    @Test(expected = ServiceStateException.class)
    public void validateActiveOrInactiveOrSuspended_DELETED() throws Exception {
        ProductValidator.validateActiveOrInactiveOrSuspended("P",
                ServiceStatus.DELETED);
    }

    @Test(expected = ServiceStateException.class)
    public void validateActiveOrInactiveOrSuspended_OBSOLETE() throws Exception {
        ProductValidator.validateActiveOrInactiveOrSuspended("P",
                ServiceStatus.OBSOLETE);
    }

    @Test
    public void validateActiveOrInactiveOrSuspended_INACTIVE() throws Exception {
        ProductValidator.validateActiveOrInactiveOrSuspended("P",
                ServiceStatus.INACTIVE);
    }

    @Test
    public void validateActiveOrInactiveOrSuspended_SUSPENDED()
            throws Exception {
        ProductValidator.validateActiveOrInactiveOrSuspended("P",
                ServiceStatus.SUSPENDED);
    }

    @Test(expected = NullPointerException.class)
    public void validateResalePermission_Null() throws Exception {
        ProductValidator.validateResalePermission("P", null);
    }

    @Test
    public void validateResalePermission_ACTIVE() throws Exception {
        ProductValidator.validateResalePermission("P", ServiceStatus.ACTIVE);
    }

    @Test(expected = ServiceStateException.class)
    public void validateResalePermission_DELETED() throws Exception {
        ProductValidator.validateResalePermission("P", ServiceStatus.DELETED);
    }

    @Test
    public void validateResalePermission_OBSOLETE() throws Exception {
        ProductValidator.validateResalePermission("P", ServiceStatus.OBSOLETE);
    }

    @Test
    public void validateResalePermission_INACTIVE() throws Exception {
        ProductValidator.validateResalePermission("P", ServiceStatus.INACTIVE);
    }

    @Test
    public void validateResalePermission_SUSPENDED() throws Exception {
        ProductValidator.validateResalePermission("P", ServiceStatus.SUSPENDED);
    }

}
