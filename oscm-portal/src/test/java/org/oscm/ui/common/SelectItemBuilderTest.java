/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Oct 11, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertEquals;

import javax.faces.model.SelectItem;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.stubs.UiDelegateStub;

/**
 * Unit tests for {@link SelectItemBuilder}.
 * 
 * @author barzu
 */
public class SelectItemBuilderTest {

    private SelectItemBuilder builder;

    @Before
    public void setup() {
        builder = new SelectItemBuilder(new UiDelegateStub());
    }

    @Test
    public void pleaseSelect() {
        // when
        SelectItem item = builder.pleaseSelect(Long.valueOf(0L));

        // then
        assertEquals(0L, ((Long) item.getValue()).longValue());
        assertEquals("common.pleaseSelect", item.getLabel());
    }

}
