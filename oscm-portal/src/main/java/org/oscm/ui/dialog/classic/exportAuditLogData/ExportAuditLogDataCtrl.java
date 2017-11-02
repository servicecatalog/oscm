/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-4-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.exportAuditLogData;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.DateConverter;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.operator.BaseOperatorBean;
import org.oscm.ui.model.AuditLogOperation;
import org.oscm.ui.validator.DateFromToValidator;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.exception.AuditLogTooManyRowsException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * @author Qiu
 * 
 */
@ViewScoped
@ManagedBean(name="exportAuditLogDataCtrl")
public class ExportAuditLogDataCtrl extends BaseOperatorBean {

    static Comparator<AuditLogOperation> logOperationComparator = new Comparator<AuditLogOperation>() {
        public int compare(AuditLogOperation op1, AuditLogOperation op2) {
            return op1.getOperationName().compareTo(op2.getOperationName());
        }
    };

    private static final long serialVersionUID = -264958393257798134L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ExportAuditLogDataCtrl.class);
    
    @ManagedProperty(value="#{exportAuditLogDataModel}")
    private ExportAuditLogDataModel model;
    
    private static final String OPERATIONS_ALL = "OPERATIONS_ALL";

    private boolean allServicesSelected = false;
    private DateFromToValidator validator = new DateFromToValidator();

    public ExportAuditLogDataModel getModel() {
        return model;
    }

    public void setModel(ExportAuditLogDataModel model) {
        this.model = model;
    }

    public void setValidator(DateFromToValidator validator) {
        this.validator = validator;
    }

    public String getInitialize() {

        ExportAuditLogDataModel m = getModel();
        if (!m.isInitialized()) {
            initializeSelectableOperationGroups();
            initializeSelectableOperations();
            m.setInitialized(true);
        }

        return "";
    }

    private void initializeSelectableOperationGroups() {
        List<SelectItem> items = new ArrayList<SelectItem>();
        Map<String, String> operationRoleEntryMap = getOperatorService()
                .getAvailableAuditLogOperationGroups();
        if (operationRoleEntryMap != null && !operationRoleEntryMap.isEmpty()) {
            for (String key : operationRoleEntryMap.keySet()) {
                items.add(new SelectItem(key, ui.getText(key)));
            }
            items.add(new SelectItem(OPERATIONS_ALL, ui.getText(OPERATIONS_ALL)));
        }
        model.setOperationGroups(operationRoleEntryMap);
        model.setAvailableSelectGroups(items);
    }

    private void initializeSelectableOperations() {
        model.setOperations(getOperatorService()
                .getAvailableAuditLogOperations());
    }

    /**
     * Retrieve the audit log data for the specified start and end date and the
     * entity type.
     * 
     * @return the logical outcome.
     * @throws ValidationException
     */
    public String getAuditLogData() throws ValidationException {

        if (model.getFromDate() == null || model.getToDate() == null
                || model.getAvailableOperations() == null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_EXPORT_AUDIT_LOG_DATA);
            logger.logError(
                    LogMessageIdentifier.ERROR_GET_USER_OPERATION_LOG_FAILED_WITH_WRONG_PARAMETER,
                    String.valueOf(model.getFromDate()),
                    String.valueOf(model.getToDate()),
                    String.valueOf(model.getAvailableOperations()));

            return OUTCOME_ERROR;
        }

        long from = DateConverter.getBeginningOfDayInCurrentTimeZone(model
                .getFromDate().getTime());
        long to = DateConverter.getEndOfDayInCurrentTimeZone(model.getToDate()
                .getTime());

        List<String> operationIds = new ArrayList<String>();
        for (AuditLogOperation operation : model.getAvailableOperations()) {
            if (operation.isSelected() == true) {
                operationIds.add(operation.getOperationId());
            }
        }

        try {
            model.setAuditLogData(getOperatorService().getUserOperationLog(
                    operationIds, from, to));
        } catch (AuditLogTooManyRowsException e) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_EXPORT_AUDIT_LOG_TOO_MANY_ENTRIES);

            return OUTCOME_ERROR;
        }
        if (!isAuditLogDataAvailable()) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_SHOW_AUDIT_LOG_DATA);
            logger.logError(LogMessageIdentifier.ERROR_GET_USER_OPERATION_LOG_RETURN_NULL);

            return OUTCOME_ERROR;
        }

        return OUTCOME_SUCCESS;
    }

    public void showOperationData() {

        List<AuditLogOperation> operations = new ArrayList<AuditLogOperation>();

        if (model.getSelectedGroup() == null) {
            model.setAvailableOperations(operations);
            return;
        }

        Map<String, String> operationGroups = model.getOperationGroups();
        Map<String, String> operationMap = model.getOperations();
        final String group = model.getSelectedGroup();
        ArgumentValidator.notNull("operationGroups", operationGroups);
        ArgumentValidator.notNull("operationMap", operationMap);

        if (group.equals(OPERATIONS_ALL)) {
            addAllOperationsToGroupAll(operations, operationMap);
        } else if (existsInGroupMap(group, operationGroups)) {
            addOperationsToGroup(operations, operationGroups, operationMap,
                    group);
        }
        Collections.sort(operations, logOperationComparator);
        model.setAvailableOperations(operations);
    }

    private boolean existsInGroupMap(final String selectedGroup,
            Map<String, String> operationGroups) {
        return operationGroups.get(selectedGroup) != null
                && !operationGroups.get(selectedGroup).isEmpty();
    }

    private void addOperationsToGroup(List<AuditLogOperation> operations,
            Map<String, String> operationGroups,
            Map<String, String> operationMap, final String selectedGroup) {
        for (String key : operationGroups.get(selectedGroup).split(",")) {
            operations.add(new AuditLogOperation(key, operationMap.get(key
                    .trim())));
        }
    }

    private void addAllOperationsToGroupAll(List<AuditLogOperation> operations,
            Map<String, String> operationMap) {
        for (Entry<String, String> entry : operationMap.entrySet()) {
            operations.add(new AuditLogOperation(entry.getKey(), entry
                    .getValue()));
        }
    }

    /**
     * Export the audit log data which retrieved by
     * <code>getAuditLogData()</code> .The read data will be written to the
     * response as text/CSV.
     * 
     * @return the logical outcome.
     * @throws IOException
     */
    public String showAuditLogData() throws IOException {

        if (!isAuditLogDataAvailable()) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_SHOW_AUDIT_LOG_DATA);
            logger.logError(LogMessageIdentifier.ERROR_GET_USER_OPERATION_LOG_RETURN_NULL);

            return OUTCOME_ERROR;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String filename = sdf.format(Calendar.getInstance().getTime())
                + "_Data.log";
        String contentType = "application/octet-stream";
        writeContentToResponse(model.getAuditLogData(), filename, contentType);
        model.setAuditLogData(null);

        return OUTCOME_SUCCESS;
    }

    /**
     * Checks if audit log data has been read.
     * 
     * @return <code>true</code> if audit log data is available otherwise
     *         <code>false</code>.
     */
    public boolean isAuditLogDataAvailable() {
        return (model.getAuditLogData() != null && model.getAuditLogData().length > 0);
    }

    /**
     * Checks if export button is enabled
     * 
     * @return <code>true</code> if export button is enabled otherwise
     *         <code>false</code>.
     */
    public boolean isButtonEnabled() {
        boolean operationsAvaliable = false;
        boolean dateAvaliable = false;
        List<AuditLogOperation> operations = model.getAvailableOperations();
        if (operations != null && operations.size() > 0) {
            for (int i = 0; i < operations.size(); i++) {
                if (operations.get(i).isSelected() == true) {
                    operationsAvaliable = true;
                    break;
                }
            }
        }
        if (model.getFromDate() == null || model.getToDate() == null) {
            dateAvaliable = false;
        } else {
            dateAvaliable = !model.getFromDate().after(model.getToDate());
        }
        return operationsAvaliable && dateAvaliable;
    }

    /**
     * Value change listener for log chooser
     * 
     * @param event
     *            ValueChangeEvent
     */
    public void processValueChange(ValueChangeEvent event) {
        setAllServicesSelected(false);
        model.setSelectedGroup((String) event.getNewValue());
        showOperationData();
    }

    public boolean isAllServicesSelected() {
        if (allServicesSelected) {
            if (model.getAvailableOperations() != null
                    && model.getAvailableOperations().size() > 0) {
                for (AuditLogOperation operation : model
                        .getAvailableOperations()) {
                    if (operation.isSelected() == false) {
                        allServicesSelected = false;
                        break;
                    }
                }
            }
        }
        return allServicesSelected;
    }

    public void setAllServicesSelected(boolean allServicesSelected) {
        this.allServicesSelected = allServicesSelected;
        if (model.getAvailableOperations() == null
                || model.getAvailableOperations().isEmpty()) {
            return;
        }
        for (AuditLogOperation o : model.getAvailableOperations()) {
            o.setSelected(allServicesSelected);
        }

    }

    public void validateFromAndToDate(final FacesContext context,
            final UIComponent toValidate, final Object value) {
        String clientId = toValidate.getClientId(context);
        validator.setToDate(model.getToDate());
        validator.setFromDate(model.getFromDate());
        try {
            validator.validate(context, toValidate, value);
        } catch (ValidatorException ex) {
            context.addMessage(
                    clientId,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                            .getLocalizedMessage(), null));
        }
    }

}
