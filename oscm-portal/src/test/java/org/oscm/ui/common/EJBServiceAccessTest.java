/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Sep 12, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 12, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.vo.VOUser;

/**
 * @author tokoda
 *
 */
public class EJBServiceAccessTest {

    EJBServiceAccess ejbServiceAccess;
    VOUser wrongUserMock;
    HttpServletRequest requestMock;
    HttpServletResponse responseMock;


    @Before
    public void setup(){
        
        wrongUserMock = mock(VOUser.class);
        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        ejbServiceAccess = new EJBServiceAccess();
    }

    @Test
    public void testDoLogin_LoginFailedByUserNull() throws Exception {
        try {
            ejbServiceAccess.doLogin(null, null, requestMock, responseMock);
            fail();
        } catch (LoginException ex) {
            verify(requestMock, times(1)).getRemoteAddr();
        }
    }

    @Test
    public void testDoLogin_LoginFailedByWrongReturnCode() throws Exception {
        try {
            ejbServiceAccess.doLogin(wrongUserMock, "wrongPassword", requestMock,
                    responseMock);
            fail();
        } catch (LoginException ex) {
            verify(requestMock, times(1)).getRemoteAddr();
        }
    }

}
