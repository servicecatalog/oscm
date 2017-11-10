/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.10.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.internal.vo.VOPSPSetting;

/**
 * @author afschar
 * 
 */
public class PSPSettingRow {

    private VOPSPSetting definition;
    private boolean selected;
    private boolean newDefinition = false;

    public PSPSettingRow(VOPSPSetting def) {
        definition = def;
    }

    public VOPSPSetting getDefinition() {
        return definition;
    }

    public void setDefinition(VOPSPSetting definition) {
        this.definition = definition;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isNewDefinition() {
        return newDefinition;
    }

    public void setNewDefinition(boolean newDefinition) {
        this.newDefinition = newDefinition;
    }

}
