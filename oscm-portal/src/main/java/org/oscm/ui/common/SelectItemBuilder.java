/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public class SelectItemBuilder {

    private UiDelegate ui;

    public SelectItemBuilder(UiDelegate ui) {
        this.ui = ui;
    }

    public SelectItem buildSelectItem(Enum<?> enumvalue, String enumerationType) {
        return new SelectItem(enumvalue, ui.getText(enumerationType + "."
                + enumvalue));
    }

    public SelectItem pleaseSelect(Object value) {
        return new SelectItem(value, ui.getText("common.pleaseSelect"));
    }

    public List<SelectItem> buildSelectItems(List<? extends Enum<?>> enums,
            String enumerationType) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Enum<?> e : enums) {
            selectItems.add(buildSelectItem(e, enumerationType));
        }
        return selectItems;
    }

    public static List<SelectItem> buildSelectItems(List<Integer> range) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Integer i : range) {
            selectItems.add(new SelectItem("" + i, "" + i));
        }
        return selectItems;
    }

}
