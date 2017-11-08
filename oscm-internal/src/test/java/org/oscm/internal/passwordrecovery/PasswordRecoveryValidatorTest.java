/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-18                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.passwordrecovery;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

/**
 * @author Mao
 * 
 */
@SuppressWarnings("boxing")
public class PasswordRecoveryValidatorTest {

    @Test
    public void Constructor() {
        new PasswordRecoveryValidator();
    }

    @Test
    public void isValidInterval() {
        assertEquals(true, PasswordRecoveryValidator.isValidInterval(0));
    }

    @Test
    public void isValidInterval_Succeed() {
        Date current = new Date();
        long time = current.getTime();
        time = time - 300020;
        assertEquals(true, PasswordRecoveryValidator.isValidInterval(time));
    }

    @Test
    public void isValidInterval_Failed() {
        Date current = new Date();
        long currentDate = current.getTime();
        assertEquals(false,
                PasswordRecoveryValidator.isValidInterval(currentDate));
    }

    @Test
    public void isExpired_Succeed() {
        Date current = new Date();
        long currentDate = current.getTime();
        assertEquals(false, PasswordRecoveryValidator.isExpired(currentDate));
    }

    @Test
    public void isExpired_Failed() {
        Date current = new Date();
        long time = current.getTime();
        time = time - 86400020;
        assertEquals(true, PasswordRecoveryValidator.isExpired(time));
    }

}
