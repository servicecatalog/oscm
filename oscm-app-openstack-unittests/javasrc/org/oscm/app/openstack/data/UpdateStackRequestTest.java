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
public class UpdateStackRequestTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructor_IllegalArgument() {
        // given

        // when
        new UpdateStackRequest(null);
    }

    @Test
    public void constructor() {
        // given
        String name = "NAME" + System.currentTimeMillis();

        // when
        UpdateStackRequest request = new UpdateStackRequest(name);

        // then
        assertTrue(request.getJSON().indexOf(name) < 0);
    }
}
