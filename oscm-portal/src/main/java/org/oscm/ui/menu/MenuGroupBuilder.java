/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: hoffmann                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.menu;

import static org.oscm.ui.authorization.Conditions.and;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.oscm.ui.authorization.Condition;
import org.oscm.ui.authorization.UIStatus;


public class MenuGroupBuilder extends AbstractMenuElement implements MenuGroup {

    private final List<MenuGroup> groups = new ArrayList<MenuGroup>();

    private final List<MenuItem> items = new ArrayList<MenuItem>();

    private boolean expanded = true;

    protected final Map<String, MenuGroupBuilder> allGroups;

    MenuGroupBuilder(String id, String labelKey,
            Map<String, MenuGroupBuilder> allGroups,
            final Condition visibility, UIStatus uiStatus) {
        super(id, labelKey, visibility, uiStatus);
        this.allGroups = allGroups;
        allGroups.put(id, this);
    }

    void toggleExpanded() {
        expanded = !expanded;
    }

    public MenuGroupBuilder addGroup(final String id,
            final Condition... visibility) {
        final MenuGroupBuilder group = new MenuGroupBuilder(id, id, allGroups,
                and(visibility), uiStatus);
        groups.add(group);
        return group;
    }

    public void addItem(final String id, final String link,
            final Condition... visibility) {
        class Item extends AbstractMenuElement implements MenuItem {
            Item() {
                super(id, id + ".title", and(visibility),
                        MenuGroupBuilder.this.uiStatus);
            }

            public String getLink() {
                return link;
            }

            public boolean isSelected() {
                return link.equals(uiStatus.getCurrentPageLink());
            }
        }
        items.add(new Item());
    }

    public List<MenuGroup> getGroups() {
        return groups;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    protected void toString(StringBuilder buffer, String indent) {
        super.toString(buffer, indent);
        indent += "    ";
        for (MenuElement e : items) {
            ((AbstractMenuElement) e).toString(buffer, indent);
        }
        for (MenuElement e : groups) {
            ((AbstractMenuElement) e).toString(buffer, indent);
        }
    }

}
