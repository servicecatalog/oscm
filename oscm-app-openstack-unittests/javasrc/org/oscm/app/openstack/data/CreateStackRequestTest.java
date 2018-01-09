/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 05.08.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.data;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author afschar
 * 
 */
public class CreateStackRequestTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructor_IllegalArgument() {
        // given

        // when
        new CreateStackRequest(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_IllegalArgument() {
        // given

        // when
        new CreateStackRequest("x").put("x", new Integer(8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void put_NullKey() {
        // given

        // when
        new CreateStackRequest("x").put(null, null);
    }

    @Test
    public void constructor() {
        // given
        String name = "NAME" + System.currentTimeMillis();

        // when
        CreateStackRequest request = new CreateStackRequest(name);

        // then
        assertTrue(request.getJSON().indexOf(name) > -1);
    }

    @Test
    public void withParameter() {
        // given
        String key = "KEY" + System.currentTimeMillis();
        String value = "VALUE" + System.currentTimeMillis();

        // when
        AbstractStackRequest request = new CreateStackRequest("x")
                .withParameter(key, value);

        // then
        assertTrue(request.getJSON().indexOf(key) > -1);
        assertTrue(request.getJSON().indexOf(value) > -1);
    }

    @Test
    public void withTemplateUrl() {
        // given
        String value = "VALUE" + System.currentTimeMillis();

        // when
        AbstractStackRequest request = new CreateStackRequest("x")
                .withTemplateUrl(value);

        // then
        assertTrue(request.getJSON().indexOf(value) > -1);
    }
}
