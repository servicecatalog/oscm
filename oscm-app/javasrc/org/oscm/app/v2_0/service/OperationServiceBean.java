/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.business.APPlatformControllerFactory;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.OperationDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.Operation;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;

@Stateless
public class OperationServiceBean {

    private static final Logger logger = LoggerFactory
            .getLogger(AsynchronousOperationProxy.class);

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    protected EntityManager em;

    @EJB
    protected OperationDAO operationDAO;

    @EJB
    protected ServiceInstanceDAO instanceDAO;

    @EJB
    protected APPConfigurationServiceBean configService;

    public OperationResult execute(String userId, String instanceId,
            String transactionId, String operationId, Properties opParameters,
            long operationKey) {

        logger.info(
                "Execute operation '{}' for instance '{}' with transactionId '{}' .",
                operationId, instanceId, transactionId);

        OperationResult result = new OperationResult();
        ServiceInstance instance = null;
        try {
            instance = instanceDAO.getInstanceById(instanceId);
        } catch (ServiceInstanceNotFoundException e) {
            // no such instance, end with error
            logger.warn(e.getMessage(), e);
            result.setErrorMessage(e.getMessage());
            return result;
        }

        // Check whether this instance is free to use
        if (!instance.isAvailable()) {
            addOperationToQueue(operationKey, opParameters, instance,

            transactionId);
            result.setAsyncExecution(true);
            return result;
        }

        try {
            final APPlatformController controller = getController(instance);

            final ProvisioningSettings settings = configService
                    .getProvisioningSettings(instance, null);

            instance.setRequestTime(System.currentTimeMillis());
            InstanceStatus status = controller.executeServiceOperation(userId,
                    instanceId, transactionId, operationId,
                    toController(opParameters), settings);

            if (status != null) {
                // If everything worked well we will save all changed parameters
                instance.setInstanceParameters(status.getChangedParameters());
                if (!status.isReady()) {
                    // not ready, add instance to regular APP polling
                    instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_OPERATION);

                    saveOperation(operationKey, opParameters, instance,
                            transactionId);
                    result.setAsyncExecution(true);
                }
                em.persist(instance);
            }

            return result;
        } catch (BadResultException | APPlatformException e) {
            addOperationToQueue(operationKey, opParameters, instance,
                    transactionId);
            logger.warn(e.getMessage(), e);
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }

    void removeOperationFromQueue(long operationKey) {
        if (isOperationFromQueue(operationKey)) {
            operationDAO.removeOperation(operationKey);
        }
    }

    void addOperationToQueue(long operationKey, final Properties opParamaters,
            ServiceInstance instance, String transactionId) {
        if (!isOperationFromQueue(operationKey)) {
            operationDAO.addOperationForQueue(instance, opParamaters,
                    transactionId);
        }
    }

    void saveOperation(long operationKey, final Properties opParamaters,
            ServiceInstance instance, String transactionId) {
        if (isOperationFromQueue(operationKey)) {
            Operation operation = em.getReference(Operation.class,
                    Long.valueOf(operationKey));
            operation.setForQueue(false);
            em.flush();
        } else {
            operationDAO.addOperation(instance, opParamaters, transactionId);
        }
    }

    APPlatformController getController(final ServiceInstance instance)
            throws APPlatformException {
        final APPlatformController controller = APPlatformControllerFactory
                .getInstance(instance.getControllerId());
        return controller;
    }

    public OperationResult executeServiceOperationFromQueue(String instanceId) {

        OperationResult result = new OperationResult();
        Operation op = operationDAO.getOperationFromQueue(instanceId);
        if (op != null) {
            result = execute(op.getUserId(), op.getServiceInstance()
                    .getInstanceId(), op.getTransactionId(),
                    op.getOperationId(), op.getParametersAsProperties(),
                    op.getTkey());
        }
        return result;
    }

    List<org.oscm.app.v2_0.data.OperationParameter> toController(Properties prop) {
        List<org.oscm.app.v2_0.data.OperationParameter> paramList = new ArrayList<org.oscm.app.v2_0.data.OperationParameter>();
        if (prop != null) {
            Set<Object> keys = prop.keySet();
            for (Object key : keys) {
                org.oscm.app.v2_0.data.OperationParameter param = new org.oscm.app.v2_0.data.OperationParameter();
                param.setName((String) key);
                param.setValue((String) prop.get(key));
                paramList.add(param);
            }
        }
        return paramList;
    }

    List<OperationParameter> toBES(
            List<org.oscm.app.v2_0.data.OperationParameter> operationParameters) {
        List<OperationParameter> result = new ArrayList<>();
        if (operationParameters != null) {
            for (org.oscm.app.v2_0.data.OperationParameter op : operationParameters) {
                OperationParameter p = new OperationParameter();
                p.setName(op.getName());
                p.setValue(op.getValue());
                result.add(p);
            }
        }
        return result;
    }

    public Properties createProperties(String userId, String operationId,
            List<OperationParameter> parameterValues) {
        final Properties props = new Properties();
        props.put(APPlatformController.KEY_OPERATION_ID, operationId);
        props.put(APPlatformController.KEY_OPERATION_USER_ID, userId);
        if (parameterValues != null) {
            for (final OperationParameter p : parameterValues) {
                props.put(p.getName(), p.getValue());
            }
        }
        return props;
    }

    boolean isOperationFromQueue(long opKey) {
        return opKey != 0L;
    }

    public List<OperationParameter> getParameterValues(String userId,
            String instanceId, String operationId) {
        logger.info("Retrieve operation parameters for instance {}", instanceId);

        ServiceInstance instance = null;
        try {
            instance = instanceDAO.getInstanceById(instanceId);
        } catch (ServiceInstanceNotFoundException e) {
            // no such instance, end with error
            logger.warn(e.getMessage(), e);
            return new ArrayList<>();
        }

        try {
            final APPlatformController controller = getController(instance);
            final ProvisioningSettings settings = configService
                    .getProvisioningSettings(instance, null);

            List<org.oscm.app.v2_0.data.OperationParameter> operationParameters = controller
                    .getOperationParameters(userId, instanceId, operationId,
                            settings);
            return toBES(operationParameters);
        } catch (BadResultException | APPlatformException e) {
            // FIXME exception handling towards BES?
            logger.warn(e.getMessage(), e);
        }
        return new ArrayList<>();
    }
}
