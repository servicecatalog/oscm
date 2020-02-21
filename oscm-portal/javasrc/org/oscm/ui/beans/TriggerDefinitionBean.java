/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Author: Brandstetter
 *
 *  Creation Date: 14.02.2012
 *
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;

/**
 * Backing bean for trigger definition related actions
 *
 */
@ViewScoped
@ManagedBean(name = "triggerDefinitionBean")
public class TriggerDefinitionBean extends BaseBean implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(TriggerDefinitionBean.class);
    private static final long serialVersionUID = 7284668598929521354L;
    private List<VOTriggerDefinition> triggerDefinitionList = null;
    private VOTriggerDefinition selectedTriggerDefinition;
    private List<TriggerType> allowedTriggerTypes;
    private boolean isSuspendTrigger = true;

    @ManagedProperty(value = "#{menuBean}")
    private MenuBean menuBean;

    // -------------------------------------------------------------------------
    public boolean isNewTriggerDefinition() {
        boolean isNew = true;
        if ((this.selectedTriggerDefinition != null) && (this.selectedTriggerDefinition.getKey() > 0)) {
            isNew = false;
        }
        return isNew;
    }

    // -------------------------------------------------------------------------
    private VOTriggerDefinition getNewTriggerDefinition() {
        final VOTriggerDefinition newTriggerDefinition = new VOTriggerDefinition();
        newTriggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        return newTriggerDefinition;
    }

    // -------------------------------------------------------------------------
    public VOTriggerDefinition getSelectedTriggerDefinition() {
        if (this.selectedTriggerDefinition == null) {
            return this.selectedTriggerDefinition = this.getNewTriggerDefinition();
        }
        return this.selectedTriggerDefinition;
    }

    public void setSelectedTriggerDefinition(final VOTriggerDefinition triggerDefinition) {
        this.selectedTriggerDefinition = triggerDefinition;
    }

    // -------------------------------------------------------------------------
    public void setSelectedTriggerDefinitionId(final long triggerDefinitionId) {
        final List<VOTriggerDefinition> list = this.getTriggerDefinitionList();
        for (final VOTriggerDefinition voTriggerDefinition : list) {
            if (voTriggerDefinition.getKey() == triggerDefinitionId) {
                this.selectedTriggerDefinition = voTriggerDefinition;
                break;
            }
        }
        this.setSuspendTrigger(this.selectedTriggerDefinition.getType().isSuspendProcess());
    }

    // -------------------------------------------------------------------------
    public long getSelectedTriggerDefinitionId() {
        if (this.selectedTriggerDefinition == null) {
            return -1;
        }
        return this.selectedTriggerDefinition.getKey();
    }

    // -------------------------------------------------------------------------
    /**
     * Get the existing trigger definitions for the current organization. If not
     * initialized, the values will be read from server.
     *
     * @return the list of {@link VOTriggerDefinition}s
     */
    public List<VOTriggerDefinition> getTriggerDefinitionList() {

        if (this.triggerDefinitionList == null && (this.isLoggedInAndAdmin() || this.isLoggedInAndPlatformOperator())) {
            this.triggerDefinitionList = this.getTriggerDefinitionService().getTriggerDefinitions();
        }

        return this.triggerDefinitionList;
    }

    // -------------------------------------------------------------------------
    public SelectItem[] getTriggerTypeValues() {
        final SelectItem[] items = new SelectItem[this.getTriggerTypes().size()];
        int i = 0;
        for (final TriggerType tType : this.getTriggerTypes()) {
            items[i++] = new SelectItem(tType, JSFUtils.getText("TriggerType." + tType + ".enum", null));
        }
        return items;
    }

    // -------------------------------------------------------------------------
    public SelectItem[] getTriggerTargetTypeValues() {
        final SelectItem[] items = new SelectItem[TriggerTargetType.values().length];
        int i = 0;
        for (final TriggerTargetType targetType : TriggerTargetType.values()) {
            items[i++] = new SelectItem(targetType, JSFUtils.getText("TriggerTargetType." + targetType, null));
        }
        return items;
    }

    // -------------------------------------------------------------------------
    public String addTriggerDefinition() {
        this.selectedTriggerDefinition = this.getNewTriggerDefinition();
        return null;
    }

    // -------------------------------------------------------------------------
    public void editTriggerDefinition() {
        this.setSuspendTrigger(this.selectedTriggerDefinition.getType().isSuspendProcess());
    }

    /*
     * invoke navigation rule for classic portal
     */
    public void newTriggerDefinition() {
        this.selectedTriggerDefinition = this.getNewTriggerDefinition();
    }

    // -------------------------------------------------------------------------
    public String save() throws Exception {

        try {
            if (this.isNewTriggerDefinition()) {
                this.getTriggerDefinitionService().createTriggerDefinition(this.selectedTriggerDefinition);
                this.addMessage(null, FacesMessage.SEVERITY_INFO, INFO_TRIGGER_DEFINITION_CREATED);
                if (this.triggerDefinitionList == null || this.triggerDefinitionList.isEmpty()) {
                    this.resetMenuBean();
                }
            } else {
                this.getTriggerDefinitionService().updateTriggerDefinition(this.selectedTriggerDefinition);
                this.addMessage(null, FacesMessage.SEVERITY_INFO, INFO_TRIGGER_DEFINITION_SAVED);
            }
        } catch (final ConcurrentModificationException exp) {
            LOGGER.error("save(...), an ConcurrentModificationException error occurred", exp);
            ExceptionHandler.execute(exp);
        } catch (final ObjectNotFoundException exp) {
            LOGGER.error("save(...), an ObjectNotFoundException error occurred", exp);
            if (exp.getDomainObjectClassEnum().equals(ClassEnum.TRIGGER_DEFINITION)) {
                ExceptionHandler.execute(new ConcurrentModificationException());
            } else {
                throw exp;
            }
        } finally {
            this.selectedTriggerDefinition = null;
            this.triggerDefinitionList = null;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    public String delete() throws Exception {

        try {
            this.getTriggerDefinitionService().deleteTriggerDefinition(this.selectedTriggerDefinition);
            this.addMessage(null, FacesMessage.SEVERITY_INFO, INFO_TRIGGER_DEFINITION_DELETED);
            this.notifyProcessBean();
            if (this.triggerDefinitionList != null && this.triggerDefinitionList.size() == 1) {
                this.resetMenuBean();
            }
        } catch (final ObjectNotFoundException exp) {
            LOGGER.error("save(...), an ObjectNotFoundException error occurred", exp);
            if (exp.getDomainObjectClassEnum().equals(ClassEnum.TRIGGER_DEFINITION)) {
                ExceptionHandler.execute(new ConcurrentModificationException());
                this.selectedTriggerDefinition = null;
            } else {
                throw exp;
            }
        } catch (final ConcurrentModificationException exp) {
            LOGGER.error("save(...), an ConcurrentModificationException error occurred", exp);
            ExceptionHandler.execute(exp);
            this.selectedTriggerDefinition = null;
        } finally {
            this.triggerDefinitionList = null;
        }

        this.selectedTriggerDefinition = null;

        return null;
    }

    // -------------------------------------------------------------------------
    public void cancel() {
        this.selectedTriggerDefinition = null;
    }

    private void notifyProcessBean() {
        this.ui.findTriggerProcessBean().reLoadTriggerProcessList(false);
    }

    // -------------------------------------------------------------------------
    public boolean isAdministrator() {
        return this.isLoggedInAndAdmin();
    }

    // -------------------------------------------------------------------------
    public String getModalTitle() {
        String action;
        if (this.isNewTriggerDefinition()) {
            action = "define";
        } else {
            action = "edit";
        }
        return this.getText("marketplace.account.processes.manageTrigger." + action + ".title", null);
    }

    public void processValueChange(final ValueChangeEvent event) {
        final TriggerType triggerType = (TriggerType) event.getNewValue();
        this.setSuspendTrigger(triggerType != null && triggerType.isSuspendProcess());
    }

    // -------------------------------------------------------------------------
    private List<TriggerType> getTriggerTypes() {
        if (this.allowedTriggerTypes == null) {
            this.allowedTriggerTypes = this.getTriggerDefinitionService().getTriggerTypes();
        }
        return this.allowedTriggerTypes;
    }

    public boolean isSuspendTrigger() {
        return this.isSuspendTrigger;
    }

    public void setSuspendTrigger(final boolean isSuspendTrigger) {
        if (!isSuspendTrigger) {
            this.selectedTriggerDefinition.setSuspendProcess(isSuspendTrigger);
        }
        this.isSuspendTrigger = isSuspendTrigger;
    }

    public MenuBean getMenuBean() {
        return this.menuBean;
    }

    public void setMenuBean(final MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    private void resetMenuBean() {
        this.getMenuBean().resetMenuVisibility();
    }
}
