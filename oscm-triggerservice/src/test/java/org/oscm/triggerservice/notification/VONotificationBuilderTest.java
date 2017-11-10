/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.triggerservice.notification;

import static org.oscm.test.matchers.JavaMatchers.isSerializable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.oscm.notification.vo.VONotification;

public class VONotificationBuilderTest {
    private String[] parameterNames = { "a", "b", "c" };
    private String[] parameterValues = { "a1", "b1", "c1" };

    @Test
    public void testAddParameter() {
        // given
        VONotificationBuilder builder = new VONotificationBuilder();

        // when
        for (int i = 0; i < parameterNames.length; i++) {
            builder.addParameter(parameterNames[i], parameterValues[i]);
        }
        VONotification notification = builder.build();

        // then
        assertNotNull(notification);
        assertEquals(parameterNames.length, notification.getProperties().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddParameter_duplicateName() {
        // given
        VONotificationBuilder builder = new VONotificationBuilder();
        builder.addParameter(parameterNames[0], parameterValues[0]);

        // when : add same parameter again
        builder.addParameter(parameterNames[0], parameterValues[1]);
    }

    @Test(expected = NullPointerException.class)
    public void testAddParameter_parameterNameIsNull() {
        // given
        VONotificationBuilder builder = new VONotificationBuilder();

        // when
        builder.addParameter(null, "testValue");
    }

    @Test
    public void testAddParameter_parameterValueIsNull() {
        // given
        VONotificationBuilder builder = new VONotificationBuilder();

        // when
        builder.addParameter("testName", null);
        VONotification notification = builder.build();

        // then
        assertEquals(1, notification.getProperties().size());
        assertNull(notification.getProperties().get(0).getValue());
    }

    @Test
    public void testIsSerializable() throws Exception {
        // given
        VONotificationBuilder builder = new VONotificationBuilder();
        builder.addParameter("a", "a'");

        // when
        VONotification notification = builder.build();

        // then
        assertThat(notification, isSerializable());
    }
}
