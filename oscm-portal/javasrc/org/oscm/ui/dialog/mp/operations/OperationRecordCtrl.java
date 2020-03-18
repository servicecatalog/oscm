/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2014-9-17
 *
 *******************************************************************************/

package org.oscm.ui.dialog.mp.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.internal.techserviceoperationmgmt.OperationRecordService;
import org.oscm.internal.techserviceoperationmgmt.POOperationRecord;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;

/**
 * @author maoq
 * @author trebskit
 */
@ViewScoped
@ManagedBean(name = "operationRecordCtrl")
public class OperationRecordCtrl {
    private static final Logger LOGGER = LogManager.getLogger(OperationRecordCtrl.class);
    private UiDelegate ui;
    @ManagedProperty(value = "#{operationRecordModel}")
    private OperationRecordModel model;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private OperationRecordService operationRecordService;

    public boolean isFilterRenderRequired() {
        return this.userBean.isLoggedInAndAdmin() || this.userBean.isLoggedInAndAllowedToSubscribe();
    }

    public void onFilterOperationsChange(final AjaxBehaviorEvent event) {
        final Boolean myOpsOnlyNegated = BooleanUtils.negate(this.isMyOperationsOnly());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(
                    "onFilterOperationsChange(event=%s) triggered, reloading records with myOperationsFlag=%s", event,
                    myOpsOnlyNegated));
        }
        this.loadOperationRecords(myOpsOnlyNegated);
    }

    public String loadOperationRecords() {
        final Boolean myOperationsOnlyFromSession = this.isMyOperationsOnly();
        this.loadOperationRecords(myOperationsOnlyFromSession);
        return BaseBean.OUTCOME_SUCCESS;
    }

    private void loadOperationRecords(final Boolean myOperationsOnlyFromSession) {
        final String language = this.getUiDelegate().getViewLocale().getLanguage();

        final List<POOperationRecord> poOperationRecords = this.getOperationRecordService().getOperationRecords(
                myOperationsOnlyFromSession, language);

        this.model.setOperationRecords(this.initOperationRecords(poOperationRecords));
    }

    public String deleteOperations() {
        final Collection<OperationRecord> records = this.model.getOperationRecords();

        if (records.isEmpty()) {
            return BaseBean.OUTCOME_STAY_ON_PAGE;
        }

        try {

            final List<POOperationRecord> operationRecords = new ArrayList<POOperationRecord>();
            for (final OperationRecord record : records) {
                if (record.isSelected() || this.model.isSelectAll()) {
                    operationRecords.add(record.getOperation());
                }
            }

            this.getOperationRecordService().deleteOperationRecords(operationRecords);
            this.model.setSelectAll(false);

            this.addDeletedMessage();
        } catch (final SaaSApplicationException e) {
            this.getUiDelegate().handleException(e);
        }

        return BaseBean.OUTCOME_SUCCESS;
    }

    protected List<OperationRecord> initOperationRecords(final List<POOperationRecord> poOperationRecords) {

        final List<OperationRecord> operationRecords = new ArrayList<OperationRecord>();

        boolean atLeastOneSelected = false;

        OperationRecord newOpRecord;
        OperationRecord oldOpRecord;

        for (final POOperationRecord record : poOperationRecords) {
            newOpRecord = new OperationRecord(record);
            if ((oldOpRecord = this.model.getOperationRecordByTransactionId(record.getTransactionId())) != null) {
                newOpRecord.setSelected(oldOpRecord.isSelected() || this.model.isSelectAll());
                if (!atLeastOneSelected && newOpRecord.isSelected()) {
                    atLeastOneSelected = true;
                }
            }
            operationRecords.add(newOpRecord);
        }

        return operationRecords;
    }

    protected Boolean isMyOperationsOnly() {
        return Boolean.valueOf(this.sessionBean.isMyOperationsOnly());
    }

    public OperationRecordService getOperationRecordService() {
        if (this.operationRecordService == null) {
            LOGGER.warn("OperationRecordService not injected via EJB");
            this.operationRecordService = this.ui.findService(OperationRecordService.class);
        }
        return this.operationRecordService;
    }

    @EJB
    public void setOperationRecordService(final OperationRecordService opr) {
        this.operationRecordService = opr;
    }

    public OperationRecordModel getModel() {
        return this.model;
    }

    public void setModel(final OperationRecordModel opr) {
        this.model = opr;
    }

    public SessionBean getSessionBean() {
        return this.sessionBean;
    }

    public void setSessionBean(final SessionBean bean) {
        this.sessionBean = bean;
    }

    public UiDelegate getUiDelegate() {
        if (this.ui == null) {
            this.ui = new UiDelegate();
        }
        return this.ui;
    }

    public void setUiDelegate(final UiDelegate ui) {
        this.ui = ui;
    }

    public UserBean getUserBean() {
        return this.userBean;
    }

    public void setUserBean(final UserBean userBean) {
        this.userBean = userBean;
    }

    @PostConstruct
    public void postConstruct() {
        this.loadOperationRecords();
    }

    protected void addDeletedMessage() {
        JSFUtils.addMessage(null, FacesMessage.SEVERITY_INFO, BaseBean.INFO_OPERATION_DELETED, null);
    }
}
