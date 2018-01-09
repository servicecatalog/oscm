/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.iaas;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.iaas.data.AccessInformation;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.iaas.exceptions.CommunicationException;
import org.oscm.app.iaas.exceptions.IaasException;
import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.iaas.intf.VServerCommunication;
import org.oscm.app.iaas.intf.VSystemCommunication;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformService;

/**
 * 
 * @author malhotra
 * 
 */
@Stateless
@LocalBean
public class ProcessManagerBean {

    private static final Logger logger = LoggerFactory
            .getLogger(ProcessManagerBean.class);

    APPlatformService platformService;

    @EJB(beanInterface = VServerCommunication.class)
    protected VServerCommunication vserverComm;

    @EJB(beanInterface = VSystemCommunication.class)
    protected VSystemCommunication vsystemComm;

    @EJB
    protected VSystemProcessorBean vSysProcessor;

    @EJB
    protected VServerProcessorBean vServerProcessor;

    @PostConstruct
    public void initialize() {
        try {
            platformService = APPlatformServiceFactory.getInstance();
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Provide information about the state of operation according to current
     * task state.
     * 
     * @param controllerId
     *            id of the controller
     * @param instanceId
     *            id of the instance
     * @param paramHandler
     *            entity which holds all properties of the instance.
     * @return InstanceStatus
     * @throws Exception
     */
    public InstanceStatus getControllerInstanceStatus(String controllerId,
            String instanceId, PropertyHandler paramHandler) throws Exception {
        if (paramHandler.isInstanceSuspended()) {
            return getInstanceStatusForSuspendedInstance();
        }
        FlowState oldState = paramHandler.getState();
        // Check and/or dispatch next provisioning operation
        performProvisioningProcessing(controllerId, instanceId, paramHandler);

        FlowState state = paramHandler.getState();
        if (state == FlowState.FAILED) {
            throw new APPlatformException(Messages.getAll("error_operation"));
        }
        Operation operation = paramHandler.getOperation();
        InstanceStatus result = new InstanceStatus();
        result.setIsReady((state == FlowState.FINISHED && (operation != null && !operation
                .isDeletion())) || state == FlowState.DESTROYED);
        result.setRunWithTimer(state != FlowState.MANUAL);

        if (state == FlowState.VSERVERS_STOPPING) {
            result.setAccessInfo(Messages.get(paramHandler.getCustomerLocale(),
                    "accessInfo_NOT_AVAILABLE"));
        }
        if (result.isReady()) {
            if (state != FlowState.DESTROYED
                    && oldState != FlowState.VSERVERS_STOPPING) {
                // add access info as far as applicable
                result.setAccessInfo(getConnectionData(instanceId, paramHandler));
            }
            // notify about modification
            sendMailAboutModification(instanceId, paramHandler);
        }
        return result;
    }

    /**
     * Returns the Instance Status which corresponds to a suspended instance.
     * 
     * @return InstanceStatus
     */
    private InstanceStatus getInstanceStatusForSuspendedInstance() {
        InstanceStatus result = new InstanceStatus();
        result.setRunWithTimer(true);
        result.setIsReady(false);
        return result;
    }

    /**
     * Performs a VServer or a VSystem provisioning processing
     * 
     * @param controllerId
     * @param instanceId
     * @param paramHandler
     * @throws Exception
     */
    private void performProvisioningProcessing(String controllerId,
            String instanceId, PropertyHandler paramHandler) throws Exception {
        vSysProcessor.setDelegate(vServerProcessor);
        try {
            if (paramHandler.isVirtualServerProvisioning()) {
                vServerProcessor
                        .process(controllerId, instanceId, paramHandler);
            } else {
                vSysProcessor.process(controllerId, instanceId, paramHandler);
            }
        } catch (CommunicationException e) {
            throw e.getSuspendException();
        } catch (IaasException e) {
            if (e.isBusyMessage()) {
                logger.info("Ignoring exception since it is marked as BusyMessage: "
                        + e.getMessage());
            } else {
                throw e;
            }
        } catch (APPlatformException e) {
            throw e;
        }
    }

    /**
     * Sends a notification email about a modification operation.
     * 
     * @param instanceId
     * @param paramHandler
     * @throws APPlatformException
     */
    private void sendMailAboutModification(String instanceId,
            PropertyHandler paramHandler) throws APPlatformException {
        String mail = paramHandler.getMailForNotification();
        if (mail != null) {
            Operation operationState = paramHandler.getOperation();
            if (operationState.isModification()) {
                String locale = paramHandler.getCustomerLocale();
                String subject = Messages.get(locale,
                        "mail_notify_about_modification.subject",
                        new Object[] { instanceId });
                String text = Messages.get(locale,
                        "mail_notify_about_modification.text",
                        new Object[] { instanceId });
                platformService.sendMail(Collections.singletonList(mail),
                        subject, text);
            }
        }
    }

    /**
     * Retrieve the internal private IP.
     * 
     * @param paramHandler
     *            the parameter handler instance
     * @return the internal private IP
     * @throws Exception
     */
    public String getConnectionData(String instanceId,
            PropertyHandler paramHandler) throws Exception {
        String result = null;
        if (paramHandler.isVirtualSystemProvisioning()) {
            List<AccessInformation> infos = vsystemComm
                    .getAccessInfo(paramHandler);
            StringBuffer sb = new StringBuffer();
            sb.append("ID: ").append(instanceId);
            if (infos == null || infos.isEmpty()) {
                sb.append(" (No IP information available)");
            } else {
                for (AccessInformation info : infos) {
                    sb.append("\r\n");
                    sb.append(info.getIP());
                    sb.append("\t");
                    sb.append(info.getInitialPassword());
                }
            }
            result = sb.toString();
        } else {
            String privateIp = vserverComm.getInternalIp(paramHandler);
            String password = vserverComm
                    .getVServerInitialPassword(paramHandler);
            if (privateIp != null) {
                result = instanceId + " (" + privateIp + ")"
                        + ", initial password" + " (" + password + ")";
            }
        }
        return result;
    }
}
