/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.menu;

import static org.oscm.ui.authorization.Conditions.ALWAYS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.oscm.ui.authorization.Condition;
import org.oscm.ui.authorization.UIStatus;


public class AbstractMenuElementTest implements UIStatus {

    private boolean hidden = false;

    public String getCurrentPageLink() {
        fail();
        return null;
    }

    public boolean isHidden(String id) {
        return hidden;
    }

    @Test
    public void testIdAndLabelKey() {
        final AbstractMenuElement element = new AbstractMenuElement("id",
                "label.key", ALWAYS, this) {
        };
        assertEquals("id", element.getId());
        assertEquals("label.key", element.getLabelKey());
    }

    @Test
    public void testVisibility() {
        class Visibility implements Condition {
            boolean visible = false;

            public boolean eval() {
                return visible;
            }
        }
        Visibility cond = new Visibility();

        final AbstractMenuElement element = new AbstractMenuElement("id",
                "label.key", cond, this) {
        };

        assertFalse(element.isVisible());
        cond.visible = true;
        assertTrue(element.isVisible());
    }

    @Test
    public void testHidden() {
        final AbstractMenuElement element = new AbstractMenuElement("id",
                "label.key", ALWAYS, this) {
        };

        assertTrue(element.isVisible());
        hidden = true;
        assertFalse(element.isVisible());
    }

}
