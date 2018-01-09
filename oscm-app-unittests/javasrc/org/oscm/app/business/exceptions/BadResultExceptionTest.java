/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.business.exceptions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.provisioning.data.UserResult;

/**
 * Unit tests for {@link BadResultException}
 * 
 * @author hoffmann
 * 
 */
public class BadResultExceptionTest {

    @Test
    public void testGetResult() {
        final BadResultException e = new BadResultException("This is %s!",
                "crap");
        final UserResult result = e.getResult(UserResult.class);
        assertEquals(1, result.getRc());
        assertEquals("This is crap!", result.getDesc());
    }

    @Test(expected = RuntimeException.class)
    public void testAbstractClass() {
        new BadResultException("test").getResult(AbstractBaseResult.class);
    }

}
