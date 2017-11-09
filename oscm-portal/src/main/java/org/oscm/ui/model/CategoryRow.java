/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.internal.vo.VOCategory;

public class CategoryRow {

    private VOCategory category;
    private boolean selected;

    public CategoryRow(VOCategory cat) {
        category = cat;
    }

    public VOCategory getCategory() {
        return category;
    }

    public void setCategory(VOCategory cat) {
        this.category = cat;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isNewCategory() {
        return category.getKey() <= 0;
    }

    public String getDisplayName() {
        return Category.getDisplayName(category);
    }
}
