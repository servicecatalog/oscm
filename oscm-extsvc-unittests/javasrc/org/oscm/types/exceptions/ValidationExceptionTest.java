/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.types.exceptions;

import org.junit.Assert;

import org.junit.Test;

public class ValidationExceptionTest {

    @Test
    public void testValidationException_ParamsToString1() throws Exception {
        ValidationException e = new ValidationException(
                ValidationException.ReasonEnum.BOOLEAN, "admin", new Object[] {
                        new Boolean(false), "wrong" });
        String message = e.getMessage();
        String expected = "EXCEPTIONID "
                + e.getId()
                + ": Validation failed for member admin with reason BOOLEAN (parameters=[false, wrong]).";
        Assert.assertEquals(expected, message);
    }

    @Test
    public void testValidationException_ParamsToString2() throws Exception {
        ValidationException e = new ValidationException(
                ValidationException.ReasonEnum.BOOLEAN, null, new Object[] {
                        new Boolean(false), "wrong" });
        String message = e.getMessage();
        String expected = "EXCEPTIONID "
                + e.getId()
                + ": Validation failed with reason BOOLEAN (parameters=[false, wrong]).";
        Assert.assertEquals(expected, message);
    }

    @Test
    public void testValidationException_ParamsToString3() throws Exception {
        ValidationException e = new ValidationException(
                ValidationException.ReasonEnum.BOOLEAN, null, null);
        String message = e.getMessage();
        String expected = "EXCEPTIONID " + e.getId()
                + ": Validation failed with reason BOOLEAN.";
        Assert.assertEquals(expected, message);
    }

}
