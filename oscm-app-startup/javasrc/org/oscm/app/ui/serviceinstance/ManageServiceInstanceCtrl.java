/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-2-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui.serviceinstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oscm.app.business.exceptions.ServiceInstanceException;
import org.oscm.app.domain.InstanceOperation;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.ui.BaseCtrl;
import org.oscm.app.ui.SessionConstants;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.app.v2_0.service.ServiceInstanceServiceBean;

/**
 * Controller of manage service instance page
 * 
 * @author Mao
 * 
 */
@ViewScoped
@ManagedBean
public class ManageServiceInstanceCtrl extends BaseCtrl {

    private static final String ENCRYPTED_PWD = "*********";
    private static final String PWD_SUFFIX = "_PWD";
    private static final String PASS_SUFFIX = "_PWD";
    protected ServiceInstanceServiceBean serviceInstanceService;
    protected APPTimerServiceBean timerService;

    @ManagedProperty(value = "#{manageServiceInstanceModel}")
    protected ManageServiceInstanceModel model;

    public String getInitialize() {
        ManageServiceInstanceModel model = getModel();
        try {
            if (model == null) {
                model = new ManageServiceInstanceModel();
            }
            if (!model.isInitialized()) {
                model.setServiceInstanceRows(initServiceInstanceRows());
                model.setInitialized(true);
                model.setTimePattern(TIME_PATTERN);
                model.setLoggedInUserId(initLoggedInUserId());
                setModel(model);
            }
        } catch (ServiceInstanceException | IllegalArgumentException e) {
            addError(e);
        }
        return "";
    }

    public String executeService() {
        try {
            ServiceInstanceRow selectedRow = getSelectedServiceInstanceRow();
            if (selectedRow == null) {
                addError(ERROR_NO_INSTANCE);
                return OUTCOME_ERROR;
            }
            ServiceInstance serviceInstance = selectedRow.getServiceInstance();
            InstanceOperation operation = InstanceOperation
                    .valueOf(selectedRow.getSelectedOperation());
            if (isOperationAllowed(serviceInstance, operation)) {
                ServiceUser serviceUser = readUserFromSession();
                getServiceInstanceService().executeOperation(serviceInstance,
                        serviceUser, operation);
                addMessage(OPERATION_SUCCESS);
                if (operation.equals(InstanceOperation.ABORT_PENDING)) {
                    addMessage(ABORT_PENDING_SUCCESS);
                }
            } else {
                addMessage(ERROR_OPERATION_NOT_ALLOWED);
            }
            this.model.setServiceInstanceRows(initServiceInstanceRows());
        } catch (ServiceInstanceException e) {
            addError(e);
            return OUTCOME_ERROR;
        }
        return OUTCOME_SUCCESS;
    }

    private boolean isOperationAllowed(ServiceInstance serviceInstance,
            InstanceOperation operation) throws ServiceInstanceException {
        String locale = readUserLocaleFromSession();
        ServiceInstance currentInstance = getServiceInstanceService()
                .find(serviceInstance, locale);
        boolean isOperationAllowed = filterOperation(operation,
                currentInstance);
        if (!isOperationAllowed) {
            return false;
        }
        return true;
    }

    public String initTimer() {
        getAPPTimerService().initTimers();
        addMessage(OPERATION_SUCCESS);
        return OUTCOME_SUCCESS;
    }

    private String getControllerId() {
        HttpServletRequest request = getRequest();
        String controllerId = request
                .getParameter(SessionConstants.SESSION_CTRL_ID);

        if (isEmpty(controllerId)) {
            controllerId = (String) request.getSession()
                    .getAttribute(SessionConstants.SESSION_CTRL_ID);
        }

        if (isEmpty(controllerId)) {
            throw new IllegalArgumentException(
                    message(ERROR_INVALID_CONTROLLER));
        }
        return controllerId;
    }

    public void updateSelectedServiceInstanceRow() {
        try {
            ServiceInstanceRow selectedServiceInstanceRow = getSelectedServiceInstanceRow();
            if (selectedServiceInstanceRow != null) {
                String locale = readUserLocaleFromSession();
                List<InstanceParameter> instanceParameters = initInstanceParameters(
                        selectedServiceInstanceRow.getServiceInstance(),
                        locale);
                selectedServiceInstanceRow
                        .setInstanceParameters(instanceParameters);
            }
            getModel().setSelectedInstanceRow(selectedServiceInstanceRow);
        } catch (ServiceInstanceException e) {
            addError(e);
        }
    }

