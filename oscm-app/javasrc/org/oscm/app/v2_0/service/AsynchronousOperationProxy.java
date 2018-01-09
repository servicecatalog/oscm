/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import java.util.List;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;
import org.oscm.operation.intf.OperationService;

@Stateless
@LocalBean
@WebService(serviceName = "OperationService", targetNamespace = "http://oscm.org/xsd", portName = "OperationServicePort", endpointInterface = "org.oscm.operation.intf.OperationService")
public class AsynchronousOperationProxy implements OperationService {

    @EJB
    protected OperationServiceBean operationBean;

    @EJB
    protected ServiceInstanceDAO instanceDAO;

    @EJB
    protected APPTimerServiceBean timerServcie;

    @Override
    public OperationResult executeServiceOperation(String userId,
            String instanceId, String transactionId, String operationId,
            List<OperationParameter> parameters) {

        Properties opProperties = operationBean.createProperties(userId,
                operationId, parameters);
        OperationResult result = new OperationResult();
        result = operationBean.execute(userId, instanceId, transactionId,
                operationId, opProperties, 0L);

        if (result.getErrorMessage() == null)
            try {
                ServiceInstance si = instanceDAO.getInstanceById(instanceId);
                if (si.getProvisioningStatus().isWaitingForOperation()) {
                    timerServcie.initTimers();
                }
            } catch (ServiceInstanceNotFoundException e) {
                // could not happen, otherwise the result is not null
            }
        return result;
    }

    @Override
    public List<OperationParameter> getParameterValues(
            @WebParam(name = "userId") String userId,
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "operationId") String operationId) {
        return operationBean
                .getParameterValues(userId, instanceId, operationId);
    }

}
