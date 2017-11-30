/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.menu;

import static org.oscm.ui.authorization.Conditions.ALWAYS;
import static org.oscm.ui.authorization.Conditions.NEVER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.authorization.UIStatus;

public class MenuGroupBuilderTest implements UIStatus {

    private String currentPage = null;

    private MenuGroupBuilder builder;

    public String getCurrentPageLink() {
        return currentPage;
    }

    public boolean isHidden(String id) {
        return false;
    }

    @Before
    public void setup() {
        Map<String, MenuGroupBuilder> all = new HashMap<String, MenuGroupBuilder>();
        builder = new MenuGroupBuilder("id", "label.key", all, ALWAYS, this);
        assertEquals("id", builder.getId());
        assertEquals("label.key", builder.getLabelKey());
        assertSame(builder, all.get("id"));
    }

    @Test
    public void testExpanded() {
        assertTrue(builder.isExpanded());
        builder.toggleExpanded();
        assertFalse(builder.isExpanded());
        builder.toggleExpanded();
        assertTrue(builder.isExpanded());
    }

    @Test
    public void testAddGroup() {
        builder.addGroup("id", ALWAYS);
        assertEquals(1, builder.getGroups().size());
        final MenuGroup g = builder.getGroups().get(0);
        assertEquals("id", g.getId());
        assertEquals("id", g.getLabelKey());
        assertTrue(g.isVisible());
    }

    @Test
    public void testAddGroupAnd() {
        builder.addGroup("id", ALWAYS, NEVER);
        final MenuGroup g = builder.getGroups().get(0);
        assertFalse(g.isVisible());
    }

    @Test
    public void testItem() {
        builder.addItem("id", "somepage.html", ALWAYS);
        assertEquals(1, builder.getItems().size());
        final MenuItem i = builder.getItems().get(0);
        assertEquals("id", i.getId());
        assertEquals("id.title", i.getLabelKey());
        assertEquals("somepage.html", i.getLink());
        assertTrue(i.isVisible());
    }

    @Test
    public void testItemAnd() {
        builder.addItem("id", "somepage.html", ALWAYS, NEVER);
        final MenuItem i = builder.getItems().get(0);
        assertFalse(i.isVisible());
    }

    @Test
    public void testItemSelection() {
        builder.addItem("id", "somepage.html");
        final MenuItem i = builder.getItems().get(0);
        assertFalse(i.isSelected());
        currentPage = "somepage.html";
        assertTrue(i.isSelected());
        currentPage = "otherpage.html";
        assertFalse(i.isSelected());
    }

}
