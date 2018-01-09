/*
 *  Copyright FUJITSU LIMITED 2018
 */

package org.oscm.integrationhelper;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import javax.servlet.http.HttpSession;

import org.junit.Test;

public class BssClientTest {

    @Test
    public void logoutUser_bssidNull() throws Exception {
        // given
        HttpSession session = mock(HttpSession.class);
        doReturn("subid").when(session).getAttribute(eq(Constants.SUB_KEY));
        doReturn(null).when(session).getAttribute(Constants.CM_ID);

        // when
        String string = BssClient.logoutUser(session);

        // then
        assertNull(string);
    }

    @Test
    public void logoutUser_subidNull() throws Exception {
        // given
        HttpSession session = mock(HttpSession.class);
        doReturn(null).when(session).getAttribute(eq(Constants.SUB_KEY));
        doReturn("bssid").when(session).getAttribute(Constants.CM_ID);

        // when
        String string = BssClient.logoutUser(session);

        // then
        assertNull(string);
    }

    @Test
    public void logoutUser_noBssSession() throws Exception {
        // given
        HttpSession session = mock(HttpSession.class);
        doReturn(null).when(session).getAttribute(eq(Constants.SUB_KEY));
        doReturn(null).when(session).getAttribute(Constants.CM_ID);

        // when
        String string = BssClient.logoutUser(session);

        // then
        assertNull(string);
    }

}
