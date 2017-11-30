/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-5-28                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.authorization;

import static org.oscm.ui.authorization.Conditions.ALWAYS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * unit test for PageAuthorization
 * 
 * @author gaowenxin
 * 
 */
public class PageAuthorizationTest {

    private PageAuthorization pageAuthorization;

    private String link;
    private String id;
    private Condition condition;
    private PageAuthorizationBuilder builder;
    private Map<String, Boolean> hiddenUIElements;

    @Before
    public void setup() {
        link = "testLink";
        id = "testId";
        condition = ALWAYS;
        builder = mock(PageAuthorizationBuilder.class);
        hiddenUIElements = new HashMap<String, Boolean>();
        doReturn(hiddenUIElements).when(builder).getHiddenUIElements();
        pageAuthorization = new PageAuthorization(link, condition, id, builder);
    }

    @Test
    public void getCurrentPageLink() {
        assertEquals(link, pageAuthorization.getCurrentPageLink());
    }

    @Test
    public void isAuthorized_True() {
        hiddenUIElements.put("NewID", Boolean.TRUE);
        assertTrue(pageAuthorization.isAuthorized());
    }

    @Test
    public void isAuthorized_False() {
        hiddenUIElements.put(id, Boolean.TRUE);
        assertFalse(pageAuthorization.isAuthorized());
    }

    @Test
    public void isHidden_True() {
        hiddenUIElements.put(id, Boolean.TRUE);
        assertTrue(pageAuthorization.isHidden(id));
    }

    @Test
    public void isHidden_False() {
        hiddenUIElements.put(id, Boolean.TRUE);
        assertFalse(pageAuthorization.isHidden("NewID"));
    }
}
