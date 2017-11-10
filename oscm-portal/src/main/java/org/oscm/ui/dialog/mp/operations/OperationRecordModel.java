/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2014-9-17
 *
 *******************************************************************************/

package org.oscm.ui.dialog.mp.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.oscm.internal.techserviceoperationmgmt.POOperationRecord;

/**
 * @author maoq
 * @author trebskit
 *
 */
@ViewScoped
@ManagedBean(name = "operationRecordModel")
public class OperationRecordModel {
    private boolean selectAll = false;
    /**
     * Internally kept as {@link Map} in order to speed up lookup operations
     */
    private Map<String, OperationRecord> operationRecordsMap;

    public boolean isSelectAll() {
        return this.selectAll;
    }

    public void setSelectAll(final boolean selectAll) {
        this.selectAll = selectAll;
    }

    public Collection<OperationRecord> getOperationRecords() {
        return this.operationRecordsMap != null ? this.operationRecordsMap.values() : new ArrayList<OperationRecord>();
    }

    public void setOperationRecords(final Collection<OperationRecord> operationRecords) {
        if (CollectionUtils.isEmpty(operationRecords)) {
            return;
        }
        this.operationRecordsMap = new HashMap<>(operationRecords.size());
        for (final OperationRecord or : operationRecords) {
            this.operationRecordsMap.put(or.getOperation().getTransactionId(), or);
        }
    }

    /**
     * Method calculates if button should be disabled or enabled.
     *
     * @return true if button should be disabled
     */
    public boolean isButtonDisabled() {
        if (this.selectAll) {
            return false;
        }
        if (this.operationRecordsMap == null) {
            return true;
        }

        boolean disabled = true;

        final Collection<OperationRecord> values = this.operationRecordsMap.values();
        for (final OperationRecord or : values) {
            if (or.isSelected()) {
                disabled = false;
                break;
            }
        }

        return disabled;
    }

    /**
     * Lookup for {@link OperationRecord} via {@code trId} as specified by {@link OperationRecord#getOperation()} >
     * {@link POOperationRecord#getTransactionId()}
     *
     * @param trId
     * @return an {@link OperationRecord} or null if no records available or the record by given {@code trId} not found
     */
    public final OperationRecord getOperationRecordByTransactionId(final String trId) {
        if (StringUtils.isEmpty(trId)) {
            return null;
        }
        return this.operationRecordsMap != null ? this.operationRecordsMap.get(trId) : null;
    }
}
