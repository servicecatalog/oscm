/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: pock
 *
 *  Creation Date: 18.06.2010                                                      
 *
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.enumtypes.TriggerProcessParameterType;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.ExecutionTargetException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.TriggerProcessStatusException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOTriggerProcessParameter;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.triggerservice.assembler.TriggerDefinitionAssembler;
import org.oscm.triggerservice.assembler.TriggerProcessAssembler;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.triggerservice.local.TriggerServiceLocal;
import org.oscm.triggerservice.validator.ValidationPerformer;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Session Bean implementation class of TriggerProcessService
 *
 * @author pock
 *
 */
@Stateless
@Remote(TriggerService.class)
@Local(TriggerServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class TriggerServiceBean implements TriggerService, TriggerServiceLocal {

    private final static Log4jLogger logger = LoggerFactory
        .getLogger(TriggerServiceBean.class);

    @Resource
    private SessionContext sessionCtx;

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @EJB(beanInterface = AccountServiceLocal.class)
    protected AccountServiceLocal accLocal;

    @EJB(beanInterface = ServiceProvisioningServiceLocal.class)
    protected ServiceProvisioningServiceLocal srvProvLocal;

    @EJB(beanInterface = SubscriptionServiceLocal.class)
    protected SubscriptionServiceLocal subLocal;

    @EJB(beanInterface = IdentityServiceLocal.class)
    protected IdentityServiceLocal idLocal;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    private LocalizerServiceLocal localizer;

    @EJB(beanInterface = TriggerQueueServiceLocal.class)
    protected TriggerQueueServiceLocal triggerQS;

    /**
     * Executes the business logic for the given trigger process.
     *
     * @param triggerProcess
     *            the trigger process for which the business logic has to be
     *            executed.
     * @throws SaaSApplicationException
     *             Thrown if the business logic execution fails.
     */
    private void execute(TriggerProcess triggerProcess)
        throws SaaSApplicationException {
        SaaSSystemException se;

        switch (triggerProcess.getTriggerDefinition().getType()) {

        case REGISTER_CUSTOMER_FOR_SUPPLIER:
            accLocal.registerKnownCustomerInt(triggerProcess);
            break;

        case SAVE_PAYMENT_CONFIGURATION:
            accLocal.savePaymentConfigurationInt(triggerProcess);
            break;

        case START_BILLING_RUN:
            se = new SaaSSystemException("Nothing to execute for TriggerType '"
                + TriggerType.START_BILLING_RUN + "'");
            logger.logError(
                Log4jLogger.SYSTEM_LOG,
                se,
                LogMessageIdentifier.ERROR_NO_EXECUTION_FOR_THE_TRIGGER_TYPE,
                String.valueOf(TriggerType.START_BILLING_RUN));
            throw se;

        case ACTIVATE_SERVICE:
            srvProvLocal.activateServiceInt(triggerProcess);
            break;

        case DEACTIVATE_SERVICE:
            srvProvLocal.deactivateServiceInt(triggerProcess);
            break;

        case SUBSCRIBE_TO_SERVICE:
            subLocal.subscribeToServiceInt(triggerProcess);
            break;

        case UNSUBSCRIBE_FROM_SERVICE:
            subLocal.unsubscribeFromServiceInt(triggerProcess);
            break;

        case MODIFY_SUBSCRIPTION:
            subLocal.modifySubscriptionInt(triggerProcess);
            break;

        case UPGRADE_SUBSCRIPTION:
            subLocal.upgradeSubscriptionInt(triggerProcess);
            break;

        case ADD_REVOKE_USER:
            subLocal.addRevokeUserInt(triggerProcess);
            break;

        case REGISTER_OWN_USER:
            idLocal.createUserInt(triggerProcess);
            break;

        default:
            se = new SaaSSystemException("Unhandled TriggerType '"
                + triggerProcess.getTriggerDefinition().getType() + "'");
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                LogMessageIdentifier.ERROR_UNHANDLED_TRIGGER_TYPE, String
                    .valueOf(triggerProcess.getTriggerDefinition()
                        .getType()));
            throw se;
        }
    }

    /**
     * Verifies the trigger process status.
     *
     * @param triggerProcess
     *            the trigger process form which the status is verified.
     * @param statusArray
     *            the array with the accepted trigger process statuses.
     * @throws TriggerProcessStatusException
     *             Thrown if the verification fails.
     */
    private void verifyTriggerProcessStatus(TriggerProcess triggerProcess,
        TriggerProcessStatus... statusArray)
        throws TriggerProcessStatusException {

        if (statusArray != null) {
            for (int i = 0; i < statusArray.length; i++) {
                if (triggerProcess.getStatus() == statusArray[i]) {
                    return;
                }
            }
            TriggerProcessStatusException e = new TriggerProcessStatusException(
                "Invalid trigger process status '"
                    + triggerProcess.getStatus() + "'.",
                triggerProcess.getStatus());
            logger.logError(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, e,
                LogMessageIdentifier.ERROR_INVALID_STATUS_TRIGGER_PROCESS,
                String.valueOf(triggerProcess.getStatus()));
            sessionCtx.setRollbackOnly();
            throw e;
        }

    }

    /**
     * Reads the trigger process for the given key, verifies that it belongs to
     * the current organization and has the status WAITING_FOR_APPROVAL.
     *
     * @param triggerProcessKey
     *            the key of the trigger process to read
     * @return the read trigger process key.
     * @throws ObjectNotFoundException
     *             Thrown if the TriggerProcess for the given key cannot be
     *             found.
     * @throws OperationNotPermittedException
     *             Thrown if the TriggerProcess for the given key doesn't belong
     *             to the current organization.
     */
    private TriggerProcess getTriggerProcess(long triggerProcessKey)
        throws ObjectNotFoundException, OperationNotPermittedException {
        TriggerProcess triggerProcess = dm.getReference(TriggerProcess.class,
            triggerProcessKey);
        if (triggerProcess.getTriggerDefinition().getOrganization().getKey() != dm
            .getCurrentUser().getOrganization().getKey()) {
            OperationNotPermittedException e = new OperationNotPermittedException(
                "The client has no authority for the operation.");
            logger.logError(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, e,
                LogMessageIdentifier.ERROR_NO_AUTHORITY_TO_APPROVE);
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return triggerProcess;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void approveAction(long actionKey)
        throws OperationNotPermittedException, ObjectNotFoundException,
        TriggerProcessStatusException, ExecutionTargetException {

        TriggerProcess triggerProcess = getTriggerProcess(actionKey);
        verifyTriggerProcessStatus(triggerProcess,
            TriggerProcessStatus.WAITING_FOR_APPROVAL);
        try {
            execute(triggerProcess);
            triggerProcess.setState(TriggerProcessStatus.APPROVED);
        } catch (SaaSApplicationException e) {
            sessionCtx.getBusinessObject(TriggerServiceLocal.class).setStatus(
                actionKey, TriggerProcessStatus.FAILED);
            sessionCtx.getBusinessObject(TriggerServiceLocal.class).saveReason(
                actionKey, e.getMessage(), retrieveLocale(triggerProcess));
            sessionCtx.setRollbackOnly();
            throw new ExecutionTargetException(e);
        } catch (RuntimeException e) {
            sessionCtx.getBusinessObject(TriggerServiceLocal.class).setStatus(
                actionKey, TriggerProcessStatus.FAILED);
            sessionCtx.getBusinessObject(TriggerServiceLocal.class).saveReason(
                actionKey, internalErrorMsg(),
                retrieveLocale(triggerProcess));
            throw e;
        }

    }

    private String retrieveLocale(TriggerProcess triggerProcess) {
        Organization org = triggerProcess.getTriggerDefinition()
            .getOrganization();
        String userLocale = "en";
        if (org != null && org.getLocale() != null
            && org.getLocale().length() > 0) {
            userLocale = org.getLocale();
        }
        return userLocale;
    }

    /**
     * The text is not localized, because:<br>
     * 1. No resource localization is done server side, currently.<br>
     * 2. The reason may contain non localized text, e.g. from the external
     * service.
     */
    private String internalErrorMsg() {
        return "Internal server error. Please see log file for details.";
    }

    @Override
    public void cancelActions(List<Long> actionKeys,
        List<VOLocalizedText> reason) throws ObjectNotFoundException,
        OperationNotPermittedException, TriggerProcessStatusException {

        if (actionKeys != null) {
            List<TriggerMessage> messages = new ArrayList<TriggerMessage>();
            for (long key : actionKeys) {
                TriggerProcess triggerProcess = getTriggerProcess(key);

                verifyTriggerProcessStatus(triggerProcess,
                    TriggerProcessStatus.INITIAL,
                    TriggerProcessStatus.WAITING_FOR_APPROVAL);
                triggerProcess.setState(TriggerProcessStatus.CANCELLED);
                dm.flush();
                localizer.storeLocalizedResources(key,
                    LocalizedObjectTypes.TRIGGER_PROCESS_REASON, reason);

                messages.add(new TriggerMessage(null, triggerProcess
                    .getTriggerProcessParameters(), Collections
                    .singletonList(dm.getCurrentUser().getOrganization())));
            }
            triggerQS.sendAllNonSuspendingMessages(messages);
        }

    }

    @Override
    public void deleteActions(List<Long> actionKeys)
        throws ObjectNotFoundException, OperationNotPermittedException,
        TriggerProcessStatusException {

        if (actionKeys != null) {
            for (long key : actionKeys) {
                TriggerProcess triggerProcess = getTriggerProcess(key);

                verifyTriggerProcessStatus(triggerProcess,
                    TriggerProcessStatus.APPROVED,
                    TriggerProcessStatus.CANCELLED,
                    TriggerProcessStatus.ERROR,
                    TriggerProcessStatus.FAILED,
                    TriggerProcessStatus.REJECTED,
                    TriggerProcessStatus.NOTIFIED);
                dm.remove(triggerProcess);
            }
        }

    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void rejectAction(long actionKey, List<VOLocalizedText> reason)
        throws ObjectNotFoundException, OperationNotPermittedException,
        TriggerProcessStatusException {

        TriggerProcess triggerProcess = getTriggerProcess(actionKey);
        verifyTriggerProcessStatus(triggerProcess,
            TriggerProcessStatus.WAITING_FOR_APPROVAL);

        triggerProcess.setState(TriggerProcessStatus.REJECTED);
        localizer.storeLocalizedResources(actionKey,
            LocalizedObjectTypes.TRIGGER_PROCESS_REASON, reason);

    }

    @Override
    public List<VOTriggerDefinition> getAllDefinitions() {
        List<VOTriggerDefinition> list = new ArrayList<>();
        Organization organization = dm.getCurrentUser().getOrganization();
        for (TriggerDefinition triggerDefinition : organization
            .getTriggerDefinitions()) {
            list.add(TriggerDefinitionAssembler
                .toVOTriggerDefinition(triggerDefinition));
        }
        return list;
    }

    @Override
    public List<VOTriggerProcess> getAllActions() {
        String namedQuery = "TriggerProcess.getAllForUser";
        return getActionsForQuery(namedQuery);
    }

    @SuppressWarnings(value = "unchecked")
    private List<VOTriggerProcess> getActionsForQuery(String namedQuery) {
        List<VOTriggerProcess> list = new ArrayList<>();
        PlatformUser currentUser = dm.getCurrentUser();
        Query query = dm.createNamedQuery(namedQuery);
        try {
            query.setParameter("userKey", currentUser.getKey());
        } catch (IllegalArgumentException ie) {
            logger.logDebug("Parameter is not needed");
        }
        query.setParameter("organizationKey",
            currentUser.getOrganization().getKey());
        LocalizerFacade localizerFacade = new LocalizerFacade(localizer,
            currentUser.getLocale());
        for (TriggerProcess triggerProcess : ((Collection<TriggerProcess>) query
            .getResultList())) {
            list.add(TriggerProcessAssembler.toVOTriggerProcess(triggerProcess,
                localizerFacade));
        }

        return list;
    }

    @Override
    public List<VOTriggerProcess> getAllActionsForOrganization() {
        String namedQuery = "TriggerProcess.getAllForOrganization";
        return getActionsForQuery(namedQuery);
    }

    @Override
    public List<VOTriggerProcess> getAllActionsForOrganizationRelatedSubscription() {
        List<VOTriggerProcess> triggerProcessesResult = new ArrayList<>();
        Query query = dm.createNamedQuery(
            "TriggerProcess.getAllForOrganizationRelatedSubscription");
        query.setParameter("organizationKey",
            dm.getCurrentUser().getOrganization().getKey());
        List<TriggerProcess> triggerProcesses = query.getResultList();
        DocumentBuilder builder = null;
        XPathExpression serviceIdXpath = null;
        XPathExpression subscriptionXpath = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                .newInstance();
            builder = factory.newDocumentBuilder();
            serviceIdXpath = getCompiledXPathExpression(
                "//void[@property='serviceId']/string");
            subscriptionXpath = getCompiledXPathExpression(
                "//void[@property='subscriptionId']/string");
        } catch (XPathExpressionException | ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        for (TriggerProcess triggerProcess : triggerProcesses) {
            VOTriggerProcess voTriggerProcess = new VOTriggerProcess();
            voTriggerProcess.setSubscription(getVOSubscriptionFromTrigger(
                triggerProcess, builder, subscriptionXpath));
            voTriggerProcess.setService(getVOServiceFromTrigger(triggerProcess,
                builder, serviceIdXpath));
            triggerProcessesResult.add(voTriggerProcess);
        }
        return triggerProcessesResult;
    }

    private VOService getVOServiceFromTrigger(TriggerProcess triggerProcess,
        DocumentBuilder builder, XPathExpression serviceIdXpath) {
        TriggerProcessParameter triggerProcessParameter = triggerProcess
            .getParamValueForName(TriggerProcessParameterName.PRODUCT);
        String product;
        try {
            product = AESEncrypter
                .decrypt(triggerProcessParameter.getSerializedValue());
        } catch (GeneralSecurityException e) {
            product = triggerProcessParameter.getSerializedValue();
        }
        String serviceId = retrieveValueByXpath(product, builder,
            serviceIdXpath);
        if (serviceId == null) {
            return null;
        }
        VOService voService = new VOService();
        voService.setServiceId(serviceId);
        return voService;
    }

    private VOSubscription getVOSubscriptionFromTrigger(
        TriggerProcess triggerProcess, DocumentBuilder builder,
        XPathExpression subscriptionXpath) {
        TriggerProcessParameter triggerProcessParameter = triggerProcess
            .getParamValueForName(
                org.oscm.types.enumtypes.TriggerProcessParameterName.SUBSCRIPTION);
        String subscription;
        try {
            subscription = AESEncrypter
                .decrypt(triggerProcessParameter.getSerializedValue());
        } catch (GeneralSecurityException e) {
            subscription = triggerProcessParameter.getSerializedValue();
        }
        String subsId = retrieveValueByXpath(subscription, builder,
            subscriptionXpath);
        if (subsId == null) {
            return null;
        }
        VOSubscription voSubscription = new VOSubscription();
        voSubscription.setSubscriptionId(subsId);
        return voSubscription;
    }

    private XPathExpression getCompiledXPathExpression(String xpathExpression) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        return xpath.compile(xpathExpression);
    }

    private String retrieveValueByXpath(String xml, DocumentBuilder builder,
        XPathExpression expr) {
        try {
            Document document = builder
                .parse(new InputSource(new StringReader(xml)));
            NodeList nodes = (NodeList) expr.evaluate(document,
                XPathConstants.NODESET);
            if (nodes == null || nodes.item(0) == null
                || nodes.item(0).getFirstChild() == null) {
                return null;
            }
            return nodes.item(0).getFirstChild().getNodeValue();
        } catch (SAXException | XPathExpressionException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<VOTriggerProcess> getAllActionsForSubscription(
        String subscriptionId) {
        String namedQuery = "TriggerProcess.getAllForOrganization";
        PlatformUser currentUser = dm.getCurrentUser();
        Query query = dm.createNamedQuery(namedQuery);
        query.setParameter("organizationKey",
            currentUser.getOrganization().getKey());
        Collection<TriggerProcess> resultList = query.getResultList();
        LocalizerFacade localizerFacade = new LocalizerFacade(localizer,
            currentUser.getLocale());
        List<VOTriggerProcess> list = new ArrayList<>();
        for (TriggerProcess triggerProcess : resultList) {
            if (checkTriggerProcessBySubscriptionId(triggerProcess,
                subscriptionId)) {
                list.add(TriggerProcessAssembler.toVOTriggerProcess(
                    triggerProcess, localizerFacade));
            }
        }

        return list;
    }

    private boolean checkTriggerProcessBySubscriptionId(
        TriggerProcess triggerProcess, String targetSubscriptionId) {
        if (triggerProcess == null) {
            return false;
        }
        String subscriptionId = "";
        switch (triggerProcess.getTriggerDefinition().getType()) {
        case ADD_REVOKE_USER:
        case UNSUBSCRIBE_FROM_SERVICE:
            subscriptionId = triggerProcess
                .getParamValueForName(
                    org.oscm.types.enumtypes.TriggerProcessParameterName.SUBSCRIPTION)
                .getValue(String.class);
            break;
        case UPGRADE_SUBSCRIPTION:
            subscriptionId = triggerProcess
                .getParamValueForName(
                    org.oscm.types.enumtypes.TriggerProcessParameterName.SUBSCRIPTION)
                .getValue(VOSubscription.class).getSubscriptionId();
            break;
        case MODIFY_SUBSCRIPTION:
            VOSubscription voSubscription = triggerProcess
                .getParamValueForName(
                    org.oscm.types.enumtypes.TriggerProcessParameterName.SUBSCRIPTION)
                .getValue(VOSubscription.class);
            Subscription sub = dm.find(Subscription.class,
                voSubscription.getKey());
            subscriptionId = sub.getSubscriptionId();
            break;
        default:
            break;
        }

        return targetSubscriptionId.equalsIgnoreCase(subscriptionId);
    }

    /**
     * Internal method to change the status even if the caller set the
     * transaction to rollback only.
     *
     * @see AccountServiceLocal#removeOverdueOrganization(Organization)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void setStatus(long triggerProcessKey, TriggerProcessStatus status)
        throws ObjectNotFoundException {

        TriggerProcess proc = dm.getReference(TriggerProcess.class,
            triggerProcessKey);
        proc.setState(status);

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void saveReason(long triggerProcessKey, String value,
        String localeString) {

        LocalizedResource template = new LocalizedResource(localeString,
            triggerProcessKey, LocalizedObjectTypes.TRIGGER_PROCESS_REASON);
        LocalizedResource storedResource = (LocalizedResource) dm
            .find(template);
        if (storedResource == null) {
            LocalizedResource resourceToPersist = new LocalizedResource();
            resourceToPersist.setLocale(localeString);
            resourceToPersist.setObjectKey(triggerProcessKey);
            resourceToPersist
                .setObjectType(LocalizedObjectTypes.TRIGGER_PROCESS_REASON);
            resourceToPersist.setValue(value);
            try {
                dm.persist(resourceToPersist);
            } catch (NonUniqueBusinessKeyException e) {
                SaaSSystemException sse = new SaaSSystemException(
                    "Localized Resource could not be persisted although prior check was performed, "
                        + resourceToPersist, e);
                logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sse,
                    LogMessageIdentifier.ERROR_PERSIST_LOCALIZED_RESOURCE_FAILED_PRIOR_CHECK_PERFORMED,
                    String.valueOf(resourceToPersist));
                throw sse;
            }
        } else {
            storedResource.setValue(value);
        }

    }

    /**
     * Updates TriggerProcessParameters of given TriggerProcess object key.
     *
     * @param actionKey
     *            - key of TriggerProcess to update
     * @param parameters
     *            - list with updated values of TriggerProcessParameter
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws TriggerProcessStatusException
     * @throws ValidationException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void updateActionParameters(long actionKey,
        List<VOTriggerProcessParameter> parameters)
        throws ObjectNotFoundException, OperationNotPermittedException,
        TriggerProcessStatusException, ValidationException {

        if (parameters == null) {
            throw new org.oscm.internal.types.exception.IllegalArgumentException(
                "Parameter parameters must not be null.");
        }

        TriggerProcess triggerProcess = getTriggerProcess(actionKey);

        verifyTriggerProcessStatus(triggerProcess,
            TriggerProcessStatus.WAITING_FOR_APPROVAL);
        verifyTriggerDefinitionType(triggerProcess.getTriggerDefinition(),
            TriggerType.SUBSCRIBE_TO_SERVICE);
        removeAdditionalParameters(parameters);
        removeNonConfigurableParameters(parameters);
        validateConfiguredParameters(parameters, triggerProcess);

        updateTriggerProcessParameters(triggerProcess, parameters);
        dm.merge(triggerProcess);
    }

    /**
     * Based on list of VOTriggerProcessParameters removes the one where
     * configurable attribute is set to false as those parameter values should
     * not be updated.
     *
     * @param triggerParameters
     *            - list of VOTriggerProcessParameter
     */
    private void removeNonConfigurableParameters(
        List<VOTriggerProcessParameter> triggerParameters) {
        for (VOTriggerProcessParameter triggerParameter : triggerParameters) {
            List<VOParameter> parameters = ((VOService) triggerParameter
                .getValue()).getParameters();
            for (Iterator<VOParameter> it = parameters.iterator(); it.hasNext();) {
                VOParameter parameter = it.next();
                if (!parameter.isConfigurable()) {
                    it.remove();
                }
            }
        }
    }

    /**
     * All parameters are checked by keys if they are existing in db. All
     * additional parameters that do not exist in db are deleted from the list.
     * Only existing parameters can be updated.
     *
     * @param parameters
     *            - list of parameters to update
     */
    private void removeAdditionalParameters(
        List<VOTriggerProcessParameter> parameters) {

        for (Iterator<VOTriggerProcessParameter> it = parameters.iterator(); it
            .hasNext();) {
            try {
                VOTriggerProcessParameter parameter = it.next();

                getActionParameter(
                    parameter.getTriggerProcessKey().longValue(),
                    parameter.getType());

            } catch (ObjectNotFoundException | OperationNotPermittedException e) {
                it.remove();
            }
        }
    }

    /**
     * Returns converted to VO object TriggerProcessParameter
     *
     * @param actionKey
     *            - key of TriggerProcess
     * @param paramType
     *            - TriggerProcessParameterType bound to TriggerProcessParameter
     * @return - returns VOTriggerProcessParameter if object is found, otherwise
     *         exception is thrown
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     */
    @Override
    public VOTriggerProcessParameter getActionParameter(long actionKey,
        TriggerProcessParameterType paramType)
        throws OperationNotPermittedException, ObjectNotFoundException {

        if (paramType == null) {
            throw new org.oscm.internal.types.exception.IllegalArgumentException(
                "Parameter paramType must not be null.");
        }

        TriggerProcessParameter parameter = getTriggerProcessParameter(
            actionKey, paramType);

        if (parameter == null) {
            throw new ObjectNotFoundException("Parameter for action with key: "
                + actionKey + " not found.");
        }

        return TriggerProcessAssembler.toVOTriggerProcessParameter(parameter);
    }

    /**
     * Returns TriggerProcessParameter from DB
     *
     * @param actionKey
     *            - key of TriggerProcess that is bound to
     *            TriggerProcessParameter
     * @param paramType
     *            - TriggerProcessParameterName bound to searched
     *            TriggerProcessParameter
     * @return - returns TriggerProcessParameter if object is found, null
     *         otherwise
     */
    TriggerProcessParameter getTriggerProcessParameter(long actionKey,
        TriggerProcessParameterType paramType) {
        try {
            Query query = dm
                .createNamedQuery("TriggerProcessParameter.getParam");
            query.setParameter("actionKey", actionKey);
            query.setParameter("paramName",
                org.oscm.types.enumtypes.TriggerProcessParameterName
                    .valueOf(paramType.name()));
            return (TriggerProcessParameter) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Validated if TriggerProcessParameter PRODUCT configured parameters are
     * correct and can be updated.
     *
     * @param parameters
     *            - list of TriggerProcessParameter
     * @throws ValidationException
     */
    private void validateConfiguredParameters(
        List<VOTriggerProcessParameter> parameters,
        TriggerProcess triggerProcess) throws ValidationException {

        VOService service = null;
        for (VOTriggerProcessParameter parameter : parameters) {
            if (TriggerProcessParameterType.PRODUCT.equals(parameter.getType())) {
                service = (VOService) parameter.getValue();
                updateParameterDefinitions(triggerProcess, service);
                break;
            }
        }

        if (service != null) {
            for (VOParameter serviceParameter : service.getParameters()) {
                ValidationPerformer.validate(serviceParameter
                        .getParameterDefinition().getValueType(),
                    serviceParameter);
            }
        }
    }

    private void updateParameterDefinitions(TriggerProcess triggerProcess,
        VOService service) {
        TriggerProcessParameter triggerParameter = triggerProcess
            .getParamValueForName(TriggerProcessParameterName.PRODUCT);

        if (triggerParameter == null) {
            return;
        }

        Map<Long, VOParameterDefinition> keyToDefinition = new HashMap<>();
        VOService dbService = triggerParameter.getValue(VOService.class);

        for (VOParameter dbParam : dbService.getParameters()) {
            VOParameterDefinition dbParamDef = dbParam.getParameterDefinition();
            keyToDefinition.put(dbParamDef.getKey(), dbParamDef);
        }

        for (VOParameter newParam : service.getParameters()) {
            VOParameterDefinition paramDef = keyToDefinition.get(newParam.getParameterDefinition().getKey());
            if (paramDef != null) {
                newParam.setParameterDefinition(paramDef);
            }
        }
    }

    /**
     * Updates trigger process params based on given TriggerProcessParamter
     * list.
     *
     * @param triggerProcess
     *            - trigger process which parameters should be updated
     * @param parameters
     *            - TriggerProcessParameter list with updated values of
     *            VOParameters
     */
    private void updateTriggerProcessParameters(TriggerProcess triggerProcess,
        List<VOTriggerProcessParameter> parameters) {
        for (VOTriggerProcessParameter parameter : parameters) {
            org.oscm.types.enumtypes.TriggerProcessParameterName paramName = org.oscm.types.enumtypes.TriggerProcessParameterName
                .valueOf(parameter.getType().name());
            TriggerProcessParameter param = triggerProcess
                .getParamValueForName(paramName);

            if (org.oscm.types.enumtypes.TriggerProcessParameterName.PRODUCT
                .equals(paramName) && param != null) {
                VOService originalService = param.getValue(VOService.class);
                VOService updatedService = (VOService) parameter.getValue();

                updateVOParameters(originalService.getParameters(),
                    updatedService.getParameters());

                param.setValue(originalService);
            } else if (param != null) {
                param.setValue(parameter.getValue());
            }
        }
    }

    /**
     * Updates only value of original parameter when keys of original and
     * updated parameters are equal.
     *
     * @param originalParameters
     *            - original VOParameter list
     * @param updatedParameters
     *            - updated VOParameter list
     */
    private void updateVOParameters(List<VOParameter> originalParameters,
        List<VOParameter> updatedParameters) {
        for (VOParameter originalParam : originalParameters) {
            for (VOParameter updatedParam : updatedParameters) {
                if (originalParam.getKey() == updatedParam.getKey()) {
                    originalParam.setValue(updatedParam.getValue());
                }
            }
        }
    }

    private void verifyTriggerDefinitionType(
        TriggerDefinition triggerDefinition, TriggerType... types)
        throws OperationNotPermittedException {
        if (types != null) {
            for (TriggerType type : types) {
                if (triggerDefinition.getType() == type) {
                    return;
                }
            }
            OperationNotPermittedException e = new OperationNotPermittedException(
                "Trigger Type is wrong. Expected value: "
                    + TriggerType.SUBSCRIBE_TO_SERVICE.name()
                    + " Got: " + triggerDefinition.getType().name());
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.ERROR_TRIGGER_TYPE_WRONG,
                TriggerType.SUBSCRIBE_TO_SERVICE.name(), triggerDefinition
                    .getType().name());
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }
}
