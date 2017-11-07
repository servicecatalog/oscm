/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 02.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Marketplace;

/**
 * @author weiser
 * 
 */
public class SendMailControlTest {

    @Before
    public void setup() {
        SendMailControl.clear();
    }

    @After
    public void tearDown() {
        SendMailControl.clear();
    }

    @Test
    public void isSendMail() {
        assertTrue(SendMailControl.isSendMail());
    }

    @Test
    public void isSendMail_TrueSet() {
        SendMailControl.setSendMail(Boolean.TRUE);

        assertTrue(SendMailControl.isSendMail());
    }

    @Test
    public void isSendMail_FalseSet() {
        SendMailControl.setSendMail(Boolean.FALSE);

        assertFalse(SendMailControl.isSendMail());
    }

    @Test
    public void isSendMail_Reset() {
        SendMailControl.setSendMail(Boolean.FALSE);
        SendMailControl.setSendMail(null);

        assertTrue(SendMailControl.isSendMail());
    }

    @Test
    public void getPassword_Initial() {
        assertEquals(null, SendMailControl.getPassword());
    }

    @Test
    public void setMailData() {
        String pwd = "password";
        Marketplace mp = new Marketplace();
        SendMailControl.setMailData(pwd, mp);

        assertEquals(pwd, SendMailControl.getPassword());
        assertSame(mp, SendMailControl.getMarketplace());
    }

    public void clear() {
        SendMailControl.setSendMail(Boolean.FALSE);
        SendMailControl.setMailData("pwd", new Marketplace());

        SendMailControl.clear();

        assertTrue(SendMailControl.isSendMail());
        assertNull(SendMailControl.getPassword());
        assertNull(SendMailControl.getMarketplace());
    }
}
