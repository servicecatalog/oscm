/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

public class AuditLogOperation implements Comparable<AuditLogOperation> {

	String operationId;

	String operationName;

	boolean selected = false;
	
	public AuditLogOperation(String operationId, String operationName){
		this.operationId = operationId;
		this.operationName = operationName;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getOperationId() {
		return operationId;
	}

        @Override
        public int compareTo(AuditLogOperation o) {
            return this.getOperationName().compareTo(o.getOperationName());
        }
}
