/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.menu;

import static org.oscm.ui.authorization.Conditions.NEVER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.authorization.UIStatus;

public class MenuBuilderTest implements UIStatus {

    private MenuBuilder builder;
    private MenuGroupBuilder group3;

    public String getCurrentPageLink() {
        return "somewhere.html";
    }

    public boolean isHidden(String id) {
        return false;
    }

    @Before
    public void setup() {
        builder = new MenuBuilder(this);
        builder.addGroup("group1");
        group3 = builder.addGroup("group2").addGroup("group3");
    }

    @Test
    public void testToggleGroup() {
        assertTrue(group3.isExpanded());
        builder.toggleGroupExpanded("group3");
        assertFalse(group3.isExpanded());
        builder.toggleGroupExpanded("group3");
        assertTrue(group3.isExpanded());
    }

    @Test(expected = NoSuchElementException.class)
    public void testToggleGroupInvalidId() {
        builder.toggleGroupExpanded("otherid");
    }

    @Test
    public void testToString() {
        String expected = "+-- ROOT visible=true\n"
                + "    +-- item1 visible=false\n"
                + "    +-- group1 visible=true\n"
                + "    +-- group2 visible=true\n"
                + "        +-- group3 visible=true\n";

        builder.addItem("item1", "item.html", NEVER);
        assertEquals(expected, builder.toString());
    }

}
