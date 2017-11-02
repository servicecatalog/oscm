/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.operations;

import org.oscm.internal.techserviceoperationmgmt.POOperationRecord;

/**
 * @author mao
 * 
 */
public class OperationRecord {

    private boolean selected = false;

    private POOperationRecord operation;

    public OperationRecord(POOperationRecord operation) {
        this.operation = operation;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public POOperationRecord getOperation() {
        return operation;
    }

    public void setOperation(POOperationRecord operation) {
        this.operation = operation;
    }

}
