/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.taskhandling.payloads;

import static org.oscm.test.matchers.JavaMatchers.hasToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.oscm.internal.vo.VOUserDetails;

/**
 * Test class for the class ImportUserPayload
 * 
 * @author cheld
 * 
 */
public class ImportUserPayloadTest {

    @Test
    public void getInfo() {
        ImportUserPayload payload = new ImportUserPayload();
        payload.addUser(user("user1"), null);
        assertEquals("Users: user1, ", payload.getInfo());
        // System.out.println(payload.getInfo());
    }

    private VOUserDetails user(String userId) {
        VOUserDetails user = new VOUserDetails();
        user.setUserId(userId);
        return user;
    }

    @Test
    public void testToString() {
        ImportUserPayload payload = new ImportUserPayload();
        assertThat(payload, hasToString());
    }

}
