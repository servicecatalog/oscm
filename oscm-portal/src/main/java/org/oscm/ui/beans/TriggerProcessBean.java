/**
 * ****************************************************************************
 * <p/>
 *  Copyright FUJITSU LIMITED 2017                             * <p/>
 * Author: pock
 * <p/>
 * Creation Date: 22.06.2010
 * <p/>
 * *****************************************************************************
 */

package org.oscm.ui.beans;

import org.oscm.ui.model.TriggerProcess;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.internal.vo.VOTriggerProcess;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for trigger process related actions
 *
 */
@ViewScoped
@ManagedBean(name = "triggerProcessBean")
public class TriggerProcessBean extends BaseBean implements Serializable {
    private static final long serialVersionUID = 6221152754432353523L;
    private List<TriggerProcess> triggerProcessList;
    private List<VOTriggerDefinition> triggerDefinitionList = null;
    private boolean selectAll = false;

    public boolean isSelectAll() {
        return selectAll;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
        if (this.triggerProcessList != null) {
            final boolean isSelectAll = this.isSelectAll();
            CollectionUtils.forAllDo(this.triggerProcessList, new Closure() {
                @Override
                public void execute(Object tp) {
                    ((TriggerProcess) tp).setSelected(isSelectAll);
                }
            });
        }
    }

    public SessionBean getSessionBean() {
        return this.ui.findSessionBean();
    }

    public boolean isMyProcessesOnly() {
        return !this.getUserFromSession().isOrganizationAdmin() || this.isMyProcessesOnlyFromSession();
    }

    public void setMyProcessesOnly(boolean myProcessesOnly) {
        this.getSessionBean().setMyProcessesOnly(myProcessesOnly);
    }

    public List<TriggerProcess> getTriggerProcessList() {
        if (triggerProcessList == null) {
            this.reLoadTriggerProcessList(false);
        }
        return this.triggerProcessList;
    }

    boolean isMyProcessesOnlyFromSession() {
        return getSessionBean().isMyProcessesOnly();
    }

    public void setTriggerProcessList(List<TriggerProcess> triggerProcessList) {
        this.triggerProcessList = triggerProcessList;
    }