    public ManageServiceInstanceModel getModel() {
        return model;
    }

    public void setModel(ManageServiceInstanceModel model) {
        this.model = model;
    }

    public ServiceInstanceRow getSelectedServiceInstanceRow() {
        for (ServiceInstanceRow row : model.getServiceInstanceRows()) {
            if (row.getServiceInstance().getInstanceId()
                    .equals(model.getSelectedInstanceId())) {
                return row;
            }
        }
        return null;
    }

    private List<ServiceInstanceRow> initServiceInstanceRows()
            throws ServiceInstanceException {
        List<ServiceInstance> serviceInstances = getServiceInstanceService()
                .getInstancesForController(getControllerId());
        List<ServiceInstanceRow> result = new ArrayList<>();
        if (serviceInstances == null) {
            return result;
        }
        for (ServiceInstance serviceInstance : serviceInstances) {
            List<SelectItem> selectableOperations = initSelectableOperaions(
                    serviceInstance);
            ServiceInstanceRow row = new ServiceInstanceRow(serviceInstance,
                    selectableOperations);
            result.add(row);
        }
        return result;

    }

    private List<SelectItem> initSelectableOperaions(
            ServiceInstance serviceInstance) {
        List<SelectItem> selectableOperations = new ArrayList<>();
        for (InstanceOperation operation : getServiceInstanceService()
                .listOperationsForInstance(serviceInstance)) {
            if (filterOperation(operation, serviceInstance)) {
                selectableOperations.add(new SelectItem(operation));
            }
        }
        return selectableOperations;
    }

    boolean filterOperation(InstanceOperation operation,
            ServiceInstance serviceInstance) {
        boolean runWithTimer = serviceInstance.getRunWithTimer();
        boolean controllerReady = serviceInstance.isControllerReady();
        boolean isLocked = serviceInstance.isLocked();
        boolean isOperationAllowed = false;

        switch (operation) {

        case RESUME:
        case ABORT_PENDING:
            if (!runWithTimer && !controllerReady) {
                isOperationAllowed = true;
            }
            break;

        case SUSPEND:
            if (runWithTimer && !controllerReady) {
                isOperationAllowed = true;
            }
            break;

        case COMPLETE_PENDING:
            if (!runWithTimer && controllerReady) {
                isOperationAllowed = true;
            }
            break;

        case UNLOCK:
            if (isLocked) {
                isOperationAllowed = true;
            }
            break;

        default:
            isOperationAllowed = true;
            break;
        }

        return isOperationAllowed;
    }

    private List<InstanceParameter> initInstanceParameters(
            ServiceInstance serviceInstance, String locale)
            throws ServiceInstanceException {
        List<InstanceParameter> parameters = getServiceInstanceService()
                .getInstanceParameters(serviceInstance, locale);

        Collections.sort(parameters, new Comparator<InstanceParameter>() {
            @Override
            public int compare(InstanceParameter param1,
                    InstanceParameter param2) {
                return param1.getParameterKey()
                        .compareTo(param2.getParameterKey());
            }
        });
        return filterEncryptedParameterValues(parameters);
    }

    List<InstanceParameter> filterEncryptedParameterValues(
            List<InstanceParameter> parameters) {
        List<InstanceParameter> filtedParameters = parameters;
        Iterator<InstanceParameter> iterator = filtedParameters.iterator();
        while (iterator.hasNext()) {
            InstanceParameter param = iterator.next();
            if (param.isEncrypted()
                    || param.getParameterKey().endsWith(PWD_SUFFIX)
                    || param.getParameterKey().endsWith(PASS_SUFFIX)) {
                param.setParameterValue(ENCRYPTED_PWD);
            }
        }
        return filtedParameters;
    }

    public ServiceInstanceServiceBean getServiceInstanceService() {
        if (serviceInstanceService == null) {
            serviceInstanceService = lookup(ServiceInstanceServiceBean.class);
        }
        return serviceInstanceService;
    }

    public APPTimerServiceBean getAPPTimerService() {
        if (timerService == null) {
            timerService = lookup(APPTimerServiceBean.class);
        }
        return timerService;
    }

    private String initLoggedInUserId() {
        FacesContext facesContext = getFacesContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);
        if (session != null) {
            String loggedInUserId = ""
                    + session.getAttribute(SessionConstants.SESSION_USER_ID);
            return loggedInUserId;
        }
        return null;
    }
}
