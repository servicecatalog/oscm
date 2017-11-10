/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.oscm.reportingservice.business.Formatting;

public class FormattingTest {
    @Test
    public void nameAndId() {
        // when
        String displayName = Formatting.nameAndId("str1", "str2");

        // then
        assertEquals("str1 (str2)", displayName);
    }

    @Test
    public void nameAndId_nameIsNull() {
        // when
        String displayName = Formatting.nameAndId(null, "str2");

        // then
        assertEquals("str2", displayName);
    }
}
