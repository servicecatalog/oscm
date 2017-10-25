/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices;

import java.util.List;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.intf.SubscriptionService;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.OperationStatus;
import org.oscm.types.enumtypes.SubscriptionStatus;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.MandatoryUdaMissingException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.OperationStateException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.PaymentDataException;
import org.oscm.types.exceptions.PaymentInformationException;
import org.oscm.types.exceptions.PriceModelException;
import org.oscm.types.exceptions.ServiceChangedException;
import org.oscm.types.exceptions.ServiceParameterException;
import org.oscm.types.exceptions.SubscriptionAlreadyExistsException;
import org.oscm.types.exceptions.SubscriptionMigrationException;
import org.oscm.types.exceptions.SubscriptionStateException;
import org.oscm.types.exceptions.SubscriptionStillActiveException;
import org.oscm.types.exceptions.TechnicalServiceNotAliveException;
import org.oscm.types.exceptions.TechnicalServiceOperationException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOInstanceInfo;
import org.oscm.vo.VOLocalizedText;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPaymentInfo;
import org.oscm.vo.VORoleDefinition;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceOperationParameterValues;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOSubscriptionDetails;
import org.oscm.vo.VOSubscriptionIdAndOrganizations;
import org.oscm.vo.VOTechnicalServiceOperation;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserSubscription;
import org.oscm.webservices.logger.WebServiceLogger;

/**
 * End point facade for WS.
 * 
 * @author Aleh Khomich.
 * 
 */
