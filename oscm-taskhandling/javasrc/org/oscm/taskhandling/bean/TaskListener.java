/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 4, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 4, 2011                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.bean;

import java.io.Serializable;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.interceptor.Interceptors;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.facade.ServiceFacade;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.taskhandling.operations.ExternalPriceModelHandler;
import org.oscm.taskhandling.operations.TaskHandler;
import org.oscm.taskhandling.operations.TaskHandlerFactory;
import org.oscm.taskhandling.payloads.ExternalPriceModelPayload;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.pricemodel.external.ExternalPriceModelService;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.TaskErrorHandlingException;

/**
 * Message driven bean to handle the task message objects sent by the business
 * logic.
 * 
 * @author tokoda
 * 
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "UserName", propertyValue = "admin"),
        @ActivationConfigProperty(propertyName = "Password", propertyValue = "admin") }, name = "OSCMTaskQueue")
@Interceptors({ InvocationDateContainer.class })
public class TaskListener implements MessageListener {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(TaskListener.class);

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;

    @EJB(beanInterface = CommunicationServiceLocal.class)
    protected CommunicationServiceLocal cs;

    @EJB(beanInterface = ApplicationServiceLocal.class)
    protected ApplicationServiceLocal as;

    @EJB(beanInterface = DataService.class)
    protected DataService ds;

    @EJB(beanInterface = IdentityServiceLocal.class)
    protected IdentityServiceLocal is;

    @EJB(beanInterface = TaskQueueServiceLocal.class)
    protected TaskQueueServiceLocal tqs;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal cfg;

    @EJB(beanInterface = SubscriptionServiceLocal.class)
    protected SubscriptionServiceLocal ss;
    
    @EJB(beanInterface = ExternalPriceModelService.class)
    protected ExternalPriceModelService eps;

    /**
     * Message driven bean to handle the task objects sent by the business
     * logic.
     * 
     * @param message
     */
    public void onMessage(Message message) {
        if (!(message instanceof ObjectMessage)) {
            logTaskMessageInstanceError(message);
            return;
        }

        logger.logDebug("Received object message from task queue",
                Log4jLogger.SYSTEM_LOG);
        ObjectMessage om = null;
        TaskHandler handler = null;
        try {
            // obtain the task message object
            om = (ObjectMessage) message;
            Serializable messageObject = om.getObject();
            if (!(messageObject instanceof TaskMessage) && !(messageObject instanceof PriceModel)) {
                throw new IllegalArgumentException(
                        "JMS message did not contain a valid task message");
            }
            
            if (messageObject instanceof PriceModel) {
                ExternalPriceModelPayload payload = new ExternalPriceModelPayload();
                payload.setPriceModel((PriceModel) messageObject);

                messageObject = new TaskMessage(ExternalPriceModelHandler.class, payload); 
            }

            handler = TaskHandlerFactory.getInstance().getTaskHandler(
                    (TaskMessage) messageObject, createServiceFacade());
            ds.setCurrentUserKey(Long.valueOf(((TaskMessage) messageObject)
                    .getCurrentUserKey()));

            handler.execute();

        } catch (IllegalArgumentException iae) {
            logIllegalArgumentExceptionError(iae);
        } catch (IllegalAccessException iace) {
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    iace,
                    LogMessageIdentifier.ERROR_CREATE_INSTANCE_FOR_TASK_HANDLER_FAILED);
        } catch (InstantiationException ie) {
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    ie,
                    LogMessageIdentifier.ERROR_CREATE_INSTANCE_FOR_TASK_HANDLER_FAILED);
        } catch (Exception e) {
            if (handler != null) {
                try {
                    handler.handleError(e);
                } catch (Exception ex) {
                    TaskErrorHandlingException thex = new TaskErrorHandlingException(
                            "Error handling of task message was failed!", ex);
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG,
                            thex,
                            LogMessageIdentifier.ERROR_HANDLING_MESSAGE_ERROR_FAILED);
                }
            } else {
                TaskErrorHandlingException thex = new TaskErrorHandlingException(
                        "Error handling of task message was failed because the handler cannot be found!",
                        e);
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        thex,
                        LogMessageIdentifier.ERROR_HANDLING_MESSAGE_ERROR_FAILED_NO_HANDLER);
            }
        } finally {
            ds.setCurrentUserKey(null);
        }
    }

    void logTaskMessageInstanceError(Message message) {
        logger.logError(
                LogMessageIdentifier.ERROR_RECEIVE_MESSAGE_INTERPRETED_FAILED,
                String.valueOf(message));
    }

    void logIllegalArgumentExceptionError(IllegalArgumentException iae) {
        logger.logError(Log4jLogger.SYSTEM_LOG, iae,
                LogMessageIdentifier.ERROR_INVALID_ARGUMENT_AS_TASK_MESSAGE);
    }

    private ServiceFacade createServiceFacade() {
        ServiceFacade facade = new ServiceFacade();
        facade.setLocalizerService(localizer);
        facade.setCommunicationService(cs);
        facade.setApplicationService(as);
        facade.setIdentityService(is);
        facade.setDataService(ds);
        facade.setTaskQueueService(tqs);
        facade.setConfigurationService(cfg);
        facade.setSubscriptionService(ss);
        facade.setExternalPriceModelService(eps);
        return facade;
    }
}