    public boolean isButtonDisable() {
        if (this.isSelectAll()) {
            return false;
        } else if (triggerProcessList != null) {
            for (TriggerProcess triggerProcess : triggerProcessList) {
                if (triggerProcess.isSelected()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void reLoadTriggerProcessList(boolean restoreSelections) {
        Vo2ModelMapper<VOTriggerProcess, TriggerProcess> mapper = new Vo2ModelMapper<VOTriggerProcess, TriggerProcess>() {
            @Override
            public TriggerProcess createModel(final VOTriggerProcess vo) {
                return new TriggerProcess(vo);
            }
        };

        final List<TriggerProcess> existedTriggerProcesses = this.triggerProcessList;
        if (this.isMyProcessesOnly()) {
            this.triggerProcessList = mapper
                    .map(getTriggerService().getAllActions());
        } else {
            this.triggerProcessList = mapper.map(getTriggerService()
                    .getAllActionsForOrganization());
        }

        if (restoreSelections) {
            mergeTriggerProcessRecord(triggerProcessList,
                    existedTriggerProcesses);
        }
    }

    void mergeTriggerProcessRecord(List<TriggerProcess> newRecords,
            List<TriggerProcess> existedRecords) {
        if (newRecords == null || newRecords.isEmpty()
                || existedRecords == null || existedRecords.isEmpty()) {
            return;
        }
        for (TriggerProcess newRecord : newRecords) {
            for (TriggerProcess existedRecord : existedRecords) {
                if (newRecord.getVOTriggerProcess().getKey() == existedRecord
                        .getVOTriggerProcess().getKey()) {
                    newRecord.setSelected(existedRecord.isSelected());
                }
            }
        }
    }

    /**
     * Empty action.
     *
     * @return the logical outcome success.
     */
    public String apply() {
        return OUTCOME_SUCCESS;
    }

    /**
     * Action to cancel the selected trigger processes.
     *
     * @return the logical outcome success.
     * @throws ObjectNotFoundException
     *             Thrown if one TriggerProcess for a given key cannot be found.
     * @throws OperationNotPermittedException
     *             Thrown if the TriggerProcess for the given key doesn't belong
     *             to the current organization.
     * @throws TriggerProcessStatusException
     *             Thrown if the TriggerProcess for the given key hasn't the
     *             status WAITING_FOR_APPROVAL.
     */
    public String cancelTriggerProcesses() throws SaaSApplicationException {

        if (triggerProcessList != null) {
            List<Long> keys = new ArrayList<Long>();
            for (TriggerProcess triggerProcess : triggerProcessList) {
                if (triggerProcess.isSelected()) {
                    keys.add(Long.valueOf(triggerProcess.getKey()));
                }
            }
            try {
                getTriggerService().cancelActions(keys, null);
            } catch (ObjectNotFoundException e) {
                triggerProcessList = null;
                throw new ConcurrentModificationException();
            }
            triggerProcessList = null;
            if (keys.size() > 0) {
                addMessage(null, FacesMessage.SEVERITY_INFO,
                        TRIGGER_PROCESS_CANCELED);
            }
        }

        return OUTCOME_REFRESH;
    }

    /**
     * Action to delete the selected trigger processes.
     *
     * @return the logical outcome success.
     * @throws ObjectNotFoundException
     *             Thrown if one TriggerProcess for a given key cannot be found.
     * @throws OperationNotPermittedException
     *             Thrown if the TriggerProcess for the given key doesn't belong
     *             to the current organization.
     * @throws TriggerProcessStatusException
     *             Thrown if the TriggerProcess for the given key hasn't the
     *             status WAITING_FOR_APPROVAL.
     */
    public String deleteTriggerProcesses() throws SaaSApplicationException {

        if (triggerProcessList != null) {
            List<Long> keys = new ArrayList<Long>();
            for (TriggerProcess triggerProcess : triggerProcessList) {
                if (triggerProcess.isSelected()) {
                    keys.add(Long.valueOf(triggerProcess.getKey()));
                }
            }
            try {
                getTriggerService().deleteActions(keys);
            } catch (ObjectNotFoundException e) {
                triggerProcessList = null;
                throw new ConcurrentModificationException();
            }
            triggerProcessList = null;
            if (keys.size() > 0) {
                addMessage(null, FacesMessage.SEVERITY_INFO,
                        TRIGGER_PROCESS_DELETED);
            }
        }

        return OUTCOME_REFRESH;
    }

    /**
     * Action to approve the selected trigger processes.
     *
     * @return the logical outcome success.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     */
    public String approveTriggerProcesses() throws SaaSApplicationException,
    OperationNotPermittedException {

        if (triggerProcessList != null) {
            for (TriggerProcess triggerProcess : triggerProcessList) {
                if (triggerProcess.isSelected()) {
                    getTriggerService().approveAction(triggerProcess.getKey());
                }
            }
            triggerProcessList = null;
        }

        return OUTCOME_SUCCESS;
    }

    /**
     * Check if a trigger definition for type
     * {@link TriggerType#ADD_REVOKE_USER} exists configured to suspend.
     *
     * @return <code>true</code> if a suspending trigger exists otherwise
     *         <code>false</code>.
     */
    public boolean isSuspendAddRevokeUsers() {
        List<VOTriggerDefinition> list = getTriggerDefinitionList();
        for (VOTriggerDefinition td : list) {
            if (td.getType() == TriggerType.ADD_REVOKE_USER
                    && td.isSuspendProcess()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the existing trigger definitions for the current organizations. If
     * not initialized, the values will be read from server.
     *
     * @return the list of {@link VOTriggerDefinition}s
     */
    private List<VOTriggerDefinition> getTriggerDefinitionList() {
        if (triggerDefinitionList == null) {
            triggerDefinitionList = getTriggerService().getAllDefinitions();
        }
        return triggerDefinitionList;
    }

    /*
     * value change listener for filterProcessesChooser checkbox
     */
    public void filteringChanged(ValueChangeEvent event) {
        this.setMyProcessesOnly(((Boolean) event.getNewValue()).booleanValue());
        this.reLoadTriggerProcessList(true);
    }

}