@WebService(name = "SubscriptionService", serviceName = "SubscriptionService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.SubscriptionService")
public class SubscriptionServiceWS implements SubscriptionService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(SubscriptionServiceWS.class));
    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(SubscriptionServiceWS.class);
    org.oscm.internal.intf.SubscriptionService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public void abortAsyncSubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.abortAsyncSubscription(subscriptionId, organizationId,
                    VOCollectionConverter.convertList(reason,
                            org.oscm.internal.vo.VOLocalizedText.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void abortAsyncModifySubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.abortAsyncModifySubscription(subscriptionId,
                    organizationId, VOCollectionConverter.convertList(reason,
                            org.oscm.internal.vo.VOLocalizedText.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void abortAsyncUpgradeSubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.abortAsyncUpgradeSubscription(subscriptionId,
                    organizationId, VOCollectionConverter.convertList(reason,
                            org.oscm.internal.vo.VOLocalizedText.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public boolean addRevokeUser(String subscriptionId,
            List<VOUsageLicense> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            ConcurrentModificationException, OperationPendingException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.addRevokeUser(subscriptionId,
                    VOCollectionConverter.convertList(usersToBeAdded,
                            org.oscm.internal.vo.VOUsageLicense.class),
                    VOCollectionConverter.convertList(usersToBeRevoked,
                            org.oscm.internal.vo.VOUser.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceParameterException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void completeAsyncSubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance)
            throws ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.completeAsyncSubscription(subscriptionId, organizationId,
                    VOConverter.convertToUp(instance));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY,
                    ValidationException.class.getName());
            throw new org.oscm.types.exceptions.SaaSSystemException(
                    e.getMessage());
        }
    }

    @Override
    public void completeAsyncModifySubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance)
            throws ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.completeAsyncModifySubscription(subscriptionId,
                    organizationId, VOConverter.convertToUp(instance));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void completeAsyncUpgradeSubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance)
            throws ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.completeAsyncUpgradeSubscription(subscriptionId,
                    organizationId, VOConverter.convertToUp(instance));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOUserSubscription> getSubscriptionsForCurrentUser() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getSubscriptionsForCurrentUser(),
                org.oscm.vo.VOUserSubscription.class);
    }

    @Override
    public List<VOUserSubscription> getSubscriptionsForUser(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getSubscriptionsForUser(
                            VOConverter.convertToUp(user)),
                    org.oscm.vo.VOUserSubscription.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOOrganization> getCustomersForSubscriptionId(
            String subscriptionIdentifier)
            throws OrganizationAuthoritiesException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getCustomersForSubscriptionId(
                            subscriptionIdentifier),
                    org.oscm.vo.VOOrganization.class);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOSubscriptionIdAndOrganizations> getCustomerSubscriptions()
            throws OrganizationAuthoritiesException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getCustomerSubscriptions(),
                    org.oscm.vo.VOSubscriptionIdAndOrganizations.class);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VORoleDefinition> getServiceRolesForSubscription(
            String subscription)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getServiceRolesForSubscription(subscription),
                    org.oscm.vo.VORoleDefinition.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOSubscriptionDetails getSubscriptionDetails(String subId)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter
                    .convertToApi(delegate.getSubscriptionDetails(subId));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOSubscriptionDetails getSubscriptionForCustomer(
            String organizationId, String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.getSubscriptionForCustomer(
                    organizationId, subscriptionId));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<String> getSubscriptionIdentifiers()
            throws OrganizationAuthoritiesException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.getSubscriptionIdentifiers();
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOSubscription> getSubscriptionsForOrganization() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getSubscriptionsForOrganization(),
                org.oscm.vo.VOSubscription.class);
    }

    @Override
    public List<VOService> getUpgradeOptions(String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getUpgradeOptions(subscriptionId),
                    org.oscm.vo.VOService.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOSubscriptionDetails modifySubscription(VOSubscription subscription,
            List<VOParameter> modifiedParameters, List<VOUda> udas)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            SubscriptionMigrationException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, OperationPendingException,
            MandatoryUdaMissingException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.modifySubscription(
                    VOConverter.convertToUp(subscription),
                    VOCollectionConverter.convertList(modifiedParameters,
                            org.oscm.internal.vo.VOParameter.class),
                    VOCollectionConverter.convertList(udas,
                            org.oscm.internal.vo.VOUda.class, ds)));
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionMigrationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MandatoryUdaMissingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY,
                    SubscriptionStateException.class.getName());
            throw new org.oscm.types.exceptions.SaaSSystemException(
                    e.getMessage());
        }
    }

    @Override
    public VOSubscription subscribeToService(VOSubscription subscription,
            VOService product, List<VOUsageLicense> users,
            VOPaymentInfo paymentInfo, VOBillingContact billingContact,
            List<VOUda> udas)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, PaymentInformationException,
            ServiceParameterException, ServiceChangedException,
            PriceModelException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            SubscriptionAlreadyExistsException, OperationPendingException,
            MandatoryUdaMissingException, ConcurrentModificationException,
            SubscriptionStateException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.subscribeToService(
                    VOConverter.convertToUp(subscription),
                    VOConverter.convertToUp(product),
                    VOCollectionConverter.convertList(users,
                            org.oscm.internal.vo.VOUsageLicense.class),
                    VOConverter.convertToUp(paymentInfo),
                    VOConverter.convertToUp(billingContact),
                    VOCollectionConverter.convertList(udas,
                            org.oscm.internal.vo.VOUda.class, ds)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PaymentInformationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceParameterException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceChangedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PriceModelException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionAlreadyExistsException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MandatoryUdaMissingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public boolean unsubscribeFromService(String subId)
            throws ObjectNotFoundException, SubscriptionStillActiveException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationPendingException,
            OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.unsubscribeFromService(subId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStillActiveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOSubscription upgradeSubscription(VOSubscription current,
            VOService newProduct, VOPaymentInfo paymentInfo,
            VOBillingContact billingContact, List<VOUda> udas)
            throws ObjectNotFoundException, OperationNotPermittedException,
            SubscriptionMigrationException, PaymentInformationException,
            SubscriptionStateException, ServiceChangedException,
            PriceModelException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, OperationPendingException,
            MandatoryUdaMissingException, NonUniqueBusinessKeyException,
            ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.upgradeSubscription(
                    VOConverter.convertToUp(current),
                    VOConverter.convertToUp(newProduct),
                    VOConverter.convertToUp(paymentInfo),
                    VOConverter.convertToUp(billingContact),
                    VOCollectionConverter.convertList(udas,
                            org.oscm.internal.vo.VOUda.class, ds)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionMigrationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PaymentInformationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceChangedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PriceModelException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MandatoryUdaMissingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VORoleDefinition> getServiceRolesForService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getServiceRolesForService(
                            VOConverter.convertToUp(service)),
                    org.oscm.vo.VORoleDefinition.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void updateAsyncSubscriptionProgress(String subscriptionId,
            String organizationId, List<VOLocalizedText> localizedProgress)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.updateAsyncSubscriptionProgress(subscriptionId,
                    organizationId,
                    VOCollectionConverter.convertList(localizedProgress,
                            org.oscm.internal.vo.VOLocalizedText.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void executeServiceOperation(VOSubscription subscription,
            VOTechnicalServiceOperation operation)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, ConcurrentModificationException,
            ValidationException, SubscriptionStateException,
            NonUniqueBusinessKeyException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.executeServiceOperation(
                    VOConverter.convertToUp(subscription),
                    VOConverter.convertToUp(operation));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void terminateSubscription(VOSubscription subscription,
            String reason)
            throws ObjectNotFoundException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, ConcurrentModificationException,
            SubscriptionStateException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.terminateSubscription(
                    VOConverter.convertToUp(subscription), reason);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public boolean hasCurrentUserSubscriptions() {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.hasCurrentUserSubscriptions();
    }

    @Override
    public VOSubscriptionDetails modifySubscriptionPaymentData(
            VOSubscription subscription, VOBillingContact billingContact,
            VOPaymentInfo paymentInfo) throws ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException,
            PaymentInformationException, SubscriptionStateException,
            PaymentDataException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter
                    .convertToApi(delegate.modifySubscriptionPaymentData(
                            VOConverter.convertToUp(subscription),
                            VOConverter.convertToUp(billingContact),
                            VOConverter.convertToUp(paymentInfo)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PaymentInformationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PaymentDataException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void reportIssue(String subscriptionId, String subject,
            String issueText) throws ValidationException,
            ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.reportIssue(subscriptionId, subject, issueText);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOSubscription> getSubscriptionsForOrganizationWithFilter(
            Set<SubscriptionStatus> requiredStatus) {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getSubscriptionsForOrganizationWithFilter(
                        EnumConverter.convertSet(requiredStatus,
                                org.oscm.internal.types.enumtypes.SubscriptionStatus.class)),
                org.oscm.vo.VOSubscription.class);
    }

    @Override
    public List<VOServiceOperationParameterValues> getServiceOperationParameterValues(
            VOSubscription subscription, VOTechnicalServiceOperation operation)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TechnicalServiceNotAliveException, ConcurrentModificationException,
            TechnicalServiceOperationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getServiceOperationParameterValues(
                            VOConverter.convertToUp(subscription),
                            VOConverter.convertToUp(operation)),
                    VOServiceOperationParameterValues.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void updateAccessInformation(String subscriptionId,
            String organizationId, VOInstanceInfo instanceInfo)
            throws ObjectNotFoundException, SubscriptionStateException,
            OperationNotPermittedException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.updateAccessInformation(subscriptionId, organizationId,
                    VOConverter.convertToUp(instanceInfo));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void updateAsyncOperationProgress(String transactionId,
            OperationStatus status, List<VOLocalizedText> progress)
            throws OperationNotPermittedException, OperationStateException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.updateAsyncOperationProgress(transactionId,
                    EnumConverter.convert(status,
                            org.oscm.internal.types.enumtypes.OperationStatus.class),
                    VOConverter.convertToUpVOLocalizedText(progress));
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationStateException e) {
            throw ExceptionConverter.convertToApi(e);
        }

    }

    @Override
    public void updateAsyncSubscriptionStatus(String subscriptionId,
            String organizationId, VOInstanceInfo instance)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.updateAsyncSubscriptionStatus(subscriptionId,
                    organizationId, VOConverter.convertToUp(instance));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }

    }

    @Override
    public void notifySubscriptionAboutVmsNumber(String subscriptionId,
        String organizationId, VOInstanceInfo instance)
        throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.notifySubscriptionAboutVmsNumber(subscriptionId,
                organizationId, VOConverter.convertToUp(instance));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }

    }
}
