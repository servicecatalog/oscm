/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReportEngineUrlTest {

    @Test
    public void testGetPlaceholder() {
        assertEquals("${key}", ReportEngineUrl.getPlaceholder("key"));
    }

    @Test
    public void testReplace() {
        assertEquals("Hello World!",
                ReportEngineUrl.replace("Hello ${name}!", "name", "World"));
    }

    @Test
    public void ctor() {
        new ReportEngineUrl();
    }

}
