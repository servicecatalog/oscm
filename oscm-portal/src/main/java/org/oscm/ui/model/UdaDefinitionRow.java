/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *  Completion Time: 21.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author weiser
 * 
 */
public class UdaDefinitionRow {

    private VOUdaDefinition definition;
    private boolean selected;
    private boolean newDefinition = false;

    public UdaDefinitionRow(VOUdaDefinition def) {
        definition = def;
    }

    public VOUdaDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(VOUdaDefinition definition) {
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
