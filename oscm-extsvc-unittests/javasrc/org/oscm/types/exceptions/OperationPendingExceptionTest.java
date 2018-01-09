/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 23, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.beans.OperationPendingExceptionBean;

/**
 * Unit tests for {@link OperationPendingException}.
 * 
 * @author barzu
 */
public class OperationPendingExceptionTest {

    @Test
    public void testOperationPendingException_Default() throws Exception {
        OperationPendingException e = new OperationPendingException();
        Assert.assertEquals("EXCEPTIONID " + e.getId() + ": " + null,
                e.getMessage());
    }

    @Test
    public void testOperationPendingException_Message() throws Exception {
        OperationPendingException e = new OperationPendingException("Message");
        Assert.assertEquals("EXCEPTIONID " + e.getId() + ": Message",
                e.getMessage());
    }

    @Test
    public void testOperationPendingException_MessageBean() throws Exception {
        OperationPendingException e = new OperationPendingException("Message",
                new OperationPendingExceptionBean());
        Assert.assertEquals("EXCEPTIONID " + e.getId() + ": Message",
                e.getMessage());
    }

    @Test
    public void testOperationPendingException_MessageBeanCause()
            throws Exception {
        OperationPendingException e = new OperationPendingException("Message",
                new OperationPendingExceptionBean(),
                new IllegalArgumentException());
        Assert.assertEquals("EXCEPTIONID " + e.getId() + ": Message",
                e.getMessage());
    }

    @Test
    public void testOperationPendingException_ReasonParams() throws Exception {
        OperationPendingException e = new OperationPendingException(
                "Activate service failed.",
                OperationPendingException.ReasonEnum.ACTIVATE_SERVICE,
                new Object[] { "admin", "sub1" });
        Assert.assertEquals(e.getReason(),
                OperationPendingException.ReasonEnum.ACTIVATE_SERVICE);
        OperationPendingExceptionBean faultInfo = e.getFaultInfo();
        Assert.assertEquals(faultInfo.getReason(),
                OperationPendingException.ReasonEnum.ACTIVATE_SERVICE);
        String message = e.getMessage();
        String expected = "EXCEPTIONID " + e.getId()
                + ": Activate service failed.";
        Assert.assertEquals(expected, message);
    }

    @Test
    public void testOperationPendingException_NullParams() throws Exception {
        OperationPendingException e = new OperationPendingException(
                "Add revoke user failed.",
                OperationPendingException.ReasonEnum.ADD_REVOKE_USER, null);
        String message = e.getMessage();
        String expected = "EXCEPTIONID " + e.getId()
                + ": Add revoke user failed.";
        Assert.assertEquals(expected, message);
    }

}
