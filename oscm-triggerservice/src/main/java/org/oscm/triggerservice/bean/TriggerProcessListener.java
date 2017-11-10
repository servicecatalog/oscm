/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 14.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.interceptor.Interceptors;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.triggerservice.adapter.INotificationServiceAdapter;
import org.oscm.triggerservice.adapter.NotificationServiceAdapterFactory;
import org.oscm.triggerservice.assembler.TriggerProcessAssembler;
import org.oscm.triggerservice.notification.VONotificationBuilder;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.validation.Invariants;
import org.oscm.validator.ADMValidator;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.notification.vo.VONotification;
import org.oscm.notification.vo.VOProperty;

/**
 * Message driven bean to handle the trigger process objects sent by the
 * business logic.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Interceptors({ InvocationDateContainer.class })
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "UserName", propertyValue = "admin"),
        @ActivationConfigProperty(propertyName = "Password", propertyValue = "admin") }, name = "OSCMTriggerQueue")
public class TriggerProcessListener implements MessageListener {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(TriggerProcessListener.class);

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal cs;

    public void onMessage(Message message) {

        if (!(message instanceof ObjectMessage)) {
            logger.logError(
                    LogMessageIdentifier.ERROR_RECEIVE_MESSAGE_INTERPRETED_FAILED,
                    String.valueOf(message));
            return;
        }

        logger.logDebug("Received object message from queue",
                Log4jLogger.SYSTEM_LOG);
        TriggerProcess process = null;
        try {
            // obtain the trigger process object
            ObjectMessage om = (ObjectMessage) message;
            Serializable messageObject = om.getObject();
            if (!(messageObject instanceof Long)) {
                throw new IllegalArgumentException(
                        "JMS message did not contain a valid key for the trigger process");
            }
            process = dm.getReference(TriggerProcess.class,
                    ((Long) messageObject).longValue());

            if (process.getTriggerDefinition().isSuspendProcess()
                    && process.getStatus() == TriggerProcessStatus.CANCELLED) {
                // do not send notification if the process was canceled in
                // the meantime and the trigger type operation was not completed
                if (isPreviousVersionInitial(process)) {
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_TRIGGER_PROCESS_ALREADY_CANCELED,
                            String.valueOf(process.getKey()));
                } else {
                    // otherwise notify external system.
                    handleCancelAction(process);
                }
                return;
            }

            if (process.getUser() != null) {
                dm.setCurrentUserKey(process.getUser().getKey());
            }

            // Handle message according to specified notification type
            handleTriggerProcess(process);
        } catch (Throwable e) {
            // we cannot abort here, no exception can be thrown either. So just
            // log the exception and put the process to error state.
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_EVALUATE_MESSAGE_FAILED);

            if (process != null) {
                process.setState(TriggerProcessStatus.ERROR);
                String reason = e.getMessage() == null ? e.toString() : e
                        .getMessage();
                if (reason != null
                        && reason.length() > ADMValidator.LENGTH_DESCRIPTION) {
                    reason = reason.substring(0,
                            ADMValidator.LENGTH_DESCRIPTION);
                }
                localizer.storeLocalizedResource("en", process.getKey(),
                        LocalizedObjectTypes.TRIGGER_PROCESS_REASON, reason);
            }
        } finally {
            dm.setCurrentUserKey(null);
        }
    }

    /**
     * Help method to determine if the trigger process has been cancelled before
     * it went to status "WAITING_FOR_APPROVAL". This can happen for example if
     * the JMS queue is full and the user clicks on "Abort" before the message
     * was processed . In this case the version of the TriggerProcess is 2
     * because it went immediately from status INITIAL to CANCELLED.
     * 
     * @param process
     *            the TriggerProcess to check
     * @return true if the previous version was INITIAL
     */

    private boolean isPreviousVersionInitial(TriggerProcess process) {
        if (process.getVersion() == 2) {
            return true;
        }
        return false;
    }

    /**
     * Performs the operations that are required for the given trigger process.
     * 
     * @param process
     *            The trigger process to be handled.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     * @throws ObjectNotFoundException
     */
    private void handleTriggerProcess(TriggerProcess process)
            throws IOException, WSDLException, ParserConfigurationException,
            ObjectNotFoundException {
        final TriggerDefinition td = process.getTriggerDefinition();
        final TriggerType type = td.getType();
        // The organization's locale has to be used
        String orgLocale = "en";
        Organization org = td.getOrganization();
        if (org != null && org.getLocale() != null
                && org.getLocale().length() > 0) {
            orgLocale = org.getLocale();
        }

        LocalizerFacade facade = new LocalizerFacade(localizer, orgLocale);

        switch (type) {
        case START_BILLING_RUN:
            handleBillingNotification(process);
            break;
        case ACTIVATE_SERVICE:
            handleActivateProduct(process, facade);
            break;
        case DEACTIVATE_SERVICE:
            handleDeactivateProduct(process, facade);
            break;
        case UNSUBSCRIBE_FROM_SERVICE:
            handleUnsubscribeFromProduct(process, facade);
            break;
        case UPGRADE_SUBSCRIPTION:
            handleUpgradeSubscription(process, facade);
            break;
        case MODIFY_SUBSCRIPTION:
            handleModifySubscription(process, facade);
            break;
        case ADD_REVOKE_USER:
            handleAddRevokeUser(process, facade);
            break;
        case SAVE_PAYMENT_CONFIGURATION:
            handleSavePaymentConfigurations(process, facade);
            break;
        case REGISTER_CUSTOMER_FOR_SUPPLIER:
            handleRegisterCustomer(process, facade);
            break;
        case SUBSCRIBE_TO_SERVICE:
            handleSubscribeToProduct(process, facade);
            break;
        case SUBSCRIPTION_CREATION:
            handleSubscriptionCreation(process, facade);
            break;
        case SUBSCRIPTION_MODIFICATION:
            handleSubscriptionModification(process, facade);
            break;
        case SUBSCRIPTION_TERMINATION:
            handleSubscriptionTermination(process, facade);
            break;
        case REGISTER_OWN_USER:
            handleRegisterOwnUser(process, facade);
            break;
        default:
            handleUnsupportedTriggerProcessType(process);
            break;
        }

    }

    /**
     * Logs the problem (unsupported type) and sets the process state to error.
     * 
     * @param process
     *            The process to be handled showing an unsupported trigger type.
     */
    private void handleUnsupportedTriggerProcessType(TriggerProcess process) {
        logger.logError(LogMessageIdentifier.ERROR_TRIGGER_TYPE_NOT_SUPPORTED,
                String.valueOf(process.getTriggerDefinition().getType()),
                Long.toString(process.getKey()));
        process.setState(TriggerProcessStatus.ERROR);
    }

    /**
     * Sends a notification, on subscription to a product, to the receiver
     * specified in the trigger definition.
     * 
     * @param process
     *            The current process to be handled.
     * @param facade
     *            Localizer facade to determine translated reason.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleSubscribeToProduct(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        VOSubscription subscription = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION),
                VOSubscription.class);
        VOService product = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.PRODUCT),
                VOService.class);
        List<?> usersTemp = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.USERS),
                List.class);
        List<VOUsageLicense> users = new ArrayList<VOUsageLicense>();
        if (usersTemp != null) {
            for (Object object : usersTemp) {
                users.add(VOUsageLicense.class.cast(object));
            }
        }
        serviceClient.onSubscribeToProduct(VOConverter.convertToApi(vo),
                VOConverter.convertToApi(subscription), VOConverter
                        .convertToApi(product), VOCollectionConverter
                        .convertList(users,
                                org.oscm.vo.VOUsageLicense.class));

        updateProcessState(process);
    }

    /**
     * Sends a notification on registration of a customer to the receiver
     * specified in the trigger definition.
     * 
     * @param process
     *            The current process to be handled.
     * @param facade
     *            Localizer facade to determine translated reason.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleRegisterCustomer(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        VOOrganization organization = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.ORGANIZATION),
                VOOrganization.class);
        VOUserDetails user = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.USER),
                VOUserDetails.class);
        Properties organizationProperties = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.ORGANIZATION_PROPERTIES),
                Properties.class);
        serviceClient.onRegisterCustomer(VOConverter.convertToApi(vo),
                VOConverter.convertToApi(organization),
                VOConverter.convertToApi(user), organizationProperties);

        updateProcessState(process);
    }

    /**
     * Sends a notification on the save operation of payment configurations to
     * the receiver specified in the trigger definition.
     * 
     * @param process
     *            The current process to be handled.
     * @param facade
     *            Localizer facade to determine translated reason.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleSavePaymentConfigurations(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        TriggerProcessParameter param = null;
        if (process
                .getParamValueForName(TriggerProcessParameterName.DEFAULT_CONFIGURATION) != null) {
            param = process
                    .getParamValueForName(TriggerProcessParameterName.DEFAULT_CONFIGURATION);
            Set<?> defaultConfiguration = getParamValue(param, Set.class);
            serviceClient.onSaveDefaultPaymentConfiguration(VOConverter
                    .convertToApi(vo), VOCollectionConverter.convertSet(
                    ParameterizedTypes.set(defaultConfiguration,
                            VOPaymentType.class),
                    org.oscm.vo.VOPaymentType.class));
        } else if (process
                .getParamValueForName(TriggerProcessParameterName.CUSTOMER_CONFIGURATION) != null) {
            param = process
                    .getParamValueForName(TriggerProcessParameterName.CUSTOMER_CONFIGURATION);
            VOOrganizationPaymentConfiguration customerConfiguration = getParamValue(
                    param, VOOrganizationPaymentConfiguration.class);
            serviceClient.onSaveCustomerPaymentConfiguration(
                    VOConverter.convertToApi(vo),
                    VOConverter.convertToApi(customerConfiguration));
        } else if (process
                .getParamValueForName(TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION) != null) {
            param = process
                    .getParamValueForName(TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION);
            Set<?> defaultConfiguration = getParamValue(param, Set.class);
            serviceClient.onSaveServiceDefaultPaymentConfiguration(VOConverter
                    .convertToApi(vo), VOCollectionConverter.convertSet(
                    ParameterizedTypes.set(defaultConfiguration,
                            VOPaymentType.class),
                    org.oscm.vo.VOPaymentType.class));
        } else if (process
                .getParamValueForName(TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION) != null) {
            param = process
                    .getParamValueForName(TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION);
            VOServicePaymentConfiguration conf = getParamValue(param,
                    VOServicePaymentConfiguration.class);
            serviceClient.onSaveServicePaymentConfiguration(
                    VOConverter.convertToApi(vo),
                    VOConverter.convertToApi(conf));
        }

        updateProcessState(process);
    }

    /**
     * Sends a notification on the addition or removal of users to the receiver
     * specified in the trigger definition.
     * 
     * @param process
     *            The current process to be handled.
     * @param facade
     *            Localizer facade to determine translated reason.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleAddRevokeUser(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        String subscriptionId = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION),
                String.class);
        List<VOUsageLicense> usersToBeAdded = ParameterizedTypes
                .list(getParamValue(
                        process.getParamValueForName(TriggerProcessParameterName.USERS_TO_ADD),
                        List.class), VOUsageLicense.class);
        List<VOUser> usersToBeRevoked = ParameterizedTypes
                .list(getParamValue(
                        process.getParamValueForName(TriggerProcessParameterName.USERS_TO_REVOKE),
                        List.class), VOUser.class);
        serviceClient.onAddRevokeUser(VOConverter.convertToApi(vo),
                subscriptionId, VOCollectionConverter
                        .convertList(usersToBeAdded,
                                org.oscm.vo.VOUsageLicense.class),
                VOCollectionConverter.convertList(usersToBeRevoked,
                        org.oscm.vo.VOUser.class));

        updateProcessState(process);
    }

    /**
     * Sends a notification to the external system about the cancellation of a
     * trigger process.
     * 
     * @param process
     *            the TriggerProcess for which to notify the external system
     * @throws IOException
     * @throws WSDLException
     * @throws ParserConfigurationException
     */
    private void handleCancelAction(TriggerProcess process) throws IOException,
            WSDLException, ParserConfigurationException {

        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());

        serviceClient.onCancelAction(process.getKey());
    }

    /**
     * Sends a notification on the modification of a subscription to the
     * receiver specified in the trigger definition.
     * 
     * @param process
     *            The current process to be handled.
     * @param facade
     *            Localizer facade to determine translated reason.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleModifySubscription(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        VOSubscription subscription = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION),
                VOSubscription.class);
        List<VOParameter> modifiedParameters = ParameterizedTypes
                .list(getParamValue(
                        process.getParamValueForName(TriggerProcessParameterName.PARAMETERS),
                        List.class), VOParameter.class);
        serviceClient.onModifySubscription(VOConverter.convertToApi(vo),
                VOConverter.convertToApi(subscription), VOCollectionConverter
                        .convertList(modifiedParameters,
                                org.oscm.vo.VOParameter.class));

        updateProcessState(process);
    }

    /**
     * Sends a notification on the upgrade of a subscription to the receiver
     * specified in the trigger definition.
     * 
     * @param process
     *            The current process to be handled.
     * @param facade
     *            Localizer facade to determine translated reason.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleUpgradeSubscription(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        VOSubscription current = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION),
                VOSubscription.class);
        VOService newProduct = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.PRODUCT),
                VOService.class);
        serviceClient.onUpgradeSubscription(VOConverter.convertToApi(vo),
                VOConverter.convertToApi(current),
                VOConverter.convertToApi(newProduct));

        updateProcessState(process);
    }

    /**
     * Sends a notification on the unsubscribe operation.
     * 
     * @param process
     *            The current trigger process to be handled.
     * @param facade
     *            Localizer facade to determine translated reason.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleUnsubscribeFromProduct(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        String subId = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION),
                String.class);
        serviceClient.onUnsubscribeFromProduct(VOConverter.convertToApi(vo),
                subId);

        updateProcessState(process);
    }

    /**
     * Sends a notification on the deactivation of a product to the receiver
     * specified in the trigger definition.
     * 
     * @param process
     *            The current trigger process to be handled.
     * @param facade
     *            Localizer facade to determine translated reason.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleDeactivateProduct(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        VOService product = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.PRODUCT),
                VOService.class);
        serviceClient.onDeactivateProduct(VOConverter.convertToApi(vo),
                VOConverter.convertToApi(product));

        updateProcessState(process);
    }

    /**
     * Sends a notification on the activation of a product to the receiver
     * specified in the trigger definition.
     * 
     * @param process
     *            The current trigger process to be handled.
     * @param facade
     *            Localizer facade to determine translated reason.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleActivateProduct(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        VOService product = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.PRODUCT),
                VOService.class);
        serviceClient.onActivateProduct(VOConverter.convertToApi(vo),
                VOConverter.convertToApi(product));

        updateProcessState(process);
    }

    /**
     * Updates the state of the trigger process to
     * {@link TriggerProcessStatus#WAITING_FOR_APPROVAL} in case the trigger is
     * configured to suspend the current operation or to status
     * {@link TriggerProcessStatus#NOTIFIED} in case it is not.
     * 
     * @param process
     *            The trigger process to be updated.
     */
    private void updateProcessState(TriggerProcess process) {
        if (process.getTriggerDefinition().isSuspendProcess()) {
            process.setState(TriggerProcessStatus.WAITING_FOR_APPROVAL);
        } else {
            process.setState(TriggerProcessStatus.NOTIFIED);
        }
    }

    /**
     * Obtains the billing relevant data and sends an according notification to
     * the receiver specified in the trigger definition. Furthermore the process
     * state will be updated.
     * 
     * @param process
     *            The current process to be handled.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    private void handleBillingNotification(TriggerProcess process)
            throws IOException, WSDLException, ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());

        String billingResultXML = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.XML_BILLING_DATA),
                String.class);
        serviceClient.billingPerformed(billingResultXML);

        updateProcessState(process);
    }

    private void handleSubscriptionCreation(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {

        // collect parameters
        VOTriggerProcess tp = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        VOService service = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.PRODUCT),
                VOService.class);
        Invariants.assertNotNull(service,
                "mandatory parameter 'service' not set");

        @SuppressWarnings("unchecked")
        List<VOUsageLicense> users = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.USERS),
                List.class);

        VONotification notification = buildNotificationForSubscriptionCreated(
                process, service);

        // notify external system
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        serviceClient.onSubscriptionCreation(VOConverter.convertToApi(tp),
                VOConverter.convertToApi(service), VOCollectionConverter
                        .convertList(users,
                                org.oscm.vo.VOUsageLicense.class),
                notification);

        // proceed bes process to 'WAITING_FOR_APPROVAL'
        updateProcessState(process);
    }

    private VONotification buildNotificationForSubscriptionCreated(
            TriggerProcess process, VOService service) {

        VONotificationBuilder builder = new VONotificationBuilder();

        VOSubscription subscription = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION),
                VOSubscription.class);
        Invariants.assertNotNull(subscription,
                "mandatory parameter 'subscription' not set");

        builder.addParameter(VOProperty.SUBSCRIPTION_SUBSCRIPTION_ID,
                subscription.getSubscriptionId());
        builder.addParameter(VOProperty.SUBSCRIPTION_SERVICE_INSTANCE_ID,
                subscription.getServiceInstanceId());

        if (service.getPriceModel().isChargeable()) {
            VOBillingContact billingContact = getParamValue(
                    process.getParamValueForName(TriggerProcessParameterName.BILLING_CONTACT),
                    VOBillingContact.class);
            builder.addParameter(VOProperty.BILLING_CONTACT_EMAIL,
                    billingContact.getEmail());
            builder.addParameter(VOProperty.BILLING_CONTACT_COMPANYNAME,
                    billingContact.getCompanyName());
            builder.addParameter(VOProperty.BILLING_CONTACT_ADDRESS,
                    billingContact.getAddress());
        }
        return builder.build();
    }

    private void handleSubscriptionModification(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {

        // collect parameters
        VOTriggerProcess tp = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        VOSubscription subscription = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION),
                VOSubscription.class);
        Invariants.assertNotNull(subscription,
                "mandatory parameter 'subscription' not set");

        VONotification notification = new VONotificationBuilder()
                .addParameter(VOProperty.SUBSCRIPTION_SUBSCRIPTION_ID,
                        subscription.getSubscriptionId())
                .addParameter(VOProperty.SUBSCRIPTION_SERVICE_INSTANCE_ID,
                        subscription.getServiceInstanceId())
                .addParameter(VOProperty.SUBSCRIPTION_SERVICE_ID,
                        subscription.getServiceId())
                .addParameter(VOProperty.SUBSCRIPTION_SERVICE_KEY,
                        String.valueOf(subscription.getServiceKey())).build();
        List<VOParameter> modifiedParameters = getParamListValue(
                process.getParamValueForName(TriggerProcessParameterName.PARAMETERS),
                VOParameter.class);

        // notify external system
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        serviceClient.onSubscriptionModification(VOConverter.convertToApi(tp),
                VOCollectionConverter.convertList(modifiedParameters,
                        org.oscm.vo.VOParameter.class), notification);

        // proceed bes process to 'WAITING_FOR_APPROVAL'
        updateProcessState(process);
    }

    private void handleSubscriptionTermination(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        VOTriggerProcess vo = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        String subscriptionId = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION),
                String.class);
        Invariants.assertNotNull(subscriptionId,
                "mandatory parameter 'subscriptionId' not set");

        VONotificationBuilder builder = new VONotificationBuilder();
        builder.addParameter(VOProperty.SUBSCRIPTION_SUBSCRIPTION_ID,
                subscriptionId);
        VONotification notification = builder.build();

        serviceClient.onSubscriptionTermination(VOConverter.convertToApi(vo),
                notification);
        updateProcessState(process);
    }

    private void handleRegisterOwnUser(TriggerProcess process,
            LocalizerFacade facade) throws IOException, WSDLException,
            ParserConfigurationException {

        // collect parameters
        VOTriggerProcess tp = TriggerProcessAssembler.toVOTriggerProcess(
                process, facade);
        VOUserDetails user = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.USER),
                VOUserDetails.class);
        Invariants.assertNotNull(user, "mandatory parameter 'user' not set");

        List<UserRoleType> roles = getParamListValue(
                process.getParamValueForName(TriggerProcessParameterName.USER_ROLE_TYPE),
                UserRoleType.class);
        Invariants.assertNotNull(roles, "mandatory parameter 'roles' not set");

        String marketplaceId = getParamValue(
                process.getParamValueForName(TriggerProcessParameterName.MARKETPLACE_ID),
                String.class);

        // notify external system
        INotificationServiceAdapter serviceClient = getServiceClient(process
                .getTriggerDefinition());
        serviceClient.onRegisterUserInOwnOrganization(VOConverter
                .convertToApi(tp), VOConverter.convertToApi(user),
                EnumConverter.convertList(roles,
                        org.oscm.types.enumtypes.UserRoleType.class),
                marketplaceId);

        // proceed bes process to 'WAITING_FOR_APPROVAL'
        updateProcessState(process);
    }

    /**
     * Retrieves the reference to the web service client (trigger specific)
     * implementing the notification service.
     * 
     * @param td
     *            The trigger definition containing the service wsdl.
     * @return The reference to the notification service.
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     */
    protected INotificationServiceAdapter getServiceClient(TriggerDefinition td)
            throws IOException, WSDLException, ParserConfigurationException {
        // Get the value for the timeout of the outgoing WS call from the
        // configuration
        Integer wsTimeout = Integer.valueOf(cs.getConfigurationSetting(
                ConfigurationKey.WS_TIMEOUT, Configuration.GLOBAL_CONTEXT)
                .getValue());
        return NotificationServiceAdapterFactory.getNotificationServiceAdapter(
                td, wsTimeout, cs, dm);
    }

    /**
     * Retrieves the parameter value when set, <code>null</code> otherwise.
     * 
     * @param <T>
     *            The class of the parameter to be retrieved.
     * @param param
     *            The trigger parameter containing the value.
     * @param targetClass
     *            The target class.
     * @return The parameter value.
     */
    private <T> T getParamValue(TriggerProcessParameter param,
            Class<T> targetClass) {
        if (param == null) {
            return null;
        }
        return param.getValue(targetClass);
    }

    private <T> List<T> getParamListValue(TriggerProcessParameter param,
            Class<T> targetClass) {
        if (param == null) {
            return null;
        }
        return ParameterizedTypes.list(param.getValue(List.class), targetClass);
    }
}
